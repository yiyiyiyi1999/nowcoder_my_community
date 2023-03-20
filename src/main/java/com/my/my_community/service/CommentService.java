package com.my.my_community.service;

import com.my.my_community.entity.Comment;
import com.my.my_community.mapper.CommentMapper;
import com.my.my_community.mapper.DiscussPostMapper;
import com.my.my_community.util.CommunityConstant;
import com.my.my_community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }
    //新增帖子，要进行事务管理
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        //是否为空
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        //对内容进行过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));//这是自带的方法，对html标签过滤
        comment.setContent(sensitiveFilter.filter(comment.getContent()));//过滤敏感词
        int rows = commentMapper.insertComment(comment);

        //在帖子表里更新评论数量（需要判断是不是评论给帖子的）
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            //查评论数
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            //更新到帖子表
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }


}
