package com.market.api.controller;

import com.market.common.model.R;
import com.market.common.model.UserDTO;
import com.market.dal.entity.SeckillProduct;
import com.market.service.SeckillService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seckill")
@CrossOrigin
public class SeckillController {
    @Autowired
    private SeckillService seckillService;

    @GetMapping("/products")
    public R<List<SeckillProduct>> products() { return R.ok(seckillService.getActiveList()); }

    @PostMapping("/execute/{productId}")
    public R<Map<String, Object>> execute(@PathVariable Long productId, HttpServletRequest r) {
        UserDTO u = (UserDTO) r.getAttribute("currentUser");
        return R.ok(seckillService.doSeckill(u.getId(), productId));
    }

    @GetMapping("/result/{productId}")
    public R<Long> result(@PathVariable Long productId, HttpServletRequest r) {
        UserDTO u = (UserDTO) r.getAttribute("currentUser");
        return R.ok(seckillService.getSeckillResult(u.getId(), productId));
    }
}
