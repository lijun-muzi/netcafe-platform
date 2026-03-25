package com.netcafe.platform.controller.kiosk;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.auth.UserLoginResponse;
import com.netcafe.platform.domain.dto.kiosk.KioskCheckoutView;
import com.netcafe.platform.domain.dto.kiosk.KioskLoginRequest;
import com.netcafe.platform.domain.dto.kiosk.KioskLoginResponse;
import com.netcafe.platform.domain.dto.kiosk.KioskMachineOverviewView;
import com.netcafe.platform.domain.dto.kiosk.KioskSessionStatusView;
import com.netcafe.platform.domain.entity.account.User;
import com.netcafe.platform.domain.entity.machine.Machine;
import com.netcafe.platform.domain.entity.session.SessionOrder;
import com.netcafe.platform.service.account.AuthService;
import com.netcafe.platform.service.account.UserService;
import com.netcafe.platform.service.machine.MachineService;
import com.netcafe.platform.service.session.SessionBillingCalculator;
import com.netcafe.platform.service.session.SessionService;
import com.netcafe.platform.service.system.SystemConfigService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kiosk")
public class KioskSessionController {
  private static final int MACHINE_IDLE = 0;
  private static final int MACHINE_USING = 1;
  private static final int MACHINE_DISABLED = 2;
  private static final int MACHINE_LOCKED = 3;
  private static final int SESSION_ONGOING = 0;
  private static final int SESSION_FINISHED = 1;
  private static final int SESSION_FORCED = 2;
  private static final int SESSION_PAUSED = 3;

  private final SessionService sessionService;
  private final UserService userService;
  private final MachineService machineService;
  private final AuthService authService;
  private final SystemConfigService systemConfigService;

  public KioskSessionController(
      SessionService sessionService,
      UserService userService,
      MachineService machineService,
      AuthService authService,
      SystemConfigService systemConfigService
  ) {
    this.sessionService = sessionService;
    this.userService = userService;
    this.machineService = machineService;
    this.authService = authService;
    this.systemConfigService = systemConfigService;
  }

  @GetMapping("/machines/{machineCode}/overview")
  public ApiResponse<KioskMachineOverviewView> machineOverview(@PathVariable String machineCode) {
    int thresholdMinutes = systemConfigService.getBasicSettings().getLowBalanceThresholdMinutes();
    Machine machine = requireMachineByCode(machineCode);
    return ApiResponse.success(buildMachineOverview(machine, sessionService.getCurrentSessionByMachine(machine.getId()), thresholdMinutes));
  }

  @PostMapping("/sessions/start")
  public ApiResponse<KioskLoginResponse> start(@Valid @RequestBody KioskLoginRequest request) {
    Machine machine = requireMachineByCode(request.getMachineCode());
    User user = requireAvailableUserByIdCard(request.getIdCard());

    SessionOrder userCurrentOrder = sessionService.getCurrentSessionByUser(user.getId());
    if (userCurrentOrder != null && !Objects.equals(userCurrentOrder.getMachineId(), machine.getId())) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "用户已在其他机位上机");
    }

    SessionOrder machineCurrentOrder = sessionService.getCurrentSessionByMachine(machine.getId());
    if (machineCurrentOrder != null && !Objects.equals(machineCurrentOrder.getUserId(), user.getId())) {
      throw new BusinessException(
          ResultCode.BAD_REQUEST,
          Objects.equals(machineCurrentOrder.getStatus(), SESSION_PAUSED)
              ? "当前机位已暂停锁定，请输入原身份证恢复"
              : "当前机位正在使用中"
      );
    }
    if (Objects.equals(machine.getStatus(), MACHINE_DISABLED)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "当前机位已停用");
    }
    if (Objects.equals(machine.getStatus(), MACHINE_LOCKED)
        && machineCurrentOrder == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "当前机位已锁定，请联系管理员处理");
    }

    Long sessionId;
    if (machineCurrentOrder != null) {
      sessionId = Objects.equals(machineCurrentOrder.getStatus(), SESSION_PAUSED)
          ? sessionService.resumeSession(machineCurrentOrder.getId()).getId()
          : machineCurrentOrder.getId();
    } else if (userCurrentOrder != null) {
      sessionId = Objects.equals(userCurrentOrder.getStatus(), SESSION_PAUSED)
          ? sessionService.resumeSession(userCurrentOrder.getId()).getId()
          : userCurrentOrder.getId();
    } else {
      sessionId = sessionService.openSession(user.getId(), machine.getId());
    }

    UserLoginResponse loginResponse = authService.loginUser(request.getIdCard().trim());
    SessionOrder currentOrder = sessionService.getById(sessionId);
    User latestUser = userService.getById(user.getId());
    Machine latestMachine = machineService.getById(machine.getId());
    int thresholdMinutes = systemConfigService.getBasicSettings().getLowBalanceThresholdMinutes();

    KioskLoginResponse response = new KioskLoginResponse();
    response.setToken(loginResponse.getToken());
    response.setTokenType(loginResponse.getTokenType());
    response.setExpiresIn(loginResponse.getExpiresIn());
    response.setUser(loginResponse.getUser());
    response.setMachine(buildMachineOverview(latestMachine, currentOrder, thresholdMinutes));
    response.setSession(buildSessionStatus(currentOrder, latestUser, latestMachine, thresholdMinutes));
    return ApiResponse.success(response);
  }

  @PostMapping("/sessions/pause")
  public ApiResponse<KioskSessionStatusView> pause() {
    Long userId = requireCurrentUserId();
    SessionOrder pausedOrder = sessionService.pauseSession(userId);
    User user = requireUser(userId);
    Machine machine = requireMachine(pausedOrder.getMachineId());
    int thresholdMinutes = systemConfigService.getBasicSettings().getLowBalanceThresholdMinutes();
    return ApiResponse.success(buildSessionStatus(pausedOrder, user, machine, thresholdMinutes));
  }

  @GetMapping("/sessions/current")
  public ApiResponse<KioskSessionStatusView> current() {
    Long userId = requireCurrentUserId();
    SessionOrder currentOrder = sessionService.getCurrentSessionByUser(userId);
    if (currentOrder == null) {
      return ApiResponse.success(null);
    }
    User user = requireUser(userId);
    Machine machine = requireMachine(currentOrder.getMachineId());
    int thresholdMinutes = systemConfigService.getBasicSettings().getLowBalanceThresholdMinutes();
    return ApiResponse.success(buildSessionStatus(currentOrder, user, machine, thresholdMinutes));
  }

  @GetMapping("/sessions/checkout-preview")
  public ApiResponse<KioskCheckoutView> checkoutPreview() {
    Long userId = requireCurrentUserId();
    SessionOrder currentOrder = requireCurrentOrder(userId);
    User user = requireUser(userId);
    Machine machine = requireMachine(currentOrder.getMachineId());
    return ApiResponse.success(buildCheckoutPreview(currentOrder, user, machine, LocalDateTime.now()));
  }

  @GetMapping("/sessions/latest-ended")
  public ApiResponse<KioskCheckoutView> latestEnded() {
    Long userId = requireCurrentUserId();
    SessionOrder latestEndedOrder = sessionService.getLatestEndedSessionByUser(userId);
    if (latestEndedOrder == null) {
      return ApiResponse.success(null);
    }
    User user = requireUser(userId);
    Machine machine = requireMachine(latestEndedOrder.getMachineId());
    return ApiResponse.success(buildLatestEndedCheckout(latestEndedOrder, user, machine));
  }

  @PostMapping("/sessions/end")
  public ApiResponse<KioskCheckoutView> end() {
    Long userId = requireCurrentUserId();
    SessionOrder currentOrder = requireCurrentOrder(userId);
    User user = requireUser(userId);
    Machine machine = requireMachine(currentOrder.getMachineId());
    KioskCheckoutView preview = buildCheckoutPreview(currentOrder, user, machine, LocalDateTime.now());
    SessionOrder endedOrder = sessionService.userEnd(userId);
    Machine latestMachine = requireMachine(machine.getId());
    preview.setSessionId(endedOrder.getId());
    preview.setStatus(endedOrder.getStatus());
    preview.setStatusLabel(resolveSessionStatusLabel(endedOrder.getStatus()));
    preview.setEndTime(endedOrder.getEndTime());
    preview.setMachineCode(latestMachine.getCode());
    return ApiResponse.success(preview);
  }

  private KioskMachineOverviewView buildMachineOverview(Machine machine, SessionOrder currentOrder, int thresholdMinutes) {
    KioskMachineOverviewView view = new KioskMachineOverviewView();
    view.setMachineId(machine.getId());
    view.setMachineCode(machine.getCode());
    view.setStatus(machine.getStatus());
    view.setStatusLabel(resolveMachineStatusLabel(machine.getStatus()));
    view.setPricePerMin(defaultMoney(machine.getPricePerMin()).setScale(4, RoundingMode.HALF_UP));
    view.setPriceLabel(formatPrice(machine.getPricePerMin()));
    view.setLowBalanceThresholdMinutes(thresholdMinutes);
    view.setAvailable(
        currentOrder == null
            && Objects.equals(machine.getStatus(), MACHINE_IDLE)
    );
    view.setAvailabilityMessage(resolveAvailabilityMessage(machine, currentOrder));
    return view;
  }

  private KioskSessionStatusView buildSessionStatus(SessionOrder order, User user, Machine machine, int thresholdMinutes) {
    KioskSessionStatusView view = new KioskSessionStatusView();
    int currentMinutes = SessionBillingCalculator.resolveDurationMinutes(order, LocalDateTime.now(), true);
    BigDecimal currentFee = SessionBillingCalculator.resolveLiveCharge(order, LocalDateTime.now(), true);
    int remainingMinutes = SessionBillingCalculator.resolveRemainingMinutes(user.getBalance(), order.getPriceSnapshot());
    view.setSessionId(order.getId());
    view.setUserId(user.getId());
    view.setUserName(user.getName());
    view.setMachineId(machine.getId());
    view.setMachineCode(machine.getCode());
    view.setStartTime(order.getStartTime());
    view.setCurrentDurationMinutes(currentMinutes);
    view.setCurrentDurationLabel(currentMinutes + " 分钟");
    view.setBilledMinutes(order.getBilledMinutes() == null ? 0 : order.getBilledMinutes());
    view.setPricePerMin(defaultMoney(order.getPriceSnapshot()).setScale(4, RoundingMode.HALF_UP));
    view.setPriceLabel(formatPrice(order.getPriceSnapshot()));
    view.setCurrentFee(currentFee);
    view.setCurrentFeeLabel(formatMoney(currentFee));
    view.setBilledAmount(defaultMoney(order.getAmount()));
    view.setBilledAmountLabel(formatMoney(order.getAmount()));
    view.setBalance(defaultMoney(user.getBalance()));
    view.setBalanceLabel(formatMoney(user.getBalance()));
    view.setRemainingMinutes(remainingMinutes);
    view.setRemainingMinutesLabel(remainingMinutes + " 分钟");
    view.setLowBalanceThresholdMinutes(thresholdMinutes);
    view.setLowBalanceWarning(remainingMinutes <= thresholdMinutes);
    if (Objects.equals(order.getStatus(), SESSION_PAUSED)) {
      view.setLowBalanceMessage("当前已暂停，恢复后继续计费");
    } else {
      view.setLowBalanceMessage(remainingMinutes <= thresholdMinutes
          ? "余额不足 " + thresholdMinutes + " 分钟，请及时联系管理员充值"
          : "余额充足");
    }
    view.setStatus(order.getStatus());
    view.setStatusLabel(resolveSessionStatusLabel(order.getStatus()));
    view.setPaused(Objects.equals(order.getStatus(), SESSION_PAUSED));
    view.setPausedAt(order.getPausedAt());
    return view;
  }

  private KioskCheckoutView buildCheckoutPreview(SessionOrder order, User user, Machine machine, LocalDateTime endTime) {
    int durationMinutes = SessionBillingCalculator.resolveDurationMinutes(order, endTime, true);
    BigDecimal totalAmount = SessionBillingCalculator.resolveCharge(order.getPriceSnapshot(), durationMinutes);
    BigDecimal billedAmount = defaultMoney(order.getAmount());
    BigDecimal settlementAmount = totalAmount.subtract(billedAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    BigDecimal balanceBefore = defaultMoney(user.getBalance());
    BigDecimal balanceAfter = balanceBefore.subtract(settlementAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

    KioskCheckoutView view = new KioskCheckoutView();
    view.setSessionId(order.getId());
    view.setUserId(user.getId());
    view.setUserName(user.getName());
    view.setMachineCode(machine.getCode());
    view.setStatus(SESSION_FINISHED);
    view.setStatusLabel("待确认下机");
    view.setStartTime(order.getStartTime());
    view.setEndTime(endTime);
    view.setDurationMinutes(durationMinutes);
    view.setDurationLabel(durationMinutes + " 分钟");
    view.setPricePerMin(defaultMoney(order.getPriceSnapshot()).setScale(4, RoundingMode.HALF_UP));
    view.setPriceLabel(formatPrice(order.getPriceSnapshot()));
    view.setTotalAmount(totalAmount);
    view.setTotalAmountLabel(formatMoney(totalAmount));
    view.setSettlementAmount(settlementAmount);
    view.setSettlementAmountLabel(formatMoney(settlementAmount));
    view.setBalanceBefore(balanceBefore);
    view.setBalanceBeforeLabel(formatMoney(balanceBefore));
    view.setBalanceAfter(balanceAfter);
    view.setBalanceAfterLabel(formatMoney(balanceAfter));
    return view;
  }

  private KioskCheckoutView buildLatestEndedCheckout(SessionOrder order, User user, Machine machine) {
    BigDecimal totalAmount = defaultMoney(order.getAmount());
    BigDecimal billedBeforeEnd = SessionBillingCalculator.resolveCharge(order.getPriceSnapshot(), order.getBilledMinutes() == null ? 0 : order.getBilledMinutes());
    BigDecimal settlementAmount = totalAmount.subtract(billedBeforeEnd).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    BigDecimal balanceAfter = defaultMoney(user.getBalance());
    BigDecimal balanceBefore = balanceAfter.add(settlementAmount).setScale(2, RoundingMode.HALF_UP);

    KioskCheckoutView view = new KioskCheckoutView();
    view.setSessionId(order.getId());
    view.setUserId(user.getId());
    view.setUserName(user.getName());
    view.setMachineCode(machine.getCode());
    view.setStatus(order.getStatus());
    view.setStatusLabel(resolveSessionStatusLabel(order.getStatus()));
    view.setStartTime(order.getStartTime());
    view.setEndTime(order.getEndTime());
    view.setDurationMinutes(order.getDurationMinutes() == null ? 0 : order.getDurationMinutes());
    view.setDurationLabel((order.getDurationMinutes() == null ? 0 : order.getDurationMinutes()) + " 分钟");
    view.setPricePerMin(defaultMoney(order.getPriceSnapshot()).setScale(4, RoundingMode.HALF_UP));
    view.setPriceLabel(formatPrice(order.getPriceSnapshot()));
    view.setTotalAmount(totalAmount);
    view.setTotalAmountLabel(formatMoney(totalAmount));
    view.setSettlementAmount(settlementAmount);
    view.setSettlementAmountLabel(formatMoney(settlementAmount));
    view.setBalanceBefore(balanceBefore);
    view.setBalanceBeforeLabel(formatMoney(balanceBefore));
    view.setBalanceAfter(balanceAfter);
    view.setBalanceAfterLabel(formatMoney(balanceAfter));
    return view;
  }

  private Machine requireMachineByCode(String machineCode) {
    if (!StringUtils.hasText(machineCode)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "machineCode不能为空");
    }
    Machine machine = machineService.getOne(new LambdaQueryWrapper<Machine>()
        .eq(Machine::getCode, machineCode.trim()), false);
    if (machine == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "机位不存在");
    }
    return machine;
  }

  private Machine requireMachine(Long machineId) {
    Machine machine = machineService.getById(machineId);
    if (machine == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "机位不存在");
    }
    return machine;
  }

  private User requireAvailableUserByIdCard(String idCard) {
    if (!StringUtils.hasText(idCard)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "idCard不能为空");
    }
    User user = userService.getOne(new LambdaQueryWrapper<User>()
        .eq(User::getIdCard, idCard.trim()), false);
    if (user == null) {
      throw new BusinessException(ResultCode.AUTH_FAILED, "身份证不存在");
    }
    if (!Objects.equals(user.getStatus(), 1)) {
      throw new BusinessException(ResultCode.AUTH_FAILED, "当前用户已冻结，无法上机");
    }
    return user;
  }

  private User requireUser(Long userId) {
    User user = userService.getById(userId);
    if (user == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
    }
    return user;
  }

  private SessionOrder requireCurrentOrder(Long userId) {
    SessionOrder order = sessionService.getCurrentSessionByUser(userId);
    if (order == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "当前没有进行中的上机订单");
    }
    return order;
  }

  private Long requireCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getDetails() == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
    }
    try {
      return Long.valueOf(String.valueOf(authentication.getDetails()));
    } catch (NumberFormatException ex) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
    }
  }

  private String resolveMachineStatusLabel(Integer status) {
    if (Objects.equals(status, MACHINE_USING)) {
      return "使用中";
    }
    if (Objects.equals(status, MACHINE_LOCKED)) {
      return "暂停锁定";
    }
    if (Objects.equals(status, MACHINE_DISABLED)) {
      return "停用";
    }
    return "空闲";
  }

  private String resolveAvailabilityMessage(Machine machine, SessionOrder currentOrder) {
    if (Objects.equals(machine.getStatus(), MACHINE_DISABLED)) {
      return "当前机位已停用";
    }
    if (Objects.equals(machine.getStatus(), MACHINE_LOCKED)
        || (currentOrder != null && Objects.equals(currentOrder.getStatus(), SESSION_PAUSED))) {
      return "当前机位已暂停锁定，请输入原身份证恢复";
    }
    if (currentOrder != null) {
      return "当前机位正在使用中";
    }
    return "当前机位可用";
  }

  private String resolveSessionStatusLabel(Integer status) {
    if (Objects.equals(status, SESSION_FORCED)) {
      return "强制结束";
    }
    if (Objects.equals(status, SESSION_FINISHED)) {
      return "完成";
    }
    if (Objects.equals(status, SESSION_PAUSED)) {
      return "暂停中";
    }
    return "进行中";
  }

  private String formatPrice(BigDecimal price) {
    return formatMoney(price) + "/分钟";
  }

  private String formatMoney(BigDecimal amount) {
    return "¥" + defaultMoney(amount).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal defaultMoney(BigDecimal amount) {
    return amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
  }
}
