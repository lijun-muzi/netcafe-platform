package com.netcafe.platform.service.impl.system;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.domain.entity.system.SystemConfig;
import com.netcafe.platform.mapper.system.SystemConfigMapper;
import com.netcafe.platform.service.system.SystemConfigService;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {
}
