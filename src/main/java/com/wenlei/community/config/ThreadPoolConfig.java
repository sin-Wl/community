package com.wenlei.community.config;

// @EnableScheduling 启动定时任务线程池
// @EnableAsync 使@Async注解生效

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {

}
