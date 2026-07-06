package com.market.service;

import com.market.dal.dto.StatsDTO;
import java.util.List;
import java.util.Map;

public interface StatsService {
    StatsDTO getDashboardStats();
    List<Map<String, Object>> getOrderTrend(int days);
    List<Map<String, Object>> getRevenueTrend(int days);
    List<Map<String, Object>> getOrderStatusDistribution();
}
