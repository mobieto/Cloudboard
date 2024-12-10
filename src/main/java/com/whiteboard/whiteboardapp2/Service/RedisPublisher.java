package com.whiteboard.whiteboardapp2.Service;

import com.whiteboard.whiteboardapp2.Model.WhiteboardAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {
    @Autowired
    private RedisTemplate<String, Object> msgRedisTemplate;

    @Autowired
    private ChannelTopic channelTopic;

    public Long publish(WhiteboardAction whiteboardAction) {
        return msgRedisTemplate.convertAndSend(channelTopic.getTopic(), whiteboardAction);
    }
}
