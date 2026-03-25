package com.netcafe.platform.domain.dto.stats;

import java.math.BigDecimal;

public class StatsLowBalanceUserView {
  private Long userId;
  private String userName;
  private String mobileMasked;
  private BigDecimal balance;
  private String balanceLabel;
  private Integer remainingMinutes;
  private String remainingMinutesLabel;

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

  public String getMobileMasked() {
    return mobileMasked;
  }

  public void setMobileMasked(String mobileMasked) {
    this.mobileMasked = mobileMasked;
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
}
