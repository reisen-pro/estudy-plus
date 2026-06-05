package com.estudy.user.dto;

import lombok.Data;
import java.util.List;

@Data
public class LoginVO {

    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private String token;

    /** 钉钉登录相关 */
    private Boolean needBind;
    private String dingUserId;
    private String dingName;
    private String dingAvatar;

    /** 角色与权限 */
    private List<String> roles;
    private List<String> permissions;

    public static LoginVO of(Long userId, String username, String realName, String avatar, String token) {
        LoginVO vo = new LoginVO();
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setRealName(realName);
        vo.setAvatar(avatar);
        vo.setToken(token);
        return vo;
    }
}
