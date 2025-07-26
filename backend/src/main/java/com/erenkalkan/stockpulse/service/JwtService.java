package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.InvalidInputException;
import com.erenkalkan.stockpulse.exception.InvalidJwtTokenException;
import com.erenkalkan.stockpulse.exception.JwtConfigurationException;
import com.erenkalkan.stockpulse.exception.JwtTokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

  @Value("${JWT_SECRET}")
  private String secretKey;
  @Value("${JWT_EXPIRATION}")
  private String expirationMs;
  @Value("${JWT_ISSUER}")
  private String issuer;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    try {
      final Claims claims = extractAllClaims(token);
      return claimsResolver.apply(claims);
    }
    catch(ExpiredJwtException e) {
      throw new JwtTokenExpiredException("JWT token has expired", e);
    }
    catch(JwtException e) {
      throw new InvalidJwtTokenException("Invalid JWT token", e);
    }
  }

  public Claims extractAllClaims(String token) {
    try {
      return Jwts.parser()
              .verifyWith((SecretKey) getSigningKey())
              .build()
              .parseSignedClaims(token)
              .getPayload();
    }
    catch(ExpiredJwtException e) {
      throw new JwtTokenExpiredException("JWT token has expired", e);
    }
    catch(JwtException e) {
      throw new InvalidJwtTokenException("Invalid JWT token", e);
    }
  }

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    try {
      if (expirationMs == null || secretKey == null) {
        throw new JwtConfigurationException("JWT configuration values are not properly set");
      }

      long expiration = Long.parseLong(expirationMs);
      return Jwts.builder()
              .subject(userDetails.getUsername())
              .claims(extraClaims)
              .issuedAt(new Date(System.currentTimeMillis()))
              .expiration(new Date(System.currentTimeMillis() + expiration))
              .issuer(issuer)
              .signWith(getSigningKey())
              .compact();
    } catch (NumberFormatException e) {
      throw new InvalidInputException("Invalid JWT expiration value: " + expirationMs, e);
    } catch (Exception e) {
      throw new InvalidJwtTokenException("Failed to generate JWT token", e);
    }
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    String username = extractUsername(token);
    return username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Key getSigningKey() {
    if (secretKey == null) {
      throw new JwtConfigurationException("JWT secret key is not configured");
    }
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
