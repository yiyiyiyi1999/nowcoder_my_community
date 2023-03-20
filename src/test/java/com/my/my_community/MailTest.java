package com.my.my_community;

import com.my.my_community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@SpringBootTest
@ContextConfiguration(classes = MyCommunityApplication.class)
public class MailTest {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail(){
        mailClient.sendMail("rumin_diao@163.com","hello","sending mail test 1");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","romee");
        String content = templateEngine.process("/mail/demo",context);
        mailClient.sendMail("rumin_diao@163.com","html",content);

    }
}
