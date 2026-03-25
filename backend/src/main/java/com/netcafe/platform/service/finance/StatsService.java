package com.netcafe.platform.service.finance;

import com.netcafe.platform.domain.dto.stats.StatsIdleMachineView;
import com.netcafe.platform.domain.dto.stats.StatsLowBalanceUserView;
import com.netcafe.platform.domain.dto.stats.StatsMachineTopView;
import com.netcafe.platform.domain.dto.stats.StatsMachineUsageView;
import com.netcafe.platform.domain.dto.stats.StatsOverviewView;
import com.netcafe.platform.domain.dto.stats.StatsTrendResponse;
import com.netcafe.platform.domain.dto.stats.StatsUserTopView;
import com.netcafe.platform.domain.dto.stats.ranking.StatsRankingResponse;
import java.time.LocalDate;
import java.util.List;

public interface StatsService {
  StatsOverviewView overview(LocalDate start, LocalDate end);

  StatsTrendResponse trend(LocalDate start, LocalDate end, String granularity);

  List<StatsMachineUsageView> machineUsage(LocalDate start, LocalDate end, Integer limit);

  List<StatsMachineTopView> machineTop(LocalDate start, LocalDate end, Integer limit);

  List<StatsIdleMachineView> idleMachines(LocalDate start, LocalDate end, Integer limit);

  List<StatsUserTopView> userTop(LocalDate start, LocalDate end, Integer limit);

  List<StatsLowBalanceUserView> lowBalance(Integer thresholdMinutes, Integer limit);

  StatsRankingResponse rankings(LocalDate start, LocalDate end, Integer limit, Integer thresholdMinutes);
}
