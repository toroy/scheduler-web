package com.clubfactory.platform.scheduler.web.server.config.parambind;

import java.lang.annotation.*;

/**
 * @author xiejiajun
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
