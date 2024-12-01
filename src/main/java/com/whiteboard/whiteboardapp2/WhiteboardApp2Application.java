package com.whiteboard.whiteboardapp2;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WhiteboardApp2Application implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(WhiteboardApp2Application.class, args);
    }

    @Override
    public void run(String... args) {
        //Stroke stroke = new Stroke(0L, "1:5", 1, "black");
        //strokeRepository.save(stroke);
        //System.out.println("saved stroke");
        //System.out.println(strokeRepository.findByPixelCoords("1:5"));
    }
}
