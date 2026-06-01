package com.market.dal.mapper;

import com.market.dal.entity.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int insert(Product product);
    int updateById(Product product);
    Product selectById(@Param("id") Long id);
    List<Product> selectPage(@Param("categoryIds") List<Integer> categoryIds,
                              @Param("keyword") String keyword,
                              @Param("status") Integer status,
                              @Param("orderBy") String orderBy,
                              @Param("offset") int offset,
                              @Param("pageSize") int pageSize);
    long count(@Param("categoryIds") List<Integer> categoryIds,
               @Param("keyword") String keyword,
               @Param("status") Integer status);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int updateStock(@Param("id") Long id, @Param("delta") int delta);
    int updateSales(@Param("id") Long id, @Param("delta") int delta);
    List<Product> selectHot(@Param("limit") int limit);
    List<Product> selectByIds(@Param("ids") List<Long> ids);
    int deleteById(@Param("id") Long id);
}
