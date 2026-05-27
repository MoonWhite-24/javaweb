package com.market.api.controller.admin;

import com.market.common.model.PageResult;
import com.market.common.model.R;
import com.market.dal.entity.Order;
import com.market.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin
public class AdminOrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping
    public R<PageResult<Order>> list(@RequestParam(required = false) Long orderNo,
        @RequestParam(required = false) Integer status,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(orderService.adminList(orderNo, status, startDate, endDate, pageNum, pageSize));
    }

    @GetMapping("/{orderNo}")
    public R<Order> detail(@PathVariable Long orderNo) {
        return R.ok(orderService.getByOrderNo(orderNo));
    }
}
