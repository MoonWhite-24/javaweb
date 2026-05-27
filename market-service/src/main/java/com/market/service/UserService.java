package com.market.service;

import com.market.common.model.UserDTO;
import com.market.dal.entity.User;

import java.util.Map;

public interface UserService {
    UserDTO register(String username, String password, String phone, String captchaCode);
    Map<String, Object> login(String username, String password);
    void logout(String token);
    UserDTO getByToken(String token);
    UserDTO getById(Long id);
    void sendCaptcha(String phone);
}
