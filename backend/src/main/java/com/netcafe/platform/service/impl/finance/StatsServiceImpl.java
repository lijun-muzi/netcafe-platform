package com.netcafe.platform.service.impl.finance;

import com.netcafe.platform.service.finance.StatsService;
import org.springframework.stereotype.Service;

@Service
public class StatsServiceImpl implements StatsService {
  @Override
  public Object overview(String start, String end) {
    // TODO: 按时间范围统计流水、时长、活跃用户、ARPU
    return null;
  }
}
