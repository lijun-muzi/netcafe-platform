package com.netcafe.platform.domain.dto.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserView {
  private Long id;
  private String name;
  private String mobile;
  private String mobileMasked;
  private String idCard;
  private String idCardMasked;
  private BigDecimal balance;
  private Integer status;
  private String statusLabel;
  private LocalDateTime registerTime;
  private LocalDateTime lastSessionTime;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getMobileMasked() {
    return mobileMasked;
  }

  public void setMobileMasked(String mobileMasked) {
    this.mobileMasked = mobileMasked;
  }

  public String getIdCard() {
    return idCard;
  }

  public void setIdCard(String idCard) {
    this.idCard = idCard;
  }

  public String getIdCardMasked() {
    return idCardMasked;
  }

  public void setIdCardMasked(String idCardMasked) {
    this.idCardMasked = idCardMasked;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
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

  public LocalDateTime getRegisterTime() {
    return registerTime;
  }

  public void setRegisterTime(LocalDateTime registerTime) {
    this.registerTime = registerTime;
  }

  public LocalDateTime getLastSessionTime() {
    return lastSessionTime;
  }

  public void setLastSessionTime(LocalDateTime lastSessionTime) {
    this.lastSessionTime = lastSessionTime;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
