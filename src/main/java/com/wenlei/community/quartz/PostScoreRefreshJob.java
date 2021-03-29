package com.wenlei.community.quartz;


import com.wenlei.community.entity.DiscussPost;
import com.wenlei.community.service.DiscussPostService;
import com.wenlei.community.service.ElasticsearchService;
import com.wenlei.community.service.LikeService;
import com.wenlei.community.util.CommunityConstant;
import com.wenlei.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 热帖计算公式：log(精华分 + 评论数*10 + 点赞数*2) + (发布时间 - 牛客纪元)
 * 精华分为75分
 */

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    // 牛客纪元
    private static final Date epoch;

    // 静态代码块
    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败！", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();

        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        // 如果缓存队列中没有需要重新计算分数的帖子
        if (operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子！");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数：" + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕！");
    }

    private void refresh(Integer postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null) {
            logger.error("该帖子不存在：id = " + postId);
            return;
        }
        if (post.getStatus() == 2) {
            logger.error("该帖子已被删除！");
            return;
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;

        // 评论数量
        int commentCount = post.getCommentCount();

        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算分数
        // 先计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);

        // 更新帖子的分数
        discussPostService.updateScore(postId, score);

        // 同步ES中的数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
