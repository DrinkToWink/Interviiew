package com.myinterview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author: 一个小菜逼
 * @Date: 2020/3/29
 */
@MapperScan("com.myinterview.dao")
@SpringBootApplication
public class MyInterviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyInterviewApplication.class,args);
    }
}
