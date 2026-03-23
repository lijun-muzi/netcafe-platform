package com.netcafe.platform.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BillingScheduler {
  @Scheduled(fixedRate = 60000)
  public void billingByMinute() {
    // TODO: 扫描进行中订单并按分钟扣费
  }
}
