package com.market.api.controller;

import com.market.common.model.R;
import com.market.common.model.UserDTO;
import com.market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public R<UserDTO> profile(HttpServletRequest request) {
        UserDTO u = (UserDTO) request.getAttribute("currentUser");
        return R.ok(userService.getById(u.getId()));
    }
}
