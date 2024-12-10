package com.whiteboard.whiteboardapp2;

import com.whiteboard.whiteboardapp2.Model.WhiteboardAction;
import com.whiteboard.whiteboardapp2.Service.RedisPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
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
