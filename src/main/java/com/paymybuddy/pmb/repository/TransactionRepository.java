package com.paymybuddy.pmb.repository;

import com.paymybuddy.pmb.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySender_EmailOrReceiver_EmailOrderByCreatedAtDesc(
            String senderEmail,
            String receiverEmail
    );
}