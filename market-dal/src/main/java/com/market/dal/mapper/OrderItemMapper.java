package com.market.dal.mapper;

import com.market.dal.entity.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int insert(OrderItem item);
    int batchInsert(@Param("list") List<OrderItem> items);
    List<OrderItem> selectByOrderNo(@Param("orderNo") Long orderNo);
}
