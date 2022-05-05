package com.cola;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(value = "com.cola.mapper")
@ComponentScan(basePackages = {"com.cola","com.idworker"})
public class ChatRoomServerApplication extends SpringBootServletInitializer{

    @Bean
    public SpringUtil getSpringUtil(){
        return new SpringUtil();
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ChatRoomServerApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ChatRoomServerApplication.class, args);
    }

}
