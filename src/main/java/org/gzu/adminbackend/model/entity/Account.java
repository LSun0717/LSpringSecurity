package org.gzu.adminbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * @description TODO
 * @classname Account
 * @date 1/17/2024 1:59 AM
 * @created by LIONS7
 */
@Data
@AllArgsConstructor
@TableName("tbl_account")
public class Account {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private String password;

    private String email;

    private String role;

    private Date createdTime;
}
