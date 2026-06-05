package com.estudy.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.estudy.common.exception.BusinessException;
import com.estudy.common.result.Result;
import com.estudy.rbac.dto.AssignRoleDTO;
import com.estudy.rbac.dto.PermissionApplyDTO;
import com.estudy.rbac.dto.PermissionReviewDTO;
import com.estudy.rbac.entity.SysPermission;
import com.estudy.rbac.entity.SysPermissionApply;
import com.estudy.rbac.entity.SysRole;
import com.estudy.rbac.entity.SysUserRole;
import com.estudy.rbac.mapper.SysPermissionMapper;
import com.estudy.rbac.mapper.SysRoleMapper;
import com.estudy.rbac.mapper.SysUserRoleMapper;
import com.estudy.rbac.service.PermissionApplyService;
import com.estudy.rbac.service.RbacService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
public class RbacController {

    private final RbacService rbacService;
    private final PermissionApplyService applyService;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;

    // ============ 权限查询 ============

    @GetMapping("/my-permissions")
    public Result<List<String>> myPermissions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(rbacService.getUserPermissions(userId));
    }

    @GetMapping("/my-roles")
    public Result<List<SysRole>> myRoles(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(rbacService.getUserRoleList(userId));
    }

    @GetMapping("/check")
    public Result<Boolean> checkPermission(@RequestParam String code, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(rbacService.hasPermission(userId, code));
    }

    // ============ 权限申请 ============

    @PostMapping("/apply")
    public Result<Void> apply(@RequestBody @Valid PermissionApplyDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        applyService.apply(userId, dto);
        return Result.success();
    }

    @GetMapping("/apply/my")
    public Result<List<SysPermissionApply>> myApplies(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(applyService.myApplies(userId));
    }

    @GetMapping("/apply/pending")
    public Result<List<SysPermissionApply>> pendingList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        assertPermission(userId, "permission:approve");
        return Result.success(applyService.pendingList());
    }

    @GetMapping("/apply/all")
    public Result<List<SysPermissionApply>> allApplies(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        assertPermission(userId, "permission:approve");
        return Result.success(applyService.allApplies());
    }

    @PostMapping("/apply/review")
    public Result<Void> review(@RequestBody @Valid PermissionReviewDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        assertPermission(userId, "permission:approve");
        applyService.review(userId, dto);
        return Result.success();
    }

    // ============ 角色/权限元数据 ============

    @GetMapping("/roles")
    public Result<List<SysRole>> roleList() {
        return Result.success(roleMapper.selectList(null));
    }

    @GetMapping("/permissions")
    public Result<List<SysPermission>> permissionList() {
        return Result.success(permissionMapper.selectList(null));
    }

    // ============ 管理员：分配角色 ============

    @PostMapping("/assign-role")
    public Result<Void> assignRole(@RequestBody @Valid AssignRoleDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        assertPermission(userId, "user:manage");

        // 删除旧角色，重新分配
        userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, dto.getUserId())
        );
        SysUserRole ur = new SysUserRole();
        ur.setUserId(dto.getUserId());
        ur.setRoleId(dto.getRoleId());
        userRoleMapper.insert(ur);
        return Result.success();
    }

    private void assertPermission(Long userId, String code) {
        if (!rbacService.hasPermission(userId, code)) {
            throw new BusinessException("无操作权限");
        }
    }
}
