package com.market.common.exception;

import com.market.common.constant.ResponseCode;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }

    public int getCode() { return ResponseCode.FORBIDDEN; }
}
