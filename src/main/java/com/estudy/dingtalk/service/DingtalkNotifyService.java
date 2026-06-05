package com.estudy.dingtalk.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.estudy.dingtalk.entity.DingtalkMessageLog;
import com.estudy.dingtalk.entity.DingtalkUserBind;
import com.estudy.dingtalk.mapper.DingtalkMessageLogMapper;
import com.estudy.dingtalk.mapper.DingtalkUserBindMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DingtalkNotifyService extends ServiceImpl<DingtalkMessageLogMapper, DingtalkMessageLog> {

    private final DingtalkClient dingtalkClient;
    private final DingtalkUserBindMapper userBindMapper;

    /**
     * 发送培训通知
     */
    public void sendTrainingNotice(Long userId, String courseTitle, String courseUrl) {
        String content = String.format("## 培训通知\n\n您有新的培训任务：**%s**\n\n[点击查看课程](%s)", courseTitle, courseUrl);
        sendMarkdownNotice(userId, "training_notice", "培训通知", content, null);
    }

    /**
     * 发送考试通知
     */
    public void sendExamNotice(Long userId, String examTitle, String examUrl) {
        String content = String.format("## 考试通知\n\n您有新的考试任务：**%s**\n\n[点击进入考试](%s)", examTitle, examUrl);
        sendMarkdownNotice(userId, "exam_notice", "考试通知", content, null);
    }

    /**
     * 发送考试结果通知
     */
    public void sendExamResultNotice(Long userId, String examTitle, int score, boolean passed) {
        String result = passed ? "✅ 通过" : "❌ 未通过";
        String content = String.format("## 考试结果\n\n**%s**\n\n得分：%d分 %s", examTitle, score, result);
        sendMarkdownNotice(userId, "exam_result", "考试结果", content, null);
    }

    /**
     * 发送Markdown工作通知
     */
    public void sendMarkdownNotice(Long userId, String msgType, String title, String content, String bizId) {
        // 查钉钉绑定
        DingtalkUserBind bind = userBindMapper.selectOne(
                new LambdaQueryWrapper<DingtalkUserBind>().eq(DingtalkUserBind::getUserId, userId)
        );
        if (bind == null) {
            log.warn("用户{}未绑定钉钉，跳过通知", userId);
            return;
        }

        // 记录日志
        DingtalkMessageLog msgLog = new DingtalkMessageLog();
        msgLog.setUserId(userId);
        msgLog.setDingUserId(bind.getDingUserId());
        msgLog.setMsgType(msgType);
        msgLog.setTitle(title);
        msgLog.setContent(content);
        msgLog.setBizId(bizId);
        msgLog.setStatus(0);

        try {
            // 构造markdown消息
            JSONObject msg = new JSONObject();
            JSONObject markdown = new JSONObject();
            markdown.set("title", title);
            markdown.set("text", content);
            msg.set("msgtype", "markdown");
            msg.set("markdown", markdown);

            String taskId = dingtalkClient.sendWorkNotice(bind.getDingUserId(), "markdown", msg);
            msgLog.setStatus(1);
            msgLog.setDingTaskId(taskId);
            log.info("钉钉通知发送成功: userId={}, taskId={}", userId, taskId);
        } catch (Exception e) {
            msgLog.setStatus(2);
            msgLog.setErrorMsg(e.getMessage());
            log.error("钉钉通知发送失败: userId={}", userId, e);
        }
        save(msgLog);
    }

    /**
     * 批量发送通知(如全员培训通知)
     */
    public void batchSendNotice(List<Long> userIds, String msgType, String title, String content, String bizId) {
        for (Long userId : userIds) {
            try {
                sendMarkdownNotice(userId, msgType, title, content, bizId);
            } catch (Exception e) {
                log.error("批量通知发送失败: userId={}", userId, e);
            }
        }
    }
}
