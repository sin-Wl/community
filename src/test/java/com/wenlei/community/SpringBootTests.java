package com.wenlei.community;


import com.wenlei.community.entity.DiscussPost;
import com.wenlei.community.service.DiscussPostService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;


/**
 * 单元测试
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {

    @Autowired
    private DiscussPostService discussPostService;

    private DiscussPost data;


    // 在类初始化之前执行，只执行一次，需用static修饰
    @BeforeClass
    public static void beforeClass() {
        System.out.println("beforeClass");
    }

    // 在类销毁的时候只执行一次，需用static修饰
    @AfterClass
    public static void afterClass() {
        System.out.println("afterClass");
    }

    // 每执行一个方法之前调用一次
    @Before
    public void before() {
        System.out.println("before");

        // 初始化测试数据
        data = new DiscussPost();
        data.setUserId(111);
        data.setTitle("Test Title");
        data.setContent("Test Content");
        data.setCreateTime(new Date());

        discussPostService.addDiscussPost(data);

    }

    // 每执行一个方法之后调用一次
    @After
    public void after() {
        System.out.println("after");

        // 删除测试数据
        discussPostService.updateStatus(data.getId(), 2);
    }

    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }

    @Test
    public void testFindById() {
        DiscussPost post = discussPostService.findDiscussPostById(data.getId());

        // 断言，判断数据是否非空
        Assert.assertNotNull(post);

        // 比较两个数据是否相等
        Assert.assertEquals(data.getTitle(), post.getTitle());

        // 判断是否一致
        Assert.assertEquals(data.getContent(), post.getContent());

    }

    @Test
    public void testUpdateScore() {
        int rows = discussPostService.updateScore(data.getId(), 2000.00);
        Assert.assertEquals(1, rows);

        DiscussPost post = discussPostService.findDiscussPostById(data.getId());
        // 判断小数是否相等
        // 第三个参数：表示精度为几位小数，    此时是判断两个数到2位小数的地方是否相等
        Assert.assertEquals(2000.00, post.getScore(), 2);
    }

}
