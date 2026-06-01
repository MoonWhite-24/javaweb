package com.market.dal.mapper;

import com.market.dal.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    int insert(User user);
    int updateById(User user);
    User selectById(@Param("id") Long id);
    User selectByUsername(@Param("username") String username);
    User selectByPhone(@Param("phone") String phone);
    List<User> selectByCondition(@Param("keyword") String keyword,
                                  @Param("status") Integer status,
                                  @Param("offset") int offset,
                                  @Param("pageSize") int pageSize);
    long countByCondition(@Param("keyword") String keyword, @Param("status") Integer status);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int updateLastLogin(@Param("id") Long id);
    int deleteById(@Param("id") Long id);
}
