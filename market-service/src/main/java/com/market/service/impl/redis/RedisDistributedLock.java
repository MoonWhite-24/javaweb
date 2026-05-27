package com.market.service.impl.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Simple Redis-based distributed lock using setIfAbsent with TTL
 * and Lua-scripted safe release.
 */
@Component
public class RedisDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

    private static final String LOCK_SUCCESS = "OK";

    /**
     * Lua script that releases the lock only if the value matches.
     * This prevents a client from releasing another client's lock.
     */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setScriptText(
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "return redis.call('del', KEYS[1]) " +
                        "else " +
                        "return 0 " +
                        "end");
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Try to acquire a lock with the given key and value.
     *
     * @param key     the lock key
     * @param value   the lock value (used to verify ownership on release)
     * @param timeout the lock TTL
     * @param unit    the time unit of the timeout
     * @return true if the lock was acquired, false otherwise
     */
    public boolean tryLock(String key, String value, long timeout, TimeUnit unit) {
        Boolean result = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, value, timeout, unit);
        boolean acquired = Boolean.TRUE.equals(result);
        if (acquired) {
            log.debug("Lock acquired: key={}, value={}", key, value);
        } else {
            log.debug("Lock failed: key={}, value={}", key, value);
        }
        return acquired;
    }

    /**
     * Release the lock identified by the given key.
     * Only releases if the current value matches (prevents releasing another client's lock).
     *
     * @param key   the lock key
     * @param value the expected lock value
     * @return true if the lock was released, false otherwise
     */
    public boolean unlock(String key, String value) {
        Long result = stringRedisTemplate.execute(
                UNLOCK_SCRIPT, Collections.singletonList(key), value);
        boolean released = result != null && result == 1L;
        if (released) {
            log.debug("Lock released: key={}, value={}", key, value);
        } else {
            log.debug("Lock release failed (not owner or expired): key={}, value={}", key, value);
        }
        return released;
    }
}
