package org.gzu.adminbackend.config;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.gzu.adminbackend.filter.AuthorizeFilter;
import org.gzu.adminbackend.model.entity.Account;
import org.gzu.adminbackend.model.vo.RestBean;
import org.gzu.adminbackend.model.vo.response.AuthorizeVO;
import org.gzu.adminbackend.service.AccountService;
import org.gzu.adminbackend.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Classname: SpringSecurityConfig
 * @Description: TODO
 * @Author: lions
 * @Datetime: 1/16/2024 10:13 PM
 */
@Configuration
public class SpringSecurityConfig {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private AuthorizeFilter authorizeFilter;

    @Resource
    private AccountService accountService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(conf -> conf
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(conf -> conf
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(this::onAuthenticationSuccess)
                        .failureHandler(this::onAuthenticationFailure)
                )
                .addFilterBefore(authorizeFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(conf -> conf
                        .authenticationEntryPoint(this::onUnAuthorized)
                        .accessDeniedHandler(this::onAccessDeny)
                )
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(conf -> conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    /**
     * @Description: 登录请求失败处理
     * @Author: lions
     * @Datetime: 1/17/2024 12:29 AM
     */
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        User user = (User) authentication.getPrincipal();
        String jwt = jwtUtil.createJwt(user, 1, "lions");

        Account account = accountService.getAccountByNameOrEmail(user.getUsername());

        AuthorizeVO authorizeVO = new AuthorizeVO();
        authorizeVO.setUsername(account.getUsername());
        authorizeVO.setRole(account.getRole());
        authorizeVO.setToken(jwt);
        authorizeVO.setExpire(jwtUtil.expireTime());

        var loginResp = RestBean.success(authorizeVO, "登录成功").toJsonStr();
        response.getWriter().write(loginResp);
    }

    /**
     * @Description: 登录请求失败处理
     * @Author: lions
     * @Datetime: 1/17/2024 12:29 AM
     */
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        var failureResp = RestBean.failure(401, exception.getMessage());
        response.getWriter().write(failureResp.toJsonStr());
    }

    /**
     * @Description: 退出登录请求成功处理
     * @Author: lions
     * @Datetime: 1/17/2024 12:28 AM
     */
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        String headerJwt = request.getHeader("Authorization");
        PrintWriter responseWriter = response.getWriter();
        if (jwtUtil.expireJwt(headerJwt)) {
            RestBean<Object> successResp = RestBean.success(null, "退出登录成功");
            responseWriter.write(successResp.toJsonStr());
        } else {
            responseWriter.write(RestBean.failure(400, "退出登录失败").toJsonStr());
        }
    }

    /**
     * @Description: 未登录请求处理
     * @Author: lions
     * @Datetime: 1/17/2024 12:28 AM
     */
    public void onUnAuthorized(HttpServletRequest request,
                               HttpServletResponse response,
                               AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        var unAuthorizedResp = RestBean.unAuthorized(authException.getMessage());
        response.getWriter().write(unAuthorizedResp.toJsonStr());
    }

    /**
     * @Description: 无权限请求处理
     * @Author: lions
     * @Datetime: 1/17/2024 12:31 AM
     */
    public void onAccessDeny(HttpServletRequest request,
                             HttpServletResponse response,
                             AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        var accessDeny = RestBean.forbidden(accessDeniedException.getMessage());
        response.getWriter().write(accessDeny.toJsonStr());
    }
}
