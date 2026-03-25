package com.netcafe.platform.domain.dto.kiosk;

import com.netcafe.platform.domain.dto.auth.UserProfile;

public class KioskLoginResponse {
  private String token;
  private String tokenType;
  private Long expiresIn;
  private UserProfile user;
  private KioskMachineOverviewView machine;
  private KioskSessionStatusView session;

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

  public UserProfile getUser() {
    return user;
  }

  public void setUser(UserProfile user) {
    this.user = user;
  }

  public KioskMachineOverviewView getMachine() {
    return machine;
  }

  public void setMachine(KioskMachineOverviewView machine) {
    this.machine = machine;
  }

  public KioskSessionStatusView getSession() {
    return session;
  }

  public void setSession(KioskSessionStatusView session) {
    this.session = session;
  }
}
