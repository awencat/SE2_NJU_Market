package com.marketback.main.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("good_image")
public class GoodImage implements Serializable {

    @TableId(value = "image_id", type = IdType.AUTO)
    private Integer imageId;

    private Integer goodId;

    private String imageUrl;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}
