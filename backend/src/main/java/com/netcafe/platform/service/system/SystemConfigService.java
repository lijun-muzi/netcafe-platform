package com.netcafe.platform.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.netcafe.platform.domain.dto.system.SystemBasicSettingsUpdateRequest;
import com.netcafe.platform.domain.dto.system.SystemBasicSettingsView;
import com.netcafe.platform.domain.entity.system.SystemConfig;

public interface SystemConfigService extends IService<SystemConfig> {
  SystemBasicSettingsView getBasicSettings();

  boolean updateBasicSettings(SystemBasicSettingsUpdateRequest request);
}
