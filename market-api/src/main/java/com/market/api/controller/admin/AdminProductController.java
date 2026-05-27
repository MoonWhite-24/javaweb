package com.market.api.controller.admin;

import com.market.common.model.PageResult;
import com.market.common.model.R;
import com.market.dal.entity.Product;
import com.market.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin
public class AdminProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public R<PageResult<Product>> list(@RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer status,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(productService.adminList(categoryId, keyword, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public R<Product> get(@PathVariable Long id) { return R.ok(productService.getDetail(id)); }

    @PostMapping
    public R<Void> create(@RequestBody Product p) { productService.add(p); return R.ok(); }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Product p) {
        p.setId(id); productService.update(p); return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        productService.updateStatus(id, body.get("status")); return R.ok();
    }
}
