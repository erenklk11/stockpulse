package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.DatabaseOperationException;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
  
  private final UserRepository userRepository;
  
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByEmail(username)
            .map(user -> new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    user.getAuthorities()
            ))
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
  }

  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public User saveUser(User user) {
    try {
      return userRepository.save(user);
    } catch (Exception e) {
      throw new DatabaseOperationException("Failed to save user to database", e);
    }
  }
}
