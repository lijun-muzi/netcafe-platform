package com.netcafe.platform.domain.dto.auth;

public class AdminLoginResponse {
  private String token;
  private String tokenType;
  private Long expiresIn;
  private String role;
  private AdminProfile admin;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public Long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(Long expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public AdminProfile getAdmin() {
    return admin;
  }

  public void setAdmin(AdminProfile admin) {
    this.admin = admin;
  }
}
