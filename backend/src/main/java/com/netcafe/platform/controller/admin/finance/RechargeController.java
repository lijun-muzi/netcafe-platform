package com.netcafe.platform.controller.admin.finance;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.finance.RechargeChannelOptionView;
import com.netcafe.platform.domain.dto.finance.RechargeListResponse;
import com.netcafe.platform.domain.dto.finance.RechargeRequest;
import com.netcafe.platform.domain.dto.finance.RechargeView;
import com.netcafe.platform.domain.entity.account.Admin;
import com.netcafe.platform.domain.entity.account.User;
import com.netcafe.platform.domain.entity.finance.RechargeRecord;
import com.netcafe.platform.service.account.AdminService;
import com.netcafe.platform.service.account.UserService;
import com.netcafe.platform.service.finance.RechargeService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.netcafe.platform.service.system.AuditLogService;

@RestController
@RequestMapping("/recharges")
public class RechargeController {
  private final RechargeService rechargeService;
  private final UserService userService;
  private final AdminService adminService;
  private final AuditLogService auditLogService;

  public RechargeController(
      RechargeService rechargeService,
      UserService userService,
      AdminService adminService,
      AuditLogService auditLogService
  ) {
    this.rechargeService = rechargeService;
    this.userService = userService;
    this.adminService = adminService;
    this.auditLogService = auditLogService;
  }

  @GetMapping
  public ApiResponse<RechargeListResponse> list(
      @RequestParam(required = false) String channel,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size
  ) {
    validateDateRange(dateFrom, dateTo);
    long pageSize = Math.max(1, Math.min(size, 200));
    Page<RechargeRecord> pageRequest = new Page<>(page, pageSize);
    LambdaQueryWrapper<RechargeRecord> wrapper = new LambdaQueryWrapper<>();
    wrapper.le(RechargeRecord::getCreatedAt, LocalDateTime.now());
    if (StringUtils.hasText(channel)) {
      wrapper.eq(RechargeRecord::getChannel, channel.trim());
    }
    if (dateFrom != null) {
      wrapper.ge(RechargeRecord::getCreatedAt, dateFrom.atStartOfDay());
    }
    if (dateTo != null) {
      wrapper.lt(RechargeRecord::getCreatedAt, dateTo.plusDays(1).atStartOfDay());
    }
    wrapper.orderByDesc(RechargeRecord::getCreatedAt);
    Page<RechargeRecord> result = rechargeService.page(pageRequest, wrapper);
    Map<Long, User> userMap = result.getRecords().stream()
        .map(RechargeRecord::getUserId)
        .distinct()
        .collect(Collectors.collectingAndThen(Collectors.toList(), this::getUserMap));
    Map<Long, Admin> adminMap = result.getRecords().stream()
        .map(RechargeRecord::getOperatorAdminId)
        .filter(id -> id != null && id > 0)
        .distinct()
        .collect(Collectors.collectingAndThen(Collectors.toList(), this::getAdminMap));
    List<RechargeView> items = result.getRecords().stream()
        .map(record -> toView(record, userMap.get(record.getUserId()), adminMap.get(record.getOperatorAdminId())))
        .collect(Collectors.toList());
    return ApiResponse.success(new RechargeListResponse(result.getTotal(), result.getCurrent(), result.getSize(), items));
  }

  @GetMapping("/channel-options")
  public ApiResponse<List<RechargeChannelOptionView>> channelOptions() {
    return ApiResponse.success(List.of(
        new RechargeChannelOptionView("现金", "现金"),
        new RechargeChannelOptionView("微信", "微信"),
        new RechargeChannelOptionView("支付宝", "支付宝"),
        new RechargeChannelOptionView("其他", "其他")
    ));
  }

  @PostMapping
  public ApiResponse<Boolean> recharge(@Valid @RequestBody RechargeRequest request) {
    User user = requireUser(request.getUserId());
    Long operatorAdminId = requireCurrentAdminId();
    RechargeRecord record = userService.recharge(
        request.getUserId(),
        request.getAmount(),
        request.getChannel().trim(),
        StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null,
        operatorAdminId
    );
    if (record == null) {
      return ApiResponse.success(false);
    }
    auditLogService.record(
        operatorAdminId,
        resolveCurrentAdminRole(),
        AuditLogService.ACTION_RECHARGE,
        "USER",
        user.getId(),
        null,
        buildRechargeSnapshot(user, record)
    );
    return ApiResponse.success(true);
  }

  private Map<Long, User> getUserMap(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return Map.of();
    }
    return userService.listByIds(userIds).stream()
        .collect(Collectors.toMap(User::getId, user -> user, (left, right) -> right));
  }

  private Map<Long, Admin> getAdminMap(List<Long> adminIds) {
    if (adminIds == null || adminIds.isEmpty()) {
      return Map.of();
    }
    return adminService.listByIds(adminIds).stream()
        .collect(Collectors.toMap(Admin::getId, admin -> admin, (left, right) -> right));
  }

  private RechargeView toView(RechargeRecord record, User user, Admin admin) {
    RechargeView view = new RechargeView();
    view.setId(record.getId());
    view.setUserId(record.getUserId());
    view.setUserName(user == null ? null : user.getName());
    view.setAmount(record.getAmount() == null ? BigDecimal.ZERO : record.getAmount());
    view.setAmountLabel("¥" + view.getAmount().setScale(2, RoundingMode.HALF_UP));
    view.setChannel(record.getChannel());
    view.setOperatorAdminId(record.getOperatorAdminId());
    view.setOperatorName(admin == null ? null : admin.getName());
    view.setCreatedAt(record.getCreatedAt());
    view.setRemark(record.getRemark());
    return view;
  }

  private void validateDateRange(LocalDate dateFrom, LocalDate dateTo) {
    if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "dateFrom不能晚于dateTo");
    }
  }

  private User requireUser(Long id) {
    User user = userService.getById(id);
    if (user == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
    }
    return user;
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

  private Map<String, Object> buildRechargeSnapshot(User user, RechargeRecord record) {
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("targetLabel", "用户 " + user.getName());
    snapshot.put("userName", user.getName());
    snapshot.put("amount", record.getAmount());
    snapshot.put("channel", record.getChannel());
    snapshot.put("remark", record.getRemark());
    snapshot.put(
        "changeSummary",
        "为用户 " + user.getName() + " 充值 " + record.getAmount().setScale(2, RoundingMode.HALF_UP) + " 元，渠道 " + record.getChannel()
    );
    return snapshot;
  }
}
