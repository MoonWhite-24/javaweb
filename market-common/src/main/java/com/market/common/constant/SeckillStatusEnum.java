package com.market.common.constant;

public enum SeckillStatusEnum {
    PENDING(0, "Pending"),
    ACTIVE(1, "Active"),
    FINISHED(2, "Finished");

    private final int code;
    private final String desc;

    SeckillStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }
}
