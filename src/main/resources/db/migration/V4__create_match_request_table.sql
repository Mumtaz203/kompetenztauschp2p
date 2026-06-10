CREATE TABLE match_requests (
                                id              UUID PRIMARY KEY,
                                sender_id       UUID NOT NULL,
                                receiver_id     UUID NOT NULL,
                                status          VARCHAR(50) NOT NULL,
                                created_at      TIMESTAMP NOT NULL,
                                updated_at      TIMESTAMP NOT NULL,

                                CONSTRAINT fk_match_request_sender
                                    FOREIGN KEY (sender_id)
                                        REFERENCES app_user(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_match_request_receiver
                                    FOREIGN KEY (receiver_id)
                                        REFERENCES app_user(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT chk_match_request_different_users
                                    CHECK (sender_id <> receiver_id)
);

-- Partial unique index: sadece PENDING status'unda unique
CREATE UNIQUE INDEX idx_uq_pending_match_request
    ON match_requests(sender_id, receiver_id)
    WHERE status = 'PENDING';

-- Normal indexes
CREATE INDEX idx_match_requests_sender_id ON match_requests(sender_id);
CREATE INDEX idx_match_requests_receiver_id ON match_requests(receiver_id);
CREATE INDEX idx_match_requests_status ON match_requests(status);
CREATE INDEX idx_match_requests_receiver_status ON match_requests(receiver_id, status);
CREATE INDEX idx_match_requests_sender_status ON match_requests(sender_id, status);