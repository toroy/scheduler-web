package com.clubfactory.platform.scheduler.web.server.login;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
//import com.clubfactory.middlewaresso.client.bo.CheckTokenBO;
//import com.clubfactory.middlewaresso.client.bo.Result;
//import com.clubfactory.middlewaresso.client.common.Constant;
//import com.clubfactory.middlewaresso.client.common.CookieUtil;
//import com.clubfactory.middlewaresso.client.common.UrlUtil;
//import com.clubfactory.middlewaresso.client.common.httpwarp.HttpClientUtil;
//import com.clubfactory.middlewaresso.client.common.httpwarp.RequestBuilder;
//import com.clubfactory.middlewaresso.client.dto.SsoUserDTO;
//import com.clubfactory.middlewaresso.client.enums.LoginType;
//import com.clubfactory.middlewaresso.client.filter.MidwareSsoLoginFilter;
//import com.clubfactory.middlewaresso.client.service.SsoLoginService;
//import com.clubfactory.middlewaresso.client.user.SsoProxy;
import com.clubfactory.platform.scheduler.common.bean.BaseResult;
import com.clubfactory.platform.scheduler.common.exception.BizException;
import com.clubfactory.platform.scheduler.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.po.User;
import com.clubfactory.platform.scheduler.web.core.enums.CacheEnum;
import com.clubfactory.platform.scheduler.web.core.enums.ErrorCode;
import com.clubfactory.platform.scheduler.web.core.service.TokenService;
import com.clubfactory.platform.scheduler.web.core.service.UserService;
import com.clubfactory.platform.scheduler.web.core.utils.GuavaCacheUtil;
import com.clubfactory.platform.scheduler.web.core.utils.SpringBean;
import com.clubfactory.platform.scheduler.web.core.vo.UserVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import okhttp3.Response;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.core.env.Environment;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class LoginFilter implements Filter {

	private static final Logger logger = Logger.getLogger("LoginFilter");

//    private SsoLoginService ssoLoginService;

    private List<Pattern> excludePathPatterns;

    private String ssoServerUrl;
    
    public static final String LOGOUT = "/user/logout";

    public static final String SAVE_TOKEN_URL = "/ssoAuthToken/save";

    private List<String> excludeUrl = Lists.newArrayList("/check-health");

    private Integer sessionTimeout = 604800;//cookie过期时间，单位s,默认7天 d
    

   // @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;


        Environment env = SpringBean.getBean(Environment.class);
        List<String> profiles = Arrays.asList(env.getActiveProfiles());
        List<String> devProfiles = Lists.newArrayList("local","dev", "test");
        devProfiles.retainAll(profiles);
        if (CollectionUtils.isNotEmpty(devProfiles)){
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
        String userToken = request.getParameter("token");
        if (StringUtils.isNotBlank(userToken)) {
        	TokenService tokenService = SpringBean.getBean(TokenService.class);
        	if (tokenService.isExist(userToken)) {
                getUserInfo(userToken, tokenService);
                filterChain.doFilter(request, response);
                return;
        	}
        }


        // 登出
        if (requertUri.contains(LOGOUT)) {
			// 清除cookies，防止再登
			HttpServletResponse httpServletResponse = (HttpServletResponse) response;
			//CookieUtil.setCookie(httpServletResponse, Constant.COOKIE_NAME, null, 0);

			// 告诉前端成功，由前端进行跳转
			response.setStatus(HttpStatus.SC_OK);
			response.setContentType("application/json;charset=UTF-8");
			BaseResult<Boolean> result = new BaseResult<Boolean>();
			result.setBody(true);
			response.getWriter().write(JSON.toJSONString(result));
			// 跳转到sso登录页
			//String requestUrl = String.format(SSO_LOGIN, StringUtils.removeEnd(request.getRequestURL().toString(), request.getRequestURI().toString()));
			//response.sendRedirect(requestUrl);
			return;
		}

        if (!StringUtils.isEmpty(requertUri) && requertUri.equals(SAVE_TOKEN_URL)) {
            //saveTokenAndRedirect(request, response);
            return;
        }
//
//        String token = CookieUtil.getCookie(request, Constant.COOKIE_NAME);
//        if (StringUtils.isEmpty(token) || !checkTokenByCache(token)) {
//        	response.setStatus(HttpStatus.SC_UNAUTHORIZED);
//            return;
//        }
//
//        SsoUserDTO ssoUserDTO = new SsoUserDTO();
//        checkPermission(ssoUserDTO);
//        setLocalUser(ssoUserDTO, token);

        filterChain.doFilter(request, response);

    }

//    private void checkPermission(SsoUserDTO ssoUserDTO) {
//        if (ssoUserDTO.getEnable() != null && ssoUserDTO.getEnable().equals(0)) {
//            throw new BizException(ErrorCode.NO_PERMISSION);
//        }
//    }

    private StringBuffer getRequestUrl(HttpServletRequest request) {
        StringBuffer requestUrl = request.getRequestURL();
        if(StringUtils.isNotBlank(request.getQueryString())){
            requestUrl.append("?").append(request.getQueryString());
        }
        return requestUrl;
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

//    private SsoUserDTO getSsoUser(String token) {
//		String key = CacheEnum.TOKEN_USER.getKey(token);
//        String ssoUserDtoStr = GuavaCacheUtil.get(key);
//        SsoUserDTO ssoUserDTO = null;
//        if (StringUtils.isNotBlank(ssoUserDtoStr)) {
//        	ssoUserDTO = JSON.parseObject(ssoUserDtoStr, SsoUserDTO.class);
//        } else {
//        	ssoUserDTO = SsoProxy.getSsoUserByToken(token);
//        	GuavaCacheUtil.put(key, JSON.toJSONString(ssoUserDTO));
//        }
//        if (ssoUserDTO == null) {
//        	throw new BizException("sso 用户不存在");
//        }
//		return ssoUserDTO;
//	}

//	private void setLocalUser(SsoUserDTO ssoUserDTO, String token) {
//		UserService userService = SpringBean.getBean(UserService.class);
//        Integer departmentId = null;
//		if (CollectionUtils.isNotEmpty(ssoUserDTO.getDepartment())) {
//            departmentId = ssoUserDTO.getDepartment().get(0);
//        }
//		User user = userService.getOrSaveByUserId(ssoUserDTO.getUserid(), ssoUserDTO.getName(), ssoUserDTO.getDepartment3(), departmentId, ssoUserDTO.getAlias());
//		LoginUserDto userDto = new LoginUserDto();
//		BeanUtil.copyBeanNotNull2Bean(ssoUserDTO, userDto);
//		userDto.setIsAdmin(BooleanUtils.isTrue(user.getIsAdmin()));
//		userDto.setAlias(user.getAlias());
//		userDto.setLocalUserId(user.getId());
//		userDto.setToken(token);
//		userDto.setDepartName(user.getDepartName());
//		userDto.setAvatar(StringUtils.removeEnd(ssoUserDTO.getAvatar(), "0")+"100");
//		LocalUser.set(userDto);
//	}

//    private int getLoginType(HttpServletRequest request) {
//        String ua = request.getHeader("User-Agent");
//        int loginType = LoginType.LOGIN_BY_QRCODE.getType();
//        if (!StringUtils.isEmpty(ua) && ua.contains("wxwork")) {
//            loginType = LoginType.LOGIN_BY_SILENCE.getType();
//        }
//        return loginType;
//    }

    private boolean needLogin(String uri) {
        if (excludePathPatterns == null || excludePathPatterns.size() <= 0) {
            return true;
        }

        for (Pattern pattern : excludePathPatterns) {
            if (pattern.matcher(uri).matches()) {
                return false;
            }
        }

        return true;
    }
//
//    private void saveTokenAndRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//        String token = request.getParameter("token");
//        if (checkTokenByCache(token)) {
//            if (ssoLoginService != null) {
//                ssoLoginService.beforeSaveToken(request, response);
//            }
//            CookieUtil.setCookie(response, Constant.COOKIE_NAME, token, sessionTimeout);
//            String redirectUrl = request.getParameter("redirectUrl");
//            response.sendRedirect(redirectUrl);
//        }
//    }

//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//        try {
//            String excludePath = filterConfig.getInitParameter("excludePath");
//            if (!StringUtils.isEmpty(excludePath)) {
//                excludePathPatterns = Lists.newArrayList();
//                String[] excludePaths = excludePath.split(",");
//                for (String path : excludePaths) {
//                    excludePathPatterns.add(Pattern.compile(UrlUtil.getRegStr(path)));
//                }
//            }
//
//            //hack进去
//            ssoServerUrl = filterConfig.getInitParameter("ssoServerUrl");
//            SsoProxy.ssoServerUrl = ssoServerUrl;
//            String sessionTimeout = filterConfig.getInitParameter("sessionTimeout");
//            if (!StringUtils.isEmpty(sessionTimeout)) {
//                this.sessionTimeout = Integer.valueOf(sessionTimeout);
//            }
//
//            //initSsoLoginService(filterConfig);
//        } catch (Exception e) {
//
//        }
//    }

    private void initSsoLoginService(FilterConfig filterConfig) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
//        String scanPackagePath = filterConfig.getInitParameter("scanPackagePath");
//        if (!StringUtils.isEmpty(scanPackagePath)) {
//            Reflections reflections = new Reflections(scanPackagePath);
//            Set<Class<? extends SsoLoginService>> subClassSet = reflections.getSubTypesOf(SsoLoginService.class);
//            if (subClassSet != null && subClassSet.size() > 0) {
//                for (Class<? extends SsoLoginService> subClass : subClassSet) {
//                    Class<?> aClass = Class.forName(subClass.getName());
//                    ssoLoginService = (SsoLoginService) aClass.newInstance();
//                }
//            }
//        }
    }

//    @Override
//    public void destroy() {
//    }

    
//    private Boolean checkTokenByCache(String token) throws IOException {
//    	String key = CacheEnum.TOEKN_CHECK.getKey(token);
//    	String value = GuavaCacheUtil.get(key);
//    	if (value == null) {
//    		Boolean isTrue = checkToken(token);
//    		GuavaCacheUtil.put(key, isTrue.toString());
//    		return isTrue;
//    	}
//    	return Boolean.valueOf(value);
//    }

//    private boolean checkToken(String token) throws IOException {
//        HashMap<String, String> param = Maps.newHashMap();
//        param.put("token", token);
//
//        Response response = null;
//        try {
//        	response = HttpClientUtil.getInstance().sendAsyncRequest(RequestBuilder.createGetRequest(ssoServerUrl + Constant.CHECK_TOKEN_URL, param));
//		} catch (Exception e) {
//			throw new BizException(ErrorCode.TOKEN_CHECK_ERROR, e);
//		}
//
//        if (response.isSuccessful()) {
//            Result result = JSONObject.parseObject(response.body().string(), Result.class);
//            if (result != null && result.isSuccess() && result.getData() != null) {
//                CheckTokenBO checkTokenBO = JSONObject.parseObject(result.getData().toString(), CheckTokenBO.class);
//                return checkTokenBO.isValid();
//            }
//        }
//
//        return false;
//
//    }
}
