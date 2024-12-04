package com.whiteboard.whiteboardapp2.Service;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class WebSocketHandshakeHandler extends DefaultHandshakeHandler {
    @NonNull
    @Override
    protected Principal determineUser(@NonNull ServerHttpRequest serverHttpRequest,
                                      @NonNull  WebSocketHandler webSocketHandler,
                                      @NonNull  Map<String, Object> attributes) {
        return new Principal() {
            private final String name = UUID.randomUUID().toString();
            @Override
            public String getName() {
                return name;
            }
        };
    }
}
