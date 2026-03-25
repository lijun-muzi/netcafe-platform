package com.netcafe.platform.domain.dto.stats;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StatsOverviewView {
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal cashFlowAmount;
  private String cashFlowLabel;
  private BigDecimal sessionRevenueAmount;
  private String sessionRevenueLabel;
  private Integer totalDurationMinutes;
  private String totalDurationLabel;
  private Integer activeUsers;
  private String activeUsersLabel;
  private BigDecimal arpu;
  private String arpuLabel;
  private String peakHour;

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
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

  public Integer getTotalDurationMinutes() {
    return totalDurationMinutes;
  }

  public void setTotalDurationMinutes(Integer totalDurationMinutes) {
    this.totalDurationMinutes = totalDurationMinutes;
  }

  public String getTotalDurationLabel() {
    return totalDurationLabel;
  }

  public void setTotalDurationLabel(String totalDurationLabel) {
    this.totalDurationLabel = totalDurationLabel;
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

  public BigDecimal getArpu() {
    return arpu;
  }

  public void setArpu(BigDecimal arpu) {
    this.arpu = arpu;
  }

  public String getArpuLabel() {
    return arpuLabel;
  }

  public void setArpuLabel(String arpuLabel) {
    this.arpuLabel = arpuLabel;
  }

  public String getPeakHour() {
    return peakHour;
  }

  public void setPeakHour(String peakHour) {
    this.peakHour = peakHour;
  }
}
