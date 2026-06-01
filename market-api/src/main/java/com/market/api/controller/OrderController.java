package com.market.api.controller;

import com.market.common.model.PageResult;
import com.market.common.model.R;
import com.market.common.model.UserDTO;
import com.market.dal.entity.Order;
import com.market.service.CartService;
import com.market.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private CartService cartService;

    private Long getUserId(HttpServletRequest request) {
        return ((UserDTO) request.getAttribute("currentUser")).getId();
    }

    @PostMapping
    public R<Map<String, Object>> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = getUserId(request);
        Long shippingAddressId = body.get("shippingAddressId") != null
                ? Long.valueOf(body.get("shippingAddressId").toString()) : 0L;
        Long orderNo = orderService.createOrder(userId, shippingAddressId, cartService.list(userId));
        cartService.clearChecked(userId);
        return R.ok(Map.of("orderNo", orderNo));
    }

    @GetMapping
    public R<PageResult<Order>> list(@RequestParam(required = false) Integer status,
                                      @RequestParam(defaultValue = "1") int pageNum,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      HttpServletRequest request) {
        PageResult<Order> page = orderService.userList(getUserId(request), status, pageNum, pageSize);
        for (Order order : page.getList()) {
            order.setItems(orderService.getItems(order.getOrderNo()));
        }
        return R.ok(page);
    }

    @GetMapping("/{orderNo}")
    public R<Order> detail(@PathVariable Long orderNo, HttpServletRequest request) {
        Order order = orderService.getByOrderNo(orderNo);
        if (order != null) {
            order.setItems(orderService.getItems(orderNo));
        }
        return R.ok(order);
    }

    @PostMapping("/{orderNo}/pay")
    public R<Void> pay(@PathVariable Long orderNo, @RequestBody Map<String, Object> body) {
        BigDecimal amount = body.get("amount") != null ? new BigDecimal(body.get("amount").toString()) : BigDecimal.ZERO;
        String tradeNo = body.get("tradeNo") != null ? body.get("tradeNo").toString() : "";
        orderService.paySuccess(orderNo, amount, tradeNo);
        return R.ok();
    }

    @PostMapping("/{orderNo}/cancel")
    public R<Void> cancel(@PathVariable Long orderNo, HttpServletRequest request) {
        orderService.cancel(orderNo, getUserId(request));
        return R.ok();
    }

    @DeleteMapping("/{orderNo}")
    public R<Void> delete(@PathVariable Long orderNo, HttpServletRequest request) {
        orderService.deleteOrder(orderNo, getUserId(request));
        return R.ok();
    }
}
