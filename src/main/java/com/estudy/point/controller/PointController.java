package com.estudy.point.controller;

import com.estudy.common.result.Result;
import com.estudy.point.dto.RedeemDTO;
import com.estudy.point.dto.TipDTO;
import com.estudy.point.entity.PointAccount;
import com.estudy.point.entity.PointExchange;
import com.estudy.point.entity.PointGoods;
import com.estudy.point.entity.PointLog;
import com.estudy.point.service.PointService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    // ============ 积分账户 ============

    /** 查询我的积分 */
    @GetMapping("/account")
    public Result<PointAccount> myAccount(HttpServletRequest request) {
        return Result.success(pointService.getOrCreateAccount(getUserId(request)));
    }

    /** 年度统计 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> yearStats(
            @RequestParam(required = false) Integer year,
            HttpServletRequest request) {
        return Result.success(pointService.getYearStats(getUserId(request), year));
    }

    /** 积分明细 */
    @GetMapping("/logs")
    public Result<List<PointLog>> logs(
            @RequestParam(required = false) Integer year,
            HttpServletRequest request) {
        return Result.success(pointService.getLogs(getUserId(request), year));
    }

    // ============ 打赏 ============

    /** 打赏讲师 */
    @PostMapping("/tip")
    public Result<Void> tip(@RequestBody @Valid TipDTO dto, HttpServletRequest request) {
        pointService.tip(getUserId(request), dto);
        return Result.success();
    }

    // ============ 积分商城 ============

    /** 商品列表 */
    @GetMapping("/goods")
    public Result<List<PointGoods>> goodsList() {
        return Result.success(pointService.getGoodsList());
    }

    /** 兑换 */
    @PostMapping("/redeem")
    public Result<Void> redeem(@RequestBody @Valid RedeemDTO dto, HttpServletRequest request) {
        pointService.redeem(getUserId(request), dto);
        return Result.success();
    }

    /** 我的兑换记录 */
    @GetMapping("/exchange/my")
    public Result<List<PointExchange>> myExchanges(HttpServletRequest request) {
        return Result.success(pointService.getMyExchanges(getUserId(request)));
    }
}
