package com.netcafe.platform.controller.admin.machine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.machine.MachineBatchCreateRequest;
import com.netcafe.platform.domain.dto.machine.MachineCreateRequest;
import com.netcafe.platform.domain.dto.machine.MachineListResponse;
import com.netcafe.platform.domain.dto.machine.MachineUpdateRequest;
import com.netcafe.platform.domain.dto.machine.MachineView;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
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
@RequestMapping("/machines")
public class MachineController {
  private static final int STATUS_IDLE = 0;
  private static final int STATUS_USING = 1;
  private static final int STATUS_DISABLED = 2;
  private static final int STATUS_LOCKED = 3;
  private static final int SESSION_ONGOING = 0;
  private static final int SESSION_PAUSED = 3;
  private static final BigDecimal DEFAULT_PRICE = new BigDecimal("0.1000");

  private final MachineService machineService;
  private final SessionService sessionService;
  private final UserService userService;
  private final AuditLogService auditLogService;
  private final ObjectMapper objectMapper;

  public MachineController(
      MachineService machineService,
      SessionService sessionService,
      UserService userService,
      AuditLogService auditLogService,
      ObjectMapper objectMapper
  ) {
    this.machineService = machineService;
    this.sessionService = sessionService;
    this.userService = userService;
    this.auditLogService = auditLogService;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/monitor")
  public ApiResponse<MachineListResponse> monitor(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size
  ) {
    return ApiResponse.success(queryMachines(keyword, status, minPrice, maxPrice, page, size, true));
  }

  @GetMapping("/{id}")
  public ApiResponse<MachineView> detail(@PathVariable Long id) {
    Machine machine = requireMachine(id);
    Map<Long, SessionOrder> currentSessions = getCurrentSessionMap(List.of(id));
    Map<Long, User> users = getUserMap(currentSessions.values().stream()
        .map(SessionOrder::getUserId)
        .collect(Collectors.toList()));
    return ApiResponse.success(toView(machine, currentSessions.get(id), users));
  }

  @GetMapping
  public ApiResponse<MachineListResponse> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size
  ) {
    return ApiResponse.success(queryMachines(keyword, status, minPrice, maxPrice, page, size, false));
  }

  @PostMapping
  public ApiResponse<Boolean> create(@Valid @RequestBody MachineCreateRequest request) {
    validateStatus(request.getStatus(), false);
    ensurePriceValid(request.getPricePerMin());
    ensureCodeAvailable(request.getCode(), null);
    Machine machine = new Machine();
    machine.setCode(request.getCode().trim());
    machine.setStatus(request.getStatus() == null ? STATUS_IDLE : request.getStatus());
    machine.setPricePerMin(request.getPricePerMin() == null ? DEFAULT_PRICE : request.getPricePerMin());
    machine.setConfigJson(request.getConfigJson());
    machine.setTemplateId(request.getTemplateId());
    machine.setLastMaintainedAt(request.getLastMaintainedAt());
    machine.setCreatedAt(LocalDateTime.now());
    machine.setUpdatedAt(LocalDateTime.now());
    return ApiResponse.success(machineService.save(machine));
  }

  @PutMapping("/{id}")
  public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody MachineUpdateRequest request) {
    Machine existing = requireMachine(id);
    validateStatus(request.getStatus(), false);
    ensurePriceValid(request.getPricePerMin());
    if (StringUtils.hasText(request.getCode())) {
      ensureCodeAvailable(request.getCode(), id);
    }
    Integer targetStatus = request.getStatus() == null ? existing.getStatus() : request.getStatus();
    if (Objects.equals(targetStatus, STATUS_DISABLED) && hasActiveSession(id)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "机位使用中，无法直接停用");
    }
    Machine machine = new Machine();
    machine.setId(id);
    if (StringUtils.hasText(request.getCode())) {
      machine.setCode(request.getCode().trim());
    }
    if (request.getStatus() != null) {
      machine.setStatus(request.getStatus());
    }
    if (request.getPricePerMin() != null) {
      machine.setPricePerMin(request.getPricePerMin());
    }
    if (request.getConfigJson() != null) {
      machine.setConfigJson(request.getConfigJson());
    }
    if (request.getTemplateId() != null) {
      machine.setTemplateId(request.getTemplateId());
    }
    if (request.getLastMaintainedAt() != null) {
      machine.setLastMaintainedAt(request.getLastMaintainedAt());
    }
    machine.setUpdatedAt(LocalDateTime.now());
    return ApiResponse.success(machineService.updateById(machine));
  }

  @PutMapping("/{id}/status")
  public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @RequestBody MachineUpdateRequest request) {
    if (request.getStatus() == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "status不能为空");
    }
    validateStatus(request.getStatus(), false);
    requireMachine(id);
    if (request.getStatus() == STATUS_DISABLED && hasActiveSession(id)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "机位使用中，无法停用");
    }
    Machine machine = new Machine();
    machine.setId(id);
    machine.setStatus(request.getStatus());
    machine.setUpdatedAt(LocalDateTime.now());
    return ApiResponse.success(machineService.updateById(machine));
  }

  @PutMapping("/{id}/price")
  public ApiResponse<Boolean> updatePrice(@PathVariable Long id, @RequestBody MachineUpdateRequest request) {
    requireSuperAdmin();
    if (request.getPricePerMin() == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "pricePerMin不能为空");
    }
    Machine before = requireMachine(id);
    Long operatorAdminId = requireCurrentAdminId();
    boolean updated = machineService.updatePrice(id, request.getPricePerMin(), operatorAdminId);
    Machine after = requireMachine(id);
    auditLogService.record(
        operatorAdminId,
        resolveCurrentAdminRole(),
        AuditLogService.ACTION_UPDATE_MACHINE_PRICE,
        "MACHINE",
        id,
        buildMachinePriceSnapshot(before),
        buildMachinePriceSnapshot(after)
    );
    return ApiResponse.success(updated);
  }

  @PostMapping("/batch-create")
  public ApiResponse<Boolean> batchCreate(@Valid @RequestBody MachineBatchCreateRequest request) {
    boolean created = machineService.batchCreate(request);
    auditLogService.record(
        requireCurrentAdminId(),
        resolveCurrentAdminRole(),
        AuditLogService.ACTION_BATCH_CREATE_MACHINE,
        "MACHINE_BATCH",
        request.getTemplateId(),
        null,
        buildBatchCreateSnapshot(request)
    );
    return ApiResponse.success(created);
  }

  private MachineListResponse queryMachines(
      String keyword,
      Integer status,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      long page,
      long size,
      boolean withMonitor
  ) {
    validateStatus(status, true);
    validatePriceRange(minPrice, maxPrice);
    long pageSize = Math.max(1, Math.min(size, 200));
    Page<Machine> pageRequest = new Page<>(page, pageSize);
    LambdaQueryWrapper<Machine> wrapper = new LambdaQueryWrapper<>();
    wrapper.gt(Machine::getId, 0L);
    if (StringUtils.hasText(keyword)) {
      wrapper.likeRight(Machine::getCode, keyword.trim());
    }
    if (status != null) {
      wrapper.eq(Machine::getStatus, status);
    }
    if (minPrice != null) {
      wrapper.ge(Machine::getPricePerMin, minPrice);
    }
    if (maxPrice != null) {
      wrapper.le(Machine::getPricePerMin, maxPrice);
    }
    wrapper.orderByDesc(Machine::getId);
    Page<Machine> result = machineService.page(pageRequest, wrapper);

    Map<Long, SessionOrder> currentSessions = new HashMap<>();
    Map<Long, User> users = new HashMap<>();
    if (withMonitor || !result.getRecords().isEmpty()) {
      List<Long> machineIds = result.getRecords().stream()
          .map(Machine::getId)
          .collect(Collectors.toList());
      currentSessions = getCurrentSessionMap(machineIds);
      users = getUserMap(currentSessions.values().stream()
          .map(SessionOrder::getUserId)
          .collect(Collectors.toList()));
    }
    Map<Long, SessionOrder> currentSessionMap = currentSessions;
    Map<Long, User> userMap = users;

    List<MachineView> items = result.getRecords().stream()
        .map(machine -> toView(machine, currentSessionMap.get(machine.getId()), userMap))
        .collect(Collectors.toList());
    return new MachineListResponse(result.getTotal(), result.getCurrent(), result.getSize(), items);
  }

  private Map<Long, SessionOrder> getCurrentSessionMap(List<Long> machineIds) {
    if (machineIds == null || machineIds.isEmpty()) {
      return new HashMap<>();
    }
    return sessionService.list(new LambdaQueryWrapper<SessionOrder>()
            .in(SessionOrder::getMachineId, machineIds)
            .in(SessionOrder::getStatus, List.of(SESSION_ONGOING, SESSION_PAUSED)))
        .stream()
        .collect(Collectors.toMap(SessionOrder::getMachineId, order -> order, (left, right) -> left));
  }

  private Map<Long, User> getUserMap(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return new HashMap<>();
    }
    return userService.listByIds(userIds).stream()
        .collect(Collectors.toMap(User::getId, user -> user, (left, right) -> left));
  }

  private MachineView toView(Machine machine, SessionOrder currentSession, Map<Long, User> users) {
    MachineView view = new MachineView();
    view.setId(machine.getId());
    view.setCode(machine.getCode());
    view.setStatus(machine.getStatus());
    view.setStatusLabel(resolveMachineStatusLabel(machine.getStatus()));
    view.setStatusTone(resolveMachineStatusTone(machine.getStatus()));
    view.setPricePerMin(machine.getPricePerMin());
    view.setPriceLabel(formatPrice(machine.getPricePerMin()));
    view.setConfigJson(machine.getConfigJson());
    view.setConfigSummary(resolveConfigSummary(machine.getConfigJson()));
    view.setTemplateId(machine.getTemplateId());
    view.setLastMaintainedAt(machine.getLastMaintainedAt());
    view.setCreatedAt(machine.getCreatedAt());
    view.setUpdatedAt(machine.getUpdatedAt());
    if (currentSession != null) {
      User currentUser = users.get(currentSession.getUserId());
      view.setCurrentSessionId(currentSession.getId());
      view.setCurrentUserId(currentSession.getUserId());
      view.setCurrentUserName(currentUser == null ? null : currentUser.getName());
      view.setCurrentStartTime(currentSession.getStartTime());
      view.setCurrentDurationMinutes(resolveCurrentDuration(currentSession));
      view.setCurrentFee(resolveCurrentFee(currentSession, view.getCurrentDurationMinutes()));
    }
    return view;
  }

  private String resolveMachineStatusLabel(Integer status) {
    if (Objects.equals(status, STATUS_USING)) {
      return "使用中";
    }
    if (Objects.equals(status, STATUS_LOCKED)) {
      return "暂停锁定";
    }
    if (Objects.equals(status, STATUS_DISABLED)) {
      return "停用";
    }
    return "空闲";
  }

  private String resolveMachineStatusTone(Integer status) {
    if (Objects.equals(status, STATUS_USING)) {
      return "using";
    }
    if (Objects.equals(status, STATUS_LOCKED)) {
      return "warning";
    }
    if (Objects.equals(status, STATUS_DISABLED)) {
      return "disabled";
    }
    return "idle";
  }

  private String formatPrice(BigDecimal pricePerMin) {
    BigDecimal price = pricePerMin == null ? DEFAULT_PRICE : pricePerMin;
    return "¥" + price.setScale(2, RoundingMode.HALF_UP) + "/分钟";
  }

  private String resolveConfigSummary(String configJson) {
    if (!StringUtils.hasText(configJson)) {
      return "";
    }
    try {
      Map<String, Object> map = objectMapper.readValue(configJson, new TypeReference<>() {
      });
      List<String> parts = new ArrayList<>();
      Object spec = map.get("spec");
      if (spec != null && StringUtils.hasText(String.valueOf(spec))) {
        return String.valueOf(spec);
      }
      addConfigPart(parts, map.get("cpu"));
      addConfigPart(parts, map.get("gpu"));
      addConfigPart(parts, map.get("memory"));
      if (!parts.isEmpty()) {
        return String.join(" / ", parts);
      }
    } catch (Exception ignored) {
      // 配置摘要仅用于列表展示，解析失败时回退到原始内容。
    }
    return configJson;
  }

  private void addConfigPart(List<String> parts, Object value) {
    if (value != null && StringUtils.hasText(String.valueOf(value))) {
      parts.add(String.valueOf(value));
    }
  }

  private Integer resolveCurrentDuration(SessionOrder order) {
    return SessionBillingCalculator.resolveDurationMinutes(order, LocalDateTime.now(), true);
  }

  private BigDecimal resolveCurrentFee(SessionOrder order, Integer durationMinutes) {
    return SessionBillingCalculator.resolveLiveCharge(order, LocalDateTime.now(), true);
  }

  private void validateStatus(Integer status, boolean allowUsing) {
    if (status == null) {
      return;
    }
    if (status != STATUS_IDLE
        && status != STATUS_USING
        && status != STATUS_DISABLED
        && status != STATUS_LOCKED) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "status不合法");
    }
    if (!allowUsing && (status == STATUS_USING || status == STATUS_LOCKED)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "不能手动将机位设置为使用中或暂停锁定");
    }
  }

  private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
    ensurePriceValid(minPrice);
    ensurePriceValid(maxPrice);
    if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "minPrice不能大于maxPrice");
    }
  }

  private void ensurePriceValid(BigDecimal pricePerMin) {
    if (pricePerMin != null && pricePerMin.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "pricePerMin必须大于0");
    }
  }

  private void ensureCodeAvailable(String code, Long excludeId) {
    if (!StringUtils.hasText(code)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "code不能为空");
    }
    LambdaQueryWrapper<Machine> wrapper = new LambdaQueryWrapper<Machine>()
        .eq(Machine::getCode, code.trim());
    if (excludeId != null) {
      wrapper.ne(Machine::getId, excludeId);
    }
    if (machineService.count(wrapper) > 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "机位编号已存在");
    }
  }

  private Machine requireMachine(Long id) {
    Machine machine = machineService.getById(id);
    if (machine == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "机位不存在");
    }
    return machine;
  }

  private boolean hasActiveSession(Long machineId) {
    return sessionService.count(new LambdaQueryWrapper<SessionOrder>()
        .eq(SessionOrder::getMachineId, machineId)
        .in(SessionOrder::getStatus, List.of(SESSION_ONGOING, SESSION_PAUSED))) > 0;
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

  private void requireSuperAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getAuthorities().stream()
        .noneMatch(authority -> "ROLE_SUPER_ADMIN".equals(authority.getAuthority()))) {
      throw new BusinessException(ResultCode.FORBIDDEN, "只有超级管理员可以调价");
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

  private Map<String, Object> buildMachinePriceSnapshot(Machine machine) {
    Map<String, Object> snapshot = new HashMap<>();
    snapshot.put("targetLabel", machine.getCode());
    snapshot.put("pricePerMin", machine.getPricePerMin());
    snapshot.put(
        "changeSummary",
        "机位 " + machine.getCode() + " 单价 " + machine.getPricePerMin().setScale(2, RoundingMode.HALF_UP) + " 元/分钟"
    );
    return snapshot;
  }

  private Map<String, Object> buildBatchCreateSnapshot(MachineBatchCreateRequest request) {
    Map<String, Object> snapshot = new HashMap<>();
    snapshot.put("targetLabel", request.getCodePrefix() + "批量建机位");
    snapshot.put("templateId", request.getTemplateId());
    snapshot.put("count", request.getCount());
    snapshot.put("startNo", request.getStartNo());
    snapshot.put("codePrefix", request.getCodePrefix());
    snapshot.put("codeWidth", request.getCodeWidth());
    snapshot.put("pricePerMin", request.getPricePerMin());
    snapshot.put(
        "changeSummary",
        "模板 " + request.getTemplateId() + " 生成 " + request.getCount() + " 台机位，编号前缀 " + request.getCodePrefix()
    );
    return snapshot;
  }
}
