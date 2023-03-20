package com.my.my_community.service;

import com.my.my_community.entity.LoginTicket;
import com.my.my_community.entity.User;
import com.my.my_community.mapper.LoginTicketMapper;
import com.my.my_community.mapper.UserMapper;
import com.my.my_community.util.CommunityConstant;
import com.my.my_community.util.CommunityUtil;
import com.my.my_community.util.MailClient;
import com.my.my_community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;//注入一个值

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 查询用户
     * @param id
     * @return
     */
    public User findUserById(int id){
//        return userMapper.selectById(id);
        //从cache里查
        User user = getCache(id);
        if(user == null){
            user = ininCache(id);//为空，初始化
        }
        return user;
    }

    /**
     * 注册
     * 用map存储了返回的提示信息
     * @return
     */
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        //空值处理(用户、用户名、密码、邮箱）
        if(user == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMessage","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMessage","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMessage","邮箱不能为空");
            return map;
        }
        //开始验证，验证用户名、邮箱是否已经存在
        if(userMapper.selectByName(user.getUsername()) != null){
            map.put("usernameMessage","账号已存在");
            return map;
        }
        if(userMapper.selectByName(user.getEmail()) != null){
            map.put("emailMessage","邮箱已存在");
            return map;
        }
        //开始注册用户，就是把用户的信息存到数据库里
        //密码加密
        user.setSalt(CommunityUtil.gennerateUUID().substring(0,5));//别太长了、、
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        //用户信息
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.gennerateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));//随机头像
        user.setCreateTime(new Date());
        //调用方法添加用户即可 好好理解这些分层的思想！！dao处理数据库相关操作！！
        userMapper.insertUser(user);
        //发激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账户",content);

        return map;

    }
    //是否已激活
    public int activated(int userId, String code){
        //查询该用户
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){//已经激活过了
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            clearCache(userId);//状态更改了，清理缓存
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }

    }

    //登录功能（登陆状态、凭证相关）
    public Map<String,Object> login(String username, String password, int expiredSeconds){
        Map<String,Object> map = new HashMap<>();
        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMessage","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMessage","密码不能为空");
            return map;
        }
        //合法性验证，是否用户存在
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMessage","该账号不存在");
            return map;
        }
        //账户是否已激活
        if(user.getStatus() == 0){
            map.put("statusMessage","该账号未激活");
            return map;
        }
        //验证密码
        password = CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMessage","密码不正确");
            return map;
        }
        //信息是对的，可以登陆成功了，生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.gennerateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds *1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        //存到redis里
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);//对象会自动序列化为json字符串

        map.put("ticket",loginTicket.getTicket());

        return map;
    }

    //退出：把状态改成失效的
    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        //取出loginTicket
        LoginTicket loginTicket = (LoginTicket)redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        //覆盖原有的值
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    //根据ticket查询用户
    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selecByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        //取出loginTicket
        return (LoginTicket)redisTemplate.opsForValue().get(redisKey);
    }

    //修改头像路径
    public int updateHeader(int userId, String headerUrl){
        int rows = userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;
    }
    //修改密码
    public Map<String,Object> updatePassword(int userId, String lastPwd, String curPwd, String curPwdAgain){
        Map<String,Object> map = new HashMap<>();
        //判断是否为空
        if(curPwd == null || lastPwd == null || curPwdAgain == null){
            map.put("error","密码为空");
            return map;
        }
        //判断原密码是否正确
        User user = userMapper.selectById(userId);
        if(!CommunityUtil.md5(lastPwd+user.getSalt()).equals(user.getPassword())){
            map.put("error","原密码不正确");
            return map;
        }
        //判断两次输入是否相同
        if(!curPwdAgain.equals(curPwd)){
            map.put("error","两次输入的密码不同！");
            return map;
        }
        //正确，修改密码
        userMapper.updatePwd(userId,CommunityUtil.md5(lastPwd+user.getSalt()));
        return map;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    /**
     * 用户缓存相关私有方法
     */
    //1.优先从缓存中取值
    private User getCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User)redisTemplate.opsForValue().get(userKey);
    }
    //2.取不到就初始化缓存数据，从mysql里查
    private User ininCache(int userId){
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user,3600, TimeUnit.SECONDS);
        return user;
    }
    //3.数据变更的时候，清除缓存
    private void clearCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }








}
