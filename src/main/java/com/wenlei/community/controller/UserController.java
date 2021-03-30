package com.wenlei.community.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.wenlei.community.annotation.LoginRequired;
import com.wenlei.community.entity.DiscussPost;
import com.wenlei.community.entity.Page;
import com.wenlei.community.entity.ReplyPostResult;
import com.wenlei.community.entity.User;
import com.wenlei.community.service.*;
import com.wenlei.community.util.CommunityConstant;
import com.wenlei.community.util.CommunityUtil;
import com.wenlei.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 上传文件路径
    @Value("${community.path.upload}")
    private String uploadPath;

    // 域名
    @Value("${community.path.domain}")
    private String domain;

    // 项目的访问路径
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    // 用户凭证
    @Value("${qiniu.key.access}")
    private String accessKey;

    // 加密秘钥
    @Value("${qiniu.key.secret}")
    private String secretKey;

    // header空间名字
    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    // header空间url
    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {

        // 生成上传文件的名称
        String fileName = CommunityUtil.generateUUID();

        // 设置响应信息
        StringMap policy = new StringMap();
        // 成功之后七牛云服务器返回一个json字符串，{code:0}
        policy.put("returnBody", CommunityUtil.getJSONString(0));

        // 生成上传凭证，使得七牛云服务器能够识的身份
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "/site/setting";
    }

    // 更新头像路径
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "文件名不能为空！");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }

    //云服务器重构，成为废弃方法
    @Deprecated
    @LoginRequired
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }

        // 修改文件名称，为文件配置一个专属的名称
        // 得到文件的原始文件名（用户上传时的文件名）
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));  //  ".png "
        // 判断后缀名是否合理
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确");
            return "/site/setting";
        }

        // 生成随机地文件名
        fileName = CommunityUtil.generateUUID() + suffix;

        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }
        // 更新当前用户头像的路径
        // http://localhost:8000/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    //云服务器重构，成为废弃方法
    @Deprecated
    @RequestMapping(value = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {

        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                // java7的语法，这里声明的变量，它在编译的时候会自动加上finally，然后在finally里面关闭它，前提是变量有close方法
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream()
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }

        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map == null || map.isEmpty()) {
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    // 个人主页
    @RequestMapping(value = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {

        // 主语，标明怎样显示，是我的帖子，还是TA的帖子
        String subject = "我";
        User loginUser = hostHolder.getUser();
        // 如果用户未登录，或者查看的不是当前登录用户的主页，则显示TA
        if (loginUser == null || userId != loginUser.getId()) {
            subject = "TA";
        }
        // 小标题显示信息
        model.addAttribute("subject", subject);

        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    // 用户发布的帖子
    @RequestMapping(path = "/post/{userId}", method = RequestMethod.GET)
    public String getUserPost(@PathVariable("userId") int userId, Model model, Page page) {

        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        // 用户信息
        model.addAttribute("user", user);

        // 帖子总数
        int postCount = discussPostService.findDiscussPostRows(userId);
        model.addAttribute("postCount", postCount);

        // 分页相关参数
        page.setRows(postCount);
        page.setPath("/user/post/" + userId);

        // 主语，怎样显示，是我的帖子，还是TA的帖子
        String subject = "我";
        user = hostHolder.getUser();
        if (user == null || userId != user.getId()) {
            subject = "TA";
        }
        // 小标题显示信息
        model.addAttribute("subject", subject);

        // 帖子
        List<DiscussPost> list = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(),0);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                // 查询帖子赞的数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        // 帖子相关信息
        model.addAttribute("discussPosts", discussPosts);

        return "/site/my-post";
    }

    // 用户回复的帖子
    @RequestMapping(path = "/reply/{userId}", method = RequestMethod.GET)
    public String getUserReply(@PathVariable("userId") int userId, Model model, Page page) {

        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        // 用户信息
        model.addAttribute("user", user);

        // 主语，怎样显示，是我的帖子，还是TA的帖子
        String subject = "我";
        user = hostHolder.getUser();
        if (user == null || userId != user.getId()) {
            subject = "TA";
        }
        // 小标题显示信息
        model.addAttribute("subject", subject);

        // 帖子总数
        int postCount = commentService.findPostCommentCountByUserId(userId, ENTITY_TYPE_POST);
        model.addAttribute("postCount", postCount);

        // 分页相关参数
        page.setRows(postCount);
        page.setPath("/user/reply/" + userId);

        //帖子及回复的相关信息
        List<ReplyPostResult> list = discussPostService.findReplyDiscussPosts(userId, page.getOffset(), page.getLimit());
        model.addAttribute("replyPost", list);

        return "/site/my-reply";
    }

}
