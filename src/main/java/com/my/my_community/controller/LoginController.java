package com.my.my_community.controller;

import com.google.code.kaptcha.Producer;
import com.my.my_community.entity.User;
import com.my.my_community.service.UserService;
import com.my.my_community.util.CommunityConstant;
import com.my.my_community.util.CommunityUtil;
import com.my.my_community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    //日志
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;

    //注入kaptcha对象
    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    //访问注册页面
    @RequestMapping(value = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }
    //访问登录页面
    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    //用户注册
    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){//这里的参数User其实就是从前端接收值的，只要传入的值和user的属性相匹配，就会自动注入
        Map<String, Object> map = userService.register(user);
        if(map.isEmpty() || map == null){//如果注册成功了
            model.addAttribute("message","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";//注册成功就提示注册成功
        }else{//注册失败
            model.addAttribute("usernameMessage",map.get("usernameMessage"));
            model.addAttribute("passwordMessage",map.get("passwordMessage"));
            model.addAttribute("emailMessage",map.get("emailMessage"));
            return "/site/register";
        }
    }

    //激活过程
    @RequestMapping(value = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId")int userId, @PathVariable("code")String code){
        int result = userService.activated(userId, code);
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("message","激活成功！请尽快登录！");
            model.addAttribute("target","/login");
        }else if(result == ACTIVATION_REPEAT){
            model.addAttribute("message","该账户已激活！");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("message","激活失败！您的激活码不正确！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    //登录，生成验证码（需要从服务器获取一个验证码图片，这个方法不需要返回什么新页面，只是需要从服务器返回的response里获取）
    //某次请求的时候获得验证码，用户输入之后还需要验证输得对不对，也就是说这个验证码相关得在多次请求中共用
    //再加上存在浏览器端不安全----->session
    @RequestMapping(value = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
        //生成验证码
        String text = kaptchaProducer.createText();//验证码字符串，这个需要存到session里，给后面用
        BufferedImage image = kaptchaProducer.createImage(text);//生成验证码图片
        //将验证码存到session
//        session.setAttribute("kaptcha",text);
        //将验证码存到redis
        //生成验证码的归属
        String kaptchaOwner = CommunityUtil.gennerateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        //发送给客户端
        response.addCookie(cookie);
        //将验证码存入redis
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);//存+设置过期时间

        //将图片输出到浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }

    }

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model,/*HttpSession session, */HttpServletResponse response,
                        @CookieValue("kaptchaOwner")String kaptchaOwner){
        //判断验证码对不对
//        String kaptcha = (String)session.getAttribute("kaptcha");
        String kaptcha = null;
        //判断cookie里的owner是否存在
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String)redisTemplate.opsForValue().get(redisKey);
        }

        if(!kaptcha.equalsIgnoreCase(code) || StringUtils.isBlank(code) || StringUtils.isBlank(kaptcha)){
            model.addAttribute("codeMessage","验证码不正确");
            return "/site/login";
        }
        //检查账号密码
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECOND : DEFAULT_EXPIRED_SECOND;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")){//如果有ticket，存
            //把ticket取出来给客户端cookie存
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);//cookie有效的路径
            cookie.setMaxAge(expiredSeconds);//cookie的时间
            response.addCookie(cookie);//发送cookie
            return "redirect:/index";//重定向！！
        }else{//有问题
            model.addAttribute("usernameMessage",map.get("usernameMessage"));
            model.addAttribute("passwordMessage",map.get("passwordMessage"));
            return "/site/login";
        }

    }

    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }




}
