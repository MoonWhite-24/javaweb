package com.market.service.impl;

import com.market.common.constant.RedisKeyPrefix;
import com.market.common.exception.BusinessException;
import com.market.common.model.UserDTO;
import com.market.common.util.CookieUtil;
import com.market.common.util.JsonUtil;
import com.market.common.util.PasswordUtil;
import com.market.dal.entity.User;
import com.market.dal.mapper.UserMapper;
import com.market.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final int MAX_LOGIN_FAIL = 5;
    private static final int LOGIN_LOCK_MINUTES = 15;
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public UserDTO register(String username, String password, String phone, String captchaCode) {
        String captchaKey = RedisKeyPrefix.CAPTCHA_PHONE + phone;
        String storedCode = redisTemplate.opsForValue().get(captchaKey);
        if (storedCode == null || !storedCode.equals(captchaCode)) {
            throw new BusinessException("验证码无效或已过期");
        }

        User existUser = userMapper.selectByUsername(username);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        if (phone != null && !phone.isEmpty()) {
            User existPhone = userMapper.selectByPhone(phone);
            if (existPhone != null) {
                throw new BusinessException("手机号已注册");
            }
        }

        String salt = PasswordUtil.generateSalt(8);
        String hashedPassword = PasswordUtil.hashPassword(password, salt);

        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setSalt(salt);
        user.setPhone(phone);
        user.setRole(0);
        user.setStatus(1);
        user.setCreateTime(java.time.LocalDateTime.now());
        user.setUpdateTime(java.time.LocalDateTime.now());
        userMapper.insert(user);

        redisTemplate.delete(captchaKey);
        log.info("User registered: {}", username);

        return new UserDTO(user.getId(), user.getUsername(), user.getPhone(), user.getRole(), user.getAvatar());
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        String failKey = RedisKeyPrefix.USER_LOGIN_FAIL + username;
        String failCount = redisTemplate.opsForValue().get(failKey);
        if (failCount != null && Integer.parseInt(failCount) >= MAX_LOGIN_FAIL) {
            throw new BusinessException("账户因多次登录失败已被锁定，请在 " + LOGIN_LOCK_MINUTES + " 分钟后重试");
        }

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            incrementFailCount(failKey);
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用");
        }

        if (!PasswordUtil.verify(password, user.getSalt(), user.getPassword())) {
            incrementFailCount(failKey);
            throw new BusinessException("用户名或密码错误");
        }

        redisTemplate.delete(failKey);

        String token = UUID.randomUUID().toString().replace("-", "");
        UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(),
                user.getPhone(), user.getRole(), user.getAvatar());
        redisTemplate.opsForValue().set(RedisKeyPrefix.USER_TOKEN + token,
                JsonUtil.toJson(userDTO), SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        userMapper.updateLastLogin(user.getId());
        log.info("User logged in: {}", username);

        return Map.of("user", userDTO, "token", token);
    }

    @Override
    public void logout(String token) {
        if (token != null && !token.isEmpty()) {
            redisTemplate.delete(RedisKeyPrefix.USER_TOKEN + token);
        }
    }

    @Override
    public UserDTO getByToken(String token) {
        if (token == null || token.isEmpty()) return null;
        String json = redisTemplate.opsForValue().get(RedisKeyPrefix.USER_TOKEN + token);
        if (json != null) {
            redisTemplate.expire(RedisKeyPrefix.USER_TOKEN + token, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            return JsonUtil.fromJson(json, UserDTO.class);
        }
        return null;
    }

    @Override
    public UserDTO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return null;
        return new UserDTO(user.getId(), user.getUsername(), user.getPhone(), user.getRole(), user.getAvatar());
    }

    @Override
    public void sendCaptcha(String phone) {
        String rateKey = RedisKeyPrefix.CAPTCHA_RATE + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateKey))) {
            throw new BusinessException("请等待60秒后再获取验证码");
        }
        String code = "888888";  // fixed demo captcha
        redisTemplate.opsForValue().set(RedisKeyPrefix.CAPTCHA_PHONE + phone, code, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(rateKey, "1", 60, TimeUnit.SECONDS);
        log.info("Captcha sent to {}: {}", phone, code);
    }

    private void incrementFailCount(String failKey) {
        redisTemplate.opsForValue().increment(failKey);
        redisTemplate.expire(failKey, LOGIN_LOCK_MINUTES, TimeUnit.MINUTES);
    }
}
