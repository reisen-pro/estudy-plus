package com.estudy.rbac.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.estudy.rbac.entity.*;
import com.estudy.rbac.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RbacService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRoleMapper roleMapper;

    /** 判断用户是否拥有指定权限 */
    public boolean hasPermission(Long userId, String permissionCode) {
        List<Long> roleIds = getRoleIds(userId);
        if (roleIds.isEmpty()) return false;

        SysPermission perm = permissionMapper.selectOne(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getPermissionCode, permissionCode)
        );
        if (perm == null) return false;

        return rolePermissionMapper.exists(
                new LambdaQueryWrapper<SysRolePermission>()
                        .in(SysRolePermission::getRoleId, roleIds)
                        .eq(SysRolePermission::getPermissionId, perm.getId())
        );
    }

    /** 获取用户所有权限编码 */
    public List<String> getUserPermissions(Long userId) {
        List<Long> roleIds = getRoleIds(userId);
        if (roleIds.isEmpty()) return Collections.emptyList();

        List<Long> permIds = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds)
        ).stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList());

        if (permIds.isEmpty()) return Collections.emptyList();

        return permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().in(SysPermission::getId, permIds)
        ).stream().map(SysPermission::getPermissionCode).collect(Collectors.toList());
    }

    /** 获取用户角色列表 */
    public List<SysRole> getUserRoleList(Long userId) {
        List<Long> roleIds = getRoleIds(userId);
        if (roleIds.isEmpty()) return Collections.emptyList();
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds));
    }

    /** 获取用户角色编码列表 */
    public List<String> getUserRoleCodes(Long userId) {
        return getUserRoleList(userId).stream().map(SysRole::getRoleCode).collect(Collectors.toList());
    }

    private List<Long> getRoleIds(Long userId) {
        return userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }
}
