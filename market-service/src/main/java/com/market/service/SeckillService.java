package com.market.service;

import com.market.dal.entity.SeckillProduct;
import java.util.List;
import java.util.Map;

public interface SeckillService {
    Map<String, Object> doSeckill(Long userId, Long seckillProductId);
    Long getSeckillResult(Long userId, Long seckillProductId);
    List<SeckillProduct> getActiveList();
    List<SeckillProduct> adminList(Integer status, int pageNum, int pageSize);
    long adminCount(Integer status);
    void add(SeckillProduct sp);
    void update(SeckillProduct sp);
    void delete(Long id);
    void initStockToRedis(Long seckillProductId);
    void syncStatus();
}
