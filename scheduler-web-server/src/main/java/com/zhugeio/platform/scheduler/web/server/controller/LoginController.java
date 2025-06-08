package com.zhugeio.platform.scheduler.web.server.controller;

import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.web.core.Constants;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.service.LoginBizService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

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

    public void saveCookie(String token, HttpServletResponse response) {
        Cookie tokenCookie = new Cookie(Constants.TOKEN_KEY, token);
        tokenCookie.setPath("/");
        response.addCookie(tokenCookie);
    }
}
