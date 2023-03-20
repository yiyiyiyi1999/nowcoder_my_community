package com.my.my_community.util;

import java.util.Spliterator;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIC_TICKET = "ticket";
    private static final String PREFIX_USER = "user";

    /**
     * 根据传入的变量生成key（帖子、评论）
     */
    //生成某个实体的赞like:entity:entityType:entityId -> value用set存，set中存着userId
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
    /**
     * 根据用户生成key
     */
    //like:user:userId
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }
    /**
     * 某个用户关注的实体
     */
    //followee:userId:entityType -> zset(entityId,now)根据时间排序
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }
    /**
     * 某个用户拥有的粉丝
     */
    //follower:entityType:entityId-> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }
    /**
     * 登录验证码
     * 这个时候用户没登陆，没法传userId，但是还需要标识用户，那就生成一个凭证标识用户，之后让字符串过期
     */
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }
    /**
     * 登录凭证
     */
    public static String getTicketKey(String ticket){
        return PREFIC_TICKET + SPLIT + ticket;
    }
    /**
     * 用户
     */
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }








}
