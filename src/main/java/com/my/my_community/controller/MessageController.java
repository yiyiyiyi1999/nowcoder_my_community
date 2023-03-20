package com.my.my_community.controller;

import com.alibaba.fastjson.JSONObject;
import com.my.my_community.entity.Message;
import com.my.my_community.entity.Page;
import com.my.my_community.entity.User;
import com.my.my_community.service.MessageService;
import com.my.my_community.service.UserService;
import com.my.my_community.util.CommunityConstant;
import com.my.my_community.util.CommunityUtil;
import com.my.my_community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 获取会话列表
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(value = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUsers();
        //设置page相关
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        //查询每一条会话列表的相关信息，放在List里
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for(Message message: conversationList){
                Map<String,Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                int targetId = user.getId()==message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target",userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);

        //查询总的未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        //未读通知数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/letter";
    }

    /**
     * 查看和某个用户的会话
     * @param conversationId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(value = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId")String conversationId, Page page, Model model){
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        //得到私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        //存
        List<Map<String,Object>> letters = new ArrayList<>();
        if(letterList != null){
            for(Message message: letterList){
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                 letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        model.addAttribute("target",getLetterTarget(conversationId));

        //把未读设置为已读
        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }


        return "/site/letter-detail";


    }
    //找到未读消息
    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();
        if(letterList != null){
            for (Message message: letterList){
                //得判断用户是接收者才能有已读
                if(hostHolder.getUsers().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }
    //找到目标用户
    private User getLetterTarget(String consersationId){
        String[] ids = consersationId.split("_");
        int d0 = Integer.parseInt(ids[0]);
        int d1 = Integer.parseInt(ids[1]);
        if(hostHolder.getUsers().getId() == d0){
            return userService.findUserById(d1);
        }else{
            return userService.findUserById(d0);
        }
    }


    /**
     * 发私信
     * @param toName
     * @param content
     * @return
     */
    @RequestMapping(value = "/letter/send",method=RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content){
        User target = userService.findUserByName(toName);
        if(target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUsers().getId());
        message.setToId(target.getId());
        if(message.getToId() < message.getFromId()){
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }else{
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);

    }
    /**
     * 显示通知列表
     */
    @RequestMapping(value="/notice/list",method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUsers();
        //查询评论通知
        Message message = messageService.findLatestNotice(user.getId(),TOPIC_COMMENT);
        //还需要补充信息，用于前端显示
        if(message != null){
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message",message);
            //从content里获得相应内容
            String content = HtmlUtils.htmlUnescape(message.getContent());//反转，这样就没有转义字符了
            HashMap data = JSONObject.parseObject(content, HashMap.class);//转回去的类型和转化时候的类型一致

            messageVO.put("user",userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));

            //还有数量和未读数量
            int count = messageService.findNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVO.put("count",count);

            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_COMMENT);
            messageVO.put("unread",unread);
            model.addAttribute("commentNotice",messageVO);
        }

        //查询点赞通知
        message = messageService.findLatestNotice(user.getId(),TOPIC_LIKE);
        //还需要补充信息，用于前端显示
        if(message != null){
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message",message);
            //从content里获得相应内容
            String content = HtmlUtils.htmlUnescape(message.getContent());//反转，这样就没有转义字符了
            HashMap data = JSONObject.parseObject(content, HashMap.class);//转回去的类型和转化时候的类型一致

            messageVO.put("user",userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));

            //还有数量和未读数量
            int count = messageService.findNoticeCount(user.getId(),TOPIC_LIKE);
            messageVO.put("count",count);

            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_LIKE);
            messageVO.put("unread",unread);

            model.addAttribute("likeNotice",messageVO);
        }


        //查询关注通知
        message = messageService.findLatestNotice(user.getId(),TOPIC_FOLLOW);
        //还需要补充信息，用于前端显示
        if(message != null){
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message",message);
            //从content里获得相应内容
            String content = HtmlUtils.htmlUnescape(message.getContent());//反转，这样就没有转义字符了
            HashMap data = JSONObject.parseObject(content, HashMap.class);//转回去的类型和转化时候的类型一致

            messageVO.put("user",userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));

            //还有数量和未读数量
            int count = messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVO.put("count",count);

            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_FOLLOW);
            messageVO.put("unread",unread);

            model.addAttribute("followNotice",messageVO);
        }


        //还要显示整个系统通知的未读数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        //还有私信的数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        return "/site/notice";


    }

    /**
     * 用于显示详细通知
     */
    @RequestMapping(value="/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic")String topic, Page page, Model model){
        User user = hostHolder.getUsers();
        //分页
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(),topic));
        //开始查询所有通知
        List<Message> noticeList = messageService.findNotices(user.getId(),topic,page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        if(noticeList != null){
            for(Message notice: noticeList){
                Map<String,Object> map = new HashMap<>();
                //通知
                map.put("notice",notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user",userService.findUserById((Integer)data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));
                //通知作者
                map.put("fromUser",userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);

            }
        }
        model.addAttribute("notices",noticeVoList);

        //需要设置是否已读
        //把未读设置为已读
        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }

}
