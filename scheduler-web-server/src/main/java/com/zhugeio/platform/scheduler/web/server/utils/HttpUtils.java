package com.zhugeio.platform.scheduler.web.server.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * TODO
 *
 * @author zhoulijiang
 * @date 2025/5/20 21:15
 **/
public class HttpUtils {

    /**
     * 从 HttpServletRequest 的 Cookie 中获取指定名称的 Token
     *
     * @param request HttpServletRequest 对象
     * @param cookieName 要获取的 Cookie 名称（例如 "token"）
     * @return Token 值，如果没有找到则返回 null
     */
    public static String getTokenFromCookies(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
