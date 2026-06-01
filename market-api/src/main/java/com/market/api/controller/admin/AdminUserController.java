package com.market.api.controller.admin;

import com.market.common.model.PageResult;
import com.market.common.model.R;
import com.market.dal.entity.User;
import com.market.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public R<PageResult<User>> list(@RequestParam(defaultValue = "1") int pageNum,
                                     @RequestParam(defaultValue = "20") int pageSize,
                                     @RequestParam(required = false) String keyword) {
        return R.ok(userService.listPage(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public R<User> detail(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user != null) {
            user.setPassword(null);
            user.setSalt(null);
        }
        return R.ok(user);
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        userService.updateStatus(id, body.get("status"));
        return R.ok();
    }
}
