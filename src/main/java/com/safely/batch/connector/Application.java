package com.safely.batch.connector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.autoconfigure.context.ContextInstanceDataAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextResourceLoaderAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {ContextResourceLoaderAutoConfiguration.class, ContextInstanceDataAutoConfiguration.class})
@EnableScheduling
public class Application {

  public static void main(String[] args) {

    SpringApplication.run(Application.class, args);
  }
}
