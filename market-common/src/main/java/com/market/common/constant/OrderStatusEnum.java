package com.market.common.constant;

public enum OrderStatusEnum {
    UNPAID(0, "Unpaid"),
    PAID(1, "Paid"),
    SHIPPED(2, "Shipped"),
    RECEIVED(3, "Received"),
    DONE(4, "Done"),
    CANCELLED(5, "Cancelled");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static OrderStatusEnum of(int code) {
        for (OrderStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}
