package com.whiteboard.whiteboardapp2.Service;

import com.whiteboard.whiteboardapp2.Repo.CacheRepository;
import com.whiteboard.whiteboardapp2.Repo.WhiteboardActionRepository;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class WhiteboardStateScheduler {
    @Autowired
    private LockProvider lockProvider;

    @Autowired
    private CacheRepository cacheRepository;

    @Autowired
    private WhiteboardActionRepository whiteboardActionRepository;

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
            // TODO: Aggregate cached state and save to sql db
            System.out.println("Scheduled task!");
        } finally {
            lock.get().unlock();
        }
    }
}
