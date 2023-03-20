package com.my.my_community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;

@SpringBootTest
@ContextConfiguration(classes = MyCommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings(){
//        String redisKey = "test:count";
//        redisTemplate.opsForValue().set(redisKey,1);//Sring类型+存数据
//
//        System.out.println(redisTemplate.opsForValue().get(redisKey));//string+取
//
//        System.out.println(redisTemplate.opsForValue().increment(redisKey));
//        System.out.println(redisTemplate.opsForValue().decrement(redisKey));

        //hash
        String redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey,"id",1);
        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));


    }
    //多次访问同一个key，将key绑定到对象
    @Test
    public void testBoundOperations(){
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        //这样的话就不用传这个key了
        operations.increment();
        operations.get();
    }
    //编程式事务
    @Test
    public void testTransactional(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                operations.multi();//启用事务

                operations.opsForSet().add("redisKey","harry");
                operations.opsForSet().add("redisKey","louis");
                operations.opsForSet().add("redisKey","greg");


                return operations.exec();//提交事务
            }
        });
        System.out.println(obj);
    }
}
