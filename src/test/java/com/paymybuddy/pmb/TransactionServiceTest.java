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
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
// chaque test est rollbacké automatiquement
class TransactionServiceTest {

    @Autowired
    UserRepository users;

    @Autowired
    TransactionRepository txs;

    @Autowired
    TransactionService service;

    @BeforeEach
    void setup() {
        // On part du principe que Alice et Bob existent déjà en base
        // (créés par Flyway / data.sql / ton seeding d’appli)
        User alice = users.findByEmail("alice@example.com").orElseThrow();
        User bob   = users.findByEmail("bob@example.com").orElseThrow();

        // IMPORTANT : d'abord on nettoie les transactions (enfants)
        txs.deleteAll();

        // On remet leurs soldes à des valeurs connues pour les tests
        alice.setBalance(new BigDecimal("100.00"));
        bob.setBalance(new BigDecimal("50.00"));

        users.save(alice);
        users.save(bob);
        // Pas de création de nouveau user → plus aucun risque de doublon d’email
    }

    @Test
    @Rollback // inutile avec @Transactional mais explicite
    void transfer_debits_sender_and_credits_receiver_and_persists_tx() {
        Transaction t = service.transfer(
                "alice@example.com",
                "bob@example.com",
                new BigDecimal("10.00"),
                "Test");

        User alice = users.findByEmail("alice@example.com").orElseThrow();
        User bob   = users.findByEmail("bob@example.com").orElseThrow();

        // Alice : 100 - 10 - 0.05 (frais) = 89.95
        assertThat(alice.getBalance()).isEqualByComparingTo("89.95");

        // Bob reçoit bien 10.00
        assertThat(bob.getBalance()).isEqualByComparingTo("60.00");

        assertThat(t.getId()).isNotNull();
        assertThat(txs.count()).isEqualTo(1);
        assertThat(t.getSender().getEmail()).isEqualTo("alice@example.com");
        assertThat(t.getReceiver().getEmail()).isEqualTo("bob@example.com");
        assertThat(t.getAmount()).isEqualByComparingTo("10.00");

        // (optionnel mais cool) vérifier les frais stockés sur la transaction
        assertThat(t.getFee()).isEqualByComparingTo("0.05");
    }

    @Test
    void transfer_should_fail_when_insufficient_balance() {
        User alice = users.findByEmail("alice@example.com").orElseThrow();
        alice.setBalance(new BigDecimal("5.00"));
        users.save(alice);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.transfer(
                        "alice@example.com",
                        "bob@example.com",
                        new BigDecimal("10.00"),
                        "Test")
        );

        // Adapte le texte au message exact dans ton service
        assertThat(ex.getMessage()).containsIgnoringCase("solde");

        // Aucune transaction ne doit être créée
        assertThat(txs.count()).isEqualTo(0);
    }

    @Test
    void transfer_should_fail_when_sender_not_found() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.transfer(
                        "unknown@example.com",
                        "bob@example.com",
                        new BigDecimal("10.00"),
                        "Test")
        );

        assertThat(ex.getMessage()).containsIgnoringCase("introuvable");
        assertThat(txs.count()).isEqualTo(0);
    }

    @Test
    void transfer_should_fail_when_receiver_not_found() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.transfer(
                        "alice@example.com",
                        "unknown@example.com",
                        new BigDecimal("10.00"),
                        "Test")
        );

        assertThat(ex.getMessage()).containsIgnoringCase("introuvable");
        assertThat(txs.count()).isEqualTo(0);
    }
}
