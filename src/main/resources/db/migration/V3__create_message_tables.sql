CREATE TABLE conversations (
                               id              UUID PRIMARY KEY,
                               user1_id        UUID NOT NULL,
                               user2_id        UUID NOT NULL,
                               created_at      TIMESTAMP NOT NULL,
                               last_message_at TIMESTAMP NOT NULL,


                               CONSTRAINT uq_conversation_users UNIQUE (user1_id, user2_id)


  );

CREATE TABLE messages (
                          id              UUID        PRIMARY KEY,
                          conversation_id UUID        NOT NULL,
                          sender_id       UUID        NOT NULL,
                          recipient_id    UUID        NOT NULL,
                          content         TEXT        NOT NULL,
                          sent_at         TIMESTAMP   NOT NULL,
                          is_read         BOOLEAN     NOT NULL DEFAULT FALSE,

                          CONSTRAINT fk_message_conversation
                              FOREIGN KEY (conversation_id)
                                  REFERENCES conversations(id)
                                  ON DELETE CASCADE
);

CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);

CREATE INDEX idx_conversations_user1_id ON conversations(user1_id);
CREATE INDEX idx_conversations_user2_id ON conversations(user2_id);
