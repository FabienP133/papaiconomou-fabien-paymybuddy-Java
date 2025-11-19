package com.paymybuddy.pmb.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "connections")
public class Connection {
    @EmbeddedId
    private ConnectionId id = new ConnectionId();

    @ManyToOne(optional = false) @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false) @MapsId("friendId")
    @JoinColumn(name = "friend_id")
    private User friend;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Connection() {}
    public Connection(User user, User friend) {
        this.user = user; this.friend = friend;
        this.id = new ConnectionId(user.getId(), friend.getId());
    }

    public ConnectionId getId() {
        return id;
    }

    public void setId(ConnectionId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}