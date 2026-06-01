package com.market.service.impl;

import com.market.common.constant.ProductStatusEnum;
import com.market.common.constant.RedisKeyPrefix;
import com.market.common.model.PageResult;
import com.market.common.util.JsonUtil;
import com.market.dal.entity.Category;
import com.market.dal.entity.Product;
import com.market.dal.mapper.ProductMapper;
import com.market.service.CategoryService;
import com.market.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CategoryService categoryService;

    private List<Integer> expandCategoryIds(Integer categoryId) {
        if (categoryId == null) return null;
        List<Integer> ids = new ArrayList<>();
        ids.add(categoryId);
        collectDescendants(categoryId, ids);
        return ids;
    }

    private void collectDescendants(Integer parentId, List<Integer> ids) {
        List<Category> all = categoryService.getAll();
        for (Category c : all) {
            if (parentId.equals(c.getParentId())) {
                ids.add(c.getId());
                collectDescendants(c.getId(), ids);
            }
        }
    }

    @Override
    public PageResult<Product> listPage(Integer categoryId, String keyword, String orderBy, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Integer> categoryIds = expandCategoryIds(categoryId);
        String cacheKey = RedisKeyPrefix.PRODUCT_LIST + (categoryId != null ? categoryId : 0) + ":" + pageNum + ":" + pageSize
                + ":" + (keyword != null ? keyword : "") + ":" + (orderBy != null ? orderBy : "");

        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return JsonUtil.fromJson(cached, PageResult.class);
        }

        List<Product> list = productMapper.selectPage(categoryIds, keyword, ProductStatusEnum.ON_SHELF.getCode(),
                orderBy, offset, pageSize);
        long total = productMapper.count(categoryIds, keyword, ProductStatusEnum.ON_SHELF.getCode());

        PageResult<Product> result = new PageResult<>(total, pageNum, pageSize, list);
        redisTemplate.opsForValue().set(cacheKey, JsonUtil.toJson(result), 5, TimeUnit.MINUTES);
        return result;
    }

    @Override
    public Product getDetail(Long id) {
        String cacheKey = RedisKeyPrefix.PRODUCT + id;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            Product product = JsonUtil.fromJson(cached, Product.class);
            if (product != null) {
                return product;
            }
        }
        Product product = productMapper.selectById(id);
        if (product != null) {
            redisTemplate.opsForValue().set(cacheKey, JsonUtil.toJson(product), 1, TimeUnit.HOURS);
        }
        return product;
    }

    @Override
    public void add(Product product) {
        if (product.getCategoryId() == null || product.getCategoryId() == 0) {
            List<Category> categories = categoryService.getAll();
            if (!categories.isEmpty()) {
                int idx = ThreadLocalRandom.current().nextInt(categories.size());
                product.setCategoryId(categories.get(idx).getId());
            }
        }
        if (product.getSales() == null) product.setSales(0);
        if (product.getCreateTime() == null) product.setCreateTime(java.time.LocalDateTime.now());
        if (product.getUpdateTime() == null) product.setUpdateTime(java.time.LocalDateTime.now());
        productMapper.insert(product);
        var keys = redisTemplate.keys(RedisKeyPrefix.PRODUCT_LIST + "*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
        log.info("Product added: {}", product.getName());
    }

    @Override
    public void update(Product product) {
        productMapper.updateById(product);
        redisTemplate.delete(RedisKeyPrefix.PRODUCT + product.getId());
        log.info("Product updated: {}", product.getId());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        productMapper.updateStatus(id, status);
        redisTemplate.delete(RedisKeyPrefix.PRODUCT + id);
    }

    @Override
    public void updateStock(Long id, int delta) {
        productMapper.updateStock(id, delta);
        redisTemplate.delete(RedisKeyPrefix.PRODUCT + id);
    }

    @Override
    public PageResult<Product> adminList(Integer categoryId, String keyword, Integer status, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Integer> categoryIds = expandCategoryIds(categoryId);
        List<Product> list = productMapper.selectPage(categoryIds, keyword, status, null, offset, pageSize);
        long total = productMapper.count(categoryIds, keyword, status);
        return new PageResult<>(total, pageNum, pageSize, list);
    }

    @Override
    public List<Product> getHot(int limit) {
        return productMapper.selectHot(limit);
    }

    @Override
    public void delete(Long id) {
        productMapper.deleteById(id);
        redisTemplate.delete(RedisKeyPrefix.PRODUCT + id);
        log.info("Product deleted: {}", id);
    }
}