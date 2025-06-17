package com.zhugeio.platform.scheduler.web.server.controller;

import com.alibaba.fastjson.JSON;
import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.web.client.vo.TableLineageGraphVo;
import com.zhugeio.platform.scheduler.web.core.Constants;
import com.zhugeio.platform.scheduler.web.server.login.LocalUser;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.service.LoginBizService;
import com.zhugeio.platform.scheduler.web.server.utils.HttpUtils;
import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录
 *
 * @author zhoulijiang
 * @date 2025/5/17 22:27
 **/
@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    private LoginBizService loginBizService;

    @PostMapping("register")
    public BaseResult<Boolean> register(@RequestBody LoginUserDto userDto) {
        Boolean isSuccess = loginBizService.register(userDto);
        return new BaseResult<>(isSuccess);
    }

    @PostMapping("login")
    public BaseResult<LoginUserDto> login(@RequestBody LoginUserDto userDto, HttpServletResponse response) {
        LoginUserDto loginUserDto = loginBizService.login(userDto);
        saveCookie(loginUserDto.getToken(), response);
        return new BaseResult<>(loginUserDto);
    }

    @GetMapping("logout")
    public void logout(HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
        String token = HttpUtils.getTokenFromCookies(servletRequest, Constants.TOKEN_KEY);
        loginBizService.logout(token);

        // 告诉前端成功，由前端进行跳转
        response.setStatus(HttpStatus.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        BaseResult<Boolean> result = new BaseResult<Boolean>();
        result.setBody(true);
        response.getWriter().write(JSON.toJSONString(result));
        return;
    }

    public void saveCookie(String token, HttpServletResponse response) {
        Cookie tokenCookie = new Cookie(Constants.TOKEN_KEY, token);
        tokenCookie.setPath("/");
        response.addCookie(tokenCookie);
    }
}
