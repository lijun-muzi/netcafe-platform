package com.netcafe.platform.service.impl.finance;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.stats.StatsIdleMachineView;
import com.netcafe.platform.domain.dto.stats.StatsLowBalanceUserView;
import com.netcafe.platform.domain.dto.stats.StatsMachineTopView;
import com.netcafe.platform.domain.dto.stats.StatsMachineUsageView;
import com.netcafe.platform.domain.dto.stats.StatsOverviewView;
import com.netcafe.platform.domain.dto.stats.StatsTrendPointView;
import com.netcafe.platform.domain.dto.stats.StatsTrendResponse;
import com.netcafe.platform.domain.dto.stats.StatsUserTopView;
import com.netcafe.platform.domain.dto.stats.ranking.StatsRankingResponse;
import com.netcafe.platform.domain.dto.system.SystemBasicSettingsView;
import com.netcafe.platform.domain.entity.account.User;
import com.netcafe.platform.domain.entity.finance.RechargeRecord;
import com.netcafe.platform.domain.entity.machine.Machine;
import com.netcafe.platform.domain.entity.session.SessionOrder;
import com.netcafe.platform.mapper.account.UserMapper;
import com.netcafe.platform.mapper.finance.RechargeRecordMapper;
import com.netcafe.platform.mapper.machine.MachineMapper;
import com.netcafe.platform.mapper.session.SessionOrderMapper;
import com.netcafe.platform.service.finance.StatsService;
import com.netcafe.platform.service.session.SessionBillingCalculator;
import com.netcafe.platform.service.system.SystemConfigService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StatsServiceImpl implements StatsService {
  private static final int SESSION_ONGOING = 0;
  private static final int SESSION_FINISHED = 1;
  private static final int SESSION_FORCED = 2;
  private static final int USER_ACTIVE = 1;
  private static final int MACHINE_IDLE = 0;
  private static final int MACHINE_USING = 1;
  private static final int MACHINE_DISABLED = 2;
  private static final int DEFAULT_LIMIT = 5;
  private static final int MAX_LIMIT = 20;
  private static final int MAX_RANGE_DAYS = 366;
  private static final String GRANULARITY_DAY = "day";
  private static final String GRANULARITY_MONTH = "month";
  private static final DateTimeFormatter DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
  private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

  private final SessionOrderMapper sessionOrderMapper;
  private final RechargeRecordMapper rechargeRecordMapper;
  private final UserMapper userMapper;
  private final MachineMapper machineMapper;
  private final SystemConfigService systemConfigService;

  public StatsServiceImpl(
      SessionOrderMapper sessionOrderMapper,
      RechargeRecordMapper rechargeRecordMapper,
      UserMapper userMapper,
      MachineMapper machineMapper,
      SystemConfigService systemConfigService
  ) {
    this.sessionOrderMapper = sessionOrderMapper;
    this.rechargeRecordMapper = rechargeRecordMapper;
    this.userMapper = userMapper;
    this.machineMapper = machineMapper;
    this.systemConfigService = systemConfigService;
  }

  @Override
  public StatsOverviewView overview(LocalDate start, LocalDate end) {
    StatsContext context = buildStatsContext(start, end);
    StatsOverviewView view = new StatsOverviewView();
    view.setStartDate(context.range.startDate);
    view.setEndDate(context.range.endDate);
    view.setCashFlowAmount(context.totalCashFlow);
    view.setCashFlowLabel(formatMoney(context.totalCashFlow));
    view.setSessionRevenueAmount(context.totalSessionRevenue);
    view.setSessionRevenueLabel(formatMoney(context.totalSessionRevenue));
    view.setTotalDurationMinutes(context.totalDurationMinutes);
    view.setTotalDurationLabel(formatMinutes(context.totalDurationMinutes));
    view.setActiveUsers(context.activeUserIds.size());
    view.setActiveUsersLabel(context.activeUserIds.size() + " 人");
    view.setArpu(calculateArpu(context.totalSessionRevenue, context.activeUserIds.size()));
    view.setArpuLabel(formatMoney(view.getArpu()));
    view.setPeakHour(context.peakHourLabel);
    return view;
  }

  @Override
  public StatsTrendResponse trend(LocalDate start, LocalDate end, String granularity) {
    StatsContext context = buildStatsContext(start, end);
    String normalizedGranularity = normalizeGranularity(granularity);
    LinkedHashMap<String, TrendBucket> buckets = initTrendBuckets(context.range, normalizedGranularity);

    for (SessionOrder order : context.sessions) {
      String key = resolveBucketKey(order.getStartTime().toLocalDate(), normalizedGranularity);
      TrendBucket bucket = buckets.get(key);
      if (bucket == null) {
        continue;
      }
      bucket.sessionRevenue = bucket.sessionRevenue.add(resolveSessionAmount(order, context.range.referenceTime));
      bucket.durationMinutes += resolveSessionDuration(order, context.range.referenceTime);
      bucket.activeUserIds.add(order.getUserId());
    }
    for (RechargeRecord record : context.recharges) {
      String key = resolveBucketKey(record.getCreatedAt().toLocalDate(), normalizedGranularity);
      TrendBucket bucket = buckets.get(key);
      if (bucket == null) {
        continue;
      }
      bucket.cashFlow = bucket.cashFlow.add(defaultMoney(record.getAmount()));
      bucket.activeUserIds.add(record.getUserId());
    }

    List<StatsTrendPointView> points = buckets.values().stream()
        .map(bucket -> {
          StatsTrendPointView view = new StatsTrendPointView();
          view.setLabel(bucket.label);
          view.setCashFlowAmount(bucket.cashFlow);
          view.setCashFlowLabel(formatMoney(bucket.cashFlow));
          view.setSessionRevenueAmount(bucket.sessionRevenue);
          view.setSessionRevenueLabel(formatMoney(bucket.sessionRevenue));
          view.setDurationMinutes(bucket.durationMinutes);
          view.setDurationLabel(formatMinutes(bucket.durationMinutes));
          view.setActiveUsers(bucket.activeUserIds.size());
          view.setActiveUsersLabel(bucket.activeUserIds.size() + " 人");
          return view;
        })
        .collect(Collectors.toList());

    StatsTrendResponse response = new StatsTrendResponse();
    response.setGranularity(normalizedGranularity);
    response.setStartDate(context.range.startDate);
    response.setEndDate(context.range.endDate);
    response.setPoints(points);
    return response;
  }

  @Override
  public List<StatsMachineUsageView> machineUsage(LocalDate start, LocalDate end, Integer limit) {
    StatsContext context = buildStatsContext(start, end);
    int safeLimit = normalizeLimit(limit, 10);
    return loadEnabledMachinesForStats().stream()
        .map(machine -> toMachineUsageView(machine, context))
        .sorted(Comparator
            .comparing(StatsMachineUsageView::getUsageRate, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(StatsMachineUsageView::getRevenueAmount, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(StatsMachineUsageView::getMachineCode, Comparator.nullsLast(String::compareTo)))
        .limit(safeLimit)
        .collect(Collectors.toList());
  }

  @Override
  public List<StatsMachineTopView> machineTop(LocalDate start, LocalDate end, Integer limit) {
    StatsContext context = buildStatsContext(start, end);
    int safeLimit = normalizeLimit(limit, DEFAULT_LIMIT);
    return context.machineRevenueMap.entrySet().stream()
        .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
        .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
        .limit(safeLimit)
        .map(entry -> {
          Machine machine = context.machineMap.get(entry.getKey());
          StatsMachineTopView view = new StatsMachineTopView();
          view.setMachineId(entry.getKey());
          view.setMachineCode(machine == null ? null : machine.getCode());
          view.setRevenueAmount(entry.getValue().setScale(2, RoundingMode.HALF_UP));
          view.setRevenueLabel(formatMoney(entry.getValue()));
          view.setSessionCount(context.machineSessionCountMap.getOrDefault(entry.getKey(), 0));
          return view;
        })
        .collect(Collectors.toList());
  }

  @Override
  public List<StatsIdleMachineView> idleMachines(LocalDate start, LocalDate end, Integer limit) {
    StatsContext context = buildStatsContext(start, end);
    int safeLimit = normalizeLimit(limit, DEFAULT_LIMIT);
    return loadEnabledMachinesForStats().stream()
        .map(machine -> toIdleMachineView(machine, context))
        .sorted(Comparator
            .comparing(StatsIdleMachineView::getIdleMinutes, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(StatsIdleMachineView::getMachineCode, Comparator.nullsLast(String::compareTo)))
        .limit(safeLimit)
        .collect(Collectors.toList());
  }

  @Override
  public List<StatsUserTopView> userTop(LocalDate start, LocalDate end, Integer limit) {
    StatsContext context = buildStatsContext(start, end);
    int safeLimit = normalizeLimit(limit, DEFAULT_LIMIT);
    return context.userAmountMap.entrySet().stream()
        .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
        .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
        .limit(safeLimit)
        .map(entry -> {
          User user = context.userMap.get(entry.getKey());
          StatsUserTopView view = new StatsUserTopView();
          view.setUserId(entry.getKey());
          view.setUserName(user == null ? null : user.getName());
          view.setMobileMasked(maskMobile(user == null ? null : user.getMobile()));
          view.setTotalAmount(entry.getValue().setScale(2, RoundingMode.HALF_UP));
          view.setTotalAmountLabel(formatMoney(entry.getValue()));
          int durationMinutes = context.userDurationMap.getOrDefault(entry.getKey(), 0);
          view.setTotalDurationMinutes(durationMinutes);
          view.setTotalDurationLabel(formatMinutes(durationMinutes));
          return view;
        })
        .collect(Collectors.toList());
  }

  @Override
  public List<StatsLowBalanceUserView> lowBalance(Integer thresholdMinutes, Integer limit) {
    int safeLimit = normalizeLimit(limit, DEFAULT_LIMIT);
    SystemBasicSettingsView basicSettings = systemConfigService.getBasicSettings();
    int effectiveThreshold = thresholdMinutes == null || thresholdMinutes <= 0
        ? basicSettings.getLowBalanceThresholdMinutes()
        : thresholdMinutes;
    BigDecimal defaultPrice = basicSettings.getDefaultPricePerMin();
    return userMapper.selectList(new LambdaQueryWrapper<User>()
            .eq(User::getStatus, USER_ACTIVE)
            .orderByAsc(User::getBalance))
        .stream()
        .map(user -> toLowBalanceUserView(user, defaultPrice))
        .filter(view -> view.getRemainingMinutes() <= effectiveThreshold)
        .limit(safeLimit)
        .collect(Collectors.toList());
  }

  @Override
  public StatsRankingResponse rankings(LocalDate start, LocalDate end, Integer limit, Integer thresholdMinutes) {
    StatsRankingResponse response = new StatsRankingResponse();
    response.setMachineRevenueTop(machineTop(start, end, limit));
    response.setUserConsumeTop(userTop(start, end, limit));
    response.setLowBalanceUsers(lowBalance(thresholdMinutes, limit));
    return response;
  }

  private StatsContext buildStatsContext(LocalDate start, LocalDate end) {
    TimeRange range = resolveRange(start, end);
    StatsContext context = new StatsContext();
    context.range = range;
    context.sessions = sessionOrderMapper.selectList(new LambdaQueryWrapper<SessionOrder>()
        .ge(SessionOrder::getStartTime, range.startAt)
        .lt(SessionOrder::getStartTime, range.endExclusive)
        .in(SessionOrder::getStatus, List.of(SESSION_ONGOING, SESSION_FINISHED, SESSION_FORCED))
        .orderByAsc(SessionOrder::getStartTime));
    context.recharges = rechargeRecordMapper.selectList(new LambdaQueryWrapper<RechargeRecord>()
        .ge(RechargeRecord::getCreatedAt, range.startAt)
        .lt(RechargeRecord::getCreatedAt, range.endExclusive)
        .orderByAsc(RechargeRecord::getCreatedAt));

    Set<Long> machineIds = new HashSet<>();
    Set<Long> userIds = new HashSet<>();
    Map<Integer, Integer> hourlyDurationMap = new HashMap<>();

    for (SessionOrder order : context.sessions) {
      int durationMinutes = resolveSessionDuration(order, range.referenceTime);
      BigDecimal amount = resolveSessionAmount(order, range.referenceTime);
      context.totalDurationMinutes += durationMinutes;
      context.totalSessionRevenue = context.totalSessionRevenue.add(amount);
      context.activeUserIds.add(order.getUserId());
      context.machineRevenueMap.merge(order.getMachineId(), amount, BigDecimal::add);
      context.machineDurationMap.merge(order.getMachineId(), durationMinutes, Integer::sum);
      context.machineSessionCountMap.merge(order.getMachineId(), 1, Integer::sum);
      context.userAmountMap.merge(order.getUserId(), amount, BigDecimal::add);
      context.userDurationMap.merge(order.getUserId(), durationMinutes, Integer::sum);
      machineIds.add(order.getMachineId());
      userIds.add(order.getUserId());
      hourlyDurationMap.merge(order.getStartTime().getHour(), durationMinutes, Integer::sum);
    }

    for (RechargeRecord record : context.recharges) {
      context.totalCashFlow = context.totalCashFlow.add(defaultMoney(record.getAmount()));
      context.activeUserIds.add(record.getUserId());
      userIds.add(record.getUserId());
    }

    context.userMap = loadUsersByIds(userIds);
    context.machineMap = loadMachinesByIds(machineIds);
    context.peakHourLabel = resolvePeakHourLabel(hourlyDurationMap);
    return context;
  }

  private TimeRange resolveRange(LocalDate start, LocalDate end) {
    LocalDate today = LocalDate.now();
    LocalDate startDate = start == null ? today.withDayOfMonth(1) : start;
    LocalDate endDate = end == null ? today : end;
    if (endDate.isBefore(startDate)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "end不能早于start");
    }
    long days = Duration.between(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()).toDays();
    if (days > MAX_RANGE_DAYS) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "统计区间不能超过366天");
    }
    TimeRange range = new TimeRange();
    range.startDate = startDate;
    range.endDate = endDate;
    range.startAt = startDate.atStartOfDay();
    range.endExclusive = endDate.plusDays(1).atStartOfDay();
    range.referenceTime = LocalDateTime.now().isBefore(range.endExclusive) ? LocalDateTime.now() : range.endExclusive;
    range.totalMinutes = (int) Math.max(Duration.between(range.startAt, range.endExclusive).toMinutes(), 1);
    return range;
  }

  private String normalizeGranularity(String granularity) {
    if (GRANULARITY_MONTH.equalsIgnoreCase(granularity)) {
      return GRANULARITY_MONTH;
    }
    return GRANULARITY_DAY;
  }

  private LinkedHashMap<String, TrendBucket> initTrendBuckets(TimeRange range, String granularity) {
    LinkedHashMap<String, TrendBucket> buckets = new LinkedHashMap<>();
    if (GRANULARITY_MONTH.equals(granularity)) {
      YearMonth cursor = YearMonth.from(range.startDate);
      YearMonth endMonth = YearMonth.from(range.endDate);
      while (!cursor.isAfter(endMonth)) {
        String key = cursor.format(MONTH_LABEL_FORMATTER);
        buckets.put(key, new TrendBucket(key));
        cursor = cursor.plusMonths(1);
      }
      return buckets;
    }
    LocalDate cursor = range.startDate;
    while (!cursor.isAfter(range.endDate)) {
      String key = cursor.format(DAY_LABEL_FORMATTER);
      buckets.put(key, new TrendBucket(key));
      cursor = cursor.plusDays(1);
    }
    return buckets;
  }

  private String resolveBucketKey(LocalDate date, String granularity) {
    if (GRANULARITY_MONTH.equals(granularity)) {
      return YearMonth.from(date).format(MONTH_LABEL_FORMATTER);
    }
    return date.format(DAY_LABEL_FORMATTER);
  }

  private StatsMachineUsageView toMachineUsageView(Machine machine, StatsContext context) {
    int usageMinutes = context.machineDurationMap.getOrDefault(machine.getId(), 0);
    BigDecimal usageRate = BigDecimal.valueOf(usageMinutes)
        .divide(BigDecimal.valueOf(context.range.totalMinutes), 4, RoundingMode.HALF_UP);
    StatsMachineUsageView view = new StatsMachineUsageView();
    view.setMachineId(machine.getId());
    view.setMachineCode(machine.getCode());
    view.setUsageMinutes(usageMinutes);
    view.setUsageMinutesLabel(formatMinutes(usageMinutes));
    view.setUsageRate(usageRate);
    view.setUsageRateLabel(formatRate(usageRate));
    BigDecimal revenue = context.machineRevenueMap.getOrDefault(machine.getId(), BigDecimal.ZERO);
    view.setRevenueAmount(revenue.setScale(2, RoundingMode.HALF_UP));
    view.setRevenueLabel(formatMoney(revenue));
    return view;
  }

  private StatsIdleMachineView toIdleMachineView(Machine machine, StatsContext context) {
    int usageMinutes = context.machineDurationMap.getOrDefault(machine.getId(), 0);
    int idleMinutes = Math.max(context.range.totalMinutes - Math.min(usageMinutes, context.range.totalMinutes), 0);
    BigDecimal idleRate = BigDecimal.valueOf(idleMinutes)
        .divide(BigDecimal.valueOf(context.range.totalMinutes), 4, RoundingMode.HALF_UP);
    StatsIdleMachineView view = new StatsIdleMachineView();
    view.setMachineId(machine.getId());
    view.setMachineCode(machine.getCode());
    view.setUsageMinutes(usageMinutes);
    view.setUsageMinutesLabel(formatMinutes(usageMinutes));
    view.setIdleMinutes(idleMinutes);
    view.setIdleMinutesLabel(formatMinutes(idleMinutes));
    view.setIdleRate(idleRate);
    view.setIdleRateLabel(formatRate(idleRate));
    return view;
  }

  private StatsLowBalanceUserView toLowBalanceUserView(User user, BigDecimal defaultPrice) {
    int remainingMinutes = SessionBillingCalculator.resolveRemainingMinutes(user.getBalance(), defaultPrice);
    StatsLowBalanceUserView view = new StatsLowBalanceUserView();
    view.setUserId(user.getId());
    view.setUserName(user.getName());
    view.setMobileMasked(maskMobile(user.getMobile()));
    view.setBalance(defaultMoney(user.getBalance()));
    view.setBalanceLabel(formatMoney(user.getBalance()));
    view.setRemainingMinutes(remainingMinutes);
    view.setRemainingMinutesLabel(remainingMinutes + " 分钟");
    return view;
  }

  private List<Machine> loadMachinesForStats() {
    return machineMapper.selectList(new LambdaQueryWrapper<Machine>()
        .in(Machine::getStatus, List.of(MACHINE_IDLE, MACHINE_USING, MACHINE_DISABLED))
        .orderByAsc(Machine::getCode));
  }

  private List<Machine> loadEnabledMachinesForStats() {
    return machineMapper.selectList(new LambdaQueryWrapper<Machine>()
        .in(Machine::getStatus, List.of(MACHINE_IDLE, MACHINE_USING))
        .orderByAsc(Machine::getCode));
  }

  private Map<Long, User> loadUsersByIds(Set<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return new HashMap<>();
    }
    return userMapper.selectBatchIds(userIds).stream()
        .collect(Collectors.toMap(User::getId, user -> user, (left, right) -> right));
  }

  private Map<Long, Machine> loadMachinesByIds(Set<Long> machineIds) {
    if (machineIds == null || machineIds.isEmpty()) {
      return new HashMap<>();
    }
    return machineMapper.selectBatchIds(machineIds).stream()
        .collect(Collectors.toMap(Machine::getId, machine -> machine, (left, right) -> right));
  }

  private int resolveSessionDuration(SessionOrder order, LocalDateTime referenceTime) {
    return SessionBillingCalculator.resolveDurationMinutes(order, referenceTime, isOngoing(order));
  }

  private BigDecimal resolveSessionAmount(SessionOrder order, LocalDateTime referenceTime) {
    if (isOngoing(order)) {
      return SessionBillingCalculator.resolveLiveCharge(order, referenceTime, true);
    }
    return defaultMoney(order.getAmount());
  }

  private boolean isOngoing(SessionOrder order) {
    return Objects.equals(order.getStatus(), SESSION_ONGOING);
  }

  private BigDecimal calculateArpu(BigDecimal totalRevenue, int activeUsers) {
    if (activeUsers <= 0) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    return defaultMoney(totalRevenue)
        .divide(BigDecimal.valueOf(activeUsers), 2, RoundingMode.HALF_UP);
  }

  private int normalizeLimit(Integer limit, int fallback) {
    if (limit == null) {
      return fallback;
    }
    return Math.max(1, Math.min(limit, MAX_LIMIT));
  }

  private String resolvePeakHourLabel(Map<Integer, Integer> hourlyDurationMap) {
    return hourlyDurationMap.entrySet().stream()
        .max(Map.Entry.<Integer, Integer>comparingByValue()
            .thenComparing(Map.Entry.comparingByKey()))
        .map(entry -> String.format("%02d:00", entry.getKey()))
        .orElse("--");
  }

  private BigDecimal defaultMoney(BigDecimal value) {
    return value == null
        ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        : value.setScale(2, RoundingMode.HALF_UP);
  }

  private String formatMoney(BigDecimal amount) {
    return "¥" + defaultMoney(amount);
  }

  private String formatMinutes(int minutes) {
    return minutes + " 分钟";
  }

  private String formatRate(BigDecimal rate) {
    return rate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) + "%";
  }

  private String maskMobile(String mobile) {
    if (mobile == null || mobile.length() < 7) {
      return mobile;
    }
    return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
  }

  private static final class TimeRange {
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime startAt;
    private LocalDateTime endExclusive;
    private LocalDateTime referenceTime;
    private int totalMinutes;
  }

  private static final class TrendBucket {
    private final String label;
    private BigDecimal cashFlow = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private BigDecimal sessionRevenue = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private int durationMinutes = 0;
    private final Set<Long> activeUserIds = new HashSet<>();

    private TrendBucket(String label) {
      this.label = label;
    }
  }

  private static final class StatsContext {
    private TimeRange range;
    private List<SessionOrder> sessions = new ArrayList<>();
    private List<RechargeRecord> recharges = new ArrayList<>();
    private Map<Long, User> userMap = new HashMap<>();
    private Map<Long, Machine> machineMap = new HashMap<>();
    private final Map<Long, BigDecimal> machineRevenueMap = new HashMap<>();
    private final Map<Long, Integer> machineDurationMap = new HashMap<>();
    private final Map<Long, Integer> machineSessionCountMap = new HashMap<>();
    private final Map<Long, BigDecimal> userAmountMap = new HashMap<>();
    private final Map<Long, Integer> userDurationMap = new HashMap<>();
    private final Set<Long> activeUserIds = new HashSet<>();
    private BigDecimal totalCashFlow = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private BigDecimal totalSessionRevenue = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private int totalDurationMinutes = 0;
    private String peakHourLabel = "--";
  }
}
