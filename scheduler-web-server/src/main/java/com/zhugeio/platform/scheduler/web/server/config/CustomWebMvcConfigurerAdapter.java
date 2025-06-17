package com.zhugeio.platform.scheduler.web.server.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.zhugeio.platform.scheduler.web.server.config.parambind.CurrentUserArgumentResolver;
import com.zhugeio.platform.scheduler.web.server.config.parambind.RequestJsonParamMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@EnableWebMvc
@Configuration
public class CustomWebMvcConfigurerAdapter implements WebMvcConfigurer {

	@Autowired
	private Environment env;

	/**
	 * 开发环境
	 */
	private final String PRO_PROFILE = "prod";


	/**
	 * fasterjson 解析
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(fastJsonHttpMessageConverterEx());
		converters.add(resourceHttpMessageConverter());
	}

	/**
	 *@RequestJsonParam 请求
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(requestJsonParamMethod());
		argumentResolvers.add(currentUserArgumentResolver());
	}
	
	/**
	 * 静态资源
	 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("**").addResourceLocations("classpath:/templates/");
        List<String> profiles = Arrays.asList(env.getActiveProfiles());
        if (!profiles.contains(PRO_PROFILE) ) {
			registry.addResourceHandler("swagger-ui.html")
					.addResourceLocations("classpath:/META-INF/resources/");
			registry.addResourceHandler("/webjars/**")
					.addResourceLocations("classpath:/META-INF/resources/webjars/");
		}
    }

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
//		registry.addInterceptor(new BaseInterceptor()).addPathPatterns("/**");
		//super.addInterceptors(registry);
	}

    
    @Bean
    public ErrorPageRegistrar errorPageRegistrar(){
        return new MyErrorPageRegistrar();
    }
    
    /**
     * 
     * 404页面
     * 
     * @author 陈泰（周利江）
     * @Date 2017年11月6日上午11:08:13
     *
     */
    private static class MyErrorPageRegistrar implements ErrorPageRegistrar {

        @Override
        public void registerErrorPages(ErrorPageRegistry registry) {
            registry.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/404.htm"));
        }

    }

	/**
	 * 返回 fasterjson
	 * 
	 * @return
	 */
	@Bean
	public FastJsonHttpMessageConverterEx fastJsonHttpMessageConverterEx() {
		FastJsonHttpMessageConverterEx fastConverter = new FastJsonHttpMessageConverterEx();
		List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
		supportedMediaTypes.add(MediaType.APPLICATION_JSON);
		supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
		supportedMediaTypes.add(MediaType.APPLICATION_ATOM_XML);
		supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
		supportedMediaTypes.add(MediaType.APPLICATION_PDF);
		supportedMediaTypes.add(MediaType.APPLICATION_RSS_XML);
		supportedMediaTypes.add(MediaType.APPLICATION_XHTML_XML);
		supportedMediaTypes.add(MediaType.APPLICATION_XML);
		supportedMediaTypes.add(MediaType.IMAGE_GIF);
		supportedMediaTypes.add(MediaType.IMAGE_JPEG);
		supportedMediaTypes.add(MediaType.IMAGE_PNG);
		supportedMediaTypes.add(MediaType.TEXT_EVENT_STREAM);
		supportedMediaTypes.add(MediaType.TEXT_HTML);
		supportedMediaTypes.add(MediaType.TEXT_MARKDOWN);
		supportedMediaTypes.add(MediaType.TEXT_PLAIN);
		supportedMediaTypes.add(MediaType.TEXT_XML);
		fastConverter.setSupportedMediaTypes(supportedMediaTypes);

		// 首字母大字母不转换
		TypeUtils.compatibleWithJavaBean = true;

        // Response保留NULL值
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.WriteMapNullValue,
				SerializerFeature.WriteNullListAsEmpty
		);
        // 统一进行日期转换
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        fastConverter.setFastJsonConfig(fastJsonConfig);
		return fastConverter;
	}

	@Bean
	public ResourceHttpMessageConverter resourceHttpMessageConverter(){
		ResourceHttpMessageConverter messageConverter = new ResourceHttpMessageConverter();
		List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
		supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
		messageConverter.setSupportedMediaTypes(supportedMediaTypes);
		return messageConverter;
	}

	/**
	 * 解析 @RequestJsonParam
	 * 
	 * @return
	 */
	@Bean
	public RequestJsonParamMethodArgumentResolver requestJsonParamMethod() {
		return new RequestJsonParamMethodArgumentResolver();
	}

	@Bean
	public CurrentUserArgumentResolver currentUserArgumentResolver(){
		return new CurrentUserArgumentResolver();
	}
	


	@Override
	public void addCorsMappings(CorsRegistry corsRegistry) {
		corsRegistry.addMapping("/**")
				.allowedMethods("GET", "HEAD", "POST","PUT", "DELETE", "OPTIONS")
				.allowedOrigins("*")
				.allowedHeaders("*")
				.allowCredentials(true).maxAge(3600);
	}
	
}
