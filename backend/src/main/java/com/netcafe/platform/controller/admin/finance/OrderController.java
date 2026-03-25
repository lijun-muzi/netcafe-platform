package com.netcafe.platform.controller.admin.finance;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.finance.SessionOrderListResponse;
import com.netcafe.platform.domain.dto.finance.SessionOrderView;
import com.netcafe.platform.domain.entity.account.User;
import com.netcafe.platform.domain.entity.machine.Machine;
import com.netcafe.platform.domain.entity.session.SessionOrder;
import com.netcafe.platform.service.account.UserService;
import com.netcafe.platform.service.machine.MachineService;
import com.netcafe.platform.service.session.SessionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/session-orders")
public class OrderController {
  private static final int STATUS_FINISHED = 1;
  private static final int STATUS_FORCED = 2;
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  private final SessionService sessionService;
  private final UserService userService;
  private final MachineService machineService;

  public OrderController(
      SessionService sessionService,
      UserService userService,
      MachineService machineService
  ) {
    this.sessionService = sessionService;
    this.userService = userService;
    this.machineService = machineService;
  }

  @GetMapping
  public ApiResponse<SessionOrderListResponse> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size
  ) {
    validateStatus(status);
    long pageSize = Math.max(1, Math.min(size, 200));
    Page<SessionOrder> pageRequest = new Page<>(page, pageSize);
    LambdaQueryWrapper<SessionOrder> wrapper = new LambdaQueryWrapper<>();
    wrapper.le(SessionOrder::getStartTime, LocalDateTime.now());
    if (status == null) {
      wrapper.in(SessionOrder::getStatus, List.of(STATUS_FINISHED, STATUS_FORCED));
    } else {
      wrapper.eq(SessionOrder::getStatus, status);
    }
    if (!applyKeywordFilter(keyword, wrapper)) {
      return ApiResponse.success(new SessionOrderListResponse(0, page, pageSize, List.of()));
    }
    wrapper.orderByDesc(SessionOrder::getStartTime);
    Page<SessionOrder> result = sessionService.page(pageRequest, wrapper);

    Map<Long, User> userMap = getUserMap(result.getRecords().stream()
        .map(SessionOrder::getUserId)
        .distinct()
        .collect(Collectors.toList()));
    Map<Long, Machine> machineMap = getMachineMap(result.getRecords().stream()
        .map(SessionOrder::getMachineId)
        .distinct()
        .collect(Collectors.toList()));
    List<SessionOrderView> items = result.getRecords().stream()
        .map(order -> toView(order, userMap.get(order.getUserId()), machineMap.get(order.getMachineId())))
        .collect(Collectors.toList());
    return ApiResponse.success(new SessionOrderListResponse(result.getTotal(), result.getCurrent(), result.getSize(), items));
  }

  private boolean applyKeywordFilter(String keyword, LambdaQueryWrapper<SessionOrder> wrapper) {
    if (!StringUtils.hasText(keyword)) {
      return true;
    }
    String normalizedKeyword = keyword.trim();
    List<Long> userIds = resolveUserIds(normalizedKeyword);
    List<Long> machineIds = resolveMachineIds(normalizedKeyword);
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

  private Map<Long, User> getUserMap(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return new HashMap<>();
    }
    return userService.listByIds(userIds).stream()
        .collect(Collectors.toMap(User::getId, user -> user, (left, right) -> right));
  }

  private Map<Long, Machine> getMachineMap(List<Long> machineIds) {
    if (machineIds == null || machineIds.isEmpty()) {
      return new HashMap<>();
    }
    return machineService.listByIds(machineIds).stream()
        .collect(Collectors.toMap(Machine::getId, machine -> machine, (left, right) -> right));
  }

  private SessionOrderView toView(SessionOrder order, User user, Machine machine) {
    SessionOrderView view = new SessionOrderView();
    view.setId(order.getId());
    view.setUserId(order.getUserId());
    view.setUserName(user == null ? null : user.getName());
    view.setMachineId(order.getMachineId());
    view.setMachineCode(machine == null ? null : machine.getCode());
    view.setStartTime(order.getStartTime());
    view.setEndTime(order.getEndTime());
    view.setTimeRangeLabel(resolveTimeRangeLabel(order.getStartTime(), order.getEndTime()));
    view.setDurationMinutes(resolveDurationMinutes(order));
    view.setDurationLabel(resolveDurationLabel(view.getDurationMinutes()));
    view.setPriceSnapshot(defaultValue(order.getPriceSnapshot()));
    view.setPriceLabel("¥" + defaultValue(order.getPriceSnapshot()).setScale(2, RoundingMode.HALF_UP));
    view.setAmount(defaultValue(order.getAmount()));
    view.setAmountLabel("¥" + defaultValue(order.getAmount()).setScale(2, RoundingMode.HALF_UP));
    view.setStatus(order.getStatus());
    view.setStatusLabel(resolveStatusLabel(order.getStatus()));
    view.setStatusTone(resolveStatusTone(order.getStatus()));
    view.setForceByAdminId(order.getForceByAdminId());
    return view;
  }

  private String resolveTimeRangeLabel(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime == null) {
      return "";
    }
    String start = startTime.format(TIME_FORMATTER);
    if (endTime == null) {
      return start + " -";
    }
    return start + " - " + endTime.format(TIME_FORMATTER);
  }

  private Integer resolveDurationMinutes(SessionOrder order) {
    if (order.getDurationMinutes() != null && order.getDurationMinutes() > 0) {
      return order.getDurationMinutes();
    }
    if (order.getBilledMinutes() != null && order.getBilledMinutes() > 0) {
      return order.getBilledMinutes();
    }
    if (order.getStartTime() == null || order.getEndTime() == null) {
      return 0;
    }
    return (int) Math.max(java.time.Duration.between(order.getStartTime(), order.getEndTime()).toMinutes(), 0);
  }

  private String resolveDurationLabel(Integer durationMinutes) {
    return (durationMinutes == null ? 0 : durationMinutes) + " 分钟";
  }

  private BigDecimal defaultValue(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private String resolveStatusLabel(Integer status) {
    if (Objects.equals(status, STATUS_FORCED)) {
      return "强制结束";
    }
    return "完成";
  }

  private String resolveStatusTone(Integer status) {
    if (Objects.equals(status, STATUS_FORCED)) {
      return "forced";
    }
    return "idle";
  }

  private void validateStatus(Integer status) {
    if (status == null) {
      return;
    }
    if (status != STATUS_FINISHED && status != STATUS_FORCED) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "status仅支持1或2");
    }
  }
}
