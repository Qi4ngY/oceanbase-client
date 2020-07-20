package com.alibaba.fastjson.support.spring.annotation;

import org.springframework.web.bind.annotation.ResponseBody;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.Annotation;

@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ResponseBody
public @interface ResponseJSONP {
    String callback() default "callback";
}
