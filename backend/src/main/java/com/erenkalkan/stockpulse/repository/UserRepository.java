package com.erenkalkan.stockpulse.repository;

import com.erenkalkan.stockpulse.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
