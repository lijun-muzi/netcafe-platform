package com.netcafe.platform.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.netcafe.platform.domain.entity.system.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
