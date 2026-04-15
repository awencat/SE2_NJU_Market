package com.marketback.main.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marketback.main.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderEntityMapper extends BaseMapper<OrderEntity> {
}
