package icu.iseenu.application;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class
})
@ComponentScan(basePackages = {
    "icu.iseenu.*"
})
@MapperScan("icu.iseenu.stock.mapper")  // 扫描MyBatis-Plus Mapper接口
@EnableScheduling // 启用定时任务功能
public class StockTradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockTradeApplication.class, args);
    }
}
