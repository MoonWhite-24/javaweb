package com.market.service;

import com.market.dal.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getTree();
    List<Category> getAll();
    Category getById(Integer id);
    void add(Category category);
    void update(Category category);
    void delete(Integer id);
    void refreshCache();
}
