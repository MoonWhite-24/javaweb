package com.market.task;

import com.market.common.constant.KafkaTopic;
import com.market.common.constant.OrderStatusEnum;
import com.market.common.util.JsonUtil;
import com.market.dal.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class OrderTimeoutCancelTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutCancelTask.class);
    private static final int BATCH_SIZE = 200;
    private static final int TIMEOUT_MINUTES = 30;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(cron = "0/30 * * * * ?")
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        List<Long> orderNos;
        int total = 0;

        do {
            orderNos = orderMapper.selectUnpaidBeforeDeadline(
                    OrderStatusEnum.UNPAID.getCode(), deadline, BATCH_SIZE);
            for (Long orderNo : orderNos) {
                try {
                    kafkaTemplate.send(KafkaTopic.ORDER_CANCEL, String.valueOf(orderNo),
                            JsonUtil.toJson(Map.of("orderNo", orderNo, "reason", "timeout")));
                    total++;
                } catch (Exception e) {
                    log.error("Failed to send cancel message for orderNo={}", orderNo, e);
                }
            }
        } while (orderNos.size() == BATCH_SIZE);

        if (total > 0) {
            log.info("Timeout cancel task completed: {} orders marked for cancellation", total);
        }
    }
}
