package com.paymybuddy.pmb.repository;

import com.paymybuddy.pmb.domain.Transaction;
import com.paymybuddy.pmb.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Toutes les transactions où l’utilisateur est soit sender, soit receiver
    List<Transaction> findBySenderOrReceiverOrderByCreatedAtDesc(User sender, User receiver);
}