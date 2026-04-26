package com.marketback.main.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("rating")
public class Rating implements Serializable {

    @TableId(value = "rating_id", type = IdType.AUTO)
    private Integer ratingId;

    private Integer userId;

    private Integer goodId;

    private Integer score;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
