package com.netcafe.platform.domain.dto.audit;

import java.time.LocalDateTime;

public class AuditLogView {
  private Long id;
  private Long operatorId;
  private String operatorName;
  private String operatorRole;
  private String operatorRoleLabel;
  private String action;
  private String actionLabel;
  private String targetType;
  private Long targetId;
  private String targetLabel;
  private String changeSummary;
  private String beforeData;
  private String afterData;
  private LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(Long operatorId) {
    this.operatorId = operatorId;
  }

  public String getOperatorName() {
    return operatorName;
  }

  public void setOperatorName(String operatorName) {
    this.operatorName = operatorName;
  }

  public String getOperatorRole() {
    return operatorRole;
  }

  public void setOperatorRole(String operatorRole) {
    this.operatorRole = operatorRole;
  }

  public String getOperatorRoleLabel() {
    return operatorRoleLabel;
  }

  public void setOperatorRoleLabel(String operatorRoleLabel) {
    this.operatorRoleLabel = operatorRoleLabel;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getActionLabel() {
    return actionLabel;
  }

  public void setActionLabel(String actionLabel) {
    this.actionLabel = actionLabel;
  }

  public String getTargetType() {
    return targetType;
  }

  public void setTargetType(String targetType) {
    this.targetType = targetType;
  }

  public Long getTargetId() {
    return targetId;
  }

  public void setTargetId(Long targetId) {
    this.targetId = targetId;
  }

  public String getTargetLabel() {
    return targetLabel;
  }

  public void setTargetLabel(String targetLabel) {
    this.targetLabel = targetLabel;
  }

  public String getChangeSummary() {
    return changeSummary;
  }

  public void setChangeSummary(String changeSummary) {
    this.changeSummary = changeSummary;
  }

  public String getBeforeData() {
    return beforeData;
  }

  public void setBeforeData(String beforeData) {
    this.beforeData = beforeData;
  }

  public String getAfterData() {
    return afterData;
  }

  public void setAfterData(String afterData) {
    this.afterData = afterData;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
