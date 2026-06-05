package com.estudy.dingtalk.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.estudy.common.exception.BusinessException;
import com.estudy.dingtalk.config.DingtalkProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 钉钉API客户端
 * 封装常用的钉钉开放平台API调用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DingtalkClient {

    private final DingtalkProperties properties;

    private static final String BASE_URL = "https://oapi.dingtalk.com";

    /** 缓存的accessToken */
    private volatile String cachedToken;
    /** token过期时间(毫秒时间戳) */
    private volatile long tokenExpireAt;

    /**
     * 获取企业内部应用的access_token
     * 带本地缓存，有效期7200秒，提前5分钟刷新
     */
    public String getAccessToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpireAt) {
            return cachedToken;
        }
        String url = BASE_URL + "/gettoken?appkey=" + properties.getAppKey()
                + "&appsecret=" + properties.getAppSecret();
        String resp = HttpUtil.get(url);
        JSONObject json = JSONUtil.parseObj(resp);
        if (json.getInt("errcode") != 0) {
            log.error("获取钉钉access_token失败: {}", resp);
            throw new BusinessException("获取钉钉access_token失败: " + json.getStr("errmsg"));
        }
        cachedToken = json.getStr("access_token");
        tokenExpireAt = System.currentTimeMillis() + 7100 * 1000L; // 提前100秒过期
        log.info("刷新钉钉access_token成功");
        return cachedToken;
    }

    /**
     * 通过临时授权码获取用户信息
     * 用于扫码登录/授权登录场景
     */
    public JSONObject getUserInfoByCode(String authCode) {
        String url = BASE_URL + "/topapi/v2/user/getuserinfo?access_token=" + getAccessToken();
        JSONObject body = new JSONObject();
        body.set("code", authCode);
        String resp = HttpUtil.post(url, body.toString());
        JSONObject json = JSONUtil.parseObj(resp);
        if (json.getInt("errcode") != 0) {
            log.error("钉钉获取用户信息失败: {}", resp);
            throw new BusinessException("钉钉授权失败: " + json.getStr("errmsg"));
        }
        return json.getJSONObject("result");
    }

    /**
     * 通过userId获取用户详情
     */
    public JSONObject getUserDetail(String dingUserId) {
        String url = BASE_URL + "/topapi/v2/user/get?access_token=" + getAccessToken();
        JSONObject body = new JSONObject();
        body.set("userid", dingUserId);
        String resp = HttpUtil.post(url, body.toString());
        JSONObject json = JSONUtil.parseObj(resp);
        if (json.getInt("errcode") != 0) {
            log.error("钉钉获取用户详情失败: {}", resp);
            throw new BusinessException("获取钉钉用户详情失败");
        }
        return json.getJSONObject("result");
    }

    /**
     * 发送工作通知消息
     * 支持文本、markdown等消息类型
     */
    public String sendWorkNotice(String dingUserId, String msgType, JSONObject msg) {
        String url = BASE_URL + "/topapi/message/corpconversation/asyncsend_v2?access_token=" + getAccessToken();
        JSONObject body = new JSONObject();
        body.set("agent_id", properties.getAgentId());
        body.set("userid_list", dingUserId);
        body.set("msg", msg);

        String resp = HttpUtil.post(url, body.toString());
        JSONObject json = JSONUtil.parseObj(resp);
        if (json.getInt("errcode") != 0) {
            log.error("钉钉发送工作通知失败: {}", resp);
            throw new BusinessException("发送钉钉通知失败: " + json.getStr("errmsg"));
        }
        return json.getStr("task_id");
    }

    /**
     * 获取钉钉扫码登录URL
     */
    public String getLoginUrl(String redirectUri, String state) {
        return "https://login.dingtalk.com/login/qrcode.htm" +
                "?appkey=" + properties.getAppKey() +
                "&redirect_uri=" + redirectUri +
                "&state=" + state +
                "&response_type=code" +
                "&scope=openid";
    }
}
