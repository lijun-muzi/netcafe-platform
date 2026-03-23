package com.netcafe.platform.domain.dto.session;

import jakarta.validation.constraints.NotNull;

public class OpenSessionRequest {
  @NotNull
  private Long userId;
  @NotNull
  private Long machineId;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getMachineId() {
    return machineId;
  }

  public void setMachineId(Long machineId) {
    this.machineId = machineId;
  }
}
