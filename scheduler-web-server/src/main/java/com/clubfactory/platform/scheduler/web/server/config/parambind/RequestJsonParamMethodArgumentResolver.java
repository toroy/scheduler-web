package com.clubfactory.platform.scheduler.web.server.config.parambind;

import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alibaba.fastjson.JSON;


public class RequestJsonParamMethodArgumentResolver implements HandlerMethodArgumentResolver{

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) throws Exception {
		RequestJsonParam requestJsonParam = parameter.getParameterAnnotation(RequestJsonParam.class);
		String[] paramValues = webRequest.getParameterValues(requestJsonParam.value());
		Class<?> paramType = parameter.getParameterType();
		if (paramValues == null) {
			return null;
		}
		try {
			if (paramValues.length == 1) {
				if (Map.class.isAssignableFrom(paramType)) {
					return JSON.parseObject(paramValues[0]);
				}
				return JSON.parseObject(paramValues[0], paramType);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not read request json parameter", e);
		}
		return null;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterAnnotation(RequestJsonParam.class) != null;
	}

}
