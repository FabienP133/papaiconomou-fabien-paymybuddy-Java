package com.paymybuddy.pmb.domain;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ConnectionId implements Serializable {
    private Long userId;
    private Long friendId;

    public ConnectionId() {
    }

    public ConnectionId(Long userId, Long friendId) { this.userId=userId; this.friendId=friendId; }

    // equals/hashCode
    @Override public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof ConnectionId ci)) return false;
        return Objects.equals(userId,ci.userId)&&Objects.equals(friendId,ci.friendId); }
    @Override public int hashCode(){
        return Objects.hash(userId,friendId); }



    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }
}