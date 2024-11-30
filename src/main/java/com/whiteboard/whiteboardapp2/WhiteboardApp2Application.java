package com.whiteboard.whiteboardapp2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
public class WhiteboardApp2Application implements CommandLineRunner {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    public static void main(String[] args) {
        SpringApplication.run(WhiteboardApp2Application.class, args);
    }

    @Override
    public void run(String... args) {
        redisTemplate.opsForValue().set("testkey1", "hello world");
        System.out.println(redisTemplate.opsForValue().get("testkey1"));
    }
}
