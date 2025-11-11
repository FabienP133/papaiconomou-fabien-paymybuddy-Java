package com.paymybuddy.pmb.repository;

import com.paymybuddy.pmb.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    java.util.Optional<User> findByEmail(String email);
}