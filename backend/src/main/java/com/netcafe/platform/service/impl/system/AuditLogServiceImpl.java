package com.netcafe.platform.service.impl.system;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.domain.entity.system.AuditLog;
import com.netcafe.platform.mapper.system.AuditLogMapper;
import com.netcafe.platform.service.system.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {
  private final ObjectMapper objectMapper;

  public AuditLogServiceImpl(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean record(
      Long operatorId,
      String operatorRole,
      String action,
      String targetType,
      Long targetId,
      Object beforeData,
      Object afterData
  ) {
    AuditLog auditLog = new AuditLog();
    auditLog.setOperatorId(operatorId);
    auditLog.setOperatorRole(operatorRole);
    auditLog.setAction(action);
    auditLog.setTargetType(targetType);
    auditLog.setTargetId(targetId);
    auditLog.setBeforeData(writeJson(beforeData));
    auditLog.setAfterData(writeJson(afterData));
    auditLog.setCreatedAt(LocalDateTime.now());
    return save(auditLog);
  }

  private String writeJson(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception ex) {
      return String.valueOf(value);
    }
  }
}
