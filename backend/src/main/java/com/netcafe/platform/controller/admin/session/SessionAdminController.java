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
import com.netcafe.platform.service.session.SessionService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

  private final SessionService sessionService;
  private final UserService userService;
  private final MachineService machineService;

  public SessionAdminController(
      SessionService sessionService,
      UserService userService,
      MachineService machineService
  ) {
    this.sessionService = sessionService;
    this.userService = userService;
    this.machineService = machineService;
  }

  @PostMapping("/open")
  public ApiResponse<Boolean> open(@Valid @RequestBody OpenSessionRequest request) {
    return ApiResponse.success(sessionService.openSession(request.getUserId(), request.getMachineId()));
  }

  @PutMapping("/{id}/force-end")
  public ApiResponse<Boolean> forceEnd(@PathVariable Long id) {
    return ApiResponse.success(sessionService.forceEnd(id, requireCurrentAdminId()));
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
    wrapper.eq(SessionOrder::getStatus, SESSION_ONGOING);
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
    if (order.getDurationMinutes() != null && order.getDurationMinutes() > 0) {
      return order.getDurationMinutes();
    }
    if (order.getBilledMinutes() != null && order.getBilledMinutes() > 0) {
      return order.getBilledMinutes();
    }
    if (!ongoingMode || order.getStartTime() == null) {
      return 0;
    }
    return (int) Math.max(Duration.between(order.getStartTime(), LocalDateTime.now()).toMinutes(), 0);
  }

  private BigDecimal resolveCurrentFee(SessionOrder order, Integer durationMinutes, boolean ongoingMode) {
    if (!ongoingMode) {
      return defaultValue(order.getAmount());
    }
    if (order.getAmount() != null && order.getAmount().compareTo(BigDecimal.ZERO) > 0) {
      return order.getAmount();
    }
    BigDecimal price = defaultValue(order.getPriceSnapshot());
    return price.multiply(BigDecimal.valueOf(durationMinutes == null ? 0 : durationMinutes))
        .setScale(2, RoundingMode.HALF_UP);
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
    return "进行中";
  }

  private String resolveStatusTone(Integer status) {
    if (Objects.equals(status, SESSION_FINISHED)) {
      return "idle";
    }
    if (Objects.equals(status, SESSION_FORCED)) {
      return "forced";
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
}
