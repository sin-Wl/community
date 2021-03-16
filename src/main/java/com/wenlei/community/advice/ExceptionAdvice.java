package com.wenlei.community.advice;

import com.wenlei.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 统一处理异常
 * @ControllerAdvice 用于修饰类，表示该类是Controller的全局配置类
 */

// 参数annotations配置为，只扫描带有controller注解的bean
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    public static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    // 处理异常
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {

        logger.error("服务器发生异常：" + e.getMessage());

        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        // 判断是否是异步请求
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) { // 异步请求
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常！"));
        } else {
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }

}