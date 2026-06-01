package com.market.dal.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private Long id;
    private Long orderNo;
    private Long userId;
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private BigDecimal totalPrice;
    private BigDecimal payment;
    private Integer paymentType;
    private String paymentTradeNo;
    private Integer status;
    private LocalDateTime paymentTime;
    private LocalDateTime sendTime;
    private LocalDateTime endTime;
    private LocalDateTime closeTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderNo() { return orderNo; }
    public void setOrderNo(Long orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getShippingName() { return shippingName; }
    public void setShippingName(String shippingName) { this.shippingName = shippingName; }
    public String getShippingPhone() { return shippingPhone; }
    public void setShippingPhone(String shippingPhone) { this.shippingPhone = shippingPhone; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public BigDecimal getPayment() { return payment; }
    public void setPayment(BigDecimal payment) { this.payment = payment; }
    public Integer getPaymentType() { return paymentType; }
    public void setPaymentType(Integer paymentType) { this.paymentType = paymentType; }
    public String getPaymentTradeNo() { return paymentTradeNo; }
    public void setPaymentTradeNo(String paymentTradeNo) { this.paymentTradeNo = paymentTradeNo; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    public LocalDateTime getSendTime() { return sendTime; }
    public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalDateTime closeTime) { this.closeTime = closeTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    private String productNames;
    public String getProductNames() { return productNames; }
    public void setProductNames(String productNames) { this.productNames = productNames; }

    private List<OrderItem> items;
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
