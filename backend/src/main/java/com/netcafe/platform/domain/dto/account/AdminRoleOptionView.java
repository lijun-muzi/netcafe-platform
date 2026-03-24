package com.netcafe.platform.domain.dto.account;

public class AdminRoleOptionView {
  private String code;
  private String label;
  private String description;

  public AdminRoleOptionView() {
  }

  public AdminRoleOptionView(String code, String label, String description) {
    this.code = code;
    this.label = label;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
