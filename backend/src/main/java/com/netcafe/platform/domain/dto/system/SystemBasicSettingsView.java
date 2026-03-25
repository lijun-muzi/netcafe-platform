package com.netcafe.platform.domain.dto.system;

import java.math.BigDecimal;

public class SystemBasicSettingsView {
  private BigDecimal defaultPricePerMin;
  private Integer lowBalanceThresholdMinutes;

  public BigDecimal getDefaultPricePerMin() {
    return defaultPricePerMin;
  }

  public void setDefaultPricePerMin(BigDecimal defaultPricePerMin) {
    this.defaultPricePerMin = defaultPricePerMin;
  }

  public Integer getLowBalanceThresholdMinutes() {
    return lowBalanceThresholdMinutes;
  }

  public void setLowBalanceThresholdMinutes(Integer lowBalanceThresholdMinutes) {
    this.lowBalanceThresholdMinutes = lowBalanceThresholdMinutes;
  }
}
