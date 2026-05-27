package com.market.service;

import com.market.common.model.PageResult;
import com.market.dal.entity.CartItem;
import com.market.dal.entity.Order;
import com.market.dal.entity.OrderItem;
import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    Long createOrder(Long userId, Long shippingAddressId, List<CartItem> items);
    void paySuccess(Long orderNo, BigDecimal amount, String tradeNo);
    void cancel(Long orderNo, Long userId);
    Order getByOrderNo(Long orderNo);
    List<OrderItem> getItems(Long orderNo);
    PageResult<Order> userList(Long userId, Integer status, int pageNum, int pageSize);
    PageResult<Order> adminList(Long orderNo, Integer status, String startDate, String endDate, int pageNum, int pageSize);
    void processOrderCreate(Long orderNo);
}
