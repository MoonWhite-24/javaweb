package com.market.api.controller.admin;

import com.market.common.model.R;
import com.market.dal.dto.StatsDTO;
import com.market.service.SeckillService;
import com.market.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminDashboardController {
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private StatsService statsService;

    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        StatsDTO statsDTO = statsService.getDashboardStats();
        Map<String, Object> result = new HashMap<>();
        result.put("todayOrderCount", statsDTO.getTodayOrderCount());
        result.put("todayRevenue", statsDTO.getTodayRevenue());
        result.put("todayNewUsers", statsDTO.getTodayNewUsers());
        result.put("unpaidOrders", statsDTO.getUnpaidOrders());
        result.put("totalProducts", statsDTO.getTotalProducts());
        result.put("totalUsers", statsDTO.getTotalUsers());
        result.put("topProducts", statsDTO.getTopProducts());
        result.put("seckillCount", seckillService.adminCount(null));
        return R.ok(result);
    }
}
