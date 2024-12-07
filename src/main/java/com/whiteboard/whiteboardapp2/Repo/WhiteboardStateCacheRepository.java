package com.whiteboard.whiteboardapp2.Repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.*;
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
            throw new RuntimeException("Error incrementing to Redis cache");
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
            throw new RuntimeException("Error incrementing to Redis cache");
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
            throw new RuntimeException("Error decrementing to Redis cache");
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
            throw new RuntimeException("Error decrementing to Redis cache");
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
    public List<String> getMulti(String key) {
        try {
            ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();
            List<String> keys = new ArrayList<>();
            ScanOptions scanOptions = ScanOptions.scanOptions().match(key).build();
            Cursor<String> cursor = redisTemplate.scan(scanOptions);

            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }

            cursor.close();

            List<String> values = valueOperations.multiGet(keys);
            values.removeIf(Objects::isNull);

            return values;
        } catch (RuntimeException runtimeException) {
            throw new RuntimeException("Error getting multiple values from Redis cache");
        }
    }

    @Override
    public HashMap<String, String> getMultiWithKeys(String key) {
        try {
            ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();
            ScanOptions scanOptions = ScanOptions.scanOptions().match(key).build();
            Cursor<String> cursor = redisTemplate.scan(scanOptions);
            HashMap<String, String> out = new HashMap<>();

            while (cursor.hasNext()) {
                String _key = cursor.next();
                out.put(_key, valueOperations.get(_key));
            }

            cursor.close();

            return out;
        } catch (RuntimeException runtimeException) {
            throw new RuntimeException("Error getting multiple keys and values from Redis cache");
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

    @Override
    public void flushAll(String key) {
        try {
            ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();
            ScanOptions scanOptions = ScanOptions.scanOptions().match(key).build();
            Cursor<String> cursor = redisTemplate.scan(scanOptions);

            while (cursor.hasNext()) {
                valueOperations.getAndDelete(cursor.next());
            }

            cursor.close();
        } catch (RuntimeException runtimeException) {
            throw new RuntimeException("Error removing from Redis cache");
        }
    }
}
