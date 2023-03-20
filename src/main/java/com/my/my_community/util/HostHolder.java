package com.my.my_community.util;

import com.my.my_community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户的信息，用于代替session对象，起到一个容器的作用
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();//属性
    //存入 and 获取
    public void setUsers(User user){
        users.set(user);
    }

    public User getUsers(){
        return users.get();
    }

    //清理
    public void clear(){
        users.remove();
    }

}
