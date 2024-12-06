package com.whiteboard.whiteboardapp2.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whiteboard.whiteboardapp2.Model.WhiteboardAction;
import com.whiteboard.whiteboardapp2.Repo.CacheRepository;
import com.whiteboard.whiteboardapp2.Repo.WhiteboardActionRepository;
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

import static com.whiteboard.whiteboardapp2.Constants.WB_ACTION_PREFIX;
import static com.whiteboard.whiteboardapp2.Constants.WB_STATE_PREFIX;

@Controller
public class DrawController {
    @Autowired
    private CacheRepository cacheRepository;

    @Autowired
    private WhiteboardActionRepository whiteboardActionRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

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

        simpMessagingTemplate.convertAndSend("/topic/new-stroke", payload);
    }

    @MessageMapping("/draw-shape")
    @SendTo("/topic/board-state")
    public void drawShape(@Payload WhiteboardAction action, Principal principal) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("data", action);
        payload.put("excludedSessionId", principal.getName());

        simpMessagingTemplate.convertAndSend("/topic/new-shape", payload);
    }

    @MessageMapping("/draw-text")
    @SendTo("/topic/board-state")
    public void drawText(@Payload WhiteboardAction action, Principal principal) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("data", action);
        payload.put("excludedSessionId", principal.getName());

        simpMessagingTemplate.convertAndSend("/topic/new-shape", payload);
    }

    @MessageMapping("/get-board-state")
    @SendToUser("/topic/board-state")
    public List<String> getBoardState(Principal principal) {
        // TODO: Send state from SQL database along with cached state from Redis
        List<String> data = cacheRepository.getMulti(WB_ACTION_PREFIX + "*");

        return data;
    }

    @MessageMapping("/get-num-users")
    @SendTo("/topic/connected-users")
    public Long getNumUsers() {
        return Long.parseLong(cacheRepository.get(WB_STATE_PREFIX + "num-users").orElse("0"));
    }

    @MessageMapping("/get-session")
    @SendToUser("/topic/session")
    public String getSession(Principal principal) {
        return principal.getName();
    }
}
