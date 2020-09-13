package com.myinterview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

//配置mvc
@Configuration
@EnableAsync//表示支持异步调用
public class WebConfig extends AsyncConfigurerSupport {

    //获取一个任务池 当spring环境中遇到注释@Async就会启动这个任务池的一条线程去运行对应的方法
    //这就实现了异步
    @Bean
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //核心线程数，当新任务过来时，会先判断线程池中的核心线程数量是5个，如果少于5个，就会重新创建线程
        //放到线程池中
        taskExecutor.setCorePoolSize(5);
        //最大线程数，如果核心线程来不及处理过多的任务，则会将任务放到队列里面，如果队列放满了，就会重新
        //创建线程来执行任务。总的线程数最大为10（核心线程+队列放满之后创建的线程）
        //当队列再次被放满时，会执行相应的策略进行处理，默认的策略是抛出异常、拒绝任务（丢掉任务）
        taskExecutor.setMaxPoolSize(10);
        //队列容量
        taskExecutor.setQueueCapacity(200);
        //进行初始化
        taskExecutor.initialize();
        //返回线程池
        return taskExecutor;
    }
}
