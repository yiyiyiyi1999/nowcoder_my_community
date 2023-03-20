package com.my.my_community.util;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;
import netscape.javascript.JSObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

public class CommunityUtil {
    /**生成随机字符串（激活码/随机名字）
     *
     * @return
     */
    public static String gennerateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**MD5加密（只能加密，不能解密）
     * 密码+随机字符串（盐），然后再加密，这样密码就更随机，难以破解
     * @param key
     * @return
     */
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
    /**
     * 封装成json对象并返回json字符串
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if(map != null){
            for(String key: map.keySet()){
                json.put(key,map.get(key));
            }
        }
        return json.toJSONString();
    }
    //重载
    public static String getJSONString(int code, String msg){
        return getJSONString(code,msg,null);
    }
    public static String getJSONString(int code){
        return getJSONString(code,null,null);
    }



}
