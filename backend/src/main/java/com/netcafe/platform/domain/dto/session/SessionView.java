package com.netcafe.platform.domain.dto.session;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SessionView {
  private Long id;
  private Long userId;
  private String userName;
  private String userMobile;
  private BigDecimal userBalance;
  private Long machineId;
  private String machineCode;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer durationMinutes;
  private Integer billedMinutes;
  private BigDecimal priceSnapshot;
  private BigDecimal currentFee;
  private BigDecimal amount;
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

  public String getUserMobile() {
    return userMobile;
  }

  public void setUserMobile(String userMobile) {
    this.userMobile = userMobile;
  }

  public BigDecimal getUserBalance() {
    return userBalance;
  }

  public void setUserBalance(BigDecimal userBalance) {
    this.userBalance = userBalance;
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

  public Integer getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(Integer durationMinutes) {
    this.durationMinutes = durationMinutes;
  }

  public Integer getBilledMinutes() {
    return billedMinutes;
  }

  public void setBilledMinutes(Integer billedMinutes) {
    this.billedMinutes = billedMinutes;
  }

  public BigDecimal getPriceSnapshot() {
    return priceSnapshot;
  }

  public void setPriceSnapshot(BigDecimal priceSnapshot) {
    this.priceSnapshot = priceSnapshot;
  }

  public BigDecimal getCurrentFee() {
    return currentFee;
  }

  public void setCurrentFee(BigDecimal currentFee) {
    this.currentFee = currentFee;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
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
