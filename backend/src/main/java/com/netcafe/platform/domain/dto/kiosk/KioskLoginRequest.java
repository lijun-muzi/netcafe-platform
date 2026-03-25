package com.netcafe.platform.domain.dto.kiosk;

import jakarta.validation.constraints.NotBlank;

public class KioskLoginRequest {
  @NotBlank(message = "machineCode不能为空")
  private String machineCode;

  @NotBlank(message = "idCard不能为空")
  private String idCard;

  public String getMachineCode() {
    return machineCode;
  }

  public void setMachineCode(String machineCode) {
    this.machineCode = machineCode;
  }

  public String getIdCard() {
    return idCard;
  }

  public void setIdCard(String idCard) {
    this.idCard = idCard;
  }
}
