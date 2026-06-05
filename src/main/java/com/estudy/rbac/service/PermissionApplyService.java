package com.estudy.rbac.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.estudy.common.exception.BusinessException;
import com.estudy.rbac.dto.PermissionApplyDTO;
import com.estudy.rbac.dto.PermissionReviewDTO;
import com.estudy.rbac.entity.SysPermission;
import com.estudy.rbac.entity.SysPermissionApply;
import com.estudy.rbac.entity.SysUserRole;
import com.estudy.rbac.mapper.SysPermissionApplyMapper;
import com.estudy.rbac.mapper.SysPermissionMapper;
import com.estudy.rbac.mapper.SysRoleMapper;
import com.estudy.rbac.mapper.SysRolePermissionMapper;
import com.estudy.rbac.mapper.SysUserRoleMapper;
import com.estudy.user.entity.SysUser;
import com.estudy.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionApplyService {

    private final SysPermissionApplyMapper applyMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysRoleMapper roleMapper;
    private final RbacService rbacService;

    /** 提交权限申请 */
    @Transactional
    public void apply(Long userId, PermissionApplyDTO dto) {
        // 校验权限编码存在
        SysPermission perm = permissionMapper.selectOne(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getPermissionCode, dto.getPermissionCode())
        );
        if (perm == null) throw new BusinessException("权限不存在");

        // 已经有权限就不用申请
        if (rbacService.hasPermission(userId, dto.getPermissionCode())) {
            throw new BusinessException("您已拥有该权限，无需申请");
        }

        // 不能重复申请（待审批的）
        boolean pending = applyMapper.exists(
                new LambdaQueryWrapper<SysPermissionApply>()
                        .eq(SysPermissionApply::getUserId, userId)
                        .eq(SysPermissionApply::getPermissionCode, dto.getPermissionCode())
                        .eq(SysPermissionApply::getStatus, 0)
        );
        if (pending) throw new BusinessException("您已有待审批的申请，请等待审批");

        SysPermissionApply apply = new SysPermissionApply();
        apply.setUserId(userId);
        apply.setPermissionCode(dto.getPermissionCode());
        apply.setReason(dto.getReason());
        apply.setStatus(0);
        applyMapper.insert(apply);
    }

    /** 我的申请列表 */
    public List<SysPermissionApply> myApplies(Long userId) {
        List<SysPermissionApply> list = applyMapper.selectList(
                new LambdaQueryWrapper<SysPermissionApply>()
                        .eq(SysPermissionApply::getUserId, userId)
                        .orderByDesc(SysPermissionApply::getCreateTime)
        );
        fillApplyExtra(list);
        return list;
    }

    /** 待审批列表（管理员用） */
    public List<SysPermissionApply> pendingList() {
        List<SysPermissionApply> list = applyMapper.selectList(
                new LambdaQueryWrapper<SysPermissionApply>()
                        .eq(SysPermissionApply::getStatus, 0)
                        .orderByAsc(SysPermissionApply::getCreateTime)
        );
        fillApplyExtra(list);
        return list;
    }

    /** 所有申请列表（管理员用） */
    public List<SysPermissionApply> allApplies() {
        List<SysPermissionApply> list = applyMapper.selectList(
                new LambdaQueryWrapper<SysPermissionApply>()
                        .orderByDesc(SysPermissionApply::getCreateTime)
        );
        fillApplyExtra(list);
        return list;
    }

    /** 审批 */
    @Transactional
    public void review(Long reviewerId, PermissionReviewDTO dto) {
        SysPermissionApply apply = applyMapper.selectById(dto.getApplyId());
        if (apply == null) throw new BusinessException("申请不存在");
        if (apply.getStatus() != 0) throw new BusinessException("该申请已处理");

        apply.setStatus(dto.getStatus());
        apply.setReviewerId(reviewerId);
        apply.setReviewRemark(dto.getReviewRemark());
        apply.setReviewTime(LocalDateTime.now());

        // 如果通过，直接给用户加上讲师角色（role_id=2，拥有 course:upload 权限）
        if (dto.getStatus() == 1 && "course:upload".equals(apply.getPermissionCode())) {
            // 检查是否已有讲师角色
            boolean hasTeacherRole = userRoleMapper.exists(
                    new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getUserId, apply.getUserId())
                            .eq(SysUserRole::getRoleId, 2L)
            );
            if (!hasTeacherRole) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(apply.getUserId());
                ur.setRoleId(2L);
                userRoleMapper.insert(ur);
            }
        }

        applyMapper.updateById(apply);
    }

    private void fillApplyExtra(List<SysPermissionApply> list) {
        if (list.isEmpty()) return;

        Set<Long> userIds = list.stream().map(SysPermissionApply::getUserId).collect(Collectors.toSet());
        Set<Long> reviewerIds = list.stream().map(SysPermissionApply::getReviewerId)
                .filter(id -> id != null).collect(Collectors.toSet());
        Set<String> permCodes = list.stream().map(SysPermissionApply::getPermissionCode).collect(Collectors.toSet());

        Map<Long, SysUser> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
        Map<Long, SysUser> reviewerMap = reviewerIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(reviewerIds).stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        Map<String, SysPermission> permMap = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().in(SysPermission::getPermissionCode, permCodes)
        ).stream().collect(Collectors.toMap(SysPermission::getPermissionCode, p -> p));

        for (SysPermissionApply a : list) {
            SysUser u = userMap.get(a.getUserId());
            if (u != null) {
                a.setUserName(u.getUsername());
                a.setRealName(u.getRealName());
            }
            SysPermission p = permMap.get(a.getPermissionCode());
            if (p != null) a.setPermissionName(p.getPermissionName());
            if (a.getReviewerId() != null) {
                SysUser r = reviewerMap.get(a.getReviewerId());
                if (r != null) a.setReviewerName(r.getRealName());
            }
        }
    }
}
