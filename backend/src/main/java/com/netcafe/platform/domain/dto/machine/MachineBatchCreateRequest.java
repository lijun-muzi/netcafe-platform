package com.netcafe.platform.domain.dto.machine;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class MachineBatchCreateRequest {
  @NotNull
  private Long templateId;
  @NotBlank
  private String codePrefix;
  @NotNull
  @Min(1)
  private Integer startNo;
  @NotNull
  @Min(1)
  @Max(100)
  private Integer count;
  @Min(1)
  @Max(6)
  private Integer codeWidth;
  @DecimalMin(value = "0.0001", inclusive = true)
  private BigDecimal pricePerMin;

  public Long getTemplateId() {
    return templateId;
  }

  public void setTemplateId(Long templateId) {
    this.templateId = templateId;
  }

  public String getCodePrefix() {
    return codePrefix;
  }

  public void setCodePrefix(String codePrefix) {
    this.codePrefix = codePrefix;
  }

  public Integer getStartNo() {
    return startNo;
  }

  public void setStartNo(Integer startNo) {
    this.startNo = startNo;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public Integer getCodeWidth() {
    return codeWidth;
  }

  public void setCodeWidth(Integer codeWidth) {
    this.codeWidth = codeWidth;
  }

  public BigDecimal getPricePerMin() {
    return pricePerMin;
  }

  public void setPricePerMin(BigDecimal pricePerMin) {
    this.pricePerMin = pricePerMin;
  }
}
