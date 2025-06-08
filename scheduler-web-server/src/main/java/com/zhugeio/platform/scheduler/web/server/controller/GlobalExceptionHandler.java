package com.zhugeio.platform.scheduler.web.server.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.common.exception.BizException;
import com.zhugeio.platform.scheduler.web.core.enums.ErrorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xiejiajun
 */
@ControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
	

    @ExceptionHandler(value = Exception.class)
    public BaseResult<String> exceptionHandler(Exception e){
        log.error(e.getMessage(),e);
        if (e instanceof NoHandlerFoundException) {
        	return new BaseResult<String>(404, BaseResult.FAILED_MSG);
        } else if (e instanceof BizException) {
        	BizException bizException = (BizException) e;
        	return new BaseResult<String>(bizException.getErrorCode(), bizException.getMessage());
        } else {
        	return new BaseResult<String>(ErrorCode.UNKNOWN_EXCEPTION.setParams(e.getMessage()));
        }
    }
}
