package com.my.my_community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        //是连接工厂
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);//根据连接工厂获得连接
        //设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());//字符串类型
        //设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());//json比较好识别，对这些类型都很适合
        //hash value比较特别，还要单独设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value的序列化方式
        template.setHashKeySerializer(RedisSerializer.json());

        template.afterPropertiesSet();//生效

        return template;
    }
}
