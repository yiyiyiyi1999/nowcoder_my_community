package com.my.my_community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.my.my_community.entity.DiscussPost;
import com.my.my_community.mapper.DiscussPostMapper;
import com.my.my_community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //帖子列表缓存
    private LoadingCache<String,List<DiscussPost>> postListCache;

    //缓存帖子总数
    private LoadingCache<Integer,Integer> postRowsCache;

    //只需要初始化一次
    @PostConstruct
    public void init(){
        //初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        //实现查询数据库，从缓存来源里查，传入的参数是key
                        if(key == null || key.length() == 0){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        //获得offset和limit
                        String[] params = key.split(":");
                        if(params.length != 2){
                            throw new IllegalArgumentException("参数错误！");
                        }

                        int offset = Integer.parseInt(params[0]);
                        int limit = Integer.parseInt(params[1]);

                        //也可以在这里加二级缓存，查redis，如果redis没有，再mysql

                        logger.info("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                    }
                });
        //初始化帖子总数
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }


    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode){
        if(userId == 0 && orderMode == 1){
            return postListCache.get(offset + ":" + limit);//这就是key
        }
        logger.info("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }
    //查询总行数调用频繁，也可以缓存进去，像是就是频繁查询的啥的，就可以用缓存，就会方便些
    public int findDiscussPostRows(int userId){
        if(userId == 0){
            return postRowsCache.get(userId);//简单地设置key
        }
        logger.info("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 新增帖子
     * @param post
     * @return
     */
    public int addDicussPost(DiscussPost post){
        if(post == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //转义html标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        //插入
        return discussPostMapper.insertDiscussPost(post);
    }
    /**
     * 查看某个帖子
     */
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }
    /**
     * 修改帖子的评论
     */
    public int updateCommentCount(int id, int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }
    /**
     * 修改帖子类型
     */
    public int updateType(int id, int type){
        return discussPostMapper.updateType(id,type);
    }
    /**
     * 修改帖子状态
     */
    public int updateStatus(int id, int status){
        return discussPostMapper.updateStatus(id,status);
    }
    /**
     * 修改帖子分数
     */
    public int updateScore(int id, double score){
        return discussPostMapper.updateScore(id, score);
    }

}
