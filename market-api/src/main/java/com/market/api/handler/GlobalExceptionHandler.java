package com.market.api.handler;

import com.market.common.exception.BusinessException;
import com.market.common.exception.ForbiddenException;
import com.market.common.exception.UnauthorizedException;
import com.market.common.model.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusiness(BusinessException e) {
        return R.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public R<Void> handleUnauthorized(UnauthorizedException e) {
        return R.error(401, "未登录");
    }

    @ExceptionHandler(ForbiddenException.class)
    public R<Void> handleForbidden(ForbiddenException e) {
        return R.error(403, "无权限");
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("Unexpected error", e);
        return R.error(500, "服务器内部错误");
    }
}
