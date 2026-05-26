package com.marketback.main.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @TableField("`condition`")
    private String condition;


    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String sellerName;

    @TableField(exist = false)
    private String sellerPhone;

    @TableField(exist = false)
    private String sellerEmail;

    @TableField(exist = false)
    private List<GoodImage> images;

    @TableField(exist = false)
    private String imageUrl;

    @TableField(exist = false)
    private String image;

    @TableField(exist = false)
    private String coverUrl;
}


