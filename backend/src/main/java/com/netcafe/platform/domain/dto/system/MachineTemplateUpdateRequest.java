package com.netcafe.platform.domain.dto.system;

import jakarta.validation.constraints.NotBlank;

public class MachineTemplateUpdateRequest {
  @NotBlank(message = "name不能为空")
  private String name;

  @NotBlank(message = "configJson不能为空")
  private String configJson;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getConfigJson() {
    return configJson;
  }

  public void setConfigJson(String configJson) {
    this.configJson = configJson;
  }
}
