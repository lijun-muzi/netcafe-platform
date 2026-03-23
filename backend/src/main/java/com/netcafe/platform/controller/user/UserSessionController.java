package com.netcafe.platform.controller.user;

import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.domain.entity.session.SessionOrder;
import com.netcafe.platform.service.session.SessionService;
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
    // TODO: 基于当前登录用户与进行中订单结算
    SessionOrder order = new SessionOrder();
    order.setStatus(1);
    return ApiResponse.success(sessionService.updateById(order));
  }
}
