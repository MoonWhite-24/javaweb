package com.market.service.impl.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory token bucket rate limiter.
 * Each key gets its own bucket. Buckets are never removed (suitable for
 * a bounded set of keys such as user IDs or IPs).
 */
@Component
public class TokenBucketRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(TokenBucketRateLimiter.class);

    private static final double DEFAULT_CAPACITY = 100.0;
    private static final double DEFAULT_REFILL_RATE = 10.0; // tokens per second

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Attempt to acquire a token for the given key.
     * The bucket is refilled based on elapsed time before the token attempt.
     *
     * @param key the rate-limit key (e.g. userId or IP)
     * @return true if a token was acquired, false if rate-limited
     */
    public boolean tryAcquire(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket());
        return bucket.tryAcquire();
    }

    /**
     * Attempt to acquire a token with custom capacity and refill rate.
     * The bucket is created with these custom values if it does not already exist
     * (but existing buckets are not re-configured).
     */
    public boolean tryAcquire(String key, double capacity, double refillRate) {
        Bucket bucket = buckets.computeIfAbsent(key,
                k -> new Bucket(capacity, refillRate));
        return bucket.tryAcquire();
    }

    /**
     * Internal bucket state. Uses synchronized on the bucket instance for thread safety.
     */
    private static class Bucket {
        private final double capacity;
        private final double refillRate; // tokens per second
        private double tokens;
        private long lastRefillTime;

        Bucket() {
            this(DEFAULT_CAPACITY, DEFAULT_REFILL_RATE);
        }

        Bucket(double capacity, double refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = capacity; // start full
            this.lastRefillTime = System.nanoTime();
        }

        synchronized boolean tryAcquire() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.nanoTime();
            double elapsedSeconds = (now - lastRefillTime) / 1_000_000_000.0;
            double newTokens = elapsedSeconds * refillRate;
            tokens = Math.min(capacity, tokens + newTokens);
            lastRefillTime = now;
        }

        // Visible for testing
        synchronized double getTokens() {
            refill();
            return tokens;
        }
    }

    // Visible for testing
    double getTokens(String key) {
        Bucket bucket = buckets.get(key);
        return bucket != null ? bucket.getTokens() : 0.0;
    }
}
