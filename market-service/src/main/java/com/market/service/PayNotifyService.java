package com.market.service;

import java.math.BigDecimal;

public interface PayNotifyService {
    String handleCallback(Long orderNo, BigDecimal amount, String tradeNo);
}
