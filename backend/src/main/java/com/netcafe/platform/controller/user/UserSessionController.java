package com.netcafe.platform.controller.user;

import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.service.session.SessionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sessions")
public class UserSessionController {
  private final SessionService sessionService;

  public UserSessionController(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @PostMapping("/user-end")
  public ApiResponse<Boolean> userEnd() {
    return ApiResponse.success(sessionService.userEnd(requireCurrentUserId()) != null);
  }

  private Long requireCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getDetails() == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
    }
    try {
      return Long.valueOf(String.valueOf(authentication.getDetails()));
    } catch (NumberFormatException ex) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
    }
  }
}
