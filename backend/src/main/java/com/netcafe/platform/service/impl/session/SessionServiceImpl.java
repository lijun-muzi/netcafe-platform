package com.netcafe.platform.service.impl.session;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.entity.account.User;
import com.netcafe.platform.domain.entity.machine.Machine;
import com.netcafe.platform.domain.entity.session.SessionOrder;
import com.netcafe.platform.mapper.account.UserMapper;
import com.netcafe.platform.mapper.machine.MachineMapper;
import com.netcafe.platform.mapper.session.SessionOrderMapper;
import com.netcafe.platform.service.session.SessionBillingCalculator;
import com.netcafe.platform.service.session.SessionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceImpl extends ServiceImpl<SessionOrderMapper, SessionOrder> implements SessionService {
  private static final int MACHINE_IDLE = 0;
  private static final int MACHINE_USING = 1;
  private static final int MACHINE_DISABLED = 2;
  private static final int MACHINE_LOCKED = 3;
  private static final int SESSION_ONGOING = 0;
  private static final int SESSION_FINISHED = 1;
  private static final int SESSION_FORCED = 2;
  private static final int SESSION_PAUSED = 3;

  private final UserMapper userMapper;
  private final MachineMapper machineMapper;

  public SessionServiceImpl(UserMapper userMapper, MachineMapper machineMapper) {
    this.userMapper = userMapper;
    this.machineMapper = machineMapper;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long openSession(Long userId, Long machineId) {
    User user = userMapper.selectById(userId);
    if (user == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
    }
    if (user.getStatus() == null || user.getStatus() != 1) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "用户已冻结，无法开通上机");
    }
    Machine machine = machineMapper.selectById(machineId);
    if (machine == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "机位不存在");
    }
    if (machine.getStatus() == null || machine.getStatus() == MACHINE_DISABLED) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "机位已停用");
    }
    if (machine.getStatus() == MACHINE_USING || machine.getStatus() == MACHINE_LOCKED) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "机位正在使用中");
    }

    long userOngoing = baseMapper.selectCount(new LambdaQueryWrapper<SessionOrder>()
        .eq(SessionOrder::getUserId, userId)
        .in(SessionOrder::getStatus, List.of(SESSION_ONGOING, SESSION_PAUSED)));
    if (userOngoing > 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "用户已有进行中的上机订单");
    }

    long machineOngoing = baseMapper.selectCount(new LambdaQueryWrapper<SessionOrder>()
        .eq(SessionOrder::getMachineId, machineId)
        .in(SessionOrder::getStatus, List.of(SESSION_ONGOING, SESSION_PAUSED)));
    if (machineOngoing > 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "机位已有进行中的上机订单");
    }

    BigDecimal pricePerMin = machine.getPricePerMin() == null ? BigDecimal.ZERO : machine.getPricePerMin();
    BigDecimal balance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
    if (balance.compareTo(pricePerMin) < 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "用户余额不足，无法开通上机");
    }

    LocalDateTime now = LocalDateTime.now();
    SessionOrder order = new SessionOrder();
    order.setUserId(userId);
    order.setMachineId(machineId);
    order.setStartTime(now);
    order.setDurationMinutes(0);
    order.setPriceSnapshot(pricePerMin);
    order.setAmount(BigDecimal.ZERO);
    order.setBilledMinutes(0);
    order.setLastBilledTime(now);
    order.setPausedAt(null);
    order.setPausedDurationSeconds(0);
    order.setStatus(SESSION_ONGOING);
    order.setCreatedAt(now);
    order.setUpdatedAt(now);
    boolean saved = save(order);
    if (!saved) {
      return null;
    }

    machine.setStatus(MACHINE_USING);
    machine.setUpdatedAt(now);
    machineMapper.updateById(machine);

    user.setLastSessionTime(now);
    user.setUpdatedAt(now);
    userMapper.updateById(user);
    return order.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean forceEnd(Long sessionId, Long operatorAdminId) {
    SessionOrder order = getById(sessionId);
    if (order == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "上机订单不存在");
    }
    if (order.getStatus() == null || (order.getStatus() != SESSION_ONGOING && order.getStatus() != SESSION_PAUSED)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "仅进行中或暂停中的订单可强制下机");
    }
    User user = userMapper.selectById(order.getUserId());
    Machine machine = machineMapper.selectById(order.getMachineId());
    if (user == null || machine == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "订单关联数据异常");
    }

    completeSession(order, user, machine, SESSION_FORCED, operatorAdminId, LocalDateTime.now());
    return true;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public SessionOrder userEnd(Long userId) {
    SessionOrder order = getCurrentSessionByUser(userId);
    if (order == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "当前没有可下机的上机订单");
    }
    User user = userMapper.selectById(order.getUserId());
    Machine machine = machineMapper.selectById(order.getMachineId());
    if (user == null || machine == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "订单关联数据异常");
    }
    completeSession(order, user, machine, SESSION_FINISHED, null, LocalDateTime.now());
    return getById(order.getId());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public SessionOrder pauseSession(Long userId) {
    SessionOrder order = getOngoingSessionByUser(userId);
    if (order == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "当前没有可暂停的上机订单");
    }
    Machine machine = machineMapper.selectById(order.getMachineId());
    if (machine == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "订单关联机位异常");
    }

    LocalDateTime now = LocalDateTime.now();
    order.setDurationMinutes(SessionBillingCalculator.resolveDurationMinutes(order, now, true));
    order.setStatus(SESSION_PAUSED);
    order.setPausedAt(now);
    order.setUpdatedAt(now);
    updateById(order);

    machine.setStatus(MACHINE_LOCKED);
    machine.setUpdatedAt(now);
    machineMapper.updateById(machine);
    return getById(order.getId());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public SessionOrder resumeSession(Long sessionId) {
    SessionOrder order = getById(sessionId);
    if (order == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "上机订单不存在");
    }
    if (!Objects.equals(order.getStatus(), SESSION_PAUSED)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "当前订单不处于暂停状态");
    }
    Machine machine = machineMapper.selectById(order.getMachineId());
    if (machine == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "订单关联机位异常");
    }
    if (Objects.equals(machine.getStatus(), MACHINE_DISABLED)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "当前机位已停用，无法恢复上机");
    }

    LocalDateTime now = LocalDateTime.now();
    int pausedDurationSeconds = safeInt(order.getPausedDurationSeconds());
    if (order.getPausedAt() != null) {
      pausedDurationSeconds += (int) Math.max(Duration.between(order.getPausedAt(), now).getSeconds(), 0);
    }
    order.setPausedDurationSeconds(pausedDurationSeconds);
    order.setPausedAt(null);
    order.setStatus(SESSION_ONGOING);
    order.setUpdatedAt(now);
    baseMapper.update(null, new LambdaUpdateWrapper<SessionOrder>()
        .eq(SessionOrder::getId, order.getId())
        .set(SessionOrder::getPausedDurationSeconds, pausedDurationSeconds)
        .set(SessionOrder::getPausedAt, null)
        .set(SessionOrder::getStatus, SESSION_ONGOING)
        .set(SessionOrder::getUpdatedAt, now));

    machine.setStatus(MACHINE_USING);
    machine.setUpdatedAt(now);
    machineMapper.updateById(machine);
    return getById(order.getId());
  }

  @Override
  public SessionOrder getCurrentSessionByUser(Long userId) {
    if (userId == null) {
      return null;
    }
    return getOne(new LambdaQueryWrapper<SessionOrder>()
        .eq(SessionOrder::getUserId, userId)
        .in(SessionOrder::getStatus, List.of(SESSION_ONGOING, SESSION_PAUSED))
        .orderByDesc(SessionOrder::getStartTime)
        .last("LIMIT 1"), false);
  }

  @Override
  public SessionOrder getCurrentSessionByMachine(Long machineId) {
    if (machineId == null) {
      return null;
    }
    return getOne(new LambdaQueryWrapper<SessionOrder>()
        .eq(SessionOrder::getMachineId, machineId)
        .in(SessionOrder::getStatus, List.of(SESSION_ONGOING, SESSION_PAUSED))
        .orderByDesc(SessionOrder::getStartTime)
        .last("LIMIT 1"), false);
  }

  @Override
  public SessionOrder getLatestEndedSessionByUser(Long userId) {
    if (userId == null) {
      return null;
    }
    return getOne(new LambdaQueryWrapper<SessionOrder>()
        .eq(SessionOrder::getUserId, userId)
        .notIn(SessionOrder::getStatus, List.of(SESSION_ONGOING, SESSION_PAUSED))
        .orderByDesc(SessionOrder::getStartTime)
        .last("LIMIT 1"), false);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void billOngoingSessions() {
    LocalDateTime billingTime = LocalDateTime.now().withSecond(0).withNano(0);
    List<SessionOrder> ongoingOrders = list(new LambdaQueryWrapper<SessionOrder>()
        .eq(SessionOrder::getStatus, SESSION_ONGOING)
        .orderByAsc(SessionOrder::getStartTime));
    if (ongoingOrders.isEmpty()) {
      return;
    }

    Map<Long, User> userMap = userMapper.selectBatchIds(ongoingOrders.stream()
            .map(SessionOrder::getUserId)
            .distinct()
            .collect(Collectors.toList()))
        .stream()
        .collect(Collectors.toMap(User::getId, user -> user, (left, right) -> left));
    Map<Long, Machine> machineMap = machineMapper.selectBatchIds(ongoingOrders.stream()
            .map(SessionOrder::getMachineId)
            .distinct()
            .collect(Collectors.toList()))
        .stream()
        .collect(Collectors.toMap(Machine::getId, machine -> machine, (left, right) -> left));

    for (SessionOrder order : ongoingOrders) {
      User user = userMap.get(order.getUserId());
      Machine machine = machineMap.get(order.getMachineId());
      if (user == null || machine == null) {
        continue;
      }
      syncOngoingOrder(order, user, machine, billingTime);
    }
  }

  private void syncOngoingOrder(SessionOrder order, User user, Machine machine, LocalDateTime billingTime) {
    int currentMinutes = SessionBillingCalculator.resolveDurationMinutes(order, billingTime, true);
    int billedMinutes = safeInt(order.getBilledMinutes());
    if (currentMinutes <= billedMinutes) {
      return;
    }

    BigDecimal totalCharge = SessionBillingCalculator.resolveCharge(order.getPriceSnapshot(), currentMinutes);
    BigDecimal billedAmount = defaultMoney(order.getAmount());
    BigDecimal pendingCharge = totalCharge.subtract(billedAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    if (pendingCharge.compareTo(BigDecimal.ZERO) <= 0) {
      order.setDurationMinutes(currentMinutes);
      order.setBilledMinutes(currentMinutes);
      order.setLastBilledTime(billingTime);
      order.setUpdatedAt(billingTime);
      updateById(order);
      return;
    }

    BigDecimal balance = defaultMoney(user.getBalance());
    if (balance.compareTo(pendingCharge) < 0) {
      completeSession(order, user, machine, SESSION_FORCED, 0L, billingTime);
      return;
    }

    order.setDurationMinutes(currentMinutes);
    order.setBilledMinutes(currentMinutes);
    order.setLastBilledTime(billingTime);
    order.setAmount(billedAmount.add(pendingCharge).setScale(2, RoundingMode.HALF_UP));
    order.setUpdatedAt(billingTime);
    updateById(order);

    user.setBalance(balance.subtract(pendingCharge).setScale(2, RoundingMode.HALF_UP));
    user.setUpdatedAt(billingTime);
    userMapper.updateById(user);
  }

  private void completeSession(
      SessionOrder order,
      User user,
      Machine machine,
      int targetStatus,
      Long forceByAdminId,
      LocalDateTime endTime
  ) {
    int currentMinutes = SessionBillingCalculator.resolveDurationMinutes(order, endTime, true);
    BigDecimal totalCharge = SessionBillingCalculator.resolveCharge(order.getPriceSnapshot(), currentMinutes);
    BigDecimal billedAmount = defaultMoney(order.getAmount());
    BigDecimal pendingCharge = totalCharge.subtract(billedAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    BigDecimal balance = defaultMoney(user.getBalance());
    BigDecimal actualCharge = pendingCharge.min(balance).setScale(2, RoundingMode.HALF_UP);

    order.setEndTime(endTime);
    order.setDurationMinutes(currentMinutes);
    order.setLastBilledTime(endTime);
    order.setAmount(billedAmount.add(actualCharge).setScale(2, RoundingMode.HALF_UP));
    order.setPausedAt(null);
    order.setStatus(targetStatus);
    order.setForceByAdminId(Objects.equals(targetStatus, SESSION_FORCED) ? forceByAdminId : null);
    order.setUpdatedAt(endTime);
    baseMapper.update(null, new LambdaUpdateWrapper<SessionOrder>()
        .eq(SessionOrder::getId, order.getId())
        .set(SessionOrder::getEndTime, endTime)
        .set(SessionOrder::getDurationMinutes, currentMinutes)
        .set(SessionOrder::getLastBilledTime, endTime)
        .set(SessionOrder::getAmount, billedAmount.add(actualCharge).setScale(2, RoundingMode.HALF_UP))
        .set(SessionOrder::getPausedAt, null)
        .set(SessionOrder::getStatus, targetStatus)
        .set(SessionOrder::getForceByAdminId, Objects.equals(targetStatus, SESSION_FORCED) ? forceByAdminId : null)
        .set(SessionOrder::getUpdatedAt, endTime));

    user.setBalance(balance.subtract(actualCharge).setScale(2, RoundingMode.HALF_UP));
    user.setLastSessionTime(endTime);
    user.setUpdatedAt(endTime);
    userMapper.updateById(user);

    machine.setStatus(MACHINE_IDLE);
    machine.setUpdatedAt(endTime);
    machineMapper.updateById(machine);
  }

  private SessionOrder getOngoingSessionByUser(Long userId) {
    if (userId == null) {
      return null;
    }
    return getOne(new LambdaQueryWrapper<SessionOrder>()
        .eq(SessionOrder::getUserId, userId)
        .eq(SessionOrder::getStatus, SESSION_ONGOING)
        .orderByDesc(SessionOrder::getStartTime)
        .last("LIMIT 1"), false);
  }

  private BigDecimal defaultMoney(BigDecimal amount) {
    return amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
  }

  private int safeInt(Integer value) {
    return value == null ? 0 : value;
  }
}
