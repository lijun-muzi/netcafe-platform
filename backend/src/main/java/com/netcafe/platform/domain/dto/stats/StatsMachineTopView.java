package com.netcafe.platform.domain.dto.stats;

import java.math.BigDecimal;

public class StatsMachineTopView {
  private Long machineId;
  private String machineCode;
  private BigDecimal revenueAmount;
  private String revenueLabel;
  private Integer sessionCount;

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

  public Integer getSessionCount() {
    return sessionCount;
  }

  public void setSessionCount(Integer sessionCount) {
    this.sessionCount = sessionCount;
  }
}
