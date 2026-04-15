package com.marketback.main.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report implements Serializable {

    @TableId(value = "report_id", type = IdType.AUTO)
    private Integer reportId;

    private Integer reporterId;

    private Integer targetUserId;

    private Integer targetGoodId;

    private String reportType;

    private String reason;

    private String status;

    @TableField("handler_admin_id")
    private Integer handlerAdminId;

    private String handleResult;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
