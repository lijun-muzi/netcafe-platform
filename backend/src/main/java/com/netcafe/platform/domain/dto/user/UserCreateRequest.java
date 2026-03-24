package com.netcafe.platform.domain.dto.user;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class UserCreateRequest {
  @NotBlank
  @Size(max = 20)
  private String mobile;

  @NotBlank
  @Size(max = 20)
  private String idCard;

  @NotBlank
  @Size(max = 50)
  private String name;

  @DecimalMin("0.00")
  private BigDecimal balance;

  @Min(0)
  @Max(1)
  private Integer status;

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getIdCard() {
    return idCard;
  }

  public void setIdCard(String idCard) {
    this.idCard = idCard;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
}
