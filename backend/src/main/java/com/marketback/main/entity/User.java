package com.marketback.main.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("`user`")
public class User implements Serializable {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String avatarUrl;

    private String campus;

    private String address;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
