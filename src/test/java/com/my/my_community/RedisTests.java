package com.my.my_community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
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

    //统计20万个数据的重复表示
    @Test
    public void testHyperLogLog(){
        String redisKey = "test:hll:01";

        for(int i = 0; i <= 100000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }
        for(int i = 0; i <= 100000; i++){
            int r = (int)(Math.random() * 100000+1);
            redisTemplate.opsForHyperLogLog().add(redisKey,r);
        }

        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);
    }

    //合并三组数据，再统计合并后的数据的独立总数
    @Test
    public void testHyperLogLogUnion(){
        String redisKey2 = "test:hll:02";
        for(int i = 1; i <= 10000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey2,i);
        }
        String redisKey3 = "test:hll:03";
        for(int i = 5001; i <= 15000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey3,i);
        }
        String redisKey4 = "test:hll:04";
        for(int i = 10001; i <= 20000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey4,i);
        }

        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey,redisKey2,redisKey3,redisKey4);

        Long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);
    }

    // 统计数据的布尔值
    @Test
    public void testBitMap(){
        String redisKey = "test:bm:01";
        //记录
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,4,true);
        redisTemplate.opsForValue().setBit(redisKey,7,true);
        //查
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,2));
        //统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());

            }
        });

    }

}
