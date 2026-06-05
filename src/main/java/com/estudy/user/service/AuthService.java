package com.estudy.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.estudy.common.exception.BusinessException;
import com.estudy.common.util.JwtUtil;
import com.estudy.rbac.service.RbacService;
import com.estudy.user.dto.LoginDTO;
import com.estudy.user.dto.LoginVO;
import com.estudy.rbac.entity.SysUserRole;
import com.estudy.rbac.mapper.SysUserRoleMapper;
import com.estudy.user.dto.RegisterDTO;
import com.estudy.user.entity.SysUser;
import com.estudy.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RbacService rbacService;
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 登录
     */
    public LoginVO login(LoginDTO dto) {
        // 查用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
        );
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        // 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        LoginVO vo = LoginVO.of(user.getId(), user.getUsername(), user.getRealName(), user.getAvatar(), token);
        vo.setRoles(rbacService.getUserRoleCodes(user.getId()));
        vo.setPermissions(rbacService.getUserPermissions(user.getId()));
        return vo;
    }

    /**
     * 注册
     */
    public void register(RegisterDTO dto) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 保存用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setStatus(1);
        userMapper.insert(user);

        // 新注册用户默认分配学员角色(role_id=3)
        SysUserRole ur = new SysUserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(3L);
        userRoleMapper.insert(ur);
    }
}
