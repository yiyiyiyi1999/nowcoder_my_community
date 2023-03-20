package com.my.my_community.controller.interceptor;

import com.my.my_community.annotation.LoginRequired;
import com.my.my_community.util.HostHolder;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){//如果拦截的是一个方法的话
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            ///得到方法上的注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            if(loginRequired != null && hostHolder.getUsers() == null){
                //不为空，说明是需要判断才能访问的
                //而且如果这时候没有用户，也就是没登陆，那就得拦截
                response.sendRedirect(request.getContextPath() + "/login");//转到登陆页面
                return false;//拒绝后续的请求

            }

        }
        return true;
    }
}
