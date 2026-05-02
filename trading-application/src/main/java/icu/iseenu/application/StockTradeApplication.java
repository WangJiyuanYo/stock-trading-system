package icu.iseenu.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class
})
@ComponentScan(basePackages = {
    "icu.iseenu.application",
    "icu.iseenu.stock",
    "icu.iseenu.feishu",
    "icu.iseenu.ai",
    "icu.iseenu.roco",
    "icu.iseenu.notification",
    "icu.iseenu.infra",
    "icu.iseenu.domain",
    "icu.iseenu.common"
})
@EnableScheduling // 启用定时任务功能
public class StockTradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockTradeApplication.class, args);
    }
}
