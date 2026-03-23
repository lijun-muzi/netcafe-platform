package com.netcafe.platform.service.impl.system;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.domain.entity.system.AuditLog;
import com.netcafe.platform.mapper.system.AuditLogMapper;
import com.netcafe.platform.service.system.AuditLogService;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {
}
