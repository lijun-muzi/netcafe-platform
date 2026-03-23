package com.netcafe.platform.controller.admin.system;

import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.domain.entity.system.SystemConfig;
import com.netcafe.platform.service.system.SystemConfigService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/config")
public class SystemConfigController {
  private final SystemConfigService systemConfigService;

  public SystemConfigController(SystemConfigService systemConfigService) {
    this.systemConfigService = systemConfigService;
  }

  @GetMapping
  public ApiResponse<List<SystemConfig>> list() {
    return ApiResponse.success(systemConfigService.list());
  }

  @PutMapping
  public ApiResponse<Boolean> update(@RequestBody List<SystemConfig> configs) {
    return ApiResponse.success(systemConfigService.updateBatchById(configs));
  }
}
