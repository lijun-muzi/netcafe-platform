package com.netcafe.platform.auth;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
  @Test
  void bcryptHashShouldMatchRawPassword() {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String raw = "123456";
    String hash = encoder.encode(raw);
    assertTrue(encoder.matches(raw, hash));
  }
}
