package com.netcafe.platform.service.account;

import com.netcafe.platform.domain.dto.auth.AdminLoginResponse;
import com.netcafe.platform.domain.dto.auth.UserLoginResponse;

public interface AuthService {
  AdminLoginResponse loginAdmin(String username, String password);

  UserLoginResponse loginUser(String idCard);

  void logout();
}
