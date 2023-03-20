package com.my.my_community.mapper;

import com.my.my_community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    //查询当前用户的所有会话列表，针对每个会话只返回一条最新的私信（还要支持分页）
    List<Message> selectConversations(int userId, int offset, int limit);
    //查询当前用户的会话数量
    int selectConversationCount(int userId);
    //查询某个会话所包含的私信列表(详情页面）（支持分页）
    List<Message> selectLetters(String conversationId, int offset, int limit);
    //查询某个会话包含的私信数量
    int selectLetterCount(String conversationId);
    //查询未读消息数量(所有未读数+和某个用户会话的未读数量，这俩放在同一个方法里）
    int selectLetterUnreadCount(int userId,String conversationId);
    // 新增消息
    int insertMessage(Message message);

    // 修改消息的状态
    int updateStatus(List<Integer> ids, int status);

    //查某个主题下最新的通知
    Message selectLatestNotice(int userId,String topic);

    //查某个主题包含的通知数量
    int selectNoticeCount(int userId, String topic);

    //查某个主题包含的未读数量
    int selectNoticeUnreadCount(int userId, String topic);

    //查询某个主题包含的所有通知，支持分页（用于点进去之后显示具体的）
    List<Message> selectNotices(int userId, String topic, int offset, int limit);

}
