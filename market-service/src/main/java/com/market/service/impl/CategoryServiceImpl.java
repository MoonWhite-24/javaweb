package com.market.service.impl;

import com.market.common.constant.RedisKeyPrefix;
import com.market.common.util.JsonUtil;
import com.market.dal.entity.Category;
import com.market.dal.mapper.CategoryMapper;
import com.market.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public List<Category> getTree() {
        String cached = redisTemplate.opsForValue().get(RedisKeyPrefix.CATEGORY_TREE);
        if (cached != null && !cached.isEmpty()) {
            return JsonUtil.fromJsonList(cached, Category.class);
        }
        List<Category> tree = buildTree();
        redisTemplate.opsForValue().set(RedisKeyPrefix.CATEGORY_TREE, JsonUtil.toJson(tree), 1, TimeUnit.HOURS);
        return tree;
    }

    @Override
    public List<Category> getAll() {
        return categoryMapper.selectAllByStatus(1);
    }

    @Override
    public Category getById(Integer id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public void add(Category category) {
        categoryMapper.insert(category);
        refreshCache();
    }

    @Override
    public void update(Category category) {
        categoryMapper.updateById(category);
        refreshCache();
    }

    @Override
    public void delete(Integer id) {
        categoryMapper.deleteById(id);
        refreshCache();
    }

    @Override
    public void refreshCache() {
        redisTemplate.delete(RedisKeyPrefix.CATEGORY_TREE);
        List<Category> tree = buildTree();
        redisTemplate.opsForValue().set(RedisKeyPrefix.CATEGORY_TREE, JsonUtil.toJson(tree), 1, TimeUnit.HOURS);
        log.debug("Category cache refreshed");
    }

    private List<Category> buildTree() {
        List<Category> all = categoryMapper.selectAllByStatus(1);
        return all.stream()
                .filter(c -> c.getParentId() == 0)
                .peek(c -> buildChildren(c, all))
                .collect(Collectors.toList());
    }

    private void buildChildren(Category parent, List<Category> all) {
        List<Category> children = all.stream()
                .filter(c -> c.getParentId().equals(parent.getId()))
                .peek(c -> buildChildren(c, all))
                .collect(Collectors.toList());
        if (!children.isEmpty()) parent.setChildren(children);
    }
}
