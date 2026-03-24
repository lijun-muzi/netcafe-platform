package com.netcafe.platform.domain.dto.machine;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MachineUpdateRequest {
  private String code;
  private Integer status;
  @DecimalMin(value = "0.0001", inclusive = true)
  private BigDecimal pricePerMin;
  private String configJson;
  private Long templateId;
  private LocalDateTime lastMaintainedAt;

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

  public BigDecimal getPricePerMin() {
    return pricePerMin;
  }

  public void setPricePerMin(BigDecimal pricePerMin) {
    this.pricePerMin = pricePerMin;
  }

  public String getConfigJson() {
    return configJson;
  }

  public void setConfigJson(String configJson) {
    this.configJson = configJson;
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
}
