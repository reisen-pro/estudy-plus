package com.estudy.common.aop;

import com.estudy.common.annotation.permission.RequirePermission;
import com.estudy.common.exception.PermissionException;
import com.estudy.rbac.service.RbacService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final RbacService rbacService;

    /**
     * 定义切点：所有标记了 @RequirePermission 的方法
     */
    @Pointcut("@annotation(com.estudy.common.annotation.permission.RequirePermission)")
    public void permissionPointcut() {
    }

    /**
     * 环绕通知：在方法执行前后进行权限校验
     */
    @Around("permissionPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取当前请求
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new PermissionException("无法获取请求上下文");
        }
        HttpServletRequest request = attributes.getRequest();

        // 2. 获取当前用户权限（从 Token / Session / SecurityContext 中获取）
        Long userId = (Long) request.getAttribute("userId");

        // 3. 获取方法上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission requirePermission = AnnotationUtils.getAnnotation(method, RequirePermission.class);

        // 4. 执行权限校验
        if (requirePermission != null) {
            String requiredPermission = requirePermission.value();

            boolean hasPermission = rbacService.hasPermission(userId, requiredPermission);

            if (!hasPermission) {
                log.warn("权限校验失败，需要权限：{}", requiredPermission);
                throw new PermissionException("无权访问该资源");
            }
        }
        // 5. 权限校验通过，继续执行原方法
        return joinPoint.proceed();
    }
}
