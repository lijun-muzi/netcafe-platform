package com.netcafe.platform.service.account;

import com.baomidou.mybatisplus.extension.service.IService;
import com.netcafe.platform.domain.entity.account.User;
import java.math.BigDecimal;

public interface UserService extends IService<User> {
  boolean recharge(Long userId, BigDecimal amount, String channel, String remark, Long operatorAdminId);
}
