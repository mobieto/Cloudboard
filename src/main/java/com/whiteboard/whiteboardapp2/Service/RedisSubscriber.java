package com.whiteboard.whiteboardapp2.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whiteboard.whiteboardapp2.Model.WhiteboardAction;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import reactor.util.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<WhiteboardAction> messages = new ArrayList<>();

    @Override
    public void onMessage(@NonNull Message message, @NonNull byte[] pattern) {
        try {
            WhiteboardAction whiteboardAction = objectMapper.readValue(message.getBody(), WhiteboardAction.class);
            messages.add(whiteboardAction);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
