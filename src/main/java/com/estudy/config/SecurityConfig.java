package com.estudy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {

    /**
     * 配置密码加密器
     * <p>
     * 使用 BCrypt 强哈希算法对用户密码进行单向加密，确保密码存储安全。
     * BCrypt 会自动生成随机盐值，相同密码每次加密结果不同，有效防止彩虹表攻击。
     * </p>
     *
     * @return BCryptPasswordEncoder 密码加密器实例
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
