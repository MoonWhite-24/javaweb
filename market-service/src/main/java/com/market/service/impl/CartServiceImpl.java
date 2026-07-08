package com.market.service.impl;

import com.market.common.constant.RedisKeyPrefix;
import com.market.common.exception.BusinessException;
import com.market.common.util.JsonUtil;
import com.market.dal.entity.CartItem;
import com.market.dal.entity.Product;
import com.market.dal.mapper.ProductMapper;
import com.market.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public void add(Long userId, Long productId, Integer quantity) {
        String key = RedisKeyPrefix.CART + userId;
        String fieldKey = productId.toString();

        Object existing = redisTemplate.opsForHash().get(key, fieldKey);
        if (existing != null) {
            CartItem item = JsonUtil.fromJson((String) existing, CartItem.class);
            item.setQuantity(item.getQuantity() + quantity);
            redisTemplate.opsForHash().put(key, fieldKey, JsonUtil.toJson(item));
        } else {
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStatus() != 1) {
                throw new BusinessException("商品不可用");
            }
            CartItem item = CartItem.from(product, quantity);
            redisTemplate.opsForHash().put(key, fieldKey, JsonUtil.toJson(item));
        }
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    @Override
    public void remove(Long userId, Long productId) {
        redisTemplate.opsForHash().delete(RedisKeyPrefix.CART + userId, productId.toString());
    }

    @Override
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        String key = RedisKeyPrefix.CART + userId;
        String fieldKey = productId.toString();
        Object existing = redisTemplate.opsForHash().get(key, fieldKey);
        if (existing == null) {
            throw new BusinessException("商品不在购物车中");
        }
        CartItem item = JsonUtil.fromJson((String) existing, CartItem.class);
        if (quantity <= 0) {
            redisTemplate.opsForHash().delete(key, fieldKey);
            return;
        }
        item.setQuantity(quantity);
        redisTemplate.opsForHash().put(key, fieldKey, JsonUtil.toJson(item));
    }

    @Override
    public void check(Long userId, Long productId, Boolean checked) {
        String key = RedisKeyPrefix.CART + userId;
        String fieldKey = productId.toString();
        Object existing = redisTemplate.opsForHash().get(key, fieldKey);
        if (existing == null) return;
        CartItem item = JsonUtil.fromJson((String) existing, CartItem.class);
        item.setChecked(checked);
        redisTemplate.opsForHash().put(key, fieldKey, JsonUtil.toJson(item));
    }

    @Override
    public void checkAll(Long userId, Boolean checked) {
        String key = RedisKeyPrefix.CART + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            CartItem item = JsonUtil.fromJson((String) entry.getValue(), CartItem.class);
            item.setChecked(checked);
            redisTemplate.opsForHash().put(key, entry.getKey(), JsonUtil.toJson(item));
        }
    }

    @Override
    public List<CartItem> list(Long userId) {
        String key = RedisKeyPrefix.CART + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        List<CartItem> items = new java.util.ArrayList<>();
        boolean cartModified = false;
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            CartItem item = JsonUtil.fromJson((String) entry.getValue(), CartItem.class);
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                boolean itemModified = false;
                // 同步库存
                if (!product.getStock().equals(item.getStock())) {
                    item.setStock(product.getStock());
                    itemModified = true;
                }
                // 同步商品图片（解决更新商品图片后订单图片不一致的问题）
                if (product.getMainImage() != null && !product.getMainImage().equals(item.getImage())) {
                    item.setImage(product.getMainImage());
                    itemModified = true;
                }
                // 同步商品名称
                if (product.getName() != null && !product.getName().equals(item.getName())) {
                    item.setName(product.getName());
                    itemModified = true;
                }
                // 同步商品价格
                if (product.getPrice() != null && !product.getPrice().equals(item.getPrice())) {
                    item.setPrice(product.getPrice());
                    itemModified = true;
                }
                if (itemModified) {
                    redisTemplate.opsForHash().put(key, entry.getKey(), JsonUtil.toJson(item));
                    cartModified = true;
                }
            }
            items.add(item);
        }
        if (cartModified) {
            redisTemplate.expire(key, 7, TimeUnit.DAYS);
        }
        items.sort(Comparator.comparing(CartItem::getCreateTime).reversed());
        return items;
    }

    @Override
    public int count(Long userId) {
        String key = RedisKeyPrefix.CART + userId;
        return redisTemplate.opsForHash().size(key).intValue();
    }

    @Override
    public void clearChecked(Long userId) {
        String key = RedisKeyPrefix.CART + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            CartItem item = JsonUtil.fromJson((String) entry.getValue(), CartItem.class);
            if (item.isChecked()) {
                redisTemplate.opsForHash().delete(key, entry.getKey());
            }
        }
    }
}
