package com.netcafe.platform.controller.admin.finance;

import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.domain.dto.finance.RechargeRequest;
import com.netcafe.platform.domain.entity.finance.RechargeRecord;
import com.netcafe.platform.service.finance.RechargeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recharges")
public class RechargeController {
  private final RechargeService rechargeService;

  public RechargeController(RechargeService rechargeService) {
    this.rechargeService = rechargeService;
  }

  @GetMapping
  public ApiResponse<List<RechargeRecord>> list() {
    return ApiResponse.success(rechargeService.list());
  }

  @PostMapping
  public ApiResponse<Boolean> recharge(@Valid @RequestBody RechargeRequest request) {
    // TODO: 更新用户余额并记录充值流水
    RechargeRecord record = new RechargeRecord();
    record.setUserId(request.getUserId());
    record.setAmount(request.getAmount());
    record.setChannel(request.getChannel());
    record.setRemark(request.getRemark());
    return ApiResponse.success(rechargeService.save(record));
  }
}
