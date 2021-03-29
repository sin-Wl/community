package com.wenlei.community.dao;

import com.wenlei.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.wenlei.community.entity.ReplyPostResult;
import java.util.List;

@Mapper
public interface DiscussPostMapper {



    // @Param注解用于给参数取别名
    // 如果方法中只有一个参数，并且在<if>里使用，则必须加别名
    int selectDiscussPostRows(@Param("userId") int userId);


    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

    List<ReplyPostResult> selectReplyDiscussPosts(int userId, int offset, int limit);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);

}
