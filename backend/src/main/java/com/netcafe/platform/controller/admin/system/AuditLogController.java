package com.netcafe.platform.controller.admin.system;

import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.domain.entity.system.AuditLog;
import com.netcafe.platform.service.system.AuditLogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit/logs")
public class AuditLogController {
  private final AuditLogService auditLogService;

  public AuditLogController(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  @GetMapping
  public ApiResponse<List<AuditLog>> list() {
    return ApiResponse.success(auditLogService.list());
  }
}
