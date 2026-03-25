package com.netcafe.platform.domain.dto.kiosk;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class KioskSessionStatusView {
  private Long sessionId;
  private Long userId;
  private String userName;
  private Long machineId;
  private String machineCode;
  private LocalDateTime startTime;
  private Integer currentDurationMinutes;
  private String currentDurationLabel;
  private Integer billedMinutes;
  private BigDecimal pricePerMin;
  private String priceLabel;
  private BigDecimal currentFee;
  private String currentFeeLabel;
  private BigDecimal billedAmount;
  private String billedAmountLabel;
  private BigDecimal balance;
  private String balanceLabel;
  private Integer remainingMinutes;
  private String remainingMinutesLabel;
  private Integer lowBalanceThresholdMinutes;
  private Boolean lowBalanceWarning;
  private String lowBalanceMessage;
  private Integer status;
  private String statusLabel;
  private Boolean paused;
  private LocalDateTime pausedAt;

  public Long getSessionId() {
    return sessionId;
  }

  public void setSessionId(Long sessionId) {
    this.sessionId = sessionId;
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

  public Integer getCurrentDurationMinutes() {
    return currentDurationMinutes;
  }

  public void setCurrentDurationMinutes(Integer currentDurationMinutes) {
    this.currentDurationMinutes = currentDurationMinutes;
  }

  public String getCurrentDurationLabel() {
    return currentDurationLabel;
  }

  public void setCurrentDurationLabel(String currentDurationLabel) {
    this.currentDurationLabel = currentDurationLabel;
  }

  public Integer getBilledMinutes() {
    return billedMinutes;
  }

  public void setBilledMinutes(Integer billedMinutes) {
    this.billedMinutes = billedMinutes;
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

  public BigDecimal getCurrentFee() {
    return currentFee;
  }

  public void setCurrentFee(BigDecimal currentFee) {
    this.currentFee = currentFee;
  }

  public String getCurrentFeeLabel() {
    return currentFeeLabel;
  }

  public void setCurrentFeeLabel(String currentFeeLabel) {
    this.currentFeeLabel = currentFeeLabel;
  }

  public BigDecimal getBilledAmount() {
    return billedAmount;
  }

  public void setBilledAmount(BigDecimal billedAmount) {
    this.billedAmount = billedAmount;
  }

  public String getBilledAmountLabel() {
    return billedAmountLabel;
  }

  public void setBilledAmountLabel(String billedAmountLabel) {
    this.billedAmountLabel = billedAmountLabel;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public String getBalanceLabel() {
    return balanceLabel;
  }

  public void setBalanceLabel(String balanceLabel) {
    this.balanceLabel = balanceLabel;
  }

  public Integer getRemainingMinutes() {
    return remainingMinutes;
  }

  public void setRemainingMinutes(Integer remainingMinutes) {
    this.remainingMinutes = remainingMinutes;
  }

  public String getRemainingMinutesLabel() {
    return remainingMinutesLabel;
  }

  public void setRemainingMinutesLabel(String remainingMinutesLabel) {
    this.remainingMinutesLabel = remainingMinutesLabel;
  }

  public Integer getLowBalanceThresholdMinutes() {
    return lowBalanceThresholdMinutes;
  }

  public void setLowBalanceThresholdMinutes(Integer lowBalanceThresholdMinutes) {
    this.lowBalanceThresholdMinutes = lowBalanceThresholdMinutes;
  }

  public Boolean getLowBalanceWarning() {
    return lowBalanceWarning;
  }

  public void setLowBalanceWarning(Boolean lowBalanceWarning) {
    this.lowBalanceWarning = lowBalanceWarning;
  }

  public String getLowBalanceMessage() {
    return lowBalanceMessage;
  }

  public void setLowBalanceMessage(String lowBalanceMessage) {
    this.lowBalanceMessage = lowBalanceMessage;
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

  public Boolean getPaused() {
    return paused;
  }

  public void setPaused(Boolean paused) {
    this.paused = paused;
  }

  public LocalDateTime getPausedAt() {
    return pausedAt;
  }

  public void setPausedAt(LocalDateTime pausedAt) {
    this.pausedAt = pausedAt;
  }
}
