package com.whiteboard.whiteboardapp2.Controller;

import com.whiteboard.whiteboardapp2.Model.WhiteboardAction;
import com.whiteboard.whiteboardapp2.Repo.CacheRepository;
import com.whiteboard.whiteboardapp2.Repo.WhiteboardActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Optional;

import static com.whiteboard.whiteboardapp2.Constants.WB_ACTION_PREFIX;

@Controller
public class DrawController {
    @Autowired
    private CacheRepository cacheRepository;

    @MessageMapping("/draw-stroke")
    @SendTo("/topic/board-state")
    public String drawStroke(@Payload WhiteboardAction action) {
        //cacheRepository.put(WB_ACTION_PREFIX + "test1", "This is a stroke !");

        return "Drew stroke!";
    }

    @MessageMapping("/draw-shape")
    @SendTo("/topic/board-state")
    public String drawShape(@Payload WhiteboardAction action) {
        return "Drew shape!";
    }

    @MessageMapping("/draw-text")
    @SendTo("/topic/board-state")
    public String drawText(@Payload WhiteboardAction action) {
        //cacheRepository.put(WB_ACTION_PREFIX + "test1", "This is a text !");

        return "Drew text!";
    }

    @MessageMapping("/get-board-state")
    @SendTo("/topic/board-state")
    public String getBoardState() {
        // TODO: Send state from SQL database along with cached state from Redis
        Optional<String> value = cacheRepository.get(WB_ACTION_PREFIX + "test1");

        return value.orElse("Value was empty !");
    }
}
