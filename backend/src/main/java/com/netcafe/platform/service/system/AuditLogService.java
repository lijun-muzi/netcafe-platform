package com.netcafe.platform.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.netcafe.platform.domain.entity.system.AuditLog;

public interface AuditLogService extends IService<AuditLog> {
  String ACTION_UPDATE_SYSTEM_CONFIG = "保存基础配置";
  String ACTION_CREATE_MACHINE_TEMPLATE = "新增模板";
  String ACTION_UPDATE_MACHINE_TEMPLATE = "编辑模板";
  String ACTION_DELETE_MACHINE_TEMPLATE = "删除模板";
  String ACTION_UPDATE_MACHINE_PRICE = "机位调价";
  String ACTION_BATCH_CREATE_MACHINE = "批量建机位";
  String ACTION_OPEN_SESSION = "开通上机";
  String ACTION_FORCE_END_SESSION = "强制下机";
  String ACTION_RECHARGE = "线下充值";

  boolean record(
      Long operatorId,
      String operatorRole,
      String action,
      String targetType,
      Long targetId,
      Object beforeData,
      Object afterData
  );
}
