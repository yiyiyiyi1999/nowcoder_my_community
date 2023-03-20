package com.my.my_community.mapper;

import com.my.my_community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    //分页
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType, int entityId);

    Comment selectCommentById(int id);

    //增加评论
    int insertComment(Comment comment);
}

