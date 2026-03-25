package com.netcafe.platform.schedule;

import com.netcafe.platform.service.session.SessionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BillingScheduler {
  private final SessionService sessionService;

  public BillingScheduler(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @Scheduled(fixedRate = 60000)
  public void billingByMinute() {
    sessionService.billOngoingSessions();
  }
}
