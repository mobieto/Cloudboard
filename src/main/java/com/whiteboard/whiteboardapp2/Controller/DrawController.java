package com.whiteboard.whiteboardapp2.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whiteboard.whiteboardapp2.Model.WhiteboardAction;
import com.whiteboard.whiteboardapp2.Repo.CacheRepository;
import com.whiteboard.whiteboardapp2.Repo.WhiteboardActionRepository;
import com.whiteboard.whiteboardapp2.Service.RedisPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.whiteboard.whiteboardapp2.Constants.*;

@Controller
public class DrawController {
    @Autowired
    private CacheRepository cacheRepository;

    @Autowired
    private WhiteboardActionRepository whiteboardActionRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private RedisPublisher redisPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MessageMapping("/draw-stroke")
    public void drawStroke(@Payload WhiteboardAction action, Principal principal) {
        try {
            String result = objectMapper.writeValueAsString(action);
            cacheRepository.put(WB_ACTION_PREFIX + action.getId(), result, true);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("data", action);
        payload.put("excludedSessionId", principal.getName());

        Map<String, Object> publishPayload = new HashMap<>();
        publishPayload.put("host", HOST_NAME);
        publishPayload.put("data", action);

        redisPublisher.publish(publishPayload);
        simpMessagingTemplate.convertAndSend("/topic/new-stroke", payload);
    }

    @MessageMapping("/draw-shape")
    public void drawShape(@Payload WhiteboardAction action, Principal principal) {
        try {
            String result = objectMapper.writeValueAsString(action);
            cacheRepository.put(WB_ACTION_PREFIX + action.getId(), result, true);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("data", action);
        payload.put("excludedSessionId", principal.getName());

        Map<String, Object> publishPayload = new HashMap<>();
        publishPayload.put("host", HOST_NAME);
        publishPayload.put("data", action);

        redisPublisher.publish(publishPayload);
        simpMessagingTemplate.convertAndSend("/topic/new-shape", payload);
    }

    @MessageMapping("/draw-text")
    public void drawText(@Payload WhiteboardAction action, Principal principal) {
        try {
            String result = objectMapper.writeValueAsString(action);
            cacheRepository.put(WB_ACTION_PREFIX + action.getId(), result, true);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("data", action);
        payload.put("excludedSessionId", principal.getName());

        Map<String, Object> publishPayload = new HashMap<>();
        publishPayload.put("host", HOST_NAME);
        publishPayload.put("data", action);

        redisPublisher.publish(publishPayload);
        simpMessagingTemplate.convertAndSend("/topic/new-text", payload);
    }

    @MessageMapping("/get-board-state")
    @SendToUser("/topic/board-state")
    public List<String> getBoardState() {
        // TODO: Send state from SQL database along with cached state from Redis
        List<String> cachedData = cacheRepository.getMulti(WB_ACTION_PREFIX + "*");
        List<WhiteboardAction> primaryData = whiteboardActionRepository.findAll();
        List<String> jsonPrimaryData = primaryData.stream().map(object -> {
            try {
                return objectMapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        return Stream.concat(jsonPrimaryData.stream(), cachedData.stream()).toList();
    }

    @MessageMapping("/clear-board")
    @SendTo("/topic/clear-board")
    public boolean clearBoard() {
        simpMessagingTemplate.convertAndSend("/topic/clear-board", false);

        cacheRepository.flushAll(WB_ACTION_PREFIX + "*");
        whiteboardActionRepository.deleteAll();

        return true;
    }

    @MessageMapping("/get-num-users")
    @SendTo("/topic/connected-users")
    public Long getNumUsers() {
        return Long.parseLong(cacheRepository.get(WB_STATE_PREFIX + "num-users").orElse("0"));
    }

    @MessageMapping("/get-session")
    @SendToUser("/topic/session")
    public List<String> getSession(Principal principal) {
        return List.of(principal.getName(), HOST_NAME);
    }
}
