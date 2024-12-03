package com.whiteboard.whiteboardapp2;

import com.whiteboard.whiteboardapp2.Repo.CacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.whiteboard.whiteboardapp2.Constants.WB_ACTION_PREFIX;

@SpringBootApplication
public class WhiteboardApp2Application implements CommandLineRunner {
    @Autowired
    private CacheRepository cacheRepository;

    public static void main(String[] args) {
        SpringApplication.run(WhiteboardApp2Application.class, args);
    }

    @Override
    public void run(String... args) {
        //Stroke stroke = new Stroke(0L, "1:5", 1, "black");
        //strokeRepository.save(stroke);
        //System.out.println("saved stroke");
        //System.out.println(strokeRepository.findByPixelCoords("1:5"));

        //System.out.println(cacheRepository.getMulti(WB_ACTION_PREFIX + "*"));
    }
}
