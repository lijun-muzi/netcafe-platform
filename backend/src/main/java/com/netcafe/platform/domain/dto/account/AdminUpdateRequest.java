package com.netcafe.platform.domain.dto.account;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminUpdateRequest {
  @Size(max = 100)
  private String password;

  @Size(max = 50)
  private String name;

  @Pattern(regexp = "SUPER_ADMIN|ADMIN")
  private String role;

  @Min(0)
  @Max(1)
  private Integer status;

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }
}
