package com.netcafe.platform.service.session;

import com.netcafe.platform.domain.entity.session.SessionOrder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

public final class SessionBillingCalculator {
  private static final int SESSION_PAUSED = 3;

  private SessionBillingCalculator() {
  }

  public static int resolveDurationMinutes(SessionOrder order, LocalDateTime referenceTime, boolean ongoing) {
    if (order == null || order.getStartTime() == null) {
      return 0;
    }
    if (!ongoing && order.getDurationMinutes() != null && order.getDurationMinutes() > 0) {
      return order.getDurationMinutes();
    }
    LocalDateTime endTime = resolveEffectiveEndTime(order, referenceTime, ongoing);
    long elapsedSeconds = Math.max(Duration.between(order.getStartTime(), endTime).getSeconds(), 0);
    long pausedSeconds = safeInt(order.getPausedDurationSeconds());
    int elapsedMinutes = (int) Math.max((elapsedSeconds - pausedSeconds) / 60, 0);
    int storedDuration = order.getDurationMinutes() == null ? 0 : order.getDurationMinutes();
    int billedMinutes = order.getBilledMinutes() == null ? 0 : order.getBilledMinutes();
    return Math.max(elapsedMinutes, Math.max(storedDuration, billedMinutes));
  }

  public static BigDecimal resolveCharge(BigDecimal pricePerMin, int durationMinutes) {
    BigDecimal price = normalizePrice(pricePerMin);
    return price.multiply(BigDecimal.valueOf(Math.max(durationMinutes, 0)))
        .setScale(2, RoundingMode.HALF_UP);
  }

  public static BigDecimal resolveLiveCharge(SessionOrder order, LocalDateTime referenceTime, boolean ongoing) {
    if (order == null) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    if (!ongoing && order.getAmount() != null) {
      return order.getAmount().setScale(2, RoundingMode.HALF_UP);
    }
    return resolveCharge(order.getPriceSnapshot(), resolveDurationMinutes(order, referenceTime, ongoing));
  }

  public static int resolveRemainingMinutes(BigDecimal balance, BigDecimal pricePerMin) {
    BigDecimal price = normalizePrice(pricePerMin);
    if (price.compareTo(BigDecimal.ZERO) <= 0) {
      return 0;
    }
    BigDecimal normalizedBalance = balance == null ? BigDecimal.ZERO : balance.max(BigDecimal.ZERO);
    return normalizedBalance.divideToIntegralValue(price).intValue();
  }

  public static BigDecimal normalizePrice(BigDecimal pricePerMin) {
    if (pricePerMin == null || pricePerMin.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }
    return pricePerMin.setScale(4, RoundingMode.HALF_UP);
  }

  private static LocalDateTime resolveEffectiveEndTime(
      SessionOrder order,
      LocalDateTime referenceTime,
      boolean ongoing
  ) {
    LocalDateTime endTime = ongoing ? referenceTime : order.getEndTime();
    if (endTime == null) {
      endTime = referenceTime;
    }
    if (order.getPausedAt() != null
        && order.getStatus() != null
        && order.getStatus() == SESSION_PAUSED
        && order.getPausedAt().isBefore(endTime)) {
      return order.getPausedAt();
    }
    return endTime;
  }

  private static int safeInt(Integer value) {
    return value == null ? 0 : value;
  }
}
