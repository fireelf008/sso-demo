package com.wsf.controller;

import com.alibaba.fastjson.JSONObject;
import com.wsf.utils.EncodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by fireelf on 2017/6/4.
 */
@Controller
public class SSOController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String KEY = "encodekey";

    /**
     * 验证token有效性
     *
     * @return
     */
    @RequestMapping(value = "/validate")
    @ResponseBody
    public JSONObject validate(HttpServletRequest request, HttpServletResponse response) {
        String token = EncodeUtils.aesDecoder(EncodeUtils.urlDecoder(request.getParameter("token"), "UTF-8"), KEY);

        //redis中验证用户登录有效性
        Object userObj = this.redisTemplate.opsForValue().get(token);
        JSONObject resultObj = new JSONObject();
        resultObj.put("success", true);
        resultObj.put("msg", null);
        resultObj.put("data", null != userObj);
        return resultObj;
    }

    /**
     * 登录
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/login")
    public String login(HttpServletRequest request, HttpServletResponse response) {

        String aesReturnStr = request.getParameter("return");
        String returnStr = EncodeUtils.aesDecoder(aesReturnStr, KEY);
        JSONObject returnObj = JSONObject.parseObject(returnStr);
        String sessionUrl = returnObj.getString("sessionUrl");
        String returnUrl = returnObj.getString("returnUrl");

        //判断是否已有其他系统登录，获取sso-server的cookie，从cookie获取token，从redis中根据token获取用户信息，
        //如果存在用户信息则跳转回sso-client，其他所有情况都跳转到登录页面
        Cookie[] cookies = request.getCookies();
        if (null != cookies) {
            boolean hasToken = false;
            String token = null;

            for (Cookie cookie : cookies) {
                if ((applicationName + "_token").equals(cookie.getName())) {
                    hasToken = true;
                    token = cookie.getValue();
                    break;
                }
            }

            //如果存在token则从redis获取用户登录信息
            if (hasToken) {
                Object userObj = this.redisTemplate.opsForValue().get(token);
                if (null != userObj) {
                    JSONObject dataObj = new JSONObject();
                    dataObj.put("token", token);
                    dataObj.put("user", userObj);
                    dataObj.put("returnUrl", returnUrl);

                    JSONObject resultObj = new JSONObject();
                    resultObj.put("success", true);
                    resultObj.put("msg", null);
                    resultObj.put("data", dataObj);

                    String url = sessionUrl + "?return=" + EncodeUtils.urlEncoder(EncodeUtils.aesEncoder(resultObj.toJSONString(), KEY), "UTF-8");
                    try {
                        response.sendRedirect(url);
                        return null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        request.setAttribute("return", aesReturnStr);
        return "login";
    }

    @RequestMapping(value = "/subLogin")
    public String subLogin(HttpServletRequest request, HttpServletResponse response) {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String aesReturnStr = request.getParameter("return");
        String returnStr = EncodeUtils.aesDecoder(aesReturnStr, KEY);
        JSONObject returnObj = JSONObject.parseObject(returnStr);
        String sessionUrl = returnObj.getString("sessionUrl");
        String returnUrl = returnObj.getString("returnUrl");

        //用户登录验证
        if ("root".equals(username) && "root".equals(password)) {

            JSONObject userObj = new JSONObject();
            userObj.put("id", 1);
            userObj.put("name", "张三");
            userObj.put("sex", "女");
            userObj.put("age", 28);

            //生成UUID
            String token = UUID.randomUUID().toString();

            //将登录用户放入redis
            this.redisTemplate.opsForValue().set(token, userObj);

            //生成cookie
            Cookie cookie = new Cookie(applicationName + "_token", token);
            response.addCookie(cookie);

            JSONObject dataObj = new JSONObject();
            dataObj.put("token", token);
            dataObj.put("user", userObj);
            dataObj.put("returnUrl", returnUrl);

            JSONObject resultObj = new JSONObject();
            resultObj.put("success", true);
            resultObj.put("msg", null);
            resultObj.put("data", dataObj);

            String url = sessionUrl + "?return=" + EncodeUtils.urlEncoder(EncodeUtils.aesEncoder(resultObj.toJSONString(), KEY), "UTF-8");
            try {
                response.sendRedirect(url);
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        request.setAttribute("return", returnStr);
        return "login";
    }
}
