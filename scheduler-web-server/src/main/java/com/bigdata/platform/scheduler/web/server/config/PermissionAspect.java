package com.bigdata.platform.scheduler.web.server.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

	// .. 当前包及子包，*即所有类，.*(..)表示任何方法名，括号表示参数，两个点表示任何参数类型
	@Pointcut("execution(* com.bigdata.platform.scheduler.web.server.service..*.*(..))")
	public void point() {

	}

	@Before("point()")
	public void doBefore(JoinPoint joinPoint) {
//		System.out.println("-----------------------------------------------");
//		System.out.println(joinPoint.getArgs());
//		System.out.println(JSON.toJSON(joinPoint.getArgs()));
//		CheckId id = JSON.parseObject(joinPoint.getArgs().toString(), CheckId.class);
//		System.out.println(id);
	}
}
