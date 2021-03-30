package com.wenlei.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

//@Component
//@Aspect
public class ServiceLogAspect {

    public static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    // 切点
    // *代表方法的返回值（什么返回值都行）
    // com.lzh0108.community.service.*.*(..)) 表示service包下面的所有类的（任意参数）所有方法
    @Pointcut("execution(* com.wenlei.community.service.*.*(..))")
    public void pointcut() {

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {

        // 日志格式
        // 用户[1.2.3.4],在[xxx],访问了[com.lzh0108.community.service.xxx.xxx()].

        // 获取request对象
        // 通过RequestContextHolder工具类获取request对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 如果是一个特殊的调用（不是来自页面的请求），则暂时不必记录日志
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();

        // 获取用户的ip地址
        String ip = request.getRemoteHost();

        // 获取时间
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // 得到类名和方法名，前面是类名，后面是方法名
        // joinPoint.getSignature().getDeclaringTypeName()得到类名
        // joinPoint.getSignature().getName()得到方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        logger.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));

    }


}