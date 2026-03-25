package com.netcafe.platform.domain.dto.stats;

import java.math.BigDecimal;

public class StatsTrendPointView {
  private String label;
  private BigDecimal cashFlowAmount;
  private String cashFlowLabel;
  private BigDecimal sessionRevenueAmount;
  private String sessionRevenueLabel;
  private Integer durationMinutes;
  private String durationLabel;
  private Integer activeUsers;
  private String activeUsersLabel;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public BigDecimal getCashFlowAmount() {
    return cashFlowAmount;
  }

  public void setCashFlowAmount(BigDecimal cashFlowAmount) {
    this.cashFlowAmount = cashFlowAmount;
  }

  public String getCashFlowLabel() {
    return cashFlowLabel;
  }

  public void setCashFlowLabel(String cashFlowLabel) {
    this.cashFlowLabel = cashFlowLabel;
  }

  public BigDecimal getSessionRevenueAmount() {
    return sessionRevenueAmount;
  }

  public void setSessionRevenueAmount(BigDecimal sessionRevenueAmount) {
    this.sessionRevenueAmount = sessionRevenueAmount;
  }

  public String getSessionRevenueLabel() {
    return sessionRevenueLabel;
  }

  public void setSessionRevenueLabel(String sessionRevenueLabel) {
    this.sessionRevenueLabel = sessionRevenueLabel;
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

  public Integer getActiveUsers() {
    return activeUsers;
  }

  public void setActiveUsers(Integer activeUsers) {
    this.activeUsers = activeUsers;
  }

  public String getActiveUsersLabel() {
    return activeUsersLabel;
  }

  public void setActiveUsersLabel(String activeUsersLabel) {
    this.activeUsersLabel = activeUsersLabel;
  }
}
