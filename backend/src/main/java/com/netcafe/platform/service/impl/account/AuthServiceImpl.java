package com.netcafe.platform.service.impl.account;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.auth.AdminLoginResponse;
import com.netcafe.platform.domain.dto.auth.AdminProfile;
import com.netcafe.platform.domain.dto.auth.UserLoginResponse;
import com.netcafe.platform.domain.dto.auth.UserProfile;
import com.netcafe.platform.domain.entity.account.Admin;
import com.netcafe.platform.domain.entity.account.User;
import com.netcafe.platform.mapper.account.AdminMapper;
import com.netcafe.platform.mapper.account.UserMapper;
import com.netcafe.platform.service.account.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
  private static final String TOKEN_TYPE = "Bearer";
  private static final String ROLE_USER = "USER";

  private final AdminMapper adminMapper;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final String jwtSecret;
  private final long jwtExpiresIn;
  private final int maxFailures;
  private final long lockMinutes;
  private final Map<String, LoginFailRecord> adminLoginFails = new ConcurrentHashMap<>();

  public AuthServiceImpl(
      AdminMapper adminMapper,
      UserMapper userMapper,
      PasswordEncoder passwordEncoder,
      @Value("${app.jwt.secret:netcafe-secret-change-me-please-32b}") String jwtSecret,
      @Value("${app.jwt.expires-in:7200}") long jwtExpiresIn,
      @Value("${app.login.max-failures:5}") int maxFailures,
      @Value("${app.login.lock-minutes:10}") long lockMinutes
  ) {
    this.adminMapper = adminMapper;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.jwtSecret = jwtSecret;
    this.jwtExpiresIn = jwtExpiresIn;
    this.maxFailures = maxFailures;
    this.lockMinutes = lockMinutes;
  }

  @Override
  public AdminLoginResponse loginAdmin(String username, String password) {
    ensureNotLocked(username);
    Admin admin = adminMapper.selectOne(
        new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username)
    );
    if (admin == null || !passwordMatches(password, admin.getPassword())) {
      recordFailure(username);
      throw new BusinessException(ResultCode.AUTH_FAILED, "账号或密码错误");
    }

    admin.setLastLoginTime(LocalDateTime.now());
    adminMapper.updateById(admin);
    clearFailures(username);

    AdminLoginResponse response = new AdminLoginResponse();
    response.setToken(generateToken(String.valueOf(admin.getId()), admin.getUsername(), admin.getRole()));
    response.setTokenType(TOKEN_TYPE);
    response.setExpiresIn(jwtExpiresIn);
    response.setRole(admin.getRole());
    response.setAdmin(buildAdminProfile(admin));
    return response;
  }

  @Override
  public UserLoginResponse loginUser(String idCard) {
    User user = userMapper.selectOne(
        new LambdaQueryWrapper<User>().eq(User::getIdCard, idCard)
    );
    if (user == null || user.getStatus() == null || user.getStatus() != 1) {
      throw new BusinessException(ResultCode.AUTH_FAILED, "身份证不存在或已冻结");
    }

    user.setLastLoginTime(LocalDateTime.now());
    userMapper.updateById(user);

    UserLoginResponse response = new UserLoginResponse();
    response.setToken(generateToken(String.valueOf(user.getId()), user.getIdCard(), ROLE_USER));
    response.setTokenType(TOKEN_TYPE);
    response.setExpiresIn(jwtExpiresIn);
    response.setUser(buildUserProfile(user));
    return response;
  }

  @Override
  public void logout() {
    // JWT 为无状态，登出由前端清除 token 即可
  }

  private boolean passwordMatches(String rawPassword, String storedPassword) {
    if (storedPassword == null) {
      return false;
    }
    if (isBcryptHash(storedPassword)) {
      return passwordEncoder.matches(rawPassword, storedPassword);
    }
    return storedPassword.equals(rawPassword);
  }

  private boolean isBcryptHash(String value) {
    return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
  }

  private String generateToken(String subject, String username, String role) {
    Instant now = Instant.now();
    Date issuedAt = Date.from(now);
    Date expiresAt = Date.from(now.plusSeconds(jwtExpiresIn));
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    return Jwts.builder()
        .setSubject(subject)
        .claim("username", username)
        .claim("role", role)
        .setIssuedAt(issuedAt)
        .setExpiration(expiresAt)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  private AdminProfile buildAdminProfile(Admin admin) {
    AdminProfile profile = new AdminProfile();
    profile.setId(admin.getId());
    profile.setUsername(admin.getUsername());
    profile.setName(admin.getName());
    profile.setRole(admin.getRole());
    return profile;
  }

  private UserProfile buildUserProfile(User user) {
    UserProfile profile = new UserProfile();
    profile.setId(user.getId());
    profile.setName(user.getName());
    profile.setMobile(user.getMobile());
    profile.setIdCard(maskIdCard(user.getIdCard()));
    return profile;
  }

  private String maskIdCard(String idCard) {
    if (idCard == null || idCard.length() < 8) {
      return idCard;
    }
    int prefix = 4;
    int suffix = 2;
    String start = idCard.substring(0, prefix);
    String end = idCard.substring(idCard.length() - suffix);
    return start + "****" + end;
  }

  private void ensureNotLocked(String username) {
    LoginFailRecord record = adminLoginFails.get(username);
    if (record == null || record.lockUntil == null) {
      return;
    }
    Instant now = Instant.now();
    if (record.lockUntil.isAfter(now)) {
      long remainSeconds = Duration.between(now, record.lockUntil).getSeconds();
      if (remainSeconds < 1) {
        remainSeconds = 1;
      }
      throw new BusinessException(ResultCode.AUTH_FAILED, "账号已锁定，请" + remainSeconds + "s后再试");
    }
    adminLoginFails.remove(username);
  }

  private void recordFailure(String username) {
    LoginFailRecord record = adminLoginFails.computeIfAbsent(username, key -> new LoginFailRecord());
    record.count++;
    if (record.count >= maxFailures) {
      record.lockUntil = Instant.now().plusSeconds(lockMinutes * 60);
      record.count = 0;
    }
  }

  private void clearFailures(String username) {
    adminLoginFails.remove(username);
  }

  private static class LoginFailRecord {
    private int count;
    private Instant lockUntil;
  }
}
