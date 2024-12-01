package com.whiteboard.whiteboardapp2.Repo;

import java.util.Optional;

public interface CacheRepository {
    void put(String key, String value);

    Optional<String> get(String key);

    void remove(String key);
}
