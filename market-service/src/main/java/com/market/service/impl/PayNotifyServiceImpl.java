package com.market.service.impl;

import com.market.common.constant.KafkaTopic;
import com.market.common.constant.RedisKeyPrefix;
import com.market.common.util.JsonUtil;
import com.market.service.OrderService;
import com.market.service.PayNotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class PayNotifyServiceImpl implements PayNotifyService {

    private static final Logger log = LoggerFactory.getLogger(PayNotifyServiceImpl.class);

    @Autowired
    private OrderService orderService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public String handleCallback(Long orderNo, BigDecimal amount, String tradeNo) {
        String idempotentKey = RedisKeyPrefix.PAY_DONE + orderNo;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 1, TimeUnit.HOURS);

        if (Boolean.FALSE.equals(acquired)) {
            log.info("Duplicate payment callback for orderNo={}", orderNo);
            return "success";
        }

        try {
            orderService.paySuccess(orderNo, amount, tradeNo);
            log.info("Payment callback processed: orderNo={}, amount={}, tradeNo={}", orderNo, amount, tradeNo);
            return "success";
        } catch (Exception e) {
            log.error("Payment callback failed: orderNo={}", orderNo, e);
            redisTemplate.delete(idempotentKey);
            return "fail";
        }
    }
}
