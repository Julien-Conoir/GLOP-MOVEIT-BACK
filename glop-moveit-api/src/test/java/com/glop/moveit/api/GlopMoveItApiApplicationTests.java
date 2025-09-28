package com.glop.moveit.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootTest(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class GlopMoveItApiApplicationTests {

    @Test
    void contextLoads() {
        // Test que le contexte Spring Boot se charge correctement
    }
}