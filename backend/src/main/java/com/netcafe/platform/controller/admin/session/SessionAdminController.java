package com.netcafe.platform.controller.admin.session;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.session.OpenSessionRequest;
import com.netcafe.platform.domain.dto.session.SessionListResponse;
import com.netcafe.platform.domain.dto.session.SessionView;
import com.netcafe.platform.domain.entity.account.User;
import com.netcafe.platform.domain.entity.machine.Machine;
import com.netcafe.platform.domain.entity.session.SessionOrder;
import com.netcafe.platform.service.account.UserService;
import com.netcafe.platform.service.machine.MachineService;
import com.netcafe.platform.service.session.SessionBillingCalculator;
import com.netcafe.platform.service.session.SessionService;
import com.netcafe.platform.service.system.AuditLogService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sessions")
public class SessionAdminController {
  private static final int SESSION_ONGOING = 0;
  private static final int SESSION_FINISHED = 1;
  private static final int SESSION_FORCED = 2;
  private static final int SESSION_PAUSED = 3;

  private final SessionService sessionService;
  private final UserService userService;
  private final MachineService machineService;
  private final AuditLogService auditLogService;

  public SessionAdminController(
      SessionService sessionService,
      UserService userService,
      MachineService machineService,
      AuditLogService auditLogService
  ) {
    this.sessionService = sessionService;
    this.userService = userService;
    this.machineService = machineService;
    this.auditLogService = auditLogService;
  }

  @PostMapping("/open")
  public ApiResponse<Boolean> open(@Valid @RequestBody OpenSessionRequest request) {
    Long sessionId = sessionService.openSession(request.getUserId(), request.getMachineId());
    if (sessionId == null) {
      return ApiResponse.success(false);
    }
    SessionOrder order = sessionService.getById(sessionId);
    User user = userService.getById(request.getUserId());
    Machine machine = machineService.getById(request.getMachineId());
    auditLogService.record(
        requireCurrentAdminId(),
        resolveCurrentAdminRole(),
        AuditLogService.ACTION_OPEN_SESSION,
        "SESSION",
        sessionId,
        null,
        buildOpenSnapshot(order, user, machine)
    );
    return ApiResponse.success(sessionId != null);
  }

  @PutMapping("/{id}/force-end")
  public ApiResponse<Boolean> forceEnd(@PathVariable Long id) {
    SessionOrder before = requireSessionOrder(id);
    User beforeUser = userService.getById(before.getUserId());
    Machine beforeMachine = machineService.getById(before.getMachineId());
    Long operatorAdminId = requireCurrentAdminId();
    boolean ended = sessionService.forceEnd(id, operatorAdminId);
    SessionOrder after = requireSessionOrder(id);
    User afterUser = userService.getById(after.getUserId());
    Machine afterMachine = machineService.getById(after.getMachineId());
    auditLogService.record(
        operatorAdminId,
        resolveCurrentAdminRole(),
        AuditLogService.ACTION_FORCE_END_SESSION,
        "SESSION",
        id,
        buildSessionSnapshot(before, beforeUser, beforeMachine),
        buildSessionSnapshot(after, afterUser, afterMachine)
    );
    return ApiResponse.success(ended);
  }

  @GetMapping("/current")
  public ApiResponse<SessionListResponse> current(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size
  ) {
    long pageSize = Math.max(1, Math.min(size, 200));
    Page<SessionOrder> pageRequest = new Page<>(page, pageSize);
    LambdaQueryWrapper<SessionOrder> wrapper = new LambdaQueryWrapper<>();
    wrapper.in(SessionOrder::getStatus, List.of(SESSION_ONGOING, SESSION_PAUSED));
    if (!applyKeywordFilter(keyword, wrapper)) {
      return ApiResponse.success(new SessionListResponse(0, page, pageSize, List.of()));
    }
    wrapper.orderByDesc(SessionOrder::getStartTime);
    Page<SessionOrder> result = sessionService.page(pageRequest, wrapper);
    return ApiResponse.success(toListResponse(result, true));
  }

  @GetMapping("/history")
  public ApiResponse<SessionListResponse> history(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size
  ) {
    validateHistoryStatus(status);
    validateDateRange(dateFrom, dateTo);
    long pageSize = Math.max(1, Math.min(size, 200));
    Page<SessionOrder> pageRequest = new Page<>(page, pageSize);
    LambdaQueryWrapper<SessionOrder> wrapper = new LambdaQueryWrapper<>();
    if (status == null) {
      wrapper.in(SessionOrder::getStatus, List.of(SESSION_FINISHED, SESSION_FORCED));
    } else {
      wrapper.eq(SessionOrder::getStatus, status);
    }
    if (dateFrom != null) {
      wrapper.ge(SessionOrder::getStartTime, dateFrom.atStartOfDay());
    }
    if (dateTo != null) {
      wrapper.lt(SessionOrder::getStartTime, dateTo.plusDays(1).atStartOfDay());
    }
    if (!applyKeywordFilter(keyword, wrapper)) {
      return ApiResponse.success(new SessionListResponse(0, page, pageSize, List.of()));
    }
    wrapper.orderByDesc(SessionOrder::getStartTime);
    Page<SessionOrder> result = sessionService.page(pageRequest, wrapper);
    return ApiResponse.success(toListResponse(result, false));
  }

  private SessionListResponse toListResponse(Page<SessionOrder> result, boolean ongoingMode) {
    List<Long> userIds = result.getRecords().stream()
        .map(SessionOrder::getUserId)
        .distinct()
        .collect(Collectors.toList());
    List<Long> machineIds = result.getRecords().stream()
        .map(SessionOrder::getMachineId)
        .distinct()
        .collect(Collectors.toList());
    Map<Long, User> users = userIds.isEmpty()
        ? new HashMap<>()
        : userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, user -> user));
    Map<Long, Machine> machines = machineIds.isEmpty()
        ? new HashMap<>()
        : machineService.listByIds(machineIds).stream().collect(Collectors.toMap(Machine::getId, machine -> machine));
    List<SessionView> items = result.getRecords().stream()
        .map(order -> toView(order, users.get(order.getUserId()), machines.get(order.getMachineId()), ongoingMode))
        .collect(Collectors.toList());
    return new SessionListResponse(result.getTotal(), result.getCurrent(), result.getSize(), items);
  }

  private SessionView toView(SessionOrder order, User user, Machine machine, boolean ongoingMode) {
    SessionView view = new SessionView();
    view.setId(order.getId());
    view.setUserId(order.getUserId());
    view.setUserName(user == null ? null : user.getName());
    view.setUserMobile(user == null ? null : user.getMobile());
    view.setUserBalance(user == null ? BigDecimal.ZERO : defaultValue(user.getBalance()));
    view.setMachineId(order.getMachineId());
    view.setMachineCode(machine == null ? null : machine.getCode());
    view.setStartTime(order.getStartTime());
    view.setEndTime(order.getEndTime());
    view.setDurationMinutes(resolveDurationMinutes(order, ongoingMode));
    view.setBilledMinutes(order.getBilledMinutes());
    view.setPriceSnapshot(order.getPriceSnapshot());
    view.setCurrentFee(resolveCurrentFee(order, view.getDurationMinutes(), ongoingMode));
    view.setAmount(defaultValue(order.getAmount()));
    view.setStatus(order.getStatus());
    view.setStatusLabel(resolveStatusLabel(order.getStatus()));
    view.setStatusTone(resolveStatusTone(order.getStatus()));
    view.setForceByAdminId(order.getForceByAdminId());
    return view;
  }

  private boolean applyKeywordFilter(String keyword, LambdaQueryWrapper<SessionOrder> wrapper) {
    if (!StringUtils.hasText(keyword)) {
      return true;
    }
    List<Long> userIds = resolveUserIds(keyword.trim());
    List<Long> machineIds = resolveMachineIds(keyword.trim());
    if (userIds.isEmpty() && machineIds.isEmpty()) {
      return false;
    }
    wrapper.and(query -> {
      boolean appended = false;
      if (!userIds.isEmpty()) {
        query.in(SessionOrder::getUserId, userIds);
        appended = true;
      }
      if (!machineIds.isEmpty()) {
        if (appended) {
          query.or();
        }
        query.in(SessionOrder::getMachineId, machineIds);
      }
    });
    return true;
  }

  private List<Long> resolveUserIds(String keyword) {
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    if (keyword.chars().allMatch(Character::isDigit)) {
      wrapper.and(query -> query
          .likeRight(User::getMobile, keyword)
          .or()
          .likeRight(User::getIdCard, keyword));
    } else {
      wrapper.likeRight(User::getName, keyword);
    }
    wrapper.orderByDesc(User::getId);
    return userService.list(wrapper).stream()
        .map(User::getId)
        .collect(Collectors.toList());
  }

  private List<Long> resolveMachineIds(String keyword) {
    return machineService.list(new LambdaQueryWrapper<Machine>()
            .likeRight(Machine::getCode, keyword)
            .orderByDesc(Machine::getId))
        .stream()
        .map(Machine::getId)
        .collect(Collectors.toList());
  }

  private Integer resolveDurationMinutes(SessionOrder order, boolean ongoingMode) {
    return SessionBillingCalculator.resolveDurationMinutes(order, LocalDateTime.now(), ongoingMode);
  }

  private BigDecimal resolveCurrentFee(SessionOrder order, Integer durationMinutes, boolean ongoingMode) {
    if (!ongoingMode) {
      return defaultValue(order.getAmount());
    }
    return SessionBillingCalculator.resolveLiveCharge(order, LocalDateTime.now(), true);
  }

  private BigDecimal defaultValue(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private String resolveStatusLabel(Integer status) {
    if (Objects.equals(status, SESSION_FINISHED)) {
      return "完成";
    }
    if (Objects.equals(status, SESSION_FORCED)) {
      return "强制结束";
    }
    if (Objects.equals(status, SESSION_PAUSED)) {
      return "暂停中";
    }
    return "进行中";
  }

  private String resolveStatusTone(Integer status) {
    if (Objects.equals(status, SESSION_FINISHED)) {
      return "idle";
    }
    if (Objects.equals(status, SESSION_FORCED)) {
      return "forced";
    }
    if (Objects.equals(status, SESSION_PAUSED)) {
      return "warning";
    }
    return "using";
  }

  private void validateHistoryStatus(Integer status) {
    if (status == null) {
      return;
    }
    if (status != SESSION_FINISHED && status != SESSION_FORCED) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "history接口仅支持完成或强制结束状态");
    }
  }

  private void validateDateRange(LocalDate dateFrom, LocalDate dateTo) {
    if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "dateFrom不能晚于dateTo");
    }
  }

  private SessionOrder requireSessionOrder(Long id) {
    SessionOrder order = sessionService.getById(id);
    if (order == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "上机订单不存在");
    }
    return order;
  }

  private Map<String, Object> buildOpenSnapshot(SessionOrder order, User user, Machine machine) {
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("targetLabel", buildSessionTargetLabel(user, machine));
    snapshot.put("userName", user == null ? null : user.getName());
    snapshot.put("machineCode", machine == null ? null : machine.getCode());
    snapshot.put("priceSnapshot", order == null ? null : order.getPriceSnapshot());
    snapshot.put("startTime", order == null ? null : order.getStartTime());
    snapshot.put("changeSummary", "用户 " + (user == null ? "-" : user.getName()) + " 在机位 "
        + (machine == null ? "-" : machine.getCode()) + " 开通上机");
    return snapshot;
  }

  private Map<String, Object> buildSessionSnapshot(SessionOrder order, User user, Machine machine) {
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("targetLabel", buildSessionTargetLabel(user, machine));
    snapshot.put("userName", user == null ? null : user.getName());
    snapshot.put("machineCode", machine == null ? null : machine.getCode());
    snapshot.put("status", order == null ? null : order.getStatus());
    snapshot.put("amount", order == null ? null : order.getAmount());
    snapshot.put("startTime", order == null ? null : order.getStartTime());
    snapshot.put("endTime", order == null ? null : order.getEndTime());
    snapshot.put(
        "changeSummary",
        "订单金额 "
            + (order == null || order.getAmount() == null ? BigDecimal.ZERO : order.getAmount().setScale(2, RoundingMode.HALF_UP))
            + "，状态 "
            + (order == null ? "-" : resolveStatusLabel(order.getStatus()))
    );
    return snapshot;
  }

  private String buildSessionTargetLabel(User user, Machine machine) {
    return "用户 " + (user == null ? "-" : user.getName()) + " / 机位 " + (machine == null ? "-" : machine.getCode());
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
