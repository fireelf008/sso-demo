package com.wsf.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.wsf.utils.EncodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by fireelf on 2017/6/4.
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${sso.server.loginUrl}")
    private String ssoServerLoginUrl;

    @Value("${sso.server.validateUrl}")
    private String ssoServerValidateUrl;

    @Value("${sso.client.sessionUrl}")
    private String ssoClientSessionUrl;

    @Value("${sso.client.returnUrl}")
    private String ssoClientReturnUrl;

    @Autowired
    private RestTemplate restTemplate;

    private static final String KEY = "encodekey";


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {

        boolean hasToken = false;
        String token = null;

        //判断是否存在session，并且session中是否存在token
        HttpSession session = request.getSession();
        if (null != session && null != session.getAttribute("token")) {
            hasToken = true;
            token = session.getAttribute("token").toString();
        }

        //有token时验证用户登录有效性
        if (hasToken) {
            //请求sso-server验证用户登录有效性
            String url = ssoServerValidateUrl + "?&token=" + EncodeUtils.urlEncoder(EncodeUtils.aesEncoder(token, KEY), "UTF-8");
            String result = this.restTemplate.getForObject(url, String.class);

            //判断用户登录有效性，如果有效则通过拦截器
            JSONObject resultObj = JSONObject.parseObject(result);
            if (resultObj.getBoolean("success") && resultObj.getBoolean("data")) {
                return true;
            }
        }

        //跳转到sso-server登录
        JSONObject returnObj = new JSONObject();
        returnObj.put("sessionUrl", ssoClientSessionUrl);
        returnObj.put("returnUrl", ssoClientReturnUrl);

        //对请求参数加密
        String url = ssoServerLoginUrl + "?return=" + EncodeUtils.urlEncoder(EncodeUtils.aesEncoder(returnObj.toJSONString(), KEY), "UTF-8");
        response.sendRedirect(url);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {

    }
}
