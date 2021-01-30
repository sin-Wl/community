package com.wenlei.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用：标识方法需要登录才能够访问
 */

// @Target用于描述该注解可以作用的目标类型，ElementType.METHOD用来描述方法
@Target(ElementType.METHOD)
// @Retention用于描述该注解被保留的时间
// @Document用于描述该注解是否可以生成到文档里
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {

}
