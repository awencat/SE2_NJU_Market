package com.marketback.main.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marketback.main.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
