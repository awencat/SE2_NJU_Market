package com.marketback.main.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class OrderEntity implements Serializable {

    @TableId(value = "order_id", type = IdType.AUTO)
    private Integer orderId;

    private Integer buyerId;

    private Integer sellerId;

    private String orderNumber;

    private BigDecimal totalAmount;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
