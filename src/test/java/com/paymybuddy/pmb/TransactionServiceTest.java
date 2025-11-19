package com.paymybuddy.pmb;

import com.paymybuddy.pmb.domain.Transaction;
import com.paymybuddy.pmb.domain.User;
import com.paymybuddy.pmb.repository.TransactionRepository;
import com.paymybuddy.pmb.repository.UserRepository;
import com.paymybuddy.pmb.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
//chaque test est rollbacké automatiquement

class TransactionServiceTest {

    @Autowired UserRepository users;
    @Autowired TransactionRepository txs;
    @Autowired
    TransactionService service;

    @BeforeEach
    void setup() {
        users.deleteAll();
        txs.deleteAll();

        User alice = new User();
        alice.setEmail("alice@example.com");
        alice.setPasswordHash("hash");
        alice.setFirstName("Alice");
        alice.setLastName("Martin");
        alice.setBalance(new BigDecimal("100.00"));
        users.save(alice);

        User bob = new User();
        bob.setEmail("bob@example.com");
        bob.setPasswordHash("hash");
        bob.setFirstName("Bob");
        bob.setLastName("Durand");
        bob.setBalance(new BigDecimal("50.00"));
        users.save(bob);
    }

    @Test
    @Rollback
        // (inutile avec @Transactional, mais explicite)
    void transfer_debits_sender_and_credits_receiver_and_persists_tx() {
        Transaction t = service.transfer(
                "alice@example.com",
                "bob@example.com",
                new BigDecimal("10.00"),
                "Test");

        User alice = users.findByEmail("alice@example.com").orElseThrow();
        User bob   = users.findByEmail("bob@example.com").orElseThrow();

        assertThat(alice.getBalance()).isEqualByComparingTo("90.00");
        assertThat(bob.getBalance()).isEqualByComparingTo("60.00");

        assertThat(t.getId()).isNotNull();
        assertThat(txs.count()).isEqualTo(1);
        assertThat(t.getSender().getEmail()).isEqualTo("alice@example.com");
        assertThat(t.getReceiver().getEmail()).isEqualTo("bob@example.com");
        assertThat(t.getAmount()).isEqualByComparingTo("10.00");
    }
}