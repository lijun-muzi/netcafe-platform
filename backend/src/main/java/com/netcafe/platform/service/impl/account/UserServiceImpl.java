package com.netcafe.platform.service.impl.account;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.domain.entity.account.User;
import com.netcafe.platform.domain.entity.finance.RechargeRecord;
import com.netcafe.platform.mapper.finance.RechargeRecordMapper;
import com.netcafe.platform.mapper.account.UserMapper;
import com.netcafe.platform.service.account.UserService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
  private final RechargeRecordMapper rechargeRecordMapper;

  public UserServiceImpl(RechargeRecordMapper rechargeRecordMapper) {
    this.rechargeRecordMapper = rechargeRecordMapper;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public RechargeRecord recharge(Long userId, BigDecimal amount, String channel, String remark, Long operatorAdminId) {
    User user = getById(userId);
    if (user == null) {
      return null;
    }
    BigDecimal currentBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
    user.setBalance(currentBalance.add(amount));
    user.setUpdatedAt(LocalDateTime.now());
    boolean updated = updateById(user);
    if (!updated) {
      return null;
    }

    RechargeRecord record = new RechargeRecord();
    record.setUserId(userId);
    record.setAmount(amount);
    record.setChannel(channel);
    record.setRemark(remark);
    record.setOperatorAdminId(operatorAdminId);
    record.setCreatedAt(LocalDateTime.now());
    rechargeRecordMapper.insert(record);

    return record;
  }
}
