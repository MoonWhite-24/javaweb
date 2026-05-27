package com.market.api.controller.admin;

import com.market.common.model.R;
import com.market.dal.entity.User;
import com.market.dal.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin
public class AdminUserController {
    @Autowired
    private UserMapper userMapper;

    @GetMapping
    public R<List<User>> list() {
        return R.ok(userMapper.selectByCondition(null, null, 0, 100));
    }
}
