package com.my.my_community;

import com.my.my_community.entity.DiscussPost;
import com.my.my_community.entity.User;
import com.my.my_community.mapper.DiscussPostMapper;
import com.my.my_community.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = MyCommunityApplication.class)
public class MapperTest {

//    private UserMapper userMapper;
    @Autowired
//    private UserMapper userMapper;
    private DiscussPostMapper discussPostMapper;
    @Test
    void selectUsers() {
//        System.out.println(userMapper.selectById(23));
//        User user = new User();
//        user.setUsername("harry");
//        System.out.println(userMapper.insertUser(user));
//        System.out.println(userMapper.updateStatus(101,0));
//        System.out.println(discussPostMapper.selectDiscussPostRows(101));
        List<DiscussPost> lists = discussPostMapper.selectDiscussPosts(0,0,10);
        for(DiscussPost post: lists){
            System.out.println(post);
        }
    }
}
