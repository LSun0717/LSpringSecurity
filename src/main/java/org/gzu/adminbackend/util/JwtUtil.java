package org.gzu.adminbackend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.gzu.adminbackend.costant.RedisConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @description JWT工具类
 * @classname JwtUtil
 * @date 1/16/2024 10:40 PM
 * @created by LIONS7
 */
@Component
public class JwtUtil {

    @Value("${spring.security.jwt.secretkey}")
    String secretKey;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    /**
     * @Description: 签发JWT
     * @param userDetails 用户详细信息
     * @param id 用户id
     * @param username 用户名
     * @Return: JWT
     * @Author: lions
     * @Datetime: 1/16/2024 10:57 PM
     */
    public String createJwt(UserDetails userDetails, int id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        Date expireTime = expireTime();

        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities", authorities)
                .withExpiresAt(expireTime)
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

    /**
     * @Description: 解析JWT
     * @param headerToken 请求头中token
     * @Return: 解析后的token
     * @Author: lions
     * @Datetime: 1/16/2024 11:25 PM
     */
    public DecodedJWT resolve(String headerToken) {
        String token = this.convertToken(headerToken);
        if (token == null) {
            return null;
        }
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
            String jwtId = decodedJWT.getId();
            if (!this.isValidJwt(jwtId)) {
                return null;
            }
            Date expiresAt = decodedJWT.getExpiresAt();
            return new Date().before(expiresAt) ? decodedJWT : null;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * @Description: 将JWT加入Redis黑名单
     * @param headerJwt 请求头中的jwt
     * @Return: 是否加入成功
     * @Author: lions
     * @Datetime: 1/17/2024 1:10 AM
     */
    public boolean expireJwt(String headerJwt) {
        String convertedToken = this.convertToken(headerJwt);
        if (convertedToken == null) {
            return false;
        }
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT decodedJWT = jwtVerifier.verify(convertedToken);
            String jwtId = decodedJWT.getId();
            Date expiresAt = decodedJWT.getExpiresAt();
            return doExpireJwt(jwtId, expiresAt);
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * @Description: 将JWT加入Redis黑名单的具体实现
     * @param jwtId jwt的id
     * @param time redis中的过期时间
     * @Return: 是否执行成功
     * @Author: lions
     * @Datetime: 1/17/2024 1:11 AM
     */
    private boolean doExpireJwt(String jwtId, Date time) {
        if (!this.isValidJwt(jwtId)) {
            return false;
        }
        Date now = new Date();
        long expire = Math.max(0, time.getTime() - now.getTime());
        stringRedisTemplate.opsForValue()
                .set(RedisConstant.JWT_BLACK_LIST_PREFIX + jwtId, "sth", expire, TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * @Description: 判断JWT是否存在于黑名单中
     * @param jwtId JWTid
     * @Return: 是否有效
     * @Author: lions
     * @Datetime: 1/17/2024 12:58 AM
     */
    private boolean isValidJwt(String jwtId) {
        return !Boolean.TRUE.equals(stringRedisTemplate.hasKey(RedisConstant.JWT_BLACK_LIST_PREFIX + jwtId));
    }

    /**
     * @Description: 解析用户信息
     * @param decodedJWT 解码后JWT
     * @Return: UserDetails
     * @Author: lions
     * @Datetime: 1/16/2024 11:34 PM
     */
    public UserDetails getUserDetail(DecodedJWT decodedJWT) {
        Map<String, Claim> claims = decodedJWT.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("****")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    /**
     * @Description: 获取用户id
     * @param decodedJWT 解码后JWT
     * @Return: 用户id
     * @Author: lions
     * @Datetime: 1/16/2024 11:41 PM
     */
    public Integer getUserId(DecodedJWT decodedJWT) {
        Map<String, Claim> claims = decodedJWT.getClaims();
        Claim userId = claims.get("id");
        return userId.asInt();
    }

    /**
     * @Description: 设置过期事件
     * @Return: 过期事件
     * @Author: lions
     * @Datetime: 1/16/2024 11:26 PM
     */
    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire);
        return calendar.getTime();
    }

    /**
     * @Description: 校验token是否合法
     * @param headerToken 请求头中token
     * @Return: 真实token
     * @Author: lions
     * @Datetime: 1/16/2024 11:20 PM
     */
    private String convertToken(String headerToken) {
        if (headerToken == null || !headerToken.startsWith("Bearer ")) {
            return null;
        }
        return headerToken.substring(7);
    }

}
