package com.marketback.main.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("good")
public class Good implements Serializable {

    @TableId(value = "good_id", type = IdType.AUTO)
    private Integer goodId;

    private Integer sellerId;

    private String title;

    private String description;

    private BigDecimal price;

    private String category;

    private String status;

    private String condition;

    private Integer viewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
