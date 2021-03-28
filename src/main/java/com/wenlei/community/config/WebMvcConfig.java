package com.wenlei.community.config;

import com.wenlei.community.interceptor.DataInterceptor;
import com.wenlei.community.interceptor.LoginRequiredInterceptor;
import com.wenlei.community.interceptor.LoginTicketInterceptor;
import com.wenlei.community.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

/*
    废弃登录拦截器
    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;
*/

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;

    // /**/.css，表示所有目录下的所有css文件
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/static/css/*.css", "/static/js/*.js", "/static/img/*.png", "/static/img/*.jpg", "/static/img/*.jpeg");
/*

        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/static/css/*.css", "/static/js/*.js", "/static/img/*.png", "/static/img/*.jpg", "/static/img/*.jpeg");
*/

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/static/css/*.css", "/static/js/*.js", "/static/img/*.png", "/static/img/*.jpg", "/static/img/*.jpeg");

        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/static/css/*.css", "/static/js/*.js", "/static/img/*.png", "/static/img/*.jpg", "/static/img/*.jpeg");

    }

}
