package com.interviewprep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class JavaFullStackInterviewPrepApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaFullStackInterviewPrepApplication.class, args);
    }
}
