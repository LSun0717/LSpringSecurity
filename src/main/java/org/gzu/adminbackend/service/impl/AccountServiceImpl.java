package org.gzu.adminbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.gzu.adminbackend.mapper.AccountMapper;
import org.gzu.adminbackend.model.entity.Account;
import org.gzu.adminbackend.service.AccountService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @description TODO
 * @classname AccountServiceImpl
 * @date 1/17/2024 2:10 AM
 * @created by LIONS7
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    /**
     * @Description: SpringSecurity 自定义查询用户信息
     * @param text 用户名或密码
     * @Return: UserDetails
     * @Author: lions
     * @Datetime: 1/17/2024 2:13 AM
     */

    @Override
    public UserDetails loadUserByUsername(String text) throws UsernameNotFoundException {
        Account account = this.getAccountByNameOrEmail(text);
        if (account == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    public Account getAccountByNameOrEmail(String text) {
        Account account = this.query()
                .eq("username", text)
                .or()
                .eq("email", text)
                .one();
        return account;
    }
}
