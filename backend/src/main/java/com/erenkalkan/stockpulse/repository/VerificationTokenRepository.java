package com.erenkalkan.stockpulse.repository;

import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

  Optional<VerificationToken> findByToken(String token);

  Optional<VerificationToken> findByUser(User user);
}
