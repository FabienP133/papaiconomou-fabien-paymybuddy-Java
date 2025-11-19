package com.paymybuddy.pmb.service;

import com.paymybuddy.pmb.domain.Transaction;
import com.paymybuddy.pmb.domain.User;
import com.paymybuddy.pmb.repository.TransactionRepository;
import com.paymybuddy.pmb.repository.UserRepository;
import com.paymybuddy.pmb.repository.ConnectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private final UserRepository users;
    private final TransactionRepository txs;
    private final ConnectionRepository connections;

    public TransactionService(UserRepository users,
                              TransactionRepository txs,
                              ConnectionRepository connections) {
        this.users = users;
        this.txs = txs;
        this.connections = connections;
    }

    @Transactional
    public Transaction transfer(String senderEmail,
                                String receiverEmail,
                                BigDecimal amount,
                                String description) {

        if (senderEmail.equalsIgnoreCase(receiverEmail)) {
            throw new IllegalArgumentException("L'expéditeur et le destinataire doivent être différents.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif.");
        }

        User sender = users.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("Expéditeur introuvable : " + senderEmail));
        User receiver = users.findByEmail(receiverEmail)
                .orElseThrow(() -> new IllegalArgumentException("Destinataire introuvable : " + receiverEmail));

        // Vérif qu'ils sont connectés
        boolean connected = connections.existsByUserAndFriend(sender, receiver);
        if (!connected) {
            throw new IllegalStateException("Ce destinataire n'est pas dans vos connexions.");
        }

        // Vérif soldes
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Solde insuffisant.");
        }

        // Débit/crédit
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        users.save(sender);
        users.save(receiver);

        // Enregistrer la transaction
        Transaction t = new Transaction();
        t.setSender(sender);
        t.setReceiver(receiver);
        t.setAmount(amount);
        t.setDescription(description);
        return txs.save(t);
    }
}