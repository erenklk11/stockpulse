package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.model.dto.RegisterRequestDTO;
import com.erenkalkan.stockpulse.model.dto.RegisterResponseDTO;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.enums.Role;
import com.erenkalkan.stockpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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


}
