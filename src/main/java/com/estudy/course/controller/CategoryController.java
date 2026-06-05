package com.estudy.course.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.estudy.common.result.Result;
import com.estudy.course.entity.CourseCategory;
import com.estudy.course.mapper.CourseCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程分类控制器
 * <p>
 * 提供课程分类的查询接口，支持树形结构展示。
 * 用于前端渲染课程分类菜单或筛选条件。
 * </p>
 */
@RestController
@RequestMapping("/api/course/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CourseCategoryMapper categoryMapper;

    /**
     * 获取课程分类树形结构
     * <p>
     * 查询所有课程分类，并按照父子关系构建两层树形结构（一级分类 -> 二级分类）。
     * 分类按排序字段升序排列。
     * </p>
     *
     * @return 树形结构的分类列表，只包含一级分类节点，每个节点的 children 字段包含其子分类
     */
    @GetMapping("/tree")
    public Result<List<CourseCategory>> tree() {
        List<CourseCategory> all = categoryMapper.selectList(
                new LambdaQueryWrapper<CourseCategory>().orderByAsc(CourseCategory::getSort)
        );

        // root节点 提取所有一级分类（parentId = 0）
        List<CourseCategory> roots = all.stream()
                .filter(c -> c.getParentId() == 0)
                .toList();

        // 为root节点装配子类 为每个一级分类关联其子分类
        for (CourseCategory root : roots) {
            root.setChildren(all.stream()
                    .filter(c -> c.getParentId().equals(root.getId()))
                    .toList());
        }
        return Result.success(roots);
    }

}
