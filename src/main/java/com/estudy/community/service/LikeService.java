package com.estudy.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.estudy.community.entity.Comment;
import com.estudy.community.entity.Like;
import com.estudy.community.entity.Post;
import com.estudy.community.mapper.LikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 点赞服务
 */
@Service
@RequiredArgsConstructor
public class LikeService extends ServiceImpl<LikeMapper, Like> {

    private final PostService postService;
    private final CommentService commentService;

    /**
     * 点赞/取消点赞(切换)
     * @return true=已点赞 false=已取消
     */
    public boolean toggleLike(Long userId, Integer targetType, Long targetId) {
        Like existing = getOne(
                new LambdaQueryWrapper<Like>()
                        .eq(Like::getUserId, userId)
                        .eq(Like::getTargetType, targetType)
                        .eq(Like::getTargetId, targetId)
        );

        if (existing != null) {
            // 取消点赞
            removeById(existing.getId());
            updateLikeCount(targetType, targetId, -1);
            return false;
        } else {
            // 点赞
            Like like = new Like();
            like.setUserId(userId);
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            save(like);
            updateLikeCount(targetType, targetId, 1);
            return true;
        }
    }

    /**
     * 更新点赞数
     * @param targetType 目标类型 1=帖子 2=评论
     * @param targetId 目标ID
     * @param delta 变化量（正数=点赞，负数=取消点赞）
     */
    private void updateLikeCount(Integer targetType, Long targetId, int delta) {
        if (targetType == 1) {
            // 帖子
            Post post = postService.getById(targetId);
            if (post != null) {
                adjustLikeCount(post, delta);
                postService.updateById(post);
            }
        } else if (targetType == 2) {
            // 评论
            Comment comment = commentService.getById(targetId);
            if (comment != null) {
                adjustLikeCount(comment, delta);
                commentService.updateById(comment);
            }
        }
    }

    /**
     * 调整点赞数（通用方法）
     * @param target 目标对象（Post 或 Comment）
     * @param delta 变化量（正数=点赞，负数=取消点赞）
     */
    private void adjustLikeCount(Object target, int delta) {
        if (target instanceof Post post) {
            if (delta > 0) {
                post.setLikeCount(post.getLikeCount() + 1);
            } else {
                post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            }
        } else if (target instanceof Comment comment) {
            if (delta > 0) {
                comment.setLikeCount(comment.getLikeCount() + 1);
            } else {
                comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            }
        }
    }
}
