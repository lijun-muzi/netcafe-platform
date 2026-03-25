package com.netcafe.platform.domain.dto.stats;

import java.math.BigDecimal;

public class StatsUserTopView {
  private Long userId;
  private String userName;
  private String mobileMasked;
  private BigDecimal totalAmount;
  private String totalAmountLabel;
  private Integer totalDurationMinutes;
  private String totalDurationLabel;

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
}
