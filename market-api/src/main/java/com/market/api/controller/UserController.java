package com.market.api.controller;

import com.market.common.model.R;
import com.market.common.model.UserDTO;
import com.market.dal.entity.User;
import com.market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public R<User> profile(HttpServletRequest request) {
        UserDTO currentUser = (UserDTO) request.getAttribute("currentUser");
        User user = userService.findById(currentUser.getId());
        user.setPassword(null);
        return R.ok(user);
    }

    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody User user, HttpServletRequest request) {
        UserDTO currentUser = (UserDTO) request.getAttribute("currentUser");
        user.setId(currentUser.getId());
        userService.updateProfile(user);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        UserDTO currentUser = (UserDTO) request.getAttribute("currentUser");
        userService.changePassword(currentUser.getId(),
                body.get("oldPassword"), body.get("newPassword"));
        return R.ok();
    }

    @DeleteMapping("/account")
    public R<Void> deleteAccount(HttpServletRequest request) {
        UserDTO currentUser = (UserDTO) request.getAttribute("currentUser");
        userService.deleteAccount(currentUser.getId());
        return R.ok();
    }
}
