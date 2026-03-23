package com.netcafe.platform.controller.admin.session;

import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.domain.dto.session.OpenSessionRequest;
import com.netcafe.platform.domain.entity.session.SessionOrder;
import com.netcafe.platform.service.session.SessionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sessions")
public class SessionAdminController {
  private final SessionService sessionService;

  public SessionAdminController(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @PostMapping("/open")
  public ApiResponse<Boolean> open(@Valid @RequestBody OpenSessionRequest request) {
    // TODO: 校验用户余额与机位状态，创建进行中订单
    SessionOrder order = new SessionOrder();
    order.setUserId(request.getUserId());
    order.setMachineId(request.getMachineId());
    order.setStatus(0);
    return ApiResponse.success(sessionService.save(order));
  }

  @PutMapping("/{id}/force-end")
  public ApiResponse<Boolean> forceEnd(@PathVariable Long id) {
    // TODO: 结算与强制结束逻辑
    SessionOrder order = new SessionOrder();
    order.setId(id);
    order.setStatus(2);
    return ApiResponse.success(sessionService.updateById(order));
  }

  @GetMapping("/current")
  public ApiResponse<List<SessionOrder>> current() {
    return ApiResponse.success(sessionService.list());
  }

  @GetMapping("/history")
  public ApiResponse<List<SessionOrder>> history() {
    return ApiResponse.success(sessionService.list());
  }
}
