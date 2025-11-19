package com.paymybuddy.pmb.repository;

import com.paymybuddy.pmb.domain.Connection;
import com.paymybuddy.pmb.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    // Toutes les relations dont "user" est le propriétaire
    List<Connection> findByUser(User user);

    // Vérifier si une relation (user -> friend) existe déjà
    boolean existsByUserAndFriend(User user, User friend);
}