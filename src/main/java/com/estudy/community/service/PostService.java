package com.estudy.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.estudy.common.exception.BusinessException;
import com.estudy.community.dto.PostCreateDTO;
import com.estudy.community.dto.PostQueryDTO;
import com.estudy.community.entity.Like;
import com.estudy.community.entity.Post;
import com.estudy.community.mapper.LikeMapper;
import com.estudy.community.mapper.PostMapper;
import com.estudy.user.entity.SysUser;
import com.estudy.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 帖子服务
 */
@Service
@RequiredArgsConstructor
public class PostService extends ServiceImpl<PostMapper, Post> {

    private final SysUserMapper userMapper;// 用户服务
    private final LikeMapper likeMapper;// 点赞服务

    /**
     * 创建新帖子
     * <p>
     * 根据用户ID和帖子创建参数生成新帖子，设置默认分类为"讨论"，
     * 初始化浏览数、点赞数、评论数为0，状态为正常。
     * </p>
     *
     * @param userId 发帖用户ID
     * @param dto 帖子创建参数，包含标题、内容、分类等信息
     * @return 新创建的帖子ID
     */
    public Long createPost(Long userId, PostCreateDTO dto) {
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        // 设置分类，默认为"讨论"
        post.setCategory(StringUtils.hasText(dto.getCategory()) ? dto.getCategory() : "讨论");

        // 初始化统计数据
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(0);
        post.setStatus(1);
        save(post);
        return post.getId();
    }

    /**
     * 分页查询帖子列表
     * <p>
     * 支持按关键词模糊搜索标题、按分类筛选，仅返回正常状态的帖子。
     * 排序规则：置顶帖优先，其次按创建时间倒序。
     * 查询结果会自动填充作者姓名和当前用户的点赞状态。
     * </p>
     *
     * @param dto 查询条件，包含页码、每页条数、关键词、分类等
     * @param currentUserId 当前登录用户ID，用于判断点赞状态（可为null）
     * @return 分页结果，包含帖子列表及总数
     */
    public Page<Post> pageQuery(PostQueryDTO dto, Long currentUserId) {
        Page<Post> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();

        // 按关键词模糊搜索标题
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(Post::getTitle, dto.getKeyword());
        }

        // 按分类筛选
        if (StringUtils.hasText(dto.getCategory())) {
            wrapper.eq(Post::getCategory, dto.getCategory());
        }

        // 仅查询正常状态，置顶优先，再按时间倒序
        wrapper.eq(Post::getStatus, 1)
                .orderByDesc(Post::getIsTop)
                .orderByDesc(Post::getCreateTime);
        Page<Post> result = page(page, wrapper);

        // 填充作者名和点赞状态
        fillPostExtra(result.getRecords(), currentUserId);
        return result;
    }

    /**
     * 获取帖子详情
     * <p>
     * 根据帖子ID查询帖子详情，并填充作者名和点赞状态。
     * 浏览量+1。
     * </p>
     *
     * @param id 帖子ID
     * @param currentUserId 当前登录用户ID，用于判断点赞状态（可为null）
     * @return 帖子详情
     */
    public Post getPostDetail(Long id, Long currentUserId) {
        Post post = getById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        // 浏览量+1
        post.setViewCount(post.getViewCount() + 1);
        updateById(post);
        // 填充
        fillPostExtra(List.of(post), currentUserId);
        return post;
    }

    /**
     * 填充帖子作者名和点赞状态
     * <p>
     * 根据帖子作者ID列表查询作者姓名，并填充到帖子对象中。
     * 如果当前用户已登录，则根据当前用户ID和帖子ID查询点赞记录，并填充到帖子对象中。
     * </p>
     *
     * @param posts 帖子列表
     * @param currentUserId 当前登录用户ID，用于查询点赞记录（可为null）
     */
    private void fillPostExtra(List<Post> posts, Long currentUserId) {
        if (posts.isEmpty()) return;
        Set<Long> userIds = posts.stream().map(Post::getUserId).collect(Collectors.toSet());
        List<SysUser> users = userMapper.selectBatchIds(userIds);
        var userMap = users.stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));

        // 查当前用户的点赞记录
        Set<Long> likedPostIds = Set.of();
        if (currentUserId != null) {
            List<Like> likes = likeMapper.selectList(
                    new LambdaQueryWrapper<Like>()
                            .eq(Like::getUserId, currentUserId)
                            .eq(Like::getTargetType, 1)
                            .in(Like::getTargetId, posts.stream().map(Post::getId).toList())
            );
            likedPostIds = likes.stream().map(Like::getTargetId).collect(Collectors.toSet());
        }

        for (Post p : posts) {
            p.setAuthorName(userMap.getOrDefault(p.getUserId(), "未知"));
            p.setLiked(currentUserId != null && likedPostIds.contains(p.getId()));
        }
    }
}
