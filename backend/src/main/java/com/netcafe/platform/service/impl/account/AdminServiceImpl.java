package com.netcafe.platform.service.impl.account;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.domain.entity.account.Admin;
import com.netcafe.platform.mapper.account.AdminMapper;
import com.netcafe.platform.service.account.AdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
  private final PasswordEncoder passwordEncoder;

  public AdminServiceImpl(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public boolean save(Admin admin) {
    encodePasswordIfNeeded(admin);
    return super.save(admin);
  }

  @Override
  public boolean updateById(Admin admin) {
    encodePasswordIfNeeded(admin);
    return super.updateById(admin);
  }

  private void encodePasswordIfNeeded(Admin admin) {
    if (admin == null || admin.getPassword() == null || admin.getPassword().isBlank()) {
      return;
    }
    String password = admin.getPassword();
    if (isBcryptHash(password)) {
      return;
    }
    admin.setPassword(passwordEncoder.encode(password));
  }

  private boolean isBcryptHash(String value) {
    return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
  }
}
