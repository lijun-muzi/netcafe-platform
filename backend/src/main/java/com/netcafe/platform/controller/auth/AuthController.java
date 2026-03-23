package com.netcafe.platform.controller.auth;

import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.domain.dto.auth.AdminLoginResponse;
import com.netcafe.platform.domain.dto.auth.AdminLoginRequest;
import com.netcafe.platform.domain.dto.auth.UserLoginResponse;
import com.netcafe.platform.domain.dto.auth.UserLoginRequest;
import com.netcafe.platform.service.account.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
    return ApiResponse.success(authService.loginAdmin(request.getUsername(), request.getPassword()));
  }

  @PostMapping("/user-login")
  public ApiResponse<UserLoginResponse> userLogin(@Valid @RequestBody UserLoginRequest request) {
    return ApiResponse.success(authService.loginUser(request.getIdCard()));
  }

  @PostMapping("/logout")
  public ApiResponse<Boolean> logout() {
    authService.logout();
    return ApiResponse.success(true);
  }
}
