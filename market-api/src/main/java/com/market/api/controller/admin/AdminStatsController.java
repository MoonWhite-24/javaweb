package com.market.api.controller.admin;

import com.market.common.model.R;
import com.market.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin
public class AdminStatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/trend")
    public R<Map<String, Object>> trend(@RequestParam(defaultValue = "7") int days) {
        if (days < 1) days = 7;
        if (days > 90) days = 90;
        return R.ok(Map.of(
                "orderTrend", statsService.getOrderTrend(days),
                "revenueTrend", statsService.getRevenueTrend(days)
        ));
    }

    @GetMapping("/order-status")
    public R<List<Map<String, Object>>> orderStatus() {
        return R.ok(statsService.getOrderStatusDistribution());
    }
}
