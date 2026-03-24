package com.netcafe.platform.service.session;

import com.baomidou.mybatisplus.extension.service.IService;
import com.netcafe.platform.domain.entity.session.SessionOrder;

public interface SessionService extends IService<SessionOrder> {
  boolean openSession(Long userId, Long machineId);

  boolean forceEnd(Long sessionId, Long operatorAdminId);
}
