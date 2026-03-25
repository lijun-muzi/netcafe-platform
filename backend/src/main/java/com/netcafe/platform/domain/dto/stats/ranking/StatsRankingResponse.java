package com.netcafe.platform.domain.dto.stats.ranking;

import com.netcafe.platform.domain.dto.stats.StatsLowBalanceUserView;
import com.netcafe.platform.domain.dto.stats.StatsMachineTopView;
import com.netcafe.platform.domain.dto.stats.StatsUserTopView;
import java.util.List;

public class StatsRankingResponse {
  private List<StatsMachineTopView> machineRevenueTop;
  private List<StatsUserTopView> userConsumeTop;
  private List<StatsLowBalanceUserView> lowBalanceUsers;

  public List<StatsMachineTopView> getMachineRevenueTop() {
    return machineRevenueTop;
  }

  public void setMachineRevenueTop(List<StatsMachineTopView> machineRevenueTop) {
    this.machineRevenueTop = machineRevenueTop;
  }

  public List<StatsUserTopView> getUserConsumeTop() {
    return userConsumeTop;
  }

  public void setUserConsumeTop(List<StatsUserTopView> userConsumeTop) {
    this.userConsumeTop = userConsumeTop;
  }

  public List<StatsLowBalanceUserView> getLowBalanceUsers() {
    return lowBalanceUsers;
  }

  public void setLowBalanceUsers(List<StatsLowBalanceUserView> lowBalanceUsers) {
    this.lowBalanceUsers = lowBalanceUsers;
  }
}
