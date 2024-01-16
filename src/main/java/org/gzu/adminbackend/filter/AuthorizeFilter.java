package org.gzu.adminbackend.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.gzu.adminbackend.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @description TODO
 * @classname AuthorizeFilter
 * @date 1/16/2024 11:15 PM
 * @created by LIONS7
 */

@Component
public class AuthorizeFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        DecodedJWT decodedJWT = jwtUtil.resolve(authorization);
        // 进行认证
        if (decodedJWT != null) {
            UserDetails userDetail = jwtUtil.getUserDetail(decodedJWT);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // request.setAttribute("id", jwtUtil.getUserId(decodedJWT));
        }
        // 认证不通过放行
        filterChain.doFilter(request, response);
    }
}
