package org.gzu.adminbackend.model.vo.response;

import lombok.Data;

import java.util.Date;

/**
 * @description TODO
 * @classname AuthorizeVO
 * @date 1/16/2024 11:10 PM
 * @created by LIONS7
 */
@Data
public class AuthorizeVO {

    private String username;

    private String role;

    private String token;

    private Date expire;

}
