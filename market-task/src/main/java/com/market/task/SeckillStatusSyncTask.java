package com.market.task;

import com.market.dal.entity.SeckillProduct;
import com.market.dal.mapper.SeckillProductMapper;
import com.market.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SeckillStatusSyncTask {

    private static final Logger log = LoggerFactory.getLogger(SeckillStatusSyncTask.class);

    @Autowired
    private SeckillProductMapper seckillProductMapper;
    @Autowired
    private SeckillService seckillService;

    @Scheduled(cron = "0/30 * * * * ?")
    public void syncSeckillStatus() {
        LocalDateTime now = LocalDateTime.now();

        List<SeckillProduct> all = seckillProductMapper.selectAll(null, 0, 1000);
        for (SeckillProduct sp : all) {
            try {
                if (sp.getStatus() == 0 && now.isAfter(sp.getStartTime()) && now.isBefore(sp.getEndTime())) {
                    seckillProductMapper.updateStatus(sp.getId(), 1);
                    seckillService.initStockToRedis(sp.getId());
                    log.info("Seckill {} started", sp.getId());
                } else if (sp.getStatus() == 1 && now.isAfter(sp.getEndTime())) {
                    seckillProductMapper.updateStatus(sp.getId(), 2);
                    log.info("Seckill {} finished", sp.getId());
                }
            } catch (Exception e) {
                log.error("Failed to sync seckill status for id={}", sp.getId(), e);
            }
        }
    }
}
