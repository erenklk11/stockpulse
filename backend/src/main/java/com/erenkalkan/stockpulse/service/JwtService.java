package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.InvalidJwtTokenException;
import com.erenkalkan.stockpulse.exception.JwtTokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class JwtService {

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
}
