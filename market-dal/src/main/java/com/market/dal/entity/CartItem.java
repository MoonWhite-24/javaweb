package com.market.dal.entity;

import java.math.BigDecimal;

public class CartItem {
    private Long productId;
    private String name;
    private String image;
    private BigDecimal price;
    private Integer quantity;
    private Boolean checked;
    private Long createTime;

    public CartItem() {}

    public static CartItem from(Product product, Integer quantity) {
        CartItem item = new CartItem();
        item.productId = product.getId();
        item.name = product.getName();
        item.image = product.getMainImage();
        item.price = product.getPrice();
        item.quantity = quantity;
        item.checked = true;
        item.createTime = System.currentTimeMillis();
        return item;
    }

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Boolean isChecked() { return checked; }
    public Boolean getChecked() { return checked; }
    public void setChecked(Boolean checked) { this.checked = checked; }
    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }
}
