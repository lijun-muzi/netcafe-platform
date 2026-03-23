package com.netcafe.platform.service.impl.machine;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.domain.entity.machine.PriceChangeLog;
import com.netcafe.platform.mapper.machine.PriceChangeLogMapper;
import com.netcafe.platform.service.machine.PriceChangeLogService;
import org.springframework.stereotype.Service;

@Service
public class PriceChangeLogServiceImpl extends ServiceImpl<PriceChangeLogMapper, PriceChangeLog> implements PriceChangeLogService {
}
