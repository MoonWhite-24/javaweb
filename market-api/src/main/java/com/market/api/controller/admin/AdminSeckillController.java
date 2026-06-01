package com.market.api.controller.admin;

import com.market.common.model.PageResult;
import com.market.common.model.R;
import com.market.dal.entity.SeckillProduct;
import com.market.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/seckill-products")
@CrossOrigin
public class AdminSeckillController {
    @Autowired
    private SeckillService seckillService;

    @GetMapping
    public R<PageResult<SeckillProduct>> list(@RequestParam(required = false) Integer status,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "20") int pageSize) {
        java.util.List<SeckillProduct> list = seckillService.adminList(status, pageNum, pageSize);
        long total = seckillService.adminCount(status);
        return R.ok(new PageResult<>(total, pageNum, pageSize, list));
    }

    @PostMapping
    public R<Void> create(@RequestBody SeckillProduct p) { seckillService.add(p); return R.ok(); }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody SeckillProduct p) {
        p.setId(id); seckillService.update(p); return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) { seckillService.delete(id); return R.ok(); }
}
