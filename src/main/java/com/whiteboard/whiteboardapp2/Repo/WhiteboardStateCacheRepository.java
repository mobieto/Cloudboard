package com.whiteboard.whiteboardapp2.Repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class WhiteboardStateCacheRepository implements CacheRepository {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final long TTL = 360; // values in cache expire after 6 minutes

    @Override
    public void put(String key, String value, boolean expire) {
        try {
            ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();
            valueOperations.set(key, value);

            if (expire) redisTemplate.expire(key, TTL, TimeUnit.SECONDS);
        } catch (RuntimeException runtimeException) {
            throw new RuntimeException("Error saving to Redis cache");
        }
    }

    @Override
    public void put(String key, String value, Long ttl) {
        try {
            ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();
            valueOperations.set(key, value);

            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
        } catch (RuntimeException runtimeException) {
            throw new RuntimeException("Error saving to Redis cache");
        }
    }

    @Override
    public Long increment(String key, Long ttl) {
        try {
            ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();
            Long result = valueOperations.increment(key);

            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);

            return result;
        } catch (RuntimeException runtimeException) {
            throw new RuntimeException("Error saving to Redis cache");
        }
    }

    @Override
    public Long increment(String key, boolean expire) {
        try {
            ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();
            Long result = valueOperations.increment(key);

            if (expire) redisTemplate.expire(key, TTL, TimeUnit.SECONDS);

            return result;
        } catch (RuntimeException runtimeException) {
            throw new RuntimeException("Error saving to Redis cache");
        }
    }

    @Override
    public Long decrement(String key, Long ttl) {
        try {
            ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();
            Long result = valueOperations.decrement(key);

            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);

            return result;
        } catch (RuntimeException runtimeException) {
            throw new RuntimeException("Error saving to Redis cache");
        }
    }

    @Override
    public Long decrement(String key, boolean expire) {
        try {
            ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();
            Long result = valueOperations.decrement(key);

            if (expire) redisTemplate.expire(key, TTL, TimeUnit.SECONDS);

            return result;
        } catch (RuntimeException runtimeException) {
            throw new RuntimeException("Error saving to Redis cache");
        }
    }

    @Override
    public Optional<String> get(String key) {
        try {
            if (redisTemplate.hasKey(key).equals(Boolean.TRUE)) {
                ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();

                return Optional.of(valueOperations.get(key));
            } else {
                return Optional.empty();
            }
        } catch (RuntimeException runtimeException) {
            throw new RuntimeException("Error getting from Redis cache");
        }
    }

    @Override
    public void remove(String key) {
        try {
            ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();

            valueOperations.getAndDelete(key);
        } catch (RuntimeException runtimeException) {
            throw new RuntimeException("Error removing from Redis cache");
        }
    }
}
