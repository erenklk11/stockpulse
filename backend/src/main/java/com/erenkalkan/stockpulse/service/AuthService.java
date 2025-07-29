package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.InvalidCredentialsException;
import com.erenkalkan.stockpulse.exception.InvalidInputException;
import com.erenkalkan.stockpulse.exception.UserAlreadyExistsException;
import com.erenkalkan.stockpulse.exception.UserNotFoundException;
import com.erenkalkan.stockpulse.model.dto.LoginRequestDTO;
import com.erenkalkan.stockpulse.model.dto.LoginResponseDTO;
import com.erenkalkan.stockpulse.model.dto.RegisterRequestDTO;
import com.erenkalkan.stockpulse.model.dto.RegisterResponseDTO;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final PasswordEncoder passwordEncoder;
  private final UserService userService;
  private final JwtService jwtService;

  public RegisterResponseDTO register(RegisterRequestDTO request) {

    if (request == null || request.getEmail() == null || request.getPassword() == null || request.getFirstName() == null) {
      throw new InvalidInputException("Required fields cannot be null");
    }

    if (userService.existsByEmail(request.getEmail())) {
      throw new UserAlreadyExistsException("User with email '" + request.getEmail() + "' already exists");
    }

    User newUser = User.builder()
            .firstName(request.getFirstName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.REGULAR_USER)
            .build();

    userService.saveUser(newUser);

    return RegisterResponseDTO.builder()
            .firstName(request.getFirstName())
            .email(request.getEmail())
            .build();
  }

  public LoginResponseDTO login(LoginRequestDTO request) {

    if (request == null || request.getEmail() == null || request.getPassword() == null) {
      throw new InvalidInputException("Required fields cannot be null");
    }

    Optional<User> foundUser = userService.findByEmail(request.getEmail());
    if (foundUser.isEmpty()) {
      throw new UserNotFoundException("User with email '" + request.getEmail() + "' does not exist");
    }

    User user = foundUser.get();

    if (user.getIsOAuthUser() != null && user.getIsOAuthUser()) {
      throw new InvalidCredentialsException("Please use Google login for this account");
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new InvalidCredentialsException("Password is incorrect");
    }

    String token = jwtService.generateToken(userService.loadUserByUsername(request.getEmail()));

    return LoginResponseDTO.builder()
            .firstName(user.getFirstName())
            .email(user.getEmail())
            .token(token)
            .build();
  }
}


