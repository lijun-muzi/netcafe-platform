package com.netcafe.platform.domain.dto.finance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SessionOrderView {
  private Long id;
  private Long userId;
  private String userName;
  private Long machineId;
  private String machineCode;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String timeRangeLabel;
  private Integer durationMinutes;
  private String durationLabel;
  private BigDecimal priceSnapshot;
  private String priceLabel;
  private BigDecimal amount;
  private String amountLabel;
  private Integer status;
  private String statusLabel;
  private String statusTone;
  private Long forceByAdminId;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

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

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  public String getTimeRangeLabel() {
    return timeRangeLabel;
  }

  public void setTimeRangeLabel(String timeRangeLabel) {
    this.timeRangeLabel = timeRangeLabel;
  }

  public Integer getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(Integer durationMinutes) {
    this.durationMinutes = durationMinutes;
  }

  public String getDurationLabel() {
    return durationLabel;
  }

  public void setDurationLabel(String durationLabel) {
    this.durationLabel = durationLabel;
  }

  public BigDecimal getPriceSnapshot() {
    return priceSnapshot;
  }

  public void setPriceSnapshot(BigDecimal priceSnapshot) {
    this.priceSnapshot = priceSnapshot;
  }

  public String getPriceLabel() {
    return priceLabel;
  }

  public void setPriceLabel(String priceLabel) {
    this.priceLabel = priceLabel;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getAmountLabel() {
    return amountLabel;
  }

  public void setAmountLabel(String amountLabel) {
    this.amountLabel = amountLabel;
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

  public String getStatusTone() {
    return statusTone;
  }

  public void setStatusTone(String statusTone) {
    this.statusTone = statusTone;
  }

  public Long getForceByAdminId() {
    return forceByAdminId;
  }

  public void setForceByAdminId(Long forceByAdminId) {
    this.forceByAdminId = forceByAdminId;
  }
}
