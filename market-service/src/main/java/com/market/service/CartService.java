package com.market.service;

import com.market.dal.entity.CartItem;
import java.util.List;

public interface CartService {
    void add(Long userId, Long productId, Integer quantity);
    void remove(Long userId, Long productId);
    void updateQuantity(Long userId, Long productId, Integer quantity);
    void check(Long userId, Long productId, Boolean checked);
    void checkAll(Long userId, Boolean checked);
    List<CartItem> list(Long userId);
    int count(Long userId);
    void clearChecked(Long userId);
}
