package com.alibaba.fastjson.support.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Annotation;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FastJsonView {
    FastJsonFilter[] include() default {};
    
    FastJsonFilter[] exclude() default {};
}
