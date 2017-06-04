package com.wsf.controller;

import com.alibaba.fastjson.JSONObject;
import com.wsf.utils.EncodeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by fireelf on 2017/6/4.
 */
@Controller
public class SSOController {

    private static final String KEY = "encodekey";

    /**
     * 跳转到sso-server登录
     * @return
     */
    public String login() {
        return null;
    }

    /**
     * 登录成功后创建session
     * @return
     */
    @RequestMapping(value = "/session")
    public String session(HttpServletRequest request, HttpServletResponse response){

        String returnStr = EncodeUtils.aesDecoder(request.getParameter("return"), KEY);
        JSONObject returnObj = JSONObject.parseObject(returnStr);
        JSONObject dataObj = returnObj.getJSONObject("data");
        JSONObject userObj = dataObj.getJSONObject("user");
        String returnUrl = dataObj.getString("returnUrl");
        String token = dataObj.getString("token");

        //将用户信息和token保存到session中，跳转到登录成功页面
        HttpSession session = request.getSession();
        session.setAttribute("user", userObj);
        session.setAttribute("token", token);

        return "redirect:" + returnUrl;
    }

    @RequestMapping(value = "/index")
    public String index() {
        return "index";
    }
}
