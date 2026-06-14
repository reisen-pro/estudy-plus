package com.estudy.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.estudy.common.result.Result;
import com.estudy.community.dto.CommentCreateDTO;
import com.estudy.community.dto.PostCreateDTO;
import com.estudy.community.dto.PostQueryDTO;
import com.estudy.community.entity.Comment;
import com.estudy.community.entity.Notification;
import com.estudy.community.entity.Post;
import com.estudy.community.enums.NotificationType;
import com.estudy.community.service.CommentService;
import com.estudy.community.service.LikeService;
import com.estudy.community.service.NotificationService;
import com.estudy.community.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 社区模块控制器
 * 提供帖子、评论、点赞、通知等社区功能的REST API接口
 */
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final PostService postService;// 帖子服务
    private final CommentService commentService;// 评论服务
    private final LikeService likeService;// 点赞服务
    private final NotificationService notificationService;// 通知服务
    private final HttpServletRequest request;// 请求对象

    private Long currentUserId() {
        return (Long) request.getAttribute("userId");
    }

    // ========== 帖子 ==========

    /**
     * 发帖
     */
    @PostMapping("/post")
    public Result<Long> createPost(@RequestBody @Valid PostCreateDTO dto) {
        return Result.success(postService.createPost(currentUserId(), dto));
    }

    /**
     * 帖子详情
     */
    @GetMapping("/post/{id}")
    public Result<Post> postDetail(@PathVariable Long id) {
        return Result.success(postService.getPostDetail(id, currentUserId()));
    }

    /**
     * 帖子分页
     */
    @GetMapping("/post/page")
    public Result<Page<Post>> postPage(PostQueryDTO dto) {
        return Result.success(postService.pageQuery(dto, currentUserId()));
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/post/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.removeById(id);
        return Result.success();
    }

    // ========== 评论 ==========

    /**
     * 发评论
     */
    @PostMapping("/comment")
    public Result<Long> createComment(@RequestBody @Valid CommentCreateDTO dto) {
        Long commentId = commentService.createComment(currentUserId(), dto);
        // 发通知给帖子作者
        Post post = postService.getById(dto.getPostId());
        if (post != null && !post.getUserId().equals(currentUserId())) {
            notificationService.sendNotification(
                    post.getUserId(), currentUserId(), NotificationType.COMMENT,
                    "新评论", "有人评论了你的帖子：" + post.getTitle(), String.valueOf(post.getId()));
        }
        return Result.success(commentId);
    }

    /**
     * 帖子评论列表(树形)
     */
    @GetMapping("/post/{postId}/comments")
    public Result<List<Comment>> postComments(@PathVariable Long postId) {
        return Result.success(commentService.getPostComments(postId));
    }

    // ========== 点赞 ==========

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like")
    public Result<Map<String, Object>> toggleLike(
            @RequestParam Integer targetType,
            @RequestParam Long targetId) {
        boolean liked = likeService.toggleLike(currentUserId(), targetType, targetId);
        // 点赞时发通知
        if (liked && targetType == 1) {
            Post post = postService.getById(targetId);
            if (post != null && !post.getUserId().equals(currentUserId())) {
                notificationService.sendNotification(
                        post.getUserId(), currentUserId(), NotificationType.LIKE_POST,
                        "收到点赞", "有人赞了你的帖子：" + post.getTitle(), String.valueOf(post.getId()));
            }
        }
        // 评论被点赞的通知
        if (liked && targetType == 2) {
            Comment comment = commentService.getById(targetId);
            if (comment != null && !comment.getUserId().equals(currentUserId())) {
                notificationService.sendNotification(
                        comment.getUserId(), currentUserId(), NotificationType.LIKE_COMMENT,
                        "收到点赞", "有人赞了你的评论", String.valueOf(comment.getPostId()));
            }
        }
        return Result.success(Map.of("liked", liked));
    }

    // ========== 通知 ==========

    /**
     * 通知列表
     */
    @GetMapping("/notification/page")
    public Result<Page<Notification>> notifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(notificationService.getUserNotifications(currentUserId(), pageNum, pageSize));
    }

    /**
     * 未读数量
     */
    @GetMapping("/notification/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        return Result.success(Map.of("count", notificationService.getUnreadCount(currentUserId())));
    }

    /**
     * 标记已读
     */
    @PutMapping("/notification/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id, currentUserId());
        return Result.success();
    }

    /**
     * 全部已读
     */
    @PutMapping("/notification/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead(currentUserId());
        return Result.success();
    }
}
