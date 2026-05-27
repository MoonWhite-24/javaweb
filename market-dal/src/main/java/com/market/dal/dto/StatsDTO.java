package com.market.dal.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class StatsDTO {
    private long todayOrderCount;
    private BigDecimal todayRevenue;
    private long todayNewUsers;
    private long unpaidOrders;
    private long totalProducts;
    private long totalUsers;
    private List<Map<String, Object>> topProducts;

    public long getTodayOrderCount() { return todayOrderCount; }
    public void setTodayOrderCount(long todayOrderCount) { this.todayOrderCount = todayOrderCount; }
    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }
    public long getTodayNewUsers() { return todayNewUsers; }
    public void setTodayNewUsers(long todayNewUsers) { this.todayNewUsers = todayNewUsers; }
    public long getUnpaidOrders() { return unpaidOrders; }
    public void setUnpaidOrders(long unpaidOrders) { this.unpaidOrders = unpaidOrders; }
    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public List<Map<String, Object>> getTopProducts() { return topProducts; }
    public void setTopProducts(List<Map<String, Object>> topProducts) { this.topProducts = topProducts; }
}
