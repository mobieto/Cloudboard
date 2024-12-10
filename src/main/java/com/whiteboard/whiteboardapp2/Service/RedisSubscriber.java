package com.whiteboard.whiteboardapp2.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whiteboard.whiteboardapp2.Model.WhiteboardAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.util.annotation.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.whiteboard.whiteboardapp2.Constants.HOST_NAME;

@Service
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void onMessage(@NonNull Message message, @NonNull byte[] pattern) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getBody(), Map.class);

            if (payload.get("host").equals(HOST_NAME)) return; // prevent same instance from sending actions again

            WhiteboardAction whiteboardAction = objectMapper.convertValue(payload.get("data"), WhiteboardAction.class);
            String action = whiteboardAction.getAction().split(",")[0];

            Map<String, Object> outPayload = new HashMap<>();
            outPayload.put("data", whiteboardAction);
            outPayload.put("excludedSessionId", "");

            simpMessagingTemplate.convertAndSend("/topic/new-" + action, outPayload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
