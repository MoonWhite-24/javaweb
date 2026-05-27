package com.market.service;

import com.market.common.model.PageResult;
import com.market.dal.entity.Product;
import java.util.List;

public interface ProductService {
    PageResult<Product> listPage(Integer categoryId, String keyword, String orderBy, int pageNum, int pageSize);
    Product getDetail(Long id);
    void add(Product product);
    void update(Product product);
    void updateStatus(Long id, Integer status);
    void updateStock(Long id, int delta);
    PageResult<Product> adminList(Integer categoryId, String keyword, Integer status, int pageNum, int pageSize);
    List<Product> getHot(int limit);
}
