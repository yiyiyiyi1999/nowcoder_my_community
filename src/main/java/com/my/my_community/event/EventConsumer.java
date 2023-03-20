package com.my.my_community.event;

import com.alibaba.fastjson.JSONObject;
import com.my.my_community.entity.DiscussPost;
import com.my.my_community.entity.Event;
import com.my.my_community.entity.Message;
import com.my.my_community.service.DiscussPostService;
import com.my.my_community.service.ElasticsearchService;
import com.my.my_community.service.MessageService;
import com.my.my_community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {
    //记录日志
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;//要像message里更新系统消息

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 消费系统通知
     * @param record
     */
    //用一个方法处理三个主题
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record){
        //判断
        if(record == null || record.value() == null){
            logger.error("消息内容为空！");
            return;
        }
        //把JSON恢复成对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("消息格式错误！");
        }
        //后台发消息，是1，然后conversation_id就不用存发送和接收那种了，改为存主题
        //存数据的格式参考message表
        //发送站内通知，增添一条message
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        //content，因为需要很多关于message的具体信息，用map
        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());
        //可能event里还有额外数据，也都存到content里
        if(!event.getData().isEmpty()){
            for(Map.Entry<String,Object> entry: event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);

    }

    /**
     * 消费发帖事件
     */
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        //判断
        if(record == null || record.value() == null){
            logger.error("消息内容为空！");
            return;
        }
        //把JSON恢复成对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("消息格式错误！");
        }
        //查到帖子，然后存到es
        DiscussPost post = discussPostService.findDiscussPostById((event.getEntityId()));
        elasticsearchService.saveDiscussPost(post);


    }
}
