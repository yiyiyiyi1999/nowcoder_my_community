package com.my.my_community.controller.interceptor;

import com.my.my_community.entity.LoginTicket;
import com.my.my_community.entity.User;
import com.my.my_community.service.UserService;
import com.my.my_community.util.CookieUtil;
import com.my.my_community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;

    /**
     * pre：在请求开始前获得用户，并把用户存到线程中
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取ticket
        String ticket = CookieUtil.getValue(request,"ticket");
        if(ticket != null){//说明已经登录了
            //根据凭证查询用户
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //判断凭证是否有效（是否登录中）
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                //只有有效的时候才查询用户信息
                User user = userService.findUserById(loginTicket.getUserId());
                //把这个user存上，在本次请求中持有user
                //浏览器对服务器是多对一的，是多线程的，线程之间应该隔离--->存到ThreadLocal
                hostHolder.setUsers(user);//存入
            }
        }
        return true;
    }

    /**
     * post:把user存到model里，这样模板引擎就可以用了
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUsers();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser", user);
        }
    }

    /**
     * 清理user
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
