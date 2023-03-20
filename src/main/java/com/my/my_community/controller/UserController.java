package com.my.my_community.controller;

import com.my.my_community.annotation.LoginRequired;
import com.my.my_community.entity.User;
import com.my.my_community.service.FollowService;
import com.my.my_community.service.LikeService;
import com.my.my_community.service.UserService;
import com.my.my_community.util.CommunityConstant;
import com.my.my_community.util.CommunityUtil;
import com.my.my_community.util.HostHolder;
import com.sun.mail.imap.protocol.MODSEQ;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${community.path.upload}")
    private String upLoadPath;//注入上传路径

    @Value("${community.path.domain}")
    private String domain;//注入域名

    @Value("${server.servlet.context-path}")
    private String contextPath;//访问路径

    @LoginRequired
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    /**
     * 上传头像
     * @param headerImage
     * @param model
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        //判断参数是否为空
        if(headerImage == null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }
        //如果图片存在
        //获得原文件名后缀
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
        }
        //为了避免覆盖，给文件生成一个随机的名字
        fileName = CommunityUtil.gennerateUUID() + "." + suffix;//这里有点
        //确定文件存放的全路径
        File dest = new File(upLoadPath + "/" + fileName);
        try {
            //存储文件
            headerImage.transferTo(dest);//把文件存入目标位置
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常");
        }
        //存完之后，更改用户得头像路径，是web访问路径
        // http://localhost:8080/community/user/header/xx(后面这段是自定义的)
        User user = hostHolder.getUsers();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }
    /**
     * 获取头像
     * 注意这个路径就是头像图片的web路径
     * 这个方法的响应是一个图片
     */
    @RequestMapping(value = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //从服务器存放的图片路径得到图片
        fileName = upLoadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        //响应图片（是啥？？？）
        response.setContentType("image/" + suffix);//这个后缀不能有点！！！
        //响应的图片是二进制数据，所以字节流
        try (
                ServletOutputStream os = response.getOutputStream();
                //得是读本地文件（输入流），然后再输出（输出流）
                FileInputStream fis = new FileInputStream(fileName);
        ){
            //缓冲区
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    /**
     * 修改密码
     */
    @LoginRequired
    @RequestMapping(value = "/updatePwd",method = RequestMethod.POST)
    public String updatePwd(String lastPwd, String curPwd,String curPwdAgain, Model model){
        User user = hostHolder.getUsers();
        Map<String, Object> map = userService.updatePassword(user.getId(), lastPwd, curPwd, curPwdAgain);
        if(map.isEmpty() || map == null){
            //成功
            return "redirect:/index";
        }else{
            model.addAttribute("error",map.get("error"));
            return "/site/setting";
        }
    }
    /**
     * 个人主页
     */
    @RequestMapping(value = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        //查用户
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        //查询获赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //查询关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //查询粉丝数量
        long followeCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followeCount);
        //查询关注数量
        boolean hasFollowed = false;
        //判断用户是否登录
        if(hostHolder.getUsers() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUsers().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }




}
