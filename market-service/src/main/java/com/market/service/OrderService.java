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
    void processSeckillOrder(Long orderNo, Long userId, Long seckillProductId);
    void deleteOrder(Long orderNo, Long userId);
    void adminDelete(Long orderNo);
    void updateStatus(Long orderNo, Integer status);
    /**
     * 刷新订单商品图片，从当前商品表同步最新图片
     * 用于解决商品图片更新后订单图片不一致的问题
     */
    int refreshOrderItemImages(Long orderNo);

    /**
     * 批量刷新所有订单的商品图片
     */
    int refreshAllOrderItemImages();
}
