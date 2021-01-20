package com.wenlei.community;
import com.wenlei.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail() {
        mailClient.sendMail("251714944@qq.com", "TEST", "welcome");
    }

    @Test
    public void testHTMLMail() {
        Context context = new Context();
        context.setVariable("username", "sunday");

        // 生成动态文件
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("251714944@qq.com", "HTML", content);

    }

}