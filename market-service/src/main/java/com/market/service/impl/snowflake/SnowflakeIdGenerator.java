package com.market.service.impl.snowflake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Snowflake distributed ID generator.
 * Generates unique, roughly time-ordered 64-bit IDs without coordination.
 */
@Component
public class SnowflakeIdGenerator {

    private static final Logger log = LoggerFactory.getLogger(SnowflakeIdGenerator.class);

    private static final long EPOCH = 1700000000000L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;

    private final long workerId;

    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(@Value("${app.snowflake.worker-id}") long workerId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    "workerId must be between 0 and " + MAX_WORKER_ID);
        }
        this.workerId = workerId;
    }

    /**
     * Generate the next unique ID. Thread-safe.
     */
    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        // Handle clock rollback
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            log.warn("Clock moved backwards by {}ms. Waiting to catch up.", offset);
            if (offset <= 5) {
                // Short rollback: wait it out
                try {
                    wait(offset << 1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted while waiting for clock", e);
                }
                timestamp = currentTimeMillis();
                if (timestamp < lastTimestamp) {
                    throw new RuntimeException(
                            "Clock moved backwards and did not recover. Refusing to generate ID.");
                }
            } else {
                throw new RuntimeException(
                        "Clock moved backwards by " + offset + "ms. Refusing to generate ID.");
            }
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0L) {
                // Sequence exhausted for this millisecond; wait until next millisecond
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
