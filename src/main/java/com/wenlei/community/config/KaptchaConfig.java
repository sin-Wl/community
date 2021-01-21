package com.wenlei.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Bean
    public Producer kaptchaProducer() {

        Properties properties = new Properties();

        // 生成的图片宽度  (单位：像素)
        properties.setProperty("kaptcha.image.width", "100");

        // 图片高度
        properties.setProperty("kaptcha.image.height", "40");

        // 字号大小
        properties.setProperty("kaptcha.textproducer.font.size", "32");

        // 字的颜色
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");

        // 生成验证码的随机字符范围
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");

        // 生成几个随机字符
        properties.setProperty("kaptcha.textproducer.char.length", "4");

        // 采用哪个噪声类(干扰)
        properties.setProperty("kaptcha.noist.impl", "com.google.code.kaptcha.impl.NoNoise");

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;

    }


}