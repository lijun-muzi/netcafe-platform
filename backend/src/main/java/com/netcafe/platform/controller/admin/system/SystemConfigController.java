package com.netcafe.platform.controller.admin.system;

import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.system.SystemBasicSettingsUpdateRequest;
import com.netcafe.platform.domain.dto.system.SystemBasicSettingsView;
import com.netcafe.platform.service.system.AuditLogService;
import com.netcafe.platform.service.system.SystemConfigService;
import jakarta.validation.Valid;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/config")
public class SystemConfigController {
  private final SystemConfigService systemConfigService;
  private final AuditLogService auditLogService;

  public SystemConfigController(SystemConfigService systemConfigService, AuditLogService auditLogService) {
    this.systemConfigService = systemConfigService;
    this.auditLogService = auditLogService;
  }

  @GetMapping
  public ApiResponse<SystemBasicSettingsView> getBasicSettings() {
    return ApiResponse.success(systemConfigService.getBasicSettings());
  }

  @PutMapping
  public ApiResponse<Boolean> update(@Valid @RequestBody SystemBasicSettingsUpdateRequest request) {
    SystemBasicSettingsView before = systemConfigService.getBasicSettings();
    boolean updated = systemConfigService.updateBasicSettings(request);
    SystemBasicSettingsView after = systemConfigService.getBasicSettings();
    auditLogService.record(
        requireCurrentAdminId(),
        resolveCurrentAdminRole(),
        AuditLogService.ACTION_UPDATE_SYSTEM_CONFIG,
        "SYSTEM_CONFIG",
        1L,
        buildSnapshot(before, "基础配置"),
        buildSnapshot(after, "基础配置")
    );
    return ApiResponse.success(updated);
  }

  private Map<String, Object> buildSnapshot(SystemBasicSettingsView settings, String targetLabel) {
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("targetLabel", targetLabel);
    snapshot.put("defaultPricePerMin", settings.getDefaultPricePerMin());
    snapshot.put("lowBalanceThresholdMinutes", settings.getLowBalanceThresholdMinutes());
    snapshot.put(
        "changeSummary",
        "默认单价 "
            + settings.getDefaultPricePerMin().setScale(2, RoundingMode.HALF_UP)
            + " 元/分钟，提醒阈值 "
            + settings.getLowBalanceThresholdMinutes()
            + " 分钟"
    );
    return snapshot;
  }

  private Long requireCurrentAdminId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getDetails() == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, "无法识别当前管理员");
    }
    try {
      return Long.valueOf(String.valueOf(authentication.getDetails()));
    } catch (NumberFormatException ex) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, "无法识别当前管理员");
    }
  }

  private String resolveCurrentAdminRole() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, "无法识别当前管理员");
    }
    return authentication.getAuthorities().stream()
        .map(authority -> authority.getAuthority())
        .filter(role -> role.startsWith("ROLE_"))
        .map(role -> role.substring(5))
        .findFirst()
        .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "无法识别当前管理员"));
  }
}
