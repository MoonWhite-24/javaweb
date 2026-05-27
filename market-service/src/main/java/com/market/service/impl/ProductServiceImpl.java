package com.market.service.impl;

import com.market.common.constant.ProductStatusEnum;
import com.market.common.constant.RedisKeyPrefix;
import com.market.common.model.PageResult;
import com.market.common.util.JsonUtil;
import com.market.dal.entity.Category;
import com.market.dal.entity.Product;
import com.market.dal.mapper.CategoryMapper;
import com.market.dal.mapper.ProductMapper;
import com.market.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageResult<Product> listPage(Integer categoryId, String keyword, String orderBy, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String cacheKey = RedisKeyPrefix.PRODUCT_LIST + categoryId + ":" + pageNum + ":" + pageSize
                + ":" + (keyword != null ? keyword : "") + ":" + (orderBy != null ? orderBy : "");

        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return JsonUtil.fromJson(cached, PageResult.class);
        }

        List<Product> list = productMapper.selectPage(categoryId, keyword, ProductStatusEnum.ON_SHELF.getCode(),
                orderBy, offset, pageSize);
        long total = productMapper.count(categoryId, keyword, ProductStatusEnum.ON_SHELF.getCode());

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
            if (product.getCategoryId() != null) {
                Category category = categoryMapper.selectById(product.getCategoryId());
                if (category != null) {
                    product.setCategoryName(category.getName());
                }
            }
            redisTemplate.opsForValue().set(cacheKey, JsonUtil.toJson(product), 1, TimeUnit.HOURS);
        }
        return product;
    }

    @Override
    public void add(Product product) {
        product.setCreateTime(java.time.LocalDateTime.now());
        product.setUpdateTime(java.time.LocalDateTime.now());
        productMapper.insert(product);
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
        List<Product> list = productMapper.selectPage(categoryId, keyword, status, null, offset, pageSize);
        long total = productMapper.count(categoryId, keyword, status);
        return new PageResult<>(total, pageNum, pageSize, list);
    }

    @Override
    public List<Product> getHot(int limit) {
        return productMapper.selectHot(limit);
    }
}
