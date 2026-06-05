package com.estudy.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.estudy.community.dto.CommentCreateDTO;
import com.estudy.community.entity.Comment;
import com.estudy.community.entity.Post;
import com.estudy.community.mapper.CommentMapper;
import com.estudy.user.entity.SysUser;
import com.estudy.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 评论服务
 */
@Service
@RequiredArgsConstructor
public class CommentService extends ServiceImpl<CommentMapper, Comment> {

    private final SysUserMapper userMapper;
    private final PostService postService;

    /**
     * 创建评论
     * @param userId 当前用户ID（评论者）
     * @param dto 评论创建参数
     * @return 新创建的评论ID
     */
    public Long createComment(Long userId, CommentCreateDTO dto) {
        Comment comment = new Comment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setReplyToUserId(dto.getReplyToUserId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);
        save(comment);

        // 帖子评论数+1
        Post post = postService.getById(dto.getPostId());
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postService.updateById(post);
        }
        return comment.getId();
    }


    /**
     * 获取帖子的评论列表（树形结构）
     * <p>
     * 返回的评论按层级组织：
     * - 顶级评论（parentId=0）作为根节点
     * - 每条顶级评论的 children 包含其所有子评论（回复）
     * </p>
     *
     * @param postId 帖子ID
     * @return 树形结构的评论列表
     */
    public List<Comment> getPostComments(Long postId) {
        // 查询该帖子下所有正常状态的评论，按时间正序排列
        List<Comment> all = list(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getPostId, postId)
                        .eq(Comment::getStatus, 1)
                        .orderByAsc(Comment::getCreateTime)
        );

        // 批量查询用户名
        Map<Long, String> userNameMap = new HashMap<>();
        if (!all.isEmpty()) {
            // 收集所有需要查询的用户ID（包括评论用户和回复目标用户）
            Set<Long> userIds = new HashSet<>();
            for (Comment comment : all) {
                if (comment.getUserId() != null && comment.getUserId() > 0) {
                    userIds.add(comment.getUserId());
                }
                if (comment.getReplyToUserId() != null && comment.getReplyToUserId() > 0) {
                    userIds.add(comment.getReplyToUserId());
                }
            }

            // 根据用户ID批量查询用户信息
            if (!userIds.isEmpty()) {
                List<SysUser> users = userMapper.selectBatchIds(userIds);
                // 将用户ID和用户名的映射关系存入Map
                for (SysUser user : users) {
                    userNameMap.put(user.getId(), user.getRealName());
                }
            }
        }

        // 填充评论的用户名和回复目标用户名
        for (Comment comment : all) {
            comment.setUserName(userNameMap.getOrDefault(comment.getUserId(), "未知"));
            if (comment.getReplyToUserId() != null && comment.getReplyToUserId() > 0) {
                comment.setReplyToUserName(userNameMap.getOrDefault(comment.getReplyToUserId(), "未知"));
            }
        }

        // 构建树形结构：先找出所有顶级评论（parentId为0的评论）
        List<Comment> roots = new ArrayList<>();
        for (Comment comment : all) {
            if (comment.getParentId() == 0) {
                roots.add(comment);
            }
        }

        // 为每个顶级评论添加子评论
        for (Comment root : roots) {
            List<Comment> children = new ArrayList<>();
            for (Comment comment : all) {
                if (root.getId().equals(comment.getParentId())) {
                    children.add(comment);
                }
            }
            root.setChildren(children);
        }

        return roots;
    }
}

