package com.netcafe.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final RestAuthenticationEntryPoint authenticationEntryPoint;
  private final RestAccessDeniedHandler accessDeniedHandler;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthenticationFilter,
      RestAuthenticationEntryPoint authenticationEntryPoint,
      RestAccessDeniedHandler accessDeniedHandler
  ) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.authenticationEntryPoint = authenticationEntryPoint;
    this.accessDeniedHandler = accessDeniedHandler;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler)
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/actuator/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/sessions/user-end").hasRole("USER")
            .requestMatchers(HttpMethod.PUT, "/machines/*/price").hasRole("SUPER_ADMIN")
            .requestMatchers("/admins/**").hasRole("SUPER_ADMIN")
            .requestMatchers("/stats/**").hasRole("SUPER_ADMIN")
            .requestMatchers("/audit/**").hasRole("SUPER_ADMIN")
            .requestMatchers("/system/**").hasRole("SUPER_ADMIN")
            .requestMatchers("/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
            .requestMatchers("/machines/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
            .requestMatchers("/machine-templates/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
            .requestMatchers("/recharges/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
            .requestMatchers("/sessions/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable);
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
