package org.foo;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@SpringBootApplication
@ComponentScan
//public class Application implements AsyncConfigurer {
public class Application extends SpringBootServletInitializer implements AsyncConfigurer {
        public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        /*

        http://localhost:8080/espring/persons   #(POST)
        {"firstName":"max","lastName":"plank","dateOfBirth":"1982-05-1"}

        http://localhost:8080/espring/persons   #(POST)
        {"firstName":"max2","lastName":"plank","dateOfBirth":"1982-05-10"}

        http://localhost:8080/espring/persons   #(POST)
        {"firstName":"max3","lastName":"plank","dateOfBirth":"1982-05-30"}

        (REST)
        http://localhost:8080/espring/persons/init?byMonth=5   #(GET)
        http://localhost:8080/espring/persons/birthdays?jobId=b878515a-f096-49c9-bdf0-19b938a1560a   #(GET)

        (WEBSOCKET)
        http://localhost:8080/espring/
        connect -> send (result will be printed)

        */
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setThreadNamePrefix("AsyncExecutor");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

}
