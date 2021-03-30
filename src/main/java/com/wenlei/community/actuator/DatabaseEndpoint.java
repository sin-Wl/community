package com.wenlei.community.actuator;


import com.wenlei.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 自定义端点
 * 判断是否能够成功获取数据库连接
 * 可以通过http请求直接访问 /actuator/database
 */

@Component
@Endpoint(id = "database")
public class DatabaseEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

    // 连接池的顶层接口
    @Autowired
    private DataSource dataSource;

    // @ReadOperation表示这个方法是通过get请求方式访问的
    @ReadOperation
    public String checkConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return CommunityUtil.getJSONString(0, "获取连接成功！");
        } catch (SQLException e) {
            logger.error("获取连接失败：" + e.getMessage());
            return CommunityUtil.getJSONString(1, "获取连接失败！");
        }
    }


}
