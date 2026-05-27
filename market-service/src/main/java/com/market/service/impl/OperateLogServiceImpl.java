package com.market.service.impl;

import com.market.common.constant.KafkaTopic;
import com.market.common.util.JsonUtil;
import com.market.dal.entity.OperateLog;
import com.market.dal.mapper.OperateLogMapper;
import com.market.service.OperateLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OperateLogServiceImpl implements OperateLogService {

    private static final Logger log = LoggerFactory.getLogger(OperateLogServiceImpl.class);

    @Autowired
    private OperateLogMapper operateLogMapper;
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void save(OperateLog operateLog) {
        try {
            if (kafkaTemplate != null) {
                kafkaTemplate.send(KafkaTopic.LOG_COLLECT, JsonUtil.toJson(operateLog));
            } else {
                operateLog.setCreateTime(java.time.LocalDateTime.now());
                operateLogMapper.insert(operateLog);
            }
        } catch (Exception e) {
            log.error("Failed to save operation log, falling back to direct DB insert", e);
            try {
                operateLog.setCreateTime(java.time.LocalDateTime.now());
                operateLogMapper.insert(operateLog);
            } catch (Exception ex) {
                log.error("Failed to save operation log to DB", ex);
            }
        }
    }
}
