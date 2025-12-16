package com.sample.sampleservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.env.Environment;


@Slf4j
@EnableFeignClients
@SpringBootApplication
@RequiredArgsConstructor
public class SampleServiceApplication {


    public static void main(String[] args) {
        Environment env = SpringApplication.run(SampleServiceApplication.class, args).getEnvironment();

        if (log.isInfoEnabled()) {
            log.info(ApplicationStartupTraces.of(env));
        }
    }
}
