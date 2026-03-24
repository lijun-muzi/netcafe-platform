package com.netcafe.platform.service.impl.session;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.entity.account.User;
import com.netcafe.platform.domain.entity.machine.Machine;
import com.netcafe.platform.domain.entity.session.SessionOrder;
import com.netcafe.platform.mapper.account.UserMapper;
import com.netcafe.platform.mapper.machine.MachineMapper;
import com.netcafe.platform.mapper.session.SessionOrderMapper;
import com.netcafe.platform.service.session.SessionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceImpl extends ServiceImpl<SessionOrderMapper, SessionOrder> implements SessionService {
  private static final int MACHINE_IDLE = 0;
  private static final int MACHINE_USING = 1;
  private static final int MACHINE_DISABLED = 2;
  private static final int SESSION_ONGOING = 0;
  private static final int SESSION_FINISHED = 1;
  private static final int SESSION_FORCED = 2;

  private final UserMapper userMapper;
  private final MachineMapper machineMapper;

  public SessionServiceImpl(UserMapper userMapper, MachineMapper machineMapper) {
    this.userMapper = userMapper;
    this.machineMapper = machineMapper;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean openSession(Long userId, Long machineId) {
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
    if (machine.getStatus() == MACHINE_USING) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "机位正在使用中");
    }

    long userOngoing = baseMapper.selectCount(new LambdaQueryWrapper<SessionOrder>()
        .eq(SessionOrder::getUserId, userId)
        .eq(SessionOrder::getStatus, SESSION_ONGOING));
    if (userOngoing > 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "用户已有进行中的上机订单");
    }

    long machineOngoing = baseMapper.selectCount(new LambdaQueryWrapper<SessionOrder>()
        .eq(SessionOrder::getMachineId, machineId)
        .eq(SessionOrder::getStatus, SESSION_ONGOING));
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
    order.setStatus(SESSION_ONGOING);
    order.setCreatedAt(now);
    order.setUpdatedAt(now);
    boolean saved = save(order);
    if (!saved) {
      return false;
    }

    machine.setStatus(MACHINE_USING);
    machine.setUpdatedAt(now);
    machineMapper.updateById(machine);

    user.setLastSessionTime(now);
    user.setUpdatedAt(now);
    userMapper.updateById(user);
    return true;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean forceEnd(Long sessionId, Long operatorAdminId) {
    SessionOrder order = getById(sessionId);
    if (order == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "上机订单不存在");
    }
    if (order.getStatus() == null || order.getStatus() != SESSION_ONGOING) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "仅进行中的订单可强制下机");
    }
    User user = userMapper.selectById(order.getUserId());
    Machine machine = machineMapper.selectById(order.getMachineId());
    if (user == null || machine == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "订单关联数据异常");
    }

    LocalDateTime endTime = LocalDateTime.now();
    int elapsedMinutes = calculateDurationMinutes(order.getStartTime(), endTime, order.getDurationMinutes());
    BigDecimal charge = calculateCharge(order.getPriceSnapshot(), elapsedMinutes);
    BigDecimal balance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
    BigDecimal finalCharge = charge.min(balance);

    order.setEndTime(endTime);
    order.setDurationMinutes(elapsedMinutes);
    order.setBilledMinutes(elapsedMinutes);
    order.setLastBilledTime(endTime);
    order.setAmount(finalCharge);
    order.setStatus(SESSION_FORCED);
    order.setForceByAdminId(operatorAdminId);
    order.setUpdatedAt(endTime);
    boolean updated = updateById(order);
    if (!updated) {
      return false;
    }

    user.setBalance(balance.subtract(finalCharge));
    user.setLastSessionTime(endTime);
    user.setUpdatedAt(endTime);
    userMapper.updateById(user);

    machine.setStatus(MACHINE_IDLE);
    machine.setUpdatedAt(endTime);
    machineMapper.updateById(machine);
    return true;
  }

  private int calculateDurationMinutes(LocalDateTime startTime, LocalDateTime endTime, Integer storedMinutes) {
    int duration = storedMinutes == null ? 0 : storedMinutes;
    if (duration > 0) {
      return duration;
    }
    long minutes = Duration.between(startTime, endTime).toMinutes();
    return (int) Math.max(minutes, 1);
  }

  private BigDecimal calculateCharge(BigDecimal pricePerMin, int minutes) {
    BigDecimal price = pricePerMin == null ? BigDecimal.ZERO : pricePerMin;
    return price.multiply(BigDecimal.valueOf(minutes)).setScale(2, RoundingMode.HALF_UP);
  }
}
