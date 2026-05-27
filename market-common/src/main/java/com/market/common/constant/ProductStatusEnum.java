package com.market.common.constant;

public enum ProductStatusEnum {
    ON_SHELF(1, "On Shelf"),
    OFF_SHELF(2, "Off Shelf"),
    DELETED(3, "Deleted");

    private final int code;
    private final String desc;

    ProductStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static ProductStatusEnum of(int code) {
        for (ProductStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}
