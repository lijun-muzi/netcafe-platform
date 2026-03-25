package com.netcafe.platform.service.session;

import com.baomidou.mybatisplus.extension.service.IService;
import com.netcafe.platform.domain.entity.session.SessionOrder;

public interface SessionService extends IService<SessionOrder> {
  Long openSession(Long userId, Long machineId);

  boolean forceEnd(Long sessionId, Long operatorAdminId);

  SessionOrder userEnd(Long userId);

  SessionOrder pauseSession(Long userId);

  SessionOrder resumeSession(Long sessionId);

  SessionOrder getCurrentSessionByUser(Long userId);

  SessionOrder getCurrentSessionByMachine(Long machineId);

  SessionOrder getLatestEndedSessionByUser(Long userId);

  void billOngoingSessions();
}
