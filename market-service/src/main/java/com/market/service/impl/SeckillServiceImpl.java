package com.market.service.impl;

import com.market.common.constant.KafkaTopic;
import com.market.common.constant.RedisKeyPrefix;
import com.market.common.constant.SeckillStatusEnum;
import com.market.common.exception.BusinessException;
import com.market.common.util.JsonUtil;
import com.market.dal.entity.SeckillProduct;
import com.market.dal.mapper.SeckillProductMapper;
import com.market.service.OrderService;
import com.market.service.SeckillService;
import com.market.service.impl.snowflake.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillServiceImpl implements SeckillService {

    private static final Logger log = LoggerFactory.getLogger(SeckillServiceImpl.class);

    @Autowired
    private SeckillProductMapper seckillProductMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SnowflakeIdGenerator idGenerator;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private OrderService orderService;

    @Override
    public Map<String, Object> doSeckill(Long userId, Long seckillProductId) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/seckill_stock.lua"));
        script.setResultType(Long.class);

        List<String> keys = Arrays.asList(
                RedisKeyPrefix.SECKILL_STOCK + seckillProductId,
                RedisKeyPrefix.SECKILL_USERS + seckillProductId
        );

        Long result = redisTemplate.execute(script, keys, String.valueOf(userId));

        Map<String, Object> response = new HashMap<>();
        if (result == 1L) {
            long orderNo = idGenerator.nextId();

            Map<String, Object> msg = Map.of(
                    "orderNo", orderNo,
                    "userId", userId,
                    "seckillProductId", seckillProductId,
                    "timestamp", System.currentTimeMillis()
            );
            kafkaTemplate.send(KafkaTopic.ORDER_CREATE, String.valueOf(orderNo), JsonUtil.toJson(msg));

            // Also create order synchronously as fallback
            try {
                orderService.processSeckillOrder(orderNo, userId, seckillProductId);
            } catch (Exception e) {
                log.error("Sync seckill order creation failed: orderNo={}", orderNo, e);
            }

            response.put("success", true);
            response.put("orderNo", orderNo);
            response.put("message", "秒杀成功，订单处理中");
            log.info("Seckill success: userId={}, spId={}, orderNo={}", userId, seckillProductId, orderNo);
        } else if (result == -1L) {
            response.put("success", false);
            response.put("message", "已抢光！");
        } else if (result == -2L) {
            response.put("success", false);
            response.put("message", "您已参与过该商品的秒杀");
        } else {
            response.put("success", false);
            response.put("message", "秒杀失败");
        }
        return response;
    }

    @Override
    public Long getSeckillResult(Long userId, Long seckillProductId) {
        String key = RedisKeyPrefix.SECKILL_ORDER + userId + ":" + seckillProductId;
        String orderNo = redisTemplate.opsForValue().get(key);
        return orderNo != null ? Long.valueOf(orderNo) : null;
    }

    @Override
    public List<SeckillProduct> getActiveList() {
        List<SeckillProduct> list = seckillProductMapper.selectActiveNow();
        for (SeckillProduct sp : list) {
            String stockStr = redisTemplate.opsForValue().get(RedisKeyPrefix.SECKILL_STOCK + sp.getId());
            if (stockStr != null) {
                sp.setStockCount(Integer.parseInt(stockStr));
            }
        }
        return list;
    }

    @Override
    public List<SeckillProduct> adminList(Integer status, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return seckillProductMapper.selectAll(status, offset, pageSize);
    }

    @Override
    public long adminCount(Integer status) {
        return seckillProductMapper.count(status);
    }

    @Override
    public void add(SeckillProduct sp) {
        if (sp.getStatus() == null) sp.setStatus(1);
        seckillProductMapper.insert(sp);
        initStockToRedis(sp.getId());
        log.info("Seckill product added: id={}", sp.getId());
    }

    @Override
    public void update(SeckillProduct sp) {
        seckillProductMapper.updateById(sp);
        initStockToRedis(sp.getId());
    }

    @Override
    public void delete(Long id) {
        seckillProductMapper.deleteById(id);
    }

    @Override
    public void initStockToRedis(Long seckillProductId) {
        SeckillProduct sp = seckillProductMapper.selectById(seckillProductId);
        if (sp != null) {
            redisTemplate.opsForValue().set(
                    RedisKeyPrefix.SECKILL_STOCK + seckillProductId,
                    String.valueOf(sp.getStockCount()));
            redisTemplate.delete(RedisKeyPrefix.SECKILL_USERS + seckillProductId);
        }
    }

    @Override
    public void syncStatus() {
        List<SeckillProduct> activeList = seckillProductMapper.selectActiveNow();
        for (SeckillProduct sp : activeList) {
            if (sp.getStatus() != SeckillStatusEnum.ACTIVE.getCode()) {
                seckillProductMapper.updateStatus(sp.getId(), SeckillStatusEnum.ACTIVE.getCode());
            }
        }
    }
}
