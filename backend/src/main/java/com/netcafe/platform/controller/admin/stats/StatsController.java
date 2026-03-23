package com.netcafe.platform.controller.admin.stats;

import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.service.finance.StatsService;
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
  public ApiResponse<Object> overview(@RequestParam String start, @RequestParam String end) {
    return ApiResponse.success(statsService.overview(start, end));
  }
}
