package com.paymybuddy.pmb.service;

import com.paymybuddy.pmb.domain.Transaction;
import com.paymybuddy.pmb.domain.User;
import com.paymybuddy.pmb.repository.TransactionRepository;
import com.paymybuddy.pmb.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TransactionService {

    private final UserRepository users;
    private final TransactionRepository txs;

    private static final BigDecimal FEE_RATE = new BigDecimal("0.005"); // 0,5%

    public TransactionService(UserRepository users, TransactionRepository txs) {
        this.users = users;
        this.txs = txs;
    }

    @Transactional
    public Transaction transfer(String senderEmail,
                                String receiverEmail,
                                BigDecimal amount,
                                String description) {


        if (senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalArgumentException("Expéditeur manquant.");
        }
        if (receiverEmail == null || receiverEmail.isBlank()) {
            throw new IllegalArgumentException("Destinataire manquant.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif.");
        }
        if (description == null) {
            description = "";
        }

        // Règles métier
        if (senderEmail.equalsIgnoreCase(receiverEmail)) {
            throw new IllegalArgumentException("L'expéditeur et le destinataire doivent être différents.");
        }

        // Chargement des users
        User sender = users.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("Expéditeur introuvable : " + senderEmail));
        User receiver = users.findByEmail(receiverEmail)
                .orElseThrow(() -> new IllegalArgumentException("Destinataire introuvable : " + receiverEmail));

        // Calcul des frais
        BigDecimal fee = amount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDebit = amount.add(fee);

        // Vérif solde
        if (sender.getBalance().compareTo(totalDebit) < 0) {
            throw new IllegalArgumentException("Solde insuffisant (montant + frais).");
        }

        // Débit / crédit
        sender.setBalance(sender.getBalance().subtract(totalDebit));
        receiver.setBalance(receiver.getBalance().add(amount));

        users.save(sender);
        users.save(receiver);

        // Persist transaction
        Transaction t = new Transaction();
        t.setSender(sender);
        t.setReceiver(receiver);
        t.setAmount(amount);
        t.setFee(fee);
        t.setDescription(description);

        return txs.save(t);
    }

    @Transactional(readOnly = true)
    public List<Transaction> listForUser(String email) {
        return txs.findBySender_EmailOrReceiver_EmailOrderByCreatedAtDesc(email, email);
    }
}