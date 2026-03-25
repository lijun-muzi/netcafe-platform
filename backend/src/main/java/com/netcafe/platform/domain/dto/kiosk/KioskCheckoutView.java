package com.netcafe.platform.domain.dto.kiosk;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class KioskCheckoutView {
  private Long sessionId;
  private Long userId;
  private String userName;
  private String machineCode;
  private Integer status;
  private String statusLabel;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer durationMinutes;
  private String durationLabel;
  private BigDecimal pricePerMin;
  private String priceLabel;
  private BigDecimal totalAmount;
  private String totalAmountLabel;
  private BigDecimal settlementAmount;
  private String settlementAmountLabel;
  private BigDecimal balanceBefore;
  private String balanceBeforeLabel;
  private BigDecimal balanceAfter;
  private String balanceAfterLabel;

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

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public String getTotalAmountLabel() {
    return totalAmountLabel;
  }

  public void setTotalAmountLabel(String totalAmountLabel) {
    this.totalAmountLabel = totalAmountLabel;
  }

  public BigDecimal getSettlementAmount() {
    return settlementAmount;
  }

  public void setSettlementAmount(BigDecimal settlementAmount) {
    this.settlementAmount = settlementAmount;
  }

  public String getSettlementAmountLabel() {
    return settlementAmountLabel;
  }

  public void setSettlementAmountLabel(String settlementAmountLabel) {
    this.settlementAmountLabel = settlementAmountLabel;
  }

  public BigDecimal getBalanceBefore() {
    return balanceBefore;
  }

  public void setBalanceBefore(BigDecimal balanceBefore) {
    this.balanceBefore = balanceBefore;
  }

  public String getBalanceBeforeLabel() {
    return balanceBeforeLabel;
  }

  public void setBalanceBeforeLabel(String balanceBeforeLabel) {
    this.balanceBeforeLabel = balanceBeforeLabel;
  }

  public BigDecimal getBalanceAfter() {
    return balanceAfter;
  }

  public void setBalanceAfter(BigDecimal balanceAfter) {
    this.balanceAfter = balanceAfter;
  }

  public String getBalanceAfterLabel() {
    return balanceAfterLabel;
  }

  public void setBalanceAfterLabel(String balanceAfterLabel) {
    this.balanceAfterLabel = balanceAfterLabel;
  }
}
