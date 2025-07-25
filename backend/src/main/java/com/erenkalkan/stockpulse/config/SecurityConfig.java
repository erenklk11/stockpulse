package com.erenkalkan.stockpulse.config;

import com.erenkalkan.stockpulse.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserService userService;

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authprovider = new DaoAuthenticationProvider();
    authprovider.setUserDetailsService(userService);
    authprovider.setPasswordEncoder(passwordEncoder());
    return authprovider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
