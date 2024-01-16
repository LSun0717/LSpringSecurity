package org.gzu.adminbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.gzu.adminbackend.model.entity.Account;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @description TODO
 * @classname AccountService
 * @date 1/17/2024 2:09 AM
 * @created by LIONS7
 */
public interface AccountService extends IService<Account>, UserDetailsService {

    Account getAccountByNameOrEmail(String text);
}
