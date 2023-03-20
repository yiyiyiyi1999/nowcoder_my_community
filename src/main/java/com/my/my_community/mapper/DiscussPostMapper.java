package com.my.my_community.mapper;

import com.my.my_community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //分页查询需要的参数：起始行的行号offset、每页显示的行数limit
    //查询某个用户的帖子（用户主页功能）/userId=0的话就是查询所有的用户
    //动态sql，根据userId是否为0展示不同的结果+分页
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);
    //查询表里一共有多少数据，用于给上面这个方法服务
    //查询用户的所有帖子+
    int selectDiscussPostRows(@Param("userId")int userId);
    //插入帖子
    int insertDiscussPost(DiscussPost discussPost);
    //查询帖子的详情
    DiscussPost selectDiscussPostById(int id);
    //修改帖子的评论
    int updateCommentCount(int id, int commentCount);

}
