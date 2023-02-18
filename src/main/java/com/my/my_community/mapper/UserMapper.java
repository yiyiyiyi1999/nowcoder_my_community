package com.my.my_community.mapper;

import com.my.my_community.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    //根据id查询用户
    User selectById(int id);
    //根据用户名查询用户
    User selectByName(String username);
    //根据邮箱查询用户
    User selectByEmail(String email);

    //增加一个用户，返回插入数据的行数
    int insertUser(User user);

    //修改用户的状态，根据id才能更好地定位用户
    //修改用户的状态status
    int updateStatus(int id, int status);
    //修改用户的头像
    int updateHeader(int id, String headerUrl);
    //修改用户的密码
    int updatePwd(int id, String password);

}
