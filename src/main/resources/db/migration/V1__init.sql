-- USERS
CREATE TABLE users (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  email         VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  first_name    VARCHAR(100),
  last_name     VARCHAR(100),
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  balance       DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- CONNECTIONS
CREATE TABLE connections (
  user_id    BIGINT NOT NULL,
  friend_id  BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, friend_id),
  CONSTRAINT fk_conn_user   FOREIGN KEY (user_id)   REFERENCES users(id)
      ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_conn_friend FOREIGN KEY (friend_id) REFERENCES users(id)
      ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- TRANSACTIONS
CREATE TABLE transactions (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  sender_id    BIGINT NULL,
  receiver_id  BIGINT NULL,
  amount       DECIMAL(19,2) NOT NULL,
  description  VARCHAR(255),
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tx_sender   FOREIGN KEY (sender_id)   REFERENCES users(id),
  CONSTRAINT fk_tx_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX idx_tx_sender   ON transactions(sender_id);
CREATE INDEX idx_tx_receiver ON transactions(receiver_id);
CREATE INDEX idx_tx_created  ON transactions(created_at);