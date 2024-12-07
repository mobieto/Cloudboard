package com.whiteboard.whiteboardapp2.Repo;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface CacheRepository {
    void put(String key, String value, Long ttl);

    void put(String key, String value, boolean expire);

    Long increment(String key, Long ttl);

    Long increment(String key, boolean expire);

    Long decrement(String key, Long ttl);

    Long decrement(String key, boolean expire);

    Optional<String> get(String key);

    List<String> getMulti(String key);

    HashMap<String, String> getMultiWithKeys(String key);

    void remove(String key);

    void flushAll(String key);
}
