package com.netcafe.platform.domain.dto.finance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RechargeView {
  private Long id;
  private Long userId;
  private String userName;
  private BigDecimal amount;
  private String amountLabel;
  private String channel;
  private Long operatorAdminId;
  private String operatorName;
  private LocalDateTime createdAt;
  private String remark;

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

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public Long getOperatorAdminId() {
    return operatorAdminId;
  }

  public void setOperatorAdminId(Long operatorAdminId) {
    this.operatorAdminId = operatorAdminId;
  }

  public String getOperatorName() {
    return operatorName;
  }

  public void setOperatorName(String operatorName) {
    this.operatorName = operatorName;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }
}
