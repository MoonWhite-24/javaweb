package com.market.dal.mapper;

import com.market.dal.entity.SeckillProduct;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SeckillProductMapper {
    int insert(SeckillProduct sp);
    int updateById(SeckillProduct sp);
    List<SeckillProduct> selectAll(@Param("status") Integer status,
                                     @Param("offset") int offset,
                                     @Param("pageSize") int pageSize);
    long count(@Param("status") Integer status);
    SeckillProduct selectById(@Param("id") Long id);
    List<SeckillProduct> selectActiveNow();
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int deleteById(@Param("id") Long id);
    List<SeckillProduct> selectByProductId(@Param("productId") Long productId);
}
