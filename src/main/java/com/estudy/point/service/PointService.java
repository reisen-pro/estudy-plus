package com.estudy.point.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.estudy.common.exception.BusinessException;
import com.estudy.course.entity.Course;
import com.estudy.course.entity.CourseLesson;
import com.estudy.course.mapper.CourseLessonMapper;
import com.estudy.course.mapper.CourseMapper;
import com.estudy.point.dto.RedeemDTO;
import com.estudy.point.dto.TipDTO;
import com.estudy.point.entity.*;
import com.estudy.point.mapper.*;
import com.estudy.user.entity.SysUser;
import com.estudy.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointAccountMapper accountMapper;
    private final PointLogMapper logMapper;
    private final PointGoodsMapper goodsMapper;
    private final PointExchangeMapper exchangeMapper;
    private final CourseMapper courseMapper;
    private final CourseLessonMapper lessonMapper;
    private final SysUserMapper userMapper;

    /** 积分规则：每10分钟 = 1积分 */
    private static final BigDecimal POINTS_PER_600_SECONDS = BigDecimal.ONE;
    private static final int SECONDS_PER_UNIT = 600; // 10分钟

    // ============ 积分账户 ============

    /** 获取或创建用户积分账户 */
    @Transactional
    public PointAccount getOrCreateAccount(Long userId) {
        PointAccount account = accountMapper.selectOne(
                new LambdaQueryWrapper<PointAccount>().eq(PointAccount::getUserId, userId)
        );
        if (account == null) {
            int year = LocalDateTime.now().getYear();
            account = new PointAccount();
            account.setUserId(userId);
            account.setBalance(BigDecimal.ZERO);
            account.setTotalEarned(BigDecimal.ZERO);
            account.setYearYear(year);
            account.setYearEarned(BigDecimal.ZERO);
            account.setYearSpent(BigDecimal.ZERO);
            accountMapper.insert(account);
        }
        // 年度检查：跨年自动清零年度统计
        checkYearReset(account);
        return account;
    }

    /** 年度重置检查 */
    @Transactional
    public void checkYearReset(PointAccount account) {
        int currentYear = LocalDateTime.now().getYear();
        if (account.getYearYear() < currentYear) {
            // 跨年了，年度积分清零，余额也清零
            BigDecimal oldBalance = account.getBalance();
            account.setYearYear(currentYear);
            account.setYearEarned(BigDecimal.ZERO);
            account.setYearSpent(BigDecimal.ZERO);
            if (oldBalance.compareTo(BigDecimal.ZERO) > 0) {
                // 记录年度清零日志
                addLog(account.getUserId(), "year_reset", oldBalance.negate(), BigDecimal.ZERO,
                        null, null, null, "年度积分清零，清零前余额:" + oldBalance, currentYear - 1);
            }
            account.setBalance(BigDecimal.ZERO);
            accountMapper.updateById(account);
        }
    }

    // ============ 学习积分 ============

    /**
     * 计算学习积分并发放
     * 规则：每10分钟1积分，看完给全量
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param lessonId 课时ID
     * @param learnedSeconds 本次学习秒数
     * @return 本次获得积分
     */
    @Transactional
    public BigDecimal earnLearnPoints(Long userId, Long courseId, Long lessonId, int learnedSeconds) {
        if (learnedSeconds <= 0) return BigDecimal.ZERO;

        // 计算积分：每600秒1积分，四舍五入到0.1
        BigDecimal points = BigDecimal.valueOf(learnedSeconds)
                .divide(BigDecimal.valueOf(SECONDS_PER_UNIT), 1, RoundingMode.HALF_UP);

        if (points.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        // 获取课时信息
        CourseLesson lesson = lessonMapper.selectById(lessonId);
        String lessonTitle = lesson != null ? lesson.getTitle() : "未知课时";

        // 加积分
        PointAccount account = getOrCreateAccount(userId);
        account.setBalance(account.getBalance().add(points));
        account.setTotalEarned(account.getTotalEarned().add(points));
        account.setYearEarned(account.getYearEarned().add(points));
        accountMapper.updateById(account);

        addLog(userId, "earn_learn", points, account.getBalance(), courseId, lessonId, null,
                "学习课时「" + lessonTitle + "」" + formatDuration(learnedSeconds), account.getYearYear());

        return points;
    }

    /**
     * 完成课程全部课时，发放剩余积分
     * 全量积分 = 课程所有课时总时长 / 600秒
     * 已发放的learn积分在progress上报时已计算，完成时发差值
     */
    @Transactional
    public BigDecimal earnCompletePoints(Long userId, Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) return BigDecimal.ZERO;

        // 查课程所有课时的总时长
        List<CourseLesson> lessons = lessonMapper.selectList(
                new LambdaQueryWrapper<CourseLesson>().eq(CourseLesson::getCourseId, courseId)
        );
        int totalDuration = lessons.stream().mapToInt(l -> l.getDuration() != null ? l.getDuration() : 0).sum();

        // 全量积分
        BigDecimal fullPoints = BigDecimal.valueOf(totalDuration)
                .divide(BigDecimal.valueOf(SECONDS_PER_UNIT), 1, RoundingMode.HALF_UP);

        if (fullPoints.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        // 检查是否已经发过完成奖励（避免重复）
        boolean alreadyCompleted = logMapper.exists(
                new LambdaQueryWrapper<PointLog>()
                        .eq(PointLog::getUserId, userId)
                        .eq(PointLog::getCourseId, courseId)
                        .eq(PointLog::getType, "earn_complete")
        );
        if (alreadyCompleted) return BigDecimal.ZERO;

        // 完成奖励 = 全量积分的10%（额外奖励）
        BigDecimal bonus = fullPoints.multiply(BigDecimal.valueOf(0.1)).setScale(1, RoundingMode.HALF_UP);
        if (bonus.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        PointAccount account = getOrCreateAccount(userId);
        account.setBalance(account.getBalance().add(bonus));
        account.setTotalEarned(account.getTotalEarned().add(bonus));
        account.setYearEarned(account.getYearEarned().add(bonus));
        accountMapper.updateById(account);

        addLog(userId, "earn_complete", bonus, account.getBalance(), courseId, null, null,
                "完成课程「" + course.getTitle() + "」奖励", account.getYearYear());

        return bonus;
    }

    // ============ 打赏 ============

    @Transactional
    public void tip(Long fromUserId, TipDTO dto) {
        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("打赏积分必须大于0");
        }

        // 查课程，获取讲师
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) throw new BusinessException("课程不存在");

        Long teacherId = course.getTeacherId();
        if (teacherId == null) {
            throw new BusinessException("该课程无讲师，无法打赏");
        }
        if (teacherId.equals(fromUserId)) {
            throw new BusinessException("不能打赏自己");
        }

        // 扣减打赏人积分
        PointAccount fromAccount = getOrCreateAccount(fromUserId);
        if (fromAccount.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new BusinessException("积分不足，当前余额:" + fromAccount.getBalance());
        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(dto.getAmount()));
        fromAccount.setYearSpent(fromAccount.getYearSpent().add(dto.getAmount()));
        accountMapper.updateById(fromAccount);

        SysUser teacher = userMapper.selectById(teacherId);
        String teacherName = teacher != null ? teacher.getRealName() : "讲师";

        addLog(fromUserId, "tip_out", dto.getAmount().negate(), fromAccount.getBalance(),
                dto.getCourseId(), null, teacherId,
                "打赏课程「" + course.getTitle() + "」讲师" + teacherName, fromAccount.getYearYear());

        // 增加讲师积分
        PointAccount toAccount = getOrCreateAccount(teacherId);
        toAccount.setBalance(toAccount.getBalance().add(dto.getAmount()));
        toAccount.setTotalEarned(toAccount.getTotalEarned().add(dto.getAmount()));
        toAccount.setYearEarned(toAccount.getYearEarned().add(dto.getAmount()));
        accountMapper.updateById(toAccount);

        SysUser fromUser = userMapper.selectById(fromUserId);
        String fromName = fromUser != null ? fromUser.getRealName() : "学员";

        addLog(teacherId, "tip_in", dto.getAmount(), toAccount.getBalance(),
                dto.getCourseId(), null, fromUserId,
                "收到" + fromName + "打赏", toAccount.getYearYear());
    }

    // ============ 兑换 ============

    @Transactional
    public void redeem(Long userId, RedeemDTO dto) {
        PointGoods goods = goodsMapper.selectById(dto.getGoodsId());
        if (goods == null || goods.getStatus() != 1) throw new BusinessException("商品不存在或已下架");
        if (goods.getStock() != -1 && goods.getStock() <= 0) throw new BusinessException("库存不足");

        PointAccount account = getOrCreateAccount(userId);
        if (account.getBalance().compareTo(goods.getPointsRequired()) < 0) {
            throw new BusinessException("积分不足，需要" + goods.getPointsRequired() + "积分");
        }

        // 扣积分
        account.setBalance(account.getBalance().subtract(goods.getPointsRequired()));
        account.setYearSpent(account.getYearSpent().add(goods.getPointsRequired()));
        accountMapper.updateById(account);

        // 扣库存
        if (goods.getStock() != -1) {
            goods.setStock(goods.getStock() - 1);
            goodsMapper.updateById(goods);
        }

        // 兑换记录
        PointExchange exchange = new PointExchange();
        exchange.setUserId(userId);
        exchange.setGoodsId(goods.getId());
        exchange.setPointsCost(goods.getPointsRequired());
        exchange.setStatus(0);
        exchangeMapper.insert(exchange);

        addLog(userId, "redeem", goods.getPointsRequired().negate(), account.getBalance(),
                null, null, null, "兑换「" + goods.getName() + "」", account.getYearYear());
    }

    // ============ 查询 ============

    /** 积分明细 */
    public List<PointLog> getLogs(Long userId, Integer year) {
        LambdaQueryWrapper<PointLog> qw = new LambdaQueryWrapper<PointLog>()
                .eq(PointLog::getUserId, userId)
                .orderByDesc(PointLog::getCreateTime);
        if (year != null) qw.eq(PointLog::getYearYear, year);
        return logMapper.selectList(qw);
    }

    /** 积分年度统计 */
    public Map<String, Object> getYearStats(Long userId, Integer year) {
        if (year == null) year = LocalDateTime.now().getYear();
        PointAccount account = getOrCreateAccount(userId);

        // 强制用请求的年份
        BigDecimal yearEarned = account.getYearYear() == year ? account.getYearEarned() : BigDecimal.ZERO;
        BigDecimal yearSpent = account.getYearYear() == year ? account.getYearSpent() : BigDecimal.ZERO;

        return Map.of(
                "year", year,
                "yearEarned", yearEarned,
                "yearSpent", yearSpent,
                "balance", account.getBalance(),
                "totalEarned", account.getTotalEarned()
        );
    }

    /** 积分商品列表 */
    public List<PointGoods> getGoodsList() {
        return goodsMapper.selectList(
                new LambdaQueryWrapper<PointGoods>().eq(PointGoods::getStatus, 1)
        );
    }

    /** 我的兑换记录 */
    public List<PointExchange> getMyExchanges(Long userId) {
        return exchangeMapper.selectList(
                new LambdaQueryWrapper<PointExchange>()
                        .eq(PointExchange::getUserId, userId)
                        .orderByDesc(PointExchange::getCreateTime)
        );
    }

    // ============ 内部方法 ============

    private void addLog(Long userId, String type, BigDecimal amount, BigDecimal balanceAfter,
                        Long courseId, Long lessonId, Long targetUserId, String description, Integer year) {
        PointLog plog = new PointLog();
        plog.setUserId(userId);
        plog.setType(type);
        plog.setAmount(amount);
        plog.setBalanceAfter(balanceAfter);
        plog.setCourseId(courseId);
        plog.setLessonId(lessonId);
        plog.setTargetUserId(targetUserId);
        plog.setDescription(description);
        plog.setYearYear(year);
        logMapper.insert(plog);
    }

    private String formatDuration(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return m > 0 ? m + "分" + (s > 0 ? s + "秒" : "") : s + "秒";
    }
}
