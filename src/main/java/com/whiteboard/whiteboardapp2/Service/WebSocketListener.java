package com.whiteboard.whiteboardapp2.Service;

import com.whiteboard.whiteboardapp2.Repo.CacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

import static com.whiteboard.whiteboardapp2.Constants.WB_STATE_PREFIX;

@Service
public class WebSocketListener extends TextWebSocketHandler {
    @Autowired
    private CacheRepository cacheRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @EventListener(SessionConnectEvent.class)
    public void handleWebSocketConnection(SessionConnectEvent event) {
        Long result = cacheRepository.increment(WB_STATE_PREFIX + "num-users", false);

        simpMessagingTemplate.convertAndSend("/topic/connected-users", result);
    }

    @EventListener(SessionDisconnectEvent.class)
    public void handleWebSocketDisconnection(SessionDisconnectEvent event) {
        Long result = cacheRepository.decrement(WB_STATE_PREFIX + "num-users", false);

        if (result < 0) cacheRepository.put(WB_STATE_PREFIX + "num-users", "0", false);

        simpMessagingTemplate.convertAndSend("/topic/connected-users", result);
    }
}
