package com.market.service.impl;

import com.market.common.constant.KafkaTopic;
import com.market.common.constant.OrderStatusEnum;
import com.market.common.constant.RedisKeyPrefix;
import com.market.common.exception.BusinessException;
import com.market.common.model.PageResult;
import com.market.common.util.JsonUtil;
import com.market.dal.entity.CartItem;
import com.market.dal.entity.Order;
import com.market.dal.entity.OrderItem;
import com.market.dal.entity.Product;
import com.market.dal.entity.SeckillProduct;
import com.market.dal.mapper.OrderItemMapper;
import com.market.dal.mapper.OrderMapper;
import com.market.dal.mapper.ProductMapper;
import com.market.dal.mapper.SeckillProductMapper;
import com.market.service.OrderService;
import com.market.service.impl.redis.RedisDistributedLock;
import com.market.service.impl.snowflake.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private SnowflakeIdGenerator idGenerator;
    @Autowired
    private RedisDistributedLock lock;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private SeckillProductMapper seckillProductMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public Long createOrder(Long userId, Long shippingAddressId, List<CartItem> items) {
        String lockKey = RedisKeyPrefix.LOCK_ORDER_CREATE + userId;
        String lockValue = UUID.randomUUID().toString();

        if (!lock.tryLock(lockKey, lockValue, 10, TimeUnit.SECONDS)) {
            throw new BusinessException("上一笔订单正在处理中，请稍候...");
        }

        try {
            List<CartItem> checkedItems = items.stream()
                    .filter(CartItem::isChecked)
                    .collect(Collectors.toList());

            if (checkedItems.isEmpty()) {
                throw new BusinessException("请选择要下单的商品");
            }

            long orderNo = idGenerator.nextId();
            BigDecimal totalPrice = checkedItems.stream()
                    .map(CartItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setTotalPrice(totalPrice);
            order.setStatus(OrderStatusEnum.UNPAID.getCode());
            order.setPaymentType(1);
            orderMapper.insert(order);

            List<OrderItem> orderItems = checkedItems.stream().map(item -> {
                OrderItem oi = new OrderItem();
                oi.setOrderNo(orderNo);
                oi.setUserId(userId);
                oi.setProductId(item.getProductId());
                oi.setProductName(item.getName());
                oi.setProductImage(item.getImage());
                oi.setUnitPrice(item.getPrice());
                oi.setQuantity(item.getQuantity());
                oi.setTotalPrice(item.getSubtotal());
                return oi;
            }).collect(Collectors.toList());
            orderItemMapper.batchInsert(orderItems);

            for (CartItem item : checkedItems) {
                redisTemplate.opsForHash().delete(RedisKeyPrefix.CART + userId, String.valueOf(item.getProductId()));
            }

            Map<String, Object> msg = Map.of("orderNo", orderNo, "userId", userId, "timestamp", System.currentTimeMillis());
            kafkaTemplate.send(KafkaTopic.ORDER_CREATE, String.valueOf(orderNo), JsonUtil.toJson(msg));

            log.info("Order created: orderNo={}, userId={}, amount={}", orderNo, userId, totalPrice);
            return orderNo;
        } finally {
            lock.unlock(lockKey, lockValue);
        }
    }

    @Override
    @Transactional
    public void paySuccess(Long orderNo, BigDecimal amount, String tradeNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != OrderStatusEnum.UNPAID.getCode()) {
            log.warn("Order {} already processed, status={}", orderNo, order.getStatus());
            return;
        }
        orderMapper.updatePayInfo(orderNo, amount, tradeNo, LocalDateTime.now());
        orderMapper.updateStatus(orderNo, OrderStatusEnum.PAID.getCode());

        kafkaTemplate.send(KafkaTopic.ORDER_PAY, String.valueOf(orderNo),
                JsonUtil.toJson(Map.of("orderNo", orderNo, "amount", amount, "tradeNo", tradeNo)));

        log.info("Order paid: orderNo={}, amount={}", orderNo, amount);
    }

    @Override
    @Transactional
    public void cancel(Long orderNo, Long userId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
                        throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != OrderStatusEnum.UNPAID.getCode()) {
            throw new BusinessException("当前状态的订单无法取消");
        }
        orderMapper.updateStatus(orderNo, OrderStatusEnum.CANCELLED.getCode());

        // Restore seckill Redis state for each order item
        List<OrderItem> items = orderItemMapper.selectByOrderNo(orderNo);
        for (OrderItem item : items) {
            List<SeckillProduct> spList = seckillProductMapper.selectByProductId(item.getProductId());
            for (SeckillProduct sp : spList) {
                redisTemplate.opsForSet().remove(RedisKeyPrefix.SECKILL_USERS + sp.getId(), String.valueOf(userId));
                redisTemplate.opsForValue().increment(RedisKeyPrefix.SECKILL_STOCK + sp.getId());
                log.info("Seckill stock restored: spId={}, userId={}", sp.getId(), userId);
            }
        }

        kafkaTemplate.send(KafkaTopic.ORDER_CANCEL, String.valueOf(orderNo),
                JsonUtil.toJson(Map.of("orderNo", orderNo, "userId", userId)));
        log.info("Order cancelled: orderNo={}", orderNo);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderNo, Long userId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != OrderStatusEnum.PAID.getCode()
                && order.getStatus() != OrderStatusEnum.CANCELLED.getCode()
                && order.getStatus() != OrderStatusEnum.DONE.getCode()) {
            throw new BusinessException("只能删除已支付、已取消或已完成的订单");
        }
        orderItemMapper.deleteByOrderNo(orderNo);
        orderMapper.deleteByOrderNo(orderNo);
        log.info("Order deleted: orderNo={}", orderNo);
    }

    @Override
    public Order getByOrderNo(Long orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public List<OrderItem> getItems(Long orderNo) {
        return orderItemMapper.selectByOrderNo(orderNo);
    }

    @Override
    public PageResult<Order> userList(Long userId, Integer status, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Order> list = orderMapper.selectByUserId(userId, status, offset, pageSize);
        long total = orderMapper.countByUserId(userId, status);
        return new PageResult<>(total, pageNum, pageSize, list);
    }

    @Override
    public PageResult<Order> adminList(Long orderNo, Integer status, String startDate, String endDate, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = startDate != null ? LocalDateTime.parse(startDate + " 00:00:00", fmt) : null;
        LocalDateTime endTime = endDate != null ? LocalDateTime.parse(endDate + " 23:59:59", fmt) : null;
        List<Order> list = orderMapper.selectByCondition(orderNo, status, startTime, endTime, offset, pageSize);
        long total = orderMapper.countByCondition(orderNo, status, startTime, endTime);
        return new PageResult<>(total, pageNum, pageSize, list);
    }

    @KafkaListener(topics = KafkaTopic.ORDER_CREATE, groupId = "market-order-processor")
    public void onOrderCreate(String message) {
        log.info("Received order-create message: {}", message);
        try {
            Map<String, Object> msg = JsonUtil.fromJson(message, Map.class);
            Long orderNo = Long.valueOf(msg.get("orderNo").toString());
            Long userId = Long.valueOf(msg.get("userId").toString());
            Long seckillProductId = Long.valueOf(msg.get("seckillProductId").toString());
            processSeckillOrder(orderNo, userId, seckillProductId);
        } catch (Exception e) {
            log.error("Failed to process order-create message", e);
        }
    }

    @Override
    @Transactional
    public void processOrderCreate(Long orderNo) {
        log.info("Processing async order create: orderNo={}", orderNo);
    }

    @Override
    @Transactional
    public void processSeckillOrder(Long orderNo, Long userId, Long seckillProductId) {
        String idempotentKey = RedisKeyPrefix.ORDER_CREATED + orderNo;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(idempotentKey, String.valueOf(orderNo), 1, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(acquired)) {
            log.info("Duplicate seckill order message ignored: orderNo={}", orderNo);
            return;
        }

        try {
            SeckillProduct sp = seckillProductMapper.selectById(seckillProductId);
            if (sp == null) {
                log.error("Seckill product not found: id={}", seckillProductId);
                return;
            }
            Product product = productMapper.selectById(sp.getProductId());
            if (product == null) {
                log.error("Product not found: id={}", sp.getProductId());
                return;
            }

            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setTotalPrice(sp.getSeckillPrice());
            order.setPayment(sp.getSeckillPrice());
            order.setStatus(OrderStatusEnum.UNPAID.getCode());
            order.setPaymentType(1);
            orderMapper.insert(order);

            OrderItem oi = new OrderItem();
            oi.setOrderNo(orderNo);
            oi.setUserId(userId);
            oi.setProductId(product.getId());
            oi.setProductName(product.getName());
            oi.setProductImage(product.getMainImage());
            oi.setUnitPrice(sp.getSeckillPrice());
            oi.setQuantity(1);
            oi.setTotalPrice(sp.getSeckillPrice());
            orderItemMapper.insert(oi);

            log.info("Seckill order created: orderNo={}, userId={}, product={}, price={}",
                    orderNo, userId, product.getName(), sp.getSeckillPrice());
        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("Failed to create seckill order: orderNo={}", orderNo, e);
        }
    }
}
