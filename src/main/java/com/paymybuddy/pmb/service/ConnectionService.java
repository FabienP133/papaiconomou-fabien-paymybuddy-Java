package com.paymybuddy.pmb.service;

import com.paymybuddy.pmb.domain.Connection;
import com.paymybuddy.pmb.domain.User;
import com.paymybuddy.pmb.repository.ConnectionRepository;
import com.paymybuddy.pmb.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConnectionService {

    private final UserRepository users;
    private final ConnectionRepository connections;

    public ConnectionService(UserRepository users, ConnectionRepository connections) {
        this.users = users;
        this.connections = connections;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException("Utilisateur courant (" + email + ") introuvable"));
    }

    @Transactional(readOnly = true)
    public List<User> listConnections() {
        User current = getCurrentUser();

        return connections.findByUser(current)
                .stream()
                .map(Connection::getFriend)
                .toList();
    }

    @Transactional
    public void addConnection(String friendEmail) {
        if (friendEmail == null || friendEmail.isBlank()) {
            throw new IllegalArgumentException("Email obligatoire.");
        }

        User current = getCurrentUser();

        User friend = users.findByEmail(friendEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Utilisateur introuvable : " + friendEmail));

        if (current.equals(friend)) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous ajouter vous-même.");
        }

        boolean exists = connections.existsByUserAndFriend(current, friend);
        if (exists) {
            throw new IllegalStateException("Cet utilisateur est déjà dans vos contacts.");
        }

        Connection c = new Connection();
        c.setUser(current);
        c.setFriend(friend);
        connections.save(c);
    }
}