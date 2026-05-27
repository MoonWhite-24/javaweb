package com.market.service.impl.aop;

import com.market.common.annotation.OperateLog;
import com.market.common.model.UserDTO;
import com.market.common.util.IpUtil;
import com.market.common.util.JsonUtil;
import com.market.service.OperateLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class OperateLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperateLogAspect.class);

    @Autowired
    private OperateLogService operateLogService;

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint pjp, OperateLog operateLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        int resultFlag = 1;
        String errorMsg = null;

        try {
            result = pjp.proceed();
        } catch (Throwable e) {
            resultFlag = 0;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            try {
                long cost = System.currentTimeMillis() - start;
                com.market.dal.entity.OperateLog logEntry = buildLog(operateLog, pjp, resultFlag, errorMsg, cost);
                operateLogService.save(logEntry);
            } catch (Exception e) {
                log.error("Failed to record operation log", e);
            }
        }
        return result;
    }

    private com.market.dal.entity.OperateLog buildLog(OperateLog anno, ProceedingJoinPoint pjp,
                                                        int result, String errorMsg, long cost) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = attrs != null ? attrs.getRequest() : null;
        UserDTO user = req != null ? (UserDTO) req.getAttribute("currentUser") : null;

        com.market.dal.entity.OperateLog logEntry = new com.market.dal.entity.OperateLog();
        logEntry.setUserId(user != null ? user.getId() : null);
        logEntry.setUsername(user != null ? user.getUsername() : null);
        logEntry.setModule(anno.module());
        logEntry.setOperation(anno.operation());
        logEntry.setParams(JsonUtil.toJson(pjp.getArgs()));
        logEntry.setResult(result);
        logEntry.setErrorMsg(errorMsg);
        logEntry.setIp(req != null ? IpUtil.getClientIp(req) : null);
        logEntry.setCostTime((int) cost);
        logEntry.setTraceId(MDC.get("traceId"));
        return logEntry;
    }
}
