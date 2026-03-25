package com.netcafe.platform.service.impl.system;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.domain.dto.system.SystemBasicSettingsUpdateRequest;
import com.netcafe.platform.domain.dto.system.SystemBasicSettingsView;
import com.netcafe.platform.domain.entity.system.SystemConfig;
import com.netcafe.platform.mapper.system.SystemConfigMapper;
import com.netcafe.platform.service.system.SystemConfigService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {
  private static final String KEY_DEFAULT_PRICE = "default_price_per_min";
  private static final String KEY_LOW_BALANCE_THRESHOLD = "low_balance_threshold_minutes";
  private static final BigDecimal DEFAULT_PRICE = new BigDecimal("0.10");
  private static final int DEFAULT_LOW_BALANCE_THRESHOLD = 60;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public SystemBasicSettingsView getBasicSettings() {
    Map<String, SystemConfig> configMap = ensureBasicConfigs().stream()
        .collect(Collectors.toMap(SystemConfig::getConfigKey, Function.identity(), (left, right) -> right));
    SystemBasicSettingsView view = new SystemBasicSettingsView();
    view.setDefaultPricePerMin(new BigDecimal(configMap.get(KEY_DEFAULT_PRICE).getConfigValue()));
    view.setLowBalanceThresholdMinutes(Integer.valueOf(configMap.get(KEY_LOW_BALANCE_THRESHOLD).getConfigValue()));
    return view;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean updateBasicSettings(SystemBasicSettingsUpdateRequest request) {
    Map<String, SystemConfig> configMap = ensureBasicConfigs().stream()
        .collect(Collectors.toMap(SystemConfig::getConfigKey, Function.identity(), (left, right) -> right));
    LocalDateTime now = LocalDateTime.now();
    SystemConfig defaultPriceConfig = configMap.get(KEY_DEFAULT_PRICE);
    defaultPriceConfig.setConfigValue(request.getDefaultPricePerMin().stripTrailingZeros().toPlainString());
    defaultPriceConfig.setUpdatedAt(now);
    SystemConfig thresholdConfig = configMap.get(KEY_LOW_BALANCE_THRESHOLD);
    thresholdConfig.setConfigValue(String.valueOf(request.getLowBalanceThresholdMinutes()));
    thresholdConfig.setUpdatedAt(now);
    return updateBatchById(List.of(defaultPriceConfig, thresholdConfig));
  }

  private List<SystemConfig> ensureBasicConfigs() {
    List<SystemConfig> configs = list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemConfig>()
        .in(SystemConfig::getConfigKey, List.of(KEY_DEFAULT_PRICE, KEY_LOW_BALANCE_THRESHOLD)));
    Map<String, SystemConfig> configMap = configs.stream()
        .collect(Collectors.toMap(SystemConfig::getConfigKey, Function.identity(), (left, right) -> right));
    LocalDateTime now = LocalDateTime.now();
    if (!configMap.containsKey(KEY_DEFAULT_PRICE)) {
      SystemConfig config = new SystemConfig();
      config.setConfigKey(KEY_DEFAULT_PRICE);
      config.setConfigValue(DEFAULT_PRICE.stripTrailingZeros().toPlainString());
      config.setDescription("默认单价（元/分钟）");
      config.setUpdatedAt(now);
      save(config);
      configs.add(config);
    }
    if (!configMap.containsKey(KEY_LOW_BALANCE_THRESHOLD)) {
      SystemConfig config = new SystemConfig();
      config.setConfigKey(KEY_LOW_BALANCE_THRESHOLD);
      config.setConfigValue(String.valueOf(DEFAULT_LOW_BALANCE_THRESHOLD));
      config.setDescription("余额提醒阈值（分钟）");
      config.setUpdatedAt(now);
      save(config);
      configs.add(config);
    }
    return configs;
  }
}
