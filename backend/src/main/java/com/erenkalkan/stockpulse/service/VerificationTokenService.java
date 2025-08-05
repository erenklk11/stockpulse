package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.*;
import com.erenkalkan.stockpulse.model.dto.ResetPasswordRequestDTO;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.entity.VerificationToken;
import com.erenkalkan.stockpulse.model.enums.TokenType;
import com.erenkalkan.stockpulse.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {

  @Value("${app.jwt.expiration}")
  private Long tokenExpirationMs;

  private final PasswordEncoder passwordEncoder;
  private final UserService userService;
  private final JwtService jwtService;
  private final EmailService emailService;
  private final VerificationTokenRepository verificationTokenRepository;

  public VerificationToken save(VerificationToken token) {
    try {
      return verificationTokenRepository.save(token);
    } catch (Exception e) {
      throw new DatabaseOperationException("Failed to save verification token to database: " + e.getMessage(), e);
    }
  }

  public boolean sendForgotPasswordEmail(String email) {

    if (email == null || email.trim().isEmpty()) {
      throw new InvalidInputException("Email cannot be null");
    }

    Optional<User> foundUser = userService.findByEmail(email);
    if (foundUser.isEmpty()) {
      throw new UserNotFoundException("User with email " + email + " does not exist");
    }
    User user = foundUser.get();

    VerificationToken token = VerificationToken.builder()
            .tokenType(TokenType.PASSWORD_RESET)
            .user(user)
            .token(jwtService.generateToken(userService.loadUserByUsername(user.getUsername())))
            .expirationMs(tokenExpirationMs)
            .build();

    emailService.sendVerificationEmail(user.getEmail(), token);

    this.save(token);

    return true;
  }

  public boolean resetPassword(ResetPasswordRequestDTO request) {

    String token = request.getToken();
    verifyToken(token);
    VerificationToken verificationToken = verificationTokenRepository.findByToken(token).get();

    User user = verificationToken.getUser();
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    verificationToken.setUsed(true);

    userService.saveUser(user);
    verificationTokenRepository.save(verificationToken);

    return true;
  }

  public boolean verifyToken(String token) {

    if (token == null || token.trim().isEmpty()) {
      throw new InvalidJwtTokenException("Authentication token is required");
    }

    Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
    if (verificationToken.isEmpty()) {
      throw new InvalidJwtTokenException("Authentication token does not exist");
    }

    if (verificationToken.get().isExpired()) {
      throw new JwtTokenExpiredException("Authentication token is expired");
    }

    if (!verificationToken.get().isValid()) {
      throw new InvalidJwtTokenException("Authentication token is not valid");
    }

    return true;
  }
}
