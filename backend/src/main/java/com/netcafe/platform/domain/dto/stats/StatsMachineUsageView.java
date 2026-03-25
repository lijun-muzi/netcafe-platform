package com.netcafe.platform.domain.dto.stats;

import java.math.BigDecimal;

public class StatsMachineUsageView {
  private Long machineId;
  private String machineCode;
  private Integer usageMinutes;
  private String usageMinutesLabel;
  private BigDecimal usageRate;
  private String usageRateLabel;
  private BigDecimal revenueAmount;
  private String revenueLabel;

  public Long getMachineId() {
    return machineId;
  }

  public void setMachineId(Long machineId) {
    this.machineId = machineId;
  }

  public String getMachineCode() {
    return machineCode;
  }

  public void setMachineCode(String machineCode) {
    this.machineCode = machineCode;
  }

  public Integer getUsageMinutes() {
    return usageMinutes;
  }

  public void setUsageMinutes(Integer usageMinutes) {
    this.usageMinutes = usageMinutes;
  }

  public String getUsageMinutesLabel() {
    return usageMinutesLabel;
  }

  public void setUsageMinutesLabel(String usageMinutesLabel) {
    this.usageMinutesLabel = usageMinutesLabel;
  }

  public BigDecimal getUsageRate() {
    return usageRate;
  }

  public void setUsageRate(BigDecimal usageRate) {
    this.usageRate = usageRate;
  }

  public String getUsageRateLabel() {
    return usageRateLabel;
  }

  public void setUsageRateLabel(String usageRateLabel) {
    this.usageRateLabel = usageRateLabel;
  }

  public BigDecimal getRevenueAmount() {
    return revenueAmount;
  }

  public void setRevenueAmount(BigDecimal revenueAmount) {
    this.revenueAmount = revenueAmount;
  }

  public String getRevenueLabel() {
    return revenueLabel;
  }

  public void setRevenueLabel(String revenueLabel) {
    this.revenueLabel = revenueLabel;
  }
}
