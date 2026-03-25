package com.netcafe.platform.controller.admin.stats;

import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.domain.dto.stats.StatsIdleMachineView;
import com.netcafe.platform.domain.dto.stats.StatsLowBalanceUserView;
import com.netcafe.platform.domain.dto.stats.StatsMachineTopView;
import com.netcafe.platform.domain.dto.stats.StatsMachineUsageView;
import com.netcafe.platform.domain.dto.stats.StatsOverviewView;
import com.netcafe.platform.domain.dto.stats.StatsTrendResponse;
import com.netcafe.platform.domain.dto.stats.StatsUserTopView;
import com.netcafe.platform.domain.dto.stats.ranking.StatsRankingResponse;
import com.netcafe.platform.service.finance.StatsService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
public class StatsController {
  private final StatsService statsService;

  public StatsController(StatsService statsService) {
    this.statsService = statsService;
  }

  @GetMapping("/overview")
  public ApiResponse<StatsOverviewView> overview(
      @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
      @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
  ) {
    return ApiResponse.success(statsService.overview(start, end));
  }

  @GetMapping("/trend")
  public ApiResponse<StatsTrendResponse> trend(
      @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
      @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
      @RequestParam(name = "granularity", required = false, defaultValue = "day") String granularity
  ) {
    return ApiResponse.success(statsService.trend(start, end, granularity));
  }

  @GetMapping("/machine-usage")
  public ApiResponse<List<StatsMachineUsageView>> machineUsage(
      @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
      @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
      @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit
  ) {
    return ApiResponse.success(statsService.machineUsage(start, end, limit));
  }

  @GetMapping("/machine-top")
  public ApiResponse<List<StatsMachineTopView>> machineTop(
      @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
      @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
      @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit
  ) {
    return ApiResponse.success(statsService.machineTop(start, end, limit));
  }

  @GetMapping("/idle-machines")
  public ApiResponse<List<StatsIdleMachineView>> idleMachines(
      @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
      @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
      @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit
  ) {
    return ApiResponse.success(statsService.idleMachines(start, end, limit));
  }

  @GetMapping("/user-top")
  public ApiResponse<List<StatsUserTopView>> userTop(
      @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
      @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
      @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit
  ) {
    return ApiResponse.success(statsService.userTop(start, end, limit));
  }

  @GetMapping("/low-balance")
  public ApiResponse<List<StatsLowBalanceUserView>> lowBalance(
      @RequestParam(name = "threshold", required = false) Integer thresholdMinutes,
      @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit
  ) {
    return ApiResponse.success(statsService.lowBalance(thresholdMinutes, limit));
  }

  @GetMapping("/rankings")
  public ApiResponse<StatsRankingResponse> rankings(
      @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
      @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
      @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit,
      @RequestParam(name = "threshold", required = false) Integer thresholdMinutes
  ) {
    return ApiResponse.success(statsService.rankings(start, end, limit, thresholdMinutes));
  }
}
