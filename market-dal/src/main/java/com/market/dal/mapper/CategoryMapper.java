package com.market.dal.mapper;

import com.market.dal.entity.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CategoryMapper {
    Category selectById(@Param("id") Integer id);
    List<Category> selectAllByStatus(@Param("status") Integer status);
    List<Category> selectByParentId(@Param("parentId") Integer parentId);
    int insert(Category category);
    int updateById(Category category);
    int deleteById(@Param("id") Integer id);
}
