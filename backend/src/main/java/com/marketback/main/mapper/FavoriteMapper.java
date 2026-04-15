package com.marketback.main.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marketback.main.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
