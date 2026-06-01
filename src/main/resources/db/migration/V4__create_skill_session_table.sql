CREATE TABLE skill_session (
                               id UUID PRIMARY KEY,
                               requester_user_id UUID NOT NULL,
                               receiver_user_id UUID NOT NULL,
                               status VARCHAR(50) NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               accepted_at TIMESTAMP NOT NULL,
                               completed_at TIMESTAMP,
                               rating_window_opened_at TIMESTAMP,
                               rating_window_ends_at TIMESTAMP,

                               CONSTRAINT chk_skill_session_different_users
                                   CHECK (requester_user_id <> receiver_user_id)
);

CREATE INDEX idx_skill_session_requester_user_id
    ON skill_session(requester_user_id);

CREATE INDEX idx_skill_session_receiver_user_id
    ON skill_session(receiver_user_id);

CREATE INDEX idx_skill_session_status
    ON skill_session(status);