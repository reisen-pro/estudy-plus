package com.estudy.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.estudy.community.entity.Notification;
import com.estudy.community.enums.NotificationType;
import com.estudy.community.mapper.NotificationMapper;
import com.estudy.user.entity.SysUser;
import com.estudy.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通知服务
 */
@Service
@RequiredArgsConstructor
public class NotificationService extends ServiceImpl<NotificationMapper, Notification> {

    private final SysUserMapper userMapper;

    /**
     * 发送系统通知
     * @param userId 接收通知的用户ID（通知给谁）
     * @param fromUserId 发起通知的用户ID（谁触发的通知，如点赞者、评论者；系统通知可为null）
     * @param type 通知类型，常见值：
     *      @see com.estudy.community.enums.NotificationType
     * @param title 通知标题（简短描述，如"张三赞了你的帖子"）
     * @param content 通知详细内容（如评论内容、回复内容等）
     * @param bizId 业务关联ID（如帖子ID、评论ID，用于点击通知后跳转到对应页面）
     */
    public void sendNotification(Long userId, Long fromUserId, NotificationType type, String title, String content, String bizId) {
        Notification n = new Notification();
        n.setUserId(userId);              // 接收者ID
        n.setFromUserId(fromUserId);      // 发起者ID
        n.setType(type.getCode());        // 通知类型（从枚举获取code值）
        n.setTitle(title);                // 通知标题
        n.setContent(content);            // 通知内容
        n.setBizId(bizId);                // 业务关联ID
        n.setIsRead(0);                   // 未读状态（0=未读，1=已读）
        save(n);
    }

    /**
     * 查询用户通知列表（分页）
     * <p>
     * 返回指定用户的通知列表，按创建时间倒序排列（最新的通知在前），
     * 并自动填充每条通知的触发者姓名。
     * </p>
     *
     * @param userId 用户ID，查询该用户收到的所有通知
     * @param pageNum 页码，从1开始
     * @param pageSize 每页条数
     * @return 分页结果，包含通知列表和总记录数
     */
    public Page<Notification> getUserNotifications(Long userId, Integer pageNum, Integer pageSize) {
        // 先生成一个分页page
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        // 拿到结果
        Page<Notification> result = page(page, wrapper);

        // 填充触发者名
        fillFromUserName(result.getRecords());
        return result;
    }

    /**
     * 未读数量
     */
    public long getUnreadCount(Long userId) {
        return count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    /**
     * 标记已读
     */
    public void markRead(Long notificationId, Long userId) {
        Notification n = getById(notificationId);
        if (n != null && n.getUserId().equals(userId)) {
            n.setIsRead(1);
            updateById(n);
        }
    }

    /**
     * 全部标记已读
     */
    public void markAllRead(Long userId) {
        List<Notification> unread = list(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
        for (Notification n : unread) {
            n.setIsRead(1);
        }
        // 对于简单的批量更新操作，不一定需要事务。
        // MyBatis-Plus 的 updateBatchById 本身就是一条 SQL 语句，要么全部成功，要么全部失败，原子性已经有保障。
        // 如果不需要额外的事务控制，可以忽略这个警告。
        updateBatchById(unread);
    }

    /**
     * 批量填充通知的触发者姓名
     * <p>
     * 通过收集所有通知中的 fromUserId，批量查询用户信息并填充到通知对象中。
     * 如果 fromUserId 为空或找不到对应用户，则默认显示为"系统"。
     * </p>
     *
     * @param notifications 通知列表，方法会直接修改列表中对象的 fromUserName 字段
     */
    private void fillFromUserName(List<Notification> notifications) {
        if (notifications.isEmpty()) return;

        // 收集所有不重复的触发者ID
        // 通过set去重
        Set<Long> fromIds = notifications.stream()
                .map(Notification::getFromUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (fromIds.isEmpty()) return;

        // 批量查询用户信息并构建 ID->姓名
        // 映射成map
        Map<Long, String> nameMap = userMapper.selectBatchIds(fromIds).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));

        // 填充每条通知的触发者姓名
        // 批量填充
        for (Notification n : notifications) {
            if (n.getFromUserId() != null) {
                n.setFromUserName(nameMap.getOrDefault(n.getFromUserId(), "系统"));
            }
        }
    }
}
