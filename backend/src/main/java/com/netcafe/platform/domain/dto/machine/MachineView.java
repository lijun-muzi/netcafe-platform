package com.netcafe.platform.domain.dto.machine;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MachineView {
  private Long id;
  private String code;
  private Integer status;
  private String statusLabel;
  private String statusTone;
  private BigDecimal pricePerMin;
  private String priceLabel;
  private String configJson;
  private String configSummary;
  private Long templateId;
  private LocalDateTime lastMaintainedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Long currentSessionId;
  private Long currentUserId;
  private String currentUserName;
  private LocalDateTime currentStartTime;
  private Integer currentDurationMinutes;
  private BigDecimal currentFee;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
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

  public String getConfigJson() {
    return configJson;
  }

  public void setConfigJson(String configJson) {
    this.configJson = configJson;
  }

  public String getConfigSummary() {
    return configSummary;
  }

  public void setConfigSummary(String configSummary) {
    this.configSummary = configSummary;
  }

  public Long getTemplateId() {
    return templateId;
  }

  public void setTemplateId(Long templateId) {
    this.templateId = templateId;
  }

  public LocalDateTime getLastMaintainedAt() {
    return lastMaintainedAt;
  }

  public void setLastMaintainedAt(LocalDateTime lastMaintainedAt) {
    this.lastMaintainedAt = lastMaintainedAt;
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

  public Long getCurrentSessionId() {
    return currentSessionId;
  }

  public void setCurrentSessionId(Long currentSessionId) {
    this.currentSessionId = currentSessionId;
  }

  public Long getCurrentUserId() {
    return currentUserId;
  }

  public void setCurrentUserId(Long currentUserId) {
    this.currentUserId = currentUserId;
  }

  public String getCurrentUserName() {
    return currentUserName;
  }

  public void setCurrentUserName(String currentUserName) {
    this.currentUserName = currentUserName;
  }

  public LocalDateTime getCurrentStartTime() {
    return currentStartTime;
  }

  public void setCurrentStartTime(LocalDateTime currentStartTime) {
    this.currentStartTime = currentStartTime;
  }

  public Integer getCurrentDurationMinutes() {
    return currentDurationMinutes;
  }

  public void setCurrentDurationMinutes(Integer currentDurationMinutes) {
    this.currentDurationMinutes = currentDurationMinutes;
  }

  public BigDecimal getCurrentFee() {
    return currentFee;
  }

  public void setCurrentFee(BigDecimal currentFee) {
    this.currentFee = currentFee;
  }
}
