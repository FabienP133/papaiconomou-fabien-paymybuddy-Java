package com.paymybuddy.pmb.repository;

import com.paymybuddy.pmb.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionRepository extends JpaRepository<Connection, ConnectionId> {
    java.util.List<Connection> findByUserId(Long userId);
}