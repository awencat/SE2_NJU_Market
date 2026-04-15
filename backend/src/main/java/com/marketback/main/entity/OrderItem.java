package com.marketback.main.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("order_item")
public class OrderItem implements Serializable {

    @TableId(value = "item_id", type = IdType.AUTO)
    private Integer itemId;

    private Integer orderId;

    private Integer goodId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;
}
