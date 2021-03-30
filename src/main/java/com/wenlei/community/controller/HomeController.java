package com.wenlei.community.controller;

import com.wenlei.community.dao.DiscussPostMapper;
import com.wenlei.community.entity.DiscussPost;
import com.wenlei.community.entity.Page;
import com.wenlei.community.entity.User;
import com.wenlei.community.service.DiscussPostService;
import com.wenlei.community.service.LikeService;
import com.wenlei.community.service.UserService;
import com.wenlei.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController  implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(value = "/",method = RequestMethod.GET)
    public String root(){
        return "forward:/index";
    }

    @RequestMapping(value = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,@RequestParam(name = "orderMode", defaultValue = "0") int orderMode){
        //url中的current参数自动封装到page中
        // 方法调用之前，SpringMVC会自动实例化Model和Page，并将Page注入Model（自动将方法中的参数注入到Model）
        // 所以，在thymeleaf中可以直接访问Page对象中的数据（而不需要手动将Page添加到Model里面）
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);
        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit(),orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null){
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("orderMode", orderMode);
        return "/index";
    }
    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }

}
