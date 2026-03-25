package com.netcafe.platform.domain.dto.stats;

import java.math.BigDecimal;

public class StatsIdleMachineView {
  private Long machineId;
  private String machineCode;
  private Integer usageMinutes;
  private String usageMinutesLabel;
  private Integer idleMinutes;
  private String idleMinutesLabel;
  private BigDecimal idleRate;
  private String idleRateLabel;

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

  public Integer getIdleMinutes() {
    return idleMinutes;
  }

  public void setIdleMinutes(Integer idleMinutes) {
    this.idleMinutes = idleMinutes;
  }

  public String getIdleMinutesLabel() {
    return idleMinutesLabel;
  }

  public void setIdleMinutesLabel(String idleMinutesLabel) {
    this.idleMinutesLabel = idleMinutesLabel;
  }

  public BigDecimal getIdleRate() {
    return idleRate;
  }

  public void setIdleRate(BigDecimal idleRate) {
    this.idleRate = idleRate;
  }

  public String getIdleRateLabel() {
    return idleRateLabel;
  }

  public void setIdleRateLabel(String idleRateLabel) {
    this.idleRateLabel = idleRateLabel;
  }
}
