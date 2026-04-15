package com.marketback.main.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("favorite")
public class Favorite implements Serializable {

    @TableId(value = "favorite_id", type = IdType.AUTO)
    private Integer favoriteId;

    private Integer userId;

    private Integer goodId;

    private LocalDateTime createdAt;
}
