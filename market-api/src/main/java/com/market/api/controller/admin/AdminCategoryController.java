package com.market.api.controller.admin;

import com.market.common.model.R;
import com.market.dal.entity.Category;
import com.market.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@CrossOrigin
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/tree")
    public R<List<Category>> tree() {
        // Return all categories including disabled ones for admin
        return R.ok(categoryService.getAll());
    }

    @PostMapping
    public R<Void> create(@RequestBody Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return R.error("分类名称不能为空");
        }
        if (category.getParentId() == null) {
            category.setParentId(0);
        }
        if (category.getLevel() == null) {
            category.setLevel(category.getParentId() == 0 ? 1 : 2);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        categoryService.add(category);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Integer id, @RequestBody Category category) {
        category.setId(id);
        category.setUpdateTime(LocalDateTime.now());
        categoryService.update(category);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return R.ok();
    }
}
