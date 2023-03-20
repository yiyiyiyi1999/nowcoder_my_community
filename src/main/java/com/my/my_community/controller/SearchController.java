package com.my.my_community.controller;

import com.my.my_community.entity.DiscussPost;
import com.my.my_community.entity.Page;
import com.my.my_community.service.DiscussPostService;
import com.my.my_community.service.ElasticsearchService;
import com.my.my_community.service.LikeService;
import com.my.my_community.service.UserService;
import com.my.my_community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //用search?keyword=xxx传keyword
    @GetMapping(path = "/search")
    public String search(String keyword, Page page, Model model) {
        //搜索帖子
        SearchPage<DiscussPost> searchPage = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());//注意search方法是从0开始

        List<Map<String, Object>> discussPosts = new ArrayList<Map<String, Object>>();
        if (searchPage != null) {
            for (SearchHit<DiscussPost> discussPostSearchHit : searchPage) {
                Map<String, Object> map = new HashMap<>();
                //帖子
                DiscussPost post = discussPostSearchHit.getContent();
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                //点赞
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);//为了取关键字

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchPage == null ? 0 : (int) searchPage.getTotalElements());

        return "/site/search";
    }


}
