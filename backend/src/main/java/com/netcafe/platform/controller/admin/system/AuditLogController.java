package com.netcafe.platform.controller.admin.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.audit.AuditActionOptionView;
import com.netcafe.platform.domain.dto.audit.AuditLogListResponse;
import com.netcafe.platform.domain.dto.audit.AuditLogView;
import com.netcafe.platform.domain.entity.account.Admin;
import com.netcafe.platform.domain.entity.system.AuditLog;
import com.netcafe.platform.service.account.AdminService;
import com.netcafe.platform.service.system.AuditLogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit/logs")
public class AuditLogController {
  private final AuditLogService auditLogService;
  private final AdminService adminService;
  private final ObjectMapper objectMapper;

  public AuditLogController(
      AuditLogService auditLogService,
      AdminService adminService,
      ObjectMapper objectMapper
  ) {
    this.auditLogService = auditLogService;
    this.adminService = adminService;
    this.objectMapper = objectMapper;
  }

  @GetMapping
  public ApiResponse<AuditLogListResponse> list(
      @RequestParam(required = false) String operatorKeyword,
      @RequestParam(required = false) String action,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size
  ) {
    validateDateRange(dateFrom, dateTo);
    long pageSize = Math.max(1, Math.min(size, 200));
    Page<AuditLog> pageRequest = new Page<>(page, pageSize);
    LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
    wrapper.le(AuditLog::getCreatedAt, LocalDateTime.now());
    if (StringUtils.hasText(action)) {
      wrapper.eq(AuditLog::getAction, action.trim());
    }
    if (dateFrom != null) {
      wrapper.ge(AuditLog::getCreatedAt, dateFrom.atStartOfDay());
    }
    if (dateTo != null) {
      wrapper.lt(AuditLog::getCreatedAt, dateTo.plusDays(1).atStartOfDay());
    }
    if (!applyOperatorFilter(operatorKeyword, wrapper)) {
      return ApiResponse.success(new AuditLogListResponse(0, page, pageSize, List.of()));
    }
    wrapper.orderByDesc(AuditLog::getCreatedAt);
    Page<AuditLog> result = auditLogService.page(pageRequest, wrapper);
    List<Long> operatorIds = result.getRecords().stream()
        .map(AuditLog::getOperatorId)
        .distinct()
        .collect(Collectors.toList());
    Map<Long, Admin> adminMap = operatorIds.isEmpty()
        ? Map.of()
        : adminService.listByIds(operatorIds).stream()
            .collect(Collectors.toMap(Admin::getId, admin -> admin, (left, right) -> right));
    List<AuditLogView> items = result.getRecords().stream()
        .map(log -> toView(log, adminMap.get(log.getOperatorId())))
        .collect(Collectors.toList());
    return ApiResponse.success(new AuditLogListResponse(result.getTotal(), result.getCurrent(), result.getSize(), items));
  }

  @GetMapping("/action-options")
  public ApiResponse<List<AuditActionOptionView>> actionOptions() {
    return ApiResponse.success(List.of(
        new AuditActionOptionView(AuditLogService.ACTION_UPDATE_SYSTEM_CONFIG, AuditLogService.ACTION_UPDATE_SYSTEM_CONFIG),
        new AuditActionOptionView(AuditLogService.ACTION_CREATE_MACHINE_TEMPLATE, AuditLogService.ACTION_CREATE_MACHINE_TEMPLATE),
        new AuditActionOptionView(AuditLogService.ACTION_UPDATE_MACHINE_TEMPLATE, AuditLogService.ACTION_UPDATE_MACHINE_TEMPLATE),
        new AuditActionOptionView(AuditLogService.ACTION_DELETE_MACHINE_TEMPLATE, AuditLogService.ACTION_DELETE_MACHINE_TEMPLATE),
        new AuditActionOptionView(AuditLogService.ACTION_UPDATE_MACHINE_PRICE, AuditLogService.ACTION_UPDATE_MACHINE_PRICE),
        new AuditActionOptionView(AuditLogService.ACTION_BATCH_CREATE_MACHINE, AuditLogService.ACTION_BATCH_CREATE_MACHINE),
        new AuditActionOptionView(AuditLogService.ACTION_OPEN_SESSION, AuditLogService.ACTION_OPEN_SESSION),
        new AuditActionOptionView(AuditLogService.ACTION_FORCE_END_SESSION, AuditLogService.ACTION_FORCE_END_SESSION),
        new AuditActionOptionView(AuditLogService.ACTION_RECHARGE, AuditLogService.ACTION_RECHARGE)
    ));
  }

  private boolean applyOperatorFilter(String operatorKeyword, LambdaQueryWrapper<AuditLog> wrapper) {
    if (!StringUtils.hasText(operatorKeyword)) {
      return true;
    }
    List<Long> operatorIds = adminService.list(new LambdaQueryWrapper<Admin>()
            .likeRight(Admin::getName, operatorKeyword.trim())
            .orderByDesc(Admin::getId))
        .stream()
        .map(Admin::getId)
        .collect(Collectors.toList());
    if (operatorIds.isEmpty()) {
      return false;
    }
    wrapper.in(AuditLog::getOperatorId, operatorIds);
    return true;
  }

  private AuditLogView toView(AuditLog log, Admin admin) {
    Map<String, Object> beforeMap = readJsonMap(log.getBeforeData());
    Map<String, Object> afterMap = readJsonMap(log.getAfterData());
    AuditLogView view = new AuditLogView();
    view.setId(log.getId());
    view.setOperatorId(log.getOperatorId());
    view.setOperatorName(admin == null ? null : admin.getName());
    view.setOperatorRole(log.getOperatorRole());
    view.setOperatorRoleLabel(resolveRoleLabel(log.getOperatorRole()));
    view.setAction(log.getAction());
    view.setActionLabel(log.getAction());
    view.setTargetType(log.getTargetType());
    view.setTargetId(log.getTargetId());
    view.setTargetLabel(resolveTargetLabel(log, beforeMap, afterMap));
    view.setChangeSummary(resolveChangeSummary(beforeMap, afterMap));
    view.setBeforeData(log.getBeforeData());
    view.setAfterData(log.getAfterData());
    view.setCreatedAt(log.getCreatedAt());
    return view;
  }

  private Map<String, Object> readJsonMap(String json) {
    if (!StringUtils.hasText(json)) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<>() {
      });
    } catch (Exception ex) {
      return Map.of("raw", json);
    }
  }

  private String resolveTargetLabel(AuditLog log, Map<String, Object> beforeMap, Map<String, Object> afterMap) {
    Object targetLabel = afterMap.get("targetLabel");
    if (targetLabel == null) {
      targetLabel = beforeMap.get("targetLabel");
    }
    if (targetLabel != null && StringUtils.hasText(String.valueOf(targetLabel))) {
      return String.valueOf(targetLabel);
    }
    return log.getTargetType() + "#" + log.getTargetId();
  }

  private String resolveChangeSummary(Map<String, Object> beforeMap, Map<String, Object> afterMap) {
    Object summary = afterMap.get("changeSummary");
    if (summary == null) {
      summary = beforeMap.get("changeSummary");
    }
    if (summary != null && StringUtils.hasText(String.valueOf(summary))) {
      return String.valueOf(summary);
    }
    Object beforeValue = beforeMap.get("rawValue");
    Object afterValue = afterMap.get("rawValue");
    if (beforeValue != null || afterValue != null) {
      return String.valueOf(beforeValue) + " → " + String.valueOf(afterValue);
    }
    return "";
  }

  private String resolveRoleLabel(String role) {
    if (Objects.equals(role, "SUPER_ADMIN")) {
      return "超级管理员";
    }
    if (Objects.equals(role, "ADMIN")) {
      return "管理员";
    }
    return role;
  }

  private void validateDateRange(LocalDate dateFrom, LocalDate dateTo) {
    if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "dateFrom不能晚于dateTo");
    }
  }
}
