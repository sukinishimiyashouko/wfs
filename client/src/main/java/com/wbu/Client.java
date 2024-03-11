package com.wbu;

import com.wbu.services.FileService;
import com.wbu.utils.ChunkAddressBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@SpringBootApplication
public class Client {
    public static void main(String[] args) {
        ConfigurableApplicationContext ioc = SpringApplication.run(Client.class, args);
//        System.out.println(ioc.getBean("httpChunkAddressBuilder",ChunkAddressBuilder.class));
    }

}
