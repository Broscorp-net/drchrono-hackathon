package com.remind.me.doc;

import com.github.messenger4j.Messenger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

@SpringBootApplication
@EnableScheduling
public class Application {

  @Bean
  public Messenger messenger(@Value("${pageAccessToken}") String pageAccessToken,
                             @Value("${appSecret}") final String appSecret,
                             @Value("${verifyToken}") final String verifyToken) {
    return Messenger.create(pageAccessToken, appSecret, verifyToken);
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}