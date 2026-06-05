package com.estudy.user.controller;

import com.estudy.common.result.Result;
import com.estudy.rbac.service.RbacService;
import com.estudy.user.dto.LoginDTO;
import com.estudy.user.dto.LoginVO;
import com.estudy.user.dto.RegisterDTO;
import com.estudy.user.entity.SysUser;
import com.estudy.user.mapper.SysUserMapper;
import com.estudy.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SysUserMapper userMapper;
    private final RbacService rbacService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    /** 获取当前用户信息 */
    @GetMapping("/info")
    public Result<LoginVO> userInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setRoles(rbacService.getUserRoleCodes(userId));
        vo.setPermissions(rbacService.getUserPermissions(userId));
        return Result.success(vo);
    }
}
