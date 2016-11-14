package com.eliteams.quick4j.web.controller;

import com.eliteams.quick4j.web.model.User;
import com.eliteams.quick4j.web.security.PermissionSign;
import com.eliteams.quick4j.web.security.RoleSign;
import com.eliteams.quick4j.web.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 用户控制器
 * 
 * @author StarZou
 * @since 2014年5月28日 下午3:54:00
 **/
@Controller
@RequestMapping(value = "/user")
public class UserController {

    @Resource
    private UserService userService;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    /**
     * 用户登录
     * 
     * @param user
     * @param result
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@Valid User user, BindingResult result, Model model, HttpServletRequest request,HttpServletResponse response) {
        try {
            Subject subject = SecurityUtils.getSubject();
            String cpath=request.getContextPath();
            // 已登陆则 跳到首页
            if (subject.isAuthenticated()) {
                response.sendRedirect(cpath+"/rest/home/index");
                return null;
            }
            if (result.hasErrors()) {
                model.addAttribute("error", "参数错误！");
                return "login";
            }
            // 身份验证
            subject.login(new UsernamePasswordToken(user.getUsername(), user.getPassword()));
            // 验证成功在Session中保存用户信息
            final User authUserInfo = userService.selectByUsername(user.getUsername());
            request.getSession().setAttribute("userInfo", authUserInfo);
            response.sendRedirect(cpath+"/rest/home/index");
            return null;
        } catch (Exception e) {
            // 身份验证失败
            model.addAttribute("error", "用户名或密码错误 ！");
            return "login";
        }
    }

    /**
     * 用户登出
     * 
     * @param session
     * @return
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session) {
        session.removeAttribute("userInfo");
        // 登出操作
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "login";
    }

    /**
     * 基于角色 标识的权限控制案例
     */
    @RequestMapping(value = "/admin")
    @ResponseBody
    @RequiresRoles(value = RoleSign.ADMIN)
    public String admin() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("admin")){
            return "拥有admin角色,能访问";
        }else {
            return "不是admin角色，不能访问";
        }
    }

    /**
     * 基于权限标识的权限控制案例
     */

    @RequestMapping(value = "/toAdd")
    public String toAdd(){
        return "/pages/register";
    }
    @RequestMapping(value = "/create")
    @ResponseBody
    @RequiresPermissions(value = PermissionSign.USER_CREATE)
    public String create(User user) {
        userService.register(user);
        return "index";
    }
}
