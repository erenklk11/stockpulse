package com.erenkalkan.stockpulse.repository;

import com.erenkalkan.stockpulse.model.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
}
