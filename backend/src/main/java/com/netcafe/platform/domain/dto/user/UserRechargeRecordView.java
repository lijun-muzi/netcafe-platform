package com.netcafe.platform.domain.dto.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserRechargeRecordView {
  private Long id;
  private Long userId;
  private BigDecimal amount;
  private String channel;
  private String remark;
  private Long operatorAdminId;
  private LocalDateTime createdAt;

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

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public Long getOperatorAdminId() {
    return operatorAdminId;
  }

  public void setOperatorAdminId(Long operatorAdminId) {
    this.operatorAdminId = operatorAdminId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
