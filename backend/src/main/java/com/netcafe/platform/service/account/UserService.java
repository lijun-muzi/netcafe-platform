package com.netcafe.platform.service.account;

import com.baomidou.mybatisplus.extension.service.IService;
import com.netcafe.platform.domain.entity.finance.RechargeRecord;
import com.netcafe.platform.domain.entity.account.User;
import java.math.BigDecimal;

public interface UserService extends IService<User> {
  RechargeRecord recharge(Long userId, BigDecimal amount, String channel, String remark, Long operatorAdminId);
}
