package com.market.api.controller;

import com.market.common.model.R;
import com.market.common.model.UserDTO;
import com.market.dal.entity.CartItem;
import com.market.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
public class CartController {
    @Autowired
    private CartService cartService;

    private Long uid(HttpServletRequest r) { return ((UserDTO) r.getAttribute("currentUser")).getId(); }

    @GetMapping
    public R<List<CartItem>> list(HttpServletRequest r) { return R.ok(cartService.list(uid(r))); }

    @PostMapping("/items")
    public R<Void> add(@RequestBody Map<String, Object> body, HttpServletRequest r) {
        cartService.add(uid(r),
                Long.valueOf(body.get("productId").toString()),
                Integer.valueOf(body.getOrDefault("quantity", 1).toString()));
        return R.ok();
    }

    @PutMapping("/items/{productId}")
    public R<Void> update(@PathVariable Long productId, @RequestBody Map<String, Integer> body, HttpServletRequest r) {
        cartService.updateQuantity(uid(r), productId, body.get("quantity"));
        return R.ok();
    }

    @DeleteMapping("/items/{productId}")
    public R<Void> delete(@PathVariable Long productId, HttpServletRequest r) {
        cartService.remove(uid(r), productId);
        return R.ok();
    }
}
