package com.market.api.controller;

import com.market.common.model.PageResult;
import com.market.common.model.R;
import com.market.dal.entity.Category;
import com.market.dal.entity.Product;
import com.market.service.CategoryService;
import com.market.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/products")
    public R<PageResult<Product>> list(@RequestParam(required = false) Integer categoryId,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String orderBy,
                                        @RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(productService.listPage(categoryId, keyword, orderBy, pageNum, pageSize));
    }

    @GetMapping("/products/{id}")
    public R<Product> detail(@PathVariable Long id) {
        return R.ok(productService.getDetail(id));
    }

    @GetMapping("/categories/tree")
    public R<List<Category>> categories() {
        return R.ok(categoryService.getTree());
    }
}
