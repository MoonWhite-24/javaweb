package com.market.common.exception;

import com.market.common.constant.ResponseCode;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public int getCode() { return ResponseCode.UNAUTHORIZED; }
}
