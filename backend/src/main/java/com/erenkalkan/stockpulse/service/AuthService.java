package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.model.dto.LoginRequestDTO;
import com.erenkalkan.stockpulse.model.dto.LoginResponseDTO;
import com.erenkalkan.stockpulse.model.dto.RegisterRequestDTO;
import com.erenkalkan.stockpulse.model.dto.RegisterResponseDTO;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.enums.Role;
import com.erenkalkan.stockpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public RegisterResponseDTO register(RegisterRequestDTO request) {

    if (request == null || request.getEmail() == null || request.getPassword() == null || request.getFirstName() == null) {
      throw new IllegalArgumentException("Required fields cannot be null");
    }

    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new IllegalStateException("User with email '" + request.getEmail() + "' already exists");
    }

    User newUser = User.builder()
            .firstName(request.getFirstName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.REGULAR_USER)
            .build();

    try {
      userRepository.save(newUser);
    } catch (Exception e) {
      throw new RuntimeException("Failed to save user to database", e);
    }

    return RegisterResponseDTO.builder()
            .firstName(request.getFirstName())
            .email(request.getEmail())
            .build();
  }

  public LoginResponseDTO login(LoginRequestDTO request) {

    if (request == null || request.getEmail() == null || request.getPassword() == null) {
      throw new IllegalArgumentException("Required fields cannot be null");
    }

    Optional<User> foundUser = userRepository.findByEmail(request.getEmail());
    if (foundUser.isEmpty()) {
      throw new IllegalStateException("User with email '" + request.getEmail() + "' does not exist");
    }

    if (!passwordEncoder.matches(request.getPassword(), foundUser.get().getPassword())) {
      throw new IllegalArgumentException("Password is wrong");
    }

    // TODO: Create JWT or OAuht2 token here

    return LoginResponseDTO.builder()
            .firstName(foundUser.get().getFirstName())
            .email(foundUser.get().getEmail())
            .token("WILL BE SET VIA SERVICE LATER")
            .build();
  }
}
