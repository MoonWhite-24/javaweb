package com.market.api.controller;

import com.market.common.model.PageResult;
import com.market.common.model.R;
import com.market.common.model.UserDTO;
import com.market.dal.entity.CartItem;
import com.market.dal.entity.Order;
import com.market.service.CartService;
import com.market.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private CartService cartService;

    private Long uid(HttpServletRequest r) { return ((UserDTO) r.getAttribute("currentUser")).getId(); }

    @PostMapping
    public R<Map<String, Long>> create(@RequestBody Map<String, Object> body, HttpServletRequest r) {
        Long shippingAddrId = body.get("shippingAddressId") != null
                ? Long.valueOf(body.get("shippingAddressId").toString()) : 0L;
        List<CartItem> items = cartService.list(uid(r));
        Long orderNo = orderService.createOrder(uid(r), shippingAddrId, items);
        return R.ok(Map.of("orderNo", orderNo));
    }

    @GetMapping
    public R<PageResult<Order>> list(@RequestParam(required = false) Integer status,
                                      @RequestParam(defaultValue = "1") int pageNum,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      HttpServletRequest r) {
        return R.ok(orderService.userList(uid(r), status, pageNum, pageSize));
    }

    @GetMapping("/{orderNo}")
    public R<Order> detail(@PathVariable Long orderNo) {
        return R.ok(orderService.getByOrderNo(orderNo));
    }

    @PostMapping("/{orderNo}/pay")
    public R<Void> pay(@PathVariable Long orderNo, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.getOrDefault("amount", "0").toString());
        String tradeNo = (String) body.getOrDefault("tradeNo", "PAY" + orderNo);
        orderService.paySuccess(orderNo, amount, tradeNo);
        return R.ok();
    }
}
