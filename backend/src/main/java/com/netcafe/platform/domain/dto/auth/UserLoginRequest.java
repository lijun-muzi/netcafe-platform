package com.netcafe.platform.domain.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class UserLoginRequest {
  @NotBlank
  private String idCard;

  public String getIdCard() {
    return idCard;
  }

  public void setIdCard(String idCard) {
    this.idCard = idCard;
  }
}
