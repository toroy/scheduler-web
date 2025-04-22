package com.clubfactory.platform.scheduler.web.server.config;

import com.clubfactory.platform.scheduler.web.server.login.LoginFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class FilterConfig {

    @Autowired
    Environment environment;

	@Bean
	public FilterRegistrationBean<LoginFilter> registFilter() {
		FilterRegistrationBean<LoginFilter> registration = new FilterRegistrationBean<LoginFilter>();
        registration.setFilter(new LoginFilter());
        registration.addUrlPatterns("/*");
        registration.addInitParameter("excludePath","/swagger-ui.html,/check-health");
        registration.setName("midwareSsoLoginFilter");
        registration.setOrder(1);

        // 初始化参数
//        registration.addInitParameter("ssoServerUrl", environment.getProperty("club-boot.sso.ssoServerUrl"));
//        registration.addInitParameter("sessionTimeout", environment.getProperty("club-boot.sso.sessionTimeout"));

        return registration;
	}
}
