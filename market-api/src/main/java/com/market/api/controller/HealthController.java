package com.market.api.controller;

import com.market.common.model.R;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@CrossOrigin
public class HealthController {
    @GetMapping("/api/health")
    public R<Map<String, String>> health() {
        return R.ok(Map.of("status", "UP", "timestamp", String.valueOf(System.currentTimeMillis())));
    }
}
