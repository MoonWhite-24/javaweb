package com.market.service.impl;

import com.market.dal.dto.StatsDTO;
import com.market.dal.entity.Product;
import com.market.dal.mapper.OrderMapper;
import com.market.dal.mapper.ProductMapper;
import com.market.dal.mapper.UserMapper;
import com.market.service.SeckillService;
import com.market.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SeckillService seckillService;

    @Override
    public StatsDTO getDashboardStats() {
        StatsDTO stats = new StatsDTO();
        stats.setTodayOrderCount(orderMapper.countTodayOrders());
        stats.setTodayRevenue(orderMapper.sumTodayRevenue());
        stats.setTodayNewUsers(orderMapper.countTodayNewUsers());
        stats.setUnpaidOrders(orderMapper.countByStatus(0));
        stats.setTotalProducts(productMapper.countAll());
        stats.setTotalUsers(userMapper.countAll());

        // Top 5 products by sales
        List<Product> hotProducts = productMapper.selectHot(5);
        List<Map<String, Object>> topProducts = hotProducts.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("sales", p.getSales());
            map.put("price", p.getPrice());
            map.put("mainImage", p.getMainImage());
            return map;
        }).collect(Collectors.toList());
        stats.setTopProducts(topProducts);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getOrderTrend(int days) {
        return orderMapper.selectDailyStats(days);
    }

    @Override
    public List<Map<String, Object>> getRevenueTrend(int days) {
        return orderMapper.selectDailyRevenue(days);
    }

    @Override
    public List<Map<String, Object>> getOrderStatusDistribution() {
        List<Map<String, Object>> raw = orderMapper.selectStatusDistribution();
        // Map numeric status codes to readable labels
        Map<Integer, String> statusLabels = Map.of(
                0, "待付款",
                1, "已付款",
                2, "已发货",
                3, "已收货",
                4, "已完成",
                5, "已取消"
        );
        return raw.stream().map(row -> {
            Map<String, Object> mapped = new HashMap<>();
            int status = ((Number) row.get("status")).intValue();
            mapped.put("status", status);
            mapped.put("label", statusLabels.getOrDefault(status, "未知"));
            mapped.put("count", row.get("count"));
            return mapped;
        }).collect(Collectors.toList());
    }
}
