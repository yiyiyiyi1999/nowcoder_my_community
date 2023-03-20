package com.my.my_community;

import com.my.my_community.entity.DiscussPost;
import com.my.my_community.entity.LoginTicket;
import com.my.my_community.entity.Message;
import com.my.my_community.entity.User;
import com.my.my_community.mapper.DiscussPostMapper;
import com.my.my_community.mapper.LoginTicketMapper;
import com.my.my_community.mapper.MessageMapper;
import com.my.my_community.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = MyCommunityApplication.class)
public class MapperTest {

//    private UserMapper userMapper;
    @Autowired
//    private UserMapper userMapper;
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    void selectUsers() {
//        System.out.println(userMapper.selectById(23));
//        User user = new User();
//        user.setUsername("harry");
//        System.out.println(userMapper.insertUser(user));
//        System.out.println(userMapper.updateStatus(101,0));
//        System.out.println(discussPostMapper.selectDiscussPostRows(101));
//        List<DiscussPost> lists = discussPostMapper.selectDiscussPosts(0,0,10);
//        for(DiscussPost post: lists){
//            System.out.println(post);
//        }
        DiscussPost post = new DiscussPost();
        post.setUserId(153);
        post.setTitle("大家好");
        post.setContent("你们好吗");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);
    }

    @Test
    void testLogin(){
//        LoginTicket loginTicket = new LoginTicket();
//        loginTicket.setUserId(101);
//        loginTicket.setTicket("abc");
//        loginTicket.setStatus(0);
//        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));
//        loginTicketMapper.insertLoginTicket(loginTicket);
//        System.out.println(loginTicketMapper.selecByTicket("abc"));
        loginTicketMapper.updateStatus("824d42959db24e4b819941f52d96c2ad",1);
    }

    @Test
    void testMessage(){
//        List<Message> list = messageMapper.selectConversations(111, 0, 20);
//        for (Message message : list) {
//            System.out.println(message);
//        }
//        int count = messageMapper.selectConversationCount(111);
//        System.out.println(count);
//
//        list = messageMapper.selectLetters("111_112", 0, 10);
//        for (Message message : list) {
//            System.out.println(message);
//        }
//
//        count = messageMapper.selectLetterCount("111_112");
//        System.out.println(count);
//
//        count = messageMapper.selectLetterUnreadCount(131, "111_131");
//        System.out.println(count);

        int count = messageMapper.selectNoticeCount(111,"comment");
        System.out.println(count);



    }

}
