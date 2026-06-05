package com.estudy.common.interceptor;

import com.estudy.common.exception.BusinessException;
import com.estudy.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Jwt认证拦截器
 * RequiredArgsConstructor 是 Lombok 注解，用于自动生成构造函数。
 * 它会为类中所有 final 修饰的字段（这里是 JwtUtil）生成一个构造器，实现依赖注入，避免手动编写构造函数代码。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录或Token已过期");
        }

        token = token.substring(7);
        if (jwtUtil.isTokenExpired(token)) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }

        // 把用户信息存到request里，后续Controller可以用
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);

        return true;
    }
}
