package com.my.my_community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class MyCommunityApplication {

    public static void main(String[] args) {
        //解决Netty启动冲突的问题
        System.setProperty("es.set.netty.running.available.processors","false");
        SpringApplication.run(MyCommunityApplication.class, args);
    }

}
