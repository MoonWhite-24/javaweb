package com.market.dal.mapper;

import com.market.dal.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderMapper {
    int insert(Order order);
    Order selectByOrderNo(@Param("orderNo") Long orderNo);
    List<Order> selectByUserId(@Param("userId") Long userId,
                                @Param("status") Integer status,
                                @Param("offset") int offset,
                                @Param("pageSize") int pageSize);
    long countByUserId(@Param("userId") Long userId, @Param("status") Integer status);
    int updateStatus(@Param("orderNo") Long orderNo, @Param("status") Integer status);
    int updatePayInfo(@Param("orderNo") Long orderNo,
                       @Param("payment") java.math.BigDecimal payment,
                       @Param("paymentTradeNo") String paymentTradeNo,
                       @Param("paymentTime") LocalDateTime paymentTime);
    List<Long> selectUnpaidBeforeDeadline(@Param("status") Integer status,
                                            @Param("deadline") LocalDateTime deadline,
                                            @Param("limit") int limit);
    List<Order> selectByCondition(@Param("orderNo") Long orderNo,
                                    @Param("status") Integer status,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    @Param("offset") int offset,
                                    @Param("pageSize") int pageSize);
    long countByCondition(@Param("orderNo") Long orderNo,
                          @Param("status") Integer status,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    long countTodayOrders();
    java.math.BigDecimal sumTodayRevenue();
    long countTodayNewUsers();
    long countByStatus(@Param("status") Integer status);
    int deleteByOrderNo(@Param("orderNo") Long orderNo);

    List<java.util.Map<String, Object>> selectDailyStats(@Param("days") int days);
    List<java.util.Map<String, Object>> selectDailyRevenue(@Param("days") int days);
    List<java.util.Map<String, Object>> selectStatusDistribution();
}
