package com.erenkalkan.stockpulse.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.erenkalkan.stockpulse.exception.InvalidCredentialsException;
import com.erenkalkan.stockpulse.model.dto.LoginResponseDTO;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthService {

    private final UserService userService;
    private final JwtService jwtService;

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    public LoginResponseDTO authenticateWithGoogle(String authorizationCode) {
        try {
            // Exchange authorization code for tokens
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    clientId,
                    clientSecret,
                    authorizationCode,
                    redirectUri
            ).execute();

            // Verify and extract user info from ID token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(tokenResponse.getIdToken());
            if (idToken == null) {
                throw new InvalidCredentialsException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String profilePicture = (String) payload.get("picture");

            if (!payload.getEmailVerified()) {
                throw new InvalidCredentialsException("Google email not verified");
            }

            // Find or create user
            User user = findOrCreateUser(email, firstName, profilePicture);

            // Generate JWT token
            String jwtToken = jwtService.generateToken(userService.loadUserByUsername(email));

            return LoginResponseDTO.builder()
                    .firstName(user.getFirstName())
                    .email(user.getEmail())
                    .token(jwtToken)
                    .build();

        } catch (IOException | GeneralSecurityException e) {
            log.error("Google OAuth authentication failed", e);
            throw new InvalidCredentialsException("Google authentication failed: " + e.getMessage());
        }
    }

    private User findOrCreateUser(String email, String firstName, String profilePicture) {
        Optional<User> existingUser = userService.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update profile picture if provided and different
            if (profilePicture != null && !profilePicture.equals(user.getProfilePicture())) {
                user.setProfilePicture(profilePicture);
                userService.saveUser(user);
            }
            return user;
        } else {
            // Create new user for Google OAuth
            User newUser = User.builder()
                    .firstName(firstName != null ? firstName : "User")
                    .email(email)
                    .password("") // Empty password for OAuth users
                    .profilePicture(profilePicture)
                    .role(Role.REGULAR_USER)
                    .isOAuthUser(true)
                    .build();

            return userService.saveUser(newUser);
        }
    }
}
