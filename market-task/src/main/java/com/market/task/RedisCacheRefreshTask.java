package com.market.task;

import com.market.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheRefreshTask {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheRefreshTask.class);

    @Autowired
    private CategoryService categoryService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void refreshCategoryCache() {
        try {
            categoryService.refreshCache();
            log.debug("Category cache refreshed by scheduled task");
        } catch (Exception e) {
            log.error("Failed to refresh category cache", e);
        }
    }
}
