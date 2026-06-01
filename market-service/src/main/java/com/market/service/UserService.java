package com.market.service;

import com.market.common.model.PageResult;
import com.market.common.model.UserDTO;
import com.market.dal.entity.User;

import java.util.Map;

public interface UserService {
    UserDTO register(String username, String password, String phone);
    Map<String, Object> login(String username, String password);
    void logout(String token);
    UserDTO getByToken(String token);
    UserDTO getById(Long id);
    User findById(Long id);
    void updateProfile(User user);
    void changePassword(Long userId, String oldPassword, String newPassword);
    void deleteAccount(Long userId);
    void updateStatus(Long id, Integer status);
    PageResult<User> listPage(int pageNum, int pageSize, String keyword);
    void sendCaptcha(String phone);
}
