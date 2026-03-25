package com.netcafe.platform.domain.dto.system;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SystemBasicSettingsUpdateRequest {
  @NotNull(message = "defaultPricePerMin不能为空")
  @DecimalMin(value = "0.0001", message = "defaultPricePerMin必须大于0")
  private BigDecimal defaultPricePerMin;

  @NotNull(message = "lowBalanceThresholdMinutes不能为空")
  @Min(value = 1, message = "lowBalanceThresholdMinutes必须大于0")
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
