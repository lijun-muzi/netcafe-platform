package com.netcafe.platform.service.impl.finance;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.domain.entity.finance.RechargeRecord;
import com.netcafe.platform.mapper.finance.RechargeRecordMapper;
import com.netcafe.platform.service.finance.RechargeService;
import org.springframework.stereotype.Service;

@Service
public class RechargeServiceImpl extends ServiceImpl<RechargeRecordMapper, RechargeRecord> implements RechargeService {
}
