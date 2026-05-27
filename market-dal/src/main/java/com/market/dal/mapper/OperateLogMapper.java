package com.market.dal.mapper;

import com.market.dal.entity.OperateLog;
import org.apache.ibatis.annotations.Param;

public interface OperateLogMapper {
    int insert(OperateLog log);
}
