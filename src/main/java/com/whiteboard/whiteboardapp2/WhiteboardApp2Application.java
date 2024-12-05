package com.whiteboard.whiteboardapp2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WhiteboardApp2Application {
    public static void main(String[] args) {
        SpringApplication.run(WhiteboardApp2Application.class, args);
    }
}
