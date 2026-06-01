package com.market.common.constant;

public class RedisKeyPrefix {

    public static final String USER_TOKEN = "user:token:";
    public static final String USER_LOGIN_FAIL = "user:login:fail:";
    public static final String CAPTCHA_PHONE = "captcha:";
    public static final String CAPTCHA_RATE = "captcha:rate:";
    public static final String CART = "cart:";
    public static final String PRODUCT = "product:";
    public static final String PRODUCT_LIST = "product:list:";
    public static final String CATEGORY_TREE = "category:tree";
    public static final String SECKILL_STOCK = "seckill:stock:";
    public static final String SECKILL_USERS = "seckill:users:";
    public static final String SECKILL_ORDER = "seckill:order:";
    public static final String RATE_IP = "rate:ip:";
    public static final String PAY_DONE = "pay:done:";
    public static final String LOCK_ORDER_CREATE = "lock:order:create:";
    public static final String ORDER_CREATED = "order:created:";

    private RedisKeyPrefix() {}
}
