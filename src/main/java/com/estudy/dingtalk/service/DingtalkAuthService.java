package com.estudy.dingtalk.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.estudy.common.exception.BusinessException;
import com.estudy.dingtalk.entity.DingtalkUserBind;
import com.estudy.dingtalk.mapper.DingtalkUserBindMapper;
import com.estudy.user.dto.LoginVO;
import com.estudy.user.entity.SysUser;
import com.estudy.user.mapper.SysUserMapper;
import com.estudy.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DingtalkAuthService extends ServiceImpl<DingtalkUserBindMapper, DingtalkUserBind> {

    private final DingtalkClient dingtalkClient;
    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;

    /**
     * 钉钉扫码登录
     * 1. 用authCode换钉钉userId
     * 2. 查绑定关系
     * 3. 已绑定→直接登录返回token
     * 4. 未绑定→返回钉钉信息，前端引导绑定
     */
    public LoginVO loginByDingtalk(String authCode) {
        // 1. 换取钉钉用户信息
        JSONObject userInfo = dingtalkClient.getUserInfoByCode(authCode);
        String dingUserId = userInfo.getStr("userid");

        // 获取详细信息
        JSONObject detail = dingtalkClient.getUserDetail(dingUserId);
        String dingName = detail.getStr("name");
        String dingAvatar = detail.getStr("avatar");

        // 2. 查绑定
        DingtalkUserBind bind = getOne(
                new LambdaQueryWrapper<DingtalkUserBind>()
                        .eq(DingtalkUserBind::getDingUserId, dingUserId)
        );

        if (bind != null) {
            // 3. 已绑定，直接登录
            SysUser user = userMapper.selectById(bind.getUserId());
            if (user == null) throw new BusinessException("关联用户不存在");

            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            LoginVO vo = new LoginVO();
            vo.setToken(token);
            vo.setUserId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            return vo;
        }

        // 4. 未绑定，返回特殊标记，前端引导绑定
        LoginVO vo = new LoginVO();
        vo.setNeedBind(true);
        vo.setDingUserId(dingUserId);
        vo.setDingName(dingName);
        vo.setDingAvatar(dingAvatar);
        return vo;
    }

    /**
     * 绑定钉钉账号
     * 已登录用户绑定钉钉
     */
    public void bindDingtalk(Long userId, String dingUserId, String dingName, String dingAvatar) {
        // 检查是否已绑定
        long count = count(
                new LambdaQueryWrapper<DingtalkUserBind>()
                        .eq(DingtalkUserBind::getUserId, userId)
        );
        if (count > 0) throw new BusinessException("该账号已绑定钉钉");

        long dingCount = count(
                new LambdaQueryWrapper<DingtalkUserBind>()
                        .eq(DingtalkUserBind::getDingUserId, dingUserId)
        );
        if (dingCount > 0) throw new BusinessException("该钉钉账号已被其他用户绑定");

        DingtalkUserBind bind = new DingtalkUserBind();
        bind.setUserId(userId);
        bind.setDingUserId(dingUserId);
        bind.setDingName(dingName);
        bind.setDingAvatar(dingAvatar);
        bind.setBindTime(LocalDateTime.now());
        save(bind);
    }

    /**
     * 解绑钉钉
     */
    public void unbindDingtalk(Long userId) {
        remove(new LambdaQueryWrapper<DingtalkUserBind>().eq(DingtalkUserBind::getUserId, userId));
    }
}
