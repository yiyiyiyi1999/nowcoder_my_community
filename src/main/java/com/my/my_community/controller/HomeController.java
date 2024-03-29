package com.my.my_community.controller;

import com.my.my_community.entity.DiscussPost;
import com.my.my_community.entity.Page;
import com.my.my_community.entity.User;
import com.my.my_community.service.DiscussPostService;
import com.my.my_community.service.LikeService;
import com.my.my_community.service.UserService;
import com.my.my_community.util.CommunityConstant;
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
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(value="/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode",defaultValue = "0") int orderMode) {
        //方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入Model
        page.setRows(discussPostService.findDiscussPostRows(0));//总行数
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit(),orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(list != null){
            for(DiscussPost post: list){
                User user = userService.findUserById(post.getUserId());
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                map.put("user",user);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("orderMode",orderMode);
        return "/index";
    }

    @RequestMapping(value = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/erroe/500";
    }

    // 拒绝访问时的提示页面
    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }


}
