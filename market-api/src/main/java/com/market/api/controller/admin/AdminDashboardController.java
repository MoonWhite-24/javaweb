package com.market.api.controller.admin;

import com.market.common.model.R;
import com.market.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminDashboardController {
    @Autowired
    private SeckillService seckillService;

    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(Map.of("seckillCount", seckillService.adminCount(null)));
    }
}
