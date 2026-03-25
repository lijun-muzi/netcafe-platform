package com.netcafe.platform.domain.dto.kiosk;

import java.math.BigDecimal;

public class KioskMachineOverviewView {
  private Long machineId;
  private String machineCode;
  private Integer status;
  private String statusLabel;
  private Boolean available;
  private String availabilityMessage;
  private BigDecimal pricePerMin;
  private String priceLabel;
  private Integer lowBalanceThresholdMinutes;

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

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getStatusLabel() {
    return statusLabel;
  }

  public void setStatusLabel(String statusLabel) {
    this.statusLabel = statusLabel;
  }

  public Boolean getAvailable() {
    return available;
  }

  public void setAvailable(Boolean available) {
    this.available = available;
  }

  public String getAvailabilityMessage() {
    return availabilityMessage;
  }

  public void setAvailabilityMessage(String availabilityMessage) {
    this.availabilityMessage = availabilityMessage;
  }

  public BigDecimal getPricePerMin() {
    return pricePerMin;
  }

  public void setPricePerMin(BigDecimal pricePerMin) {
    this.pricePerMin = pricePerMin;
  }

  public String getPriceLabel() {
    return priceLabel;
  }

  public void setPriceLabel(String priceLabel) {
    this.priceLabel = priceLabel;
  }

  public Integer getLowBalanceThresholdMinutes() {
    return lowBalanceThresholdMinutes;
  }

  public void setLowBalanceThresholdMinutes(Integer lowBalanceThresholdMinutes) {
    this.lowBalanceThresholdMinutes = lowBalanceThresholdMinutes;
  }
}
