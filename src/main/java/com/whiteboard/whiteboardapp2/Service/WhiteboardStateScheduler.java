package com.whiteboard.whiteboardapp2.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whiteboard.whiteboardapp2.Model.WhiteboardAction;
import com.whiteboard.whiteboardapp2.Repo.CacheRepository;
import com.whiteboard.whiteboardapp2.Repo.WhiteboardActionRepository;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.whiteboard.whiteboardapp2.Constants.WB_ACTION_PREFIX;
import static com.whiteboard.whiteboardapp2.Constants.WB_STATE_PREFIX;

@Service
public class WhiteboardStateScheduler {
    @Autowired
    private LockProvider lockProvider;

    @Autowired
    private CacheRepository cacheRepository;

    @Autowired
    private WhiteboardActionRepository whiteboardActionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private void save() throws JsonProcessingException {
        HashMap<String, String> data = cacheRepository.getMultiWithKeys(WB_ACTION_PREFIX + "*");
        List<WhiteboardAction> toSave = new ArrayList<>();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            toSave.add(objectMapper.readValue(value, WhiteboardAction.class));
            cacheRepository.remove(key);
        }

        whiteboardActionRepository.saveAll(toSave);
    }

    @Scheduled(cron = "0 0/5 * * * ?") // execute every 5 minutes
    public void aggregateAndSaveCache() {
        Optional<SimpleLock> lock = lockProvider.lock(
                new LockConfiguration(
                        Instant.now(),
                        "WbStateScheduler_aggregateAndSaveCache",
                        Duration.ofMinutes(4), // lock max duration
                        Duration.ofMinutes(4) // local min duration
                )
        );

        if (lock.isEmpty()) return; // lock is held by another instance

        try {
            save();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            lock.get().unlock();
        }
    }

    @EventListener({ ContextClosedEvent.class })
    public void onInstanceShutdown() {
        Long result = cacheRepository.decrement(WB_STATE_PREFIX + "instances", false);

        if (result < 1) {
            try {
                save();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @EventListener({ ApplicationReadyEvent.class })
    public void onInstanceStart() {
        cacheRepository.increment(WB_STATE_PREFIX + "instances", false);
    }
}
