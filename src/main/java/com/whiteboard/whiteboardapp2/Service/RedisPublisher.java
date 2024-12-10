package com.whiteboard.whiteboardapp2.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RedisPublisher {
    @Autowired
    private RedisTemplate<String, Object> msgRedisTemplate;

    @Autowired
    private ChannelTopic channelTopic;

    public Long publish(Map<String, Object> payload) {
        return msgRedisTemplate.convertAndSend(channelTopic.getTopic(), payload);
    }
}
