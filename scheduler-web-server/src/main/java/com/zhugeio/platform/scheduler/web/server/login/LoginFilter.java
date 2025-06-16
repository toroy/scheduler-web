package com.zhugeio.platform.scheduler.web.server.login;

import com.zhugeio.platform.scheduler.common.exception.BizException;
import com.zhugeio.platform.scheduler.web.core.Constants;
import com.zhugeio.platform.scheduler.web.core.service.TokenService;
import com.zhugeio.platform.scheduler.web.core.service.UserService;
import com.zhugeio.platform.scheduler.web.core.utils.LoginGuavaCacheUtil;
import com.zhugeio.platform.scheduler.web.core.utils.SpringBean;
import com.zhugeio.platform.scheduler.web.core.vo.UserVO;


import com.google.common.collect.Lists;

import com.zhugeio.platform.scheduler.web.server.utils.HttpUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.apache.http.HttpStatus;
import org.springframework.core.env.Environment;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
 import java.util.List;
import java.util.logging.Logger;


public class LoginFilter implements Filter {

	private static final Logger logger = Logger.getLogger("LoginFilter");

    private List<String> excludePathPatterns;

    private List<String> excludeUrl = Lists.newArrayList("/check-health");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String token = HttpUtils.getTokenFromCookies(request, Constants.TOKEN_KEY);

        Environment env = SpringBean.getBean(Environment.class);
        List<String> profiles = Arrays.asList(env.getActiveProfiles());
        List<String> devProfiles = Lists.newArrayList("local","dev", "test");
        devProfiles.retainAll(profiles);
        if (CollectionUtils.isNotEmpty(devProfiles) && StringUtils.equals(token, Constants.DEFAULT_TOKEN_KEY)) {
            LoginUserDto userDto = new LoginUserDto();
            userDto.setIsAdmin(true);
            userDto.setAlias("tourist");
            userDto.setLocalUserId(2L);
            LocalUser.set(userDto);
            filterChain.doFilter(request, response);
            return;
        }

        
        String requertUri = request.getRequestURI();
        if (!excludeUrl.contains(requertUri)) {
            logger.info("request url is " + request.getRequestURL());
        }

        if (!needLogin(requertUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 通过token直接登录
        String userToken = request.getParameter(Constants.TOKEN_KEY);
        if (StringUtils.isNotBlank(userToken)) {
        	TokenService tokenService = SpringBean.getBean(TokenService.class);
        	if (tokenService.isExist(userToken)) {
                getUserInfo(userToken, tokenService);
                filterChain.doFilter(request, response);
                return;
        	}
        }

        if (token == null) {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return;
        }

        Object userLogin = LoginGuavaCacheUtil.get(token);
        if (userLogin == null) {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return;
        }

        LoginUserDto loginUserDto = (LoginUserDto) userLogin;
        LocalUser.set(loginUserDto);

        filterChain.doFilter(request, response);

    }

    public void init(FilterConfig filterConfig) throws ServletException {
        String excludePath = filterConfig.getInitParameter("excludePath");
        if (!StringUtils.isEmpty(excludePath)) {
            excludePathPatterns = Lists.newArrayList();
            String[] excludePaths = excludePath.split(",");
            for (String path : excludePaths) {
                excludePathPatterns.add(path);
            }
        }
    }

    private void getUserInfo(String userToken, TokenService tokenService) {
        Long createUser = tokenService.getCreateUser(userToken);
        UserService userService = SpringBean.getBean(UserService.class);
        UserVO userVO = userService.getUserInfoById(createUser);
        LoginUserDto userDto = new LoginUserDto();
        userDto.setIsAdmin(userVO.getIsAdmin());
        userDto.setUserid(userVO.getUid());
        userDto.setName(userVO.getName());
        userDto.setAlias(userVO.getAlias());
        userDto.setLocalUserId(userVO.getId());
        LocalUser.set(userDto);
    }
    private boolean needLogin(String uri) {
        if (excludePathPatterns == null || excludePathPatterns.size() <= 0) {
            return true;
        }

        for (String pattern : excludePathPatterns) {
            if (uri.contains(pattern)) {
                return false;
            }
        }

        return true;
    }
}
