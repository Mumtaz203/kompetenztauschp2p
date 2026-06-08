CREATE TABLE session_ratings (
                                 id UUID PRIMARY KEY,
                                 session_id UUID NOT NULL,
                                 sender_user_id UUID NOT NULL,
                                 receiver_user_id UUID NOT NULL,
                                 status VARCHAR(50) NOT NULL,
                                 points NUMERIC(2,1) NOT NULL,
                                 comment VARCHAR(1000),
                                 created_at TIMESTAMP NOT NULL,
                                 published_at TIMESTAMP,

                                 CONSTRAINT fk_session_rating_session
                                     FOREIGN KEY (session_id)
                                         REFERENCES skill_session(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_session_rating_sender
                                     FOREIGN KEY (sender_user_id)
                                         REFERENCES app_user(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_session_rating_receiver
                                     FOREIGN KEY (receiver_user_id)
                                         REFERENCES app_user(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT uq_session_rating_session_sender
                                     UNIQUE (session_id, sender_user_id),

                                 CONSTRAINT chk_session_rating_no_self_rating
                                     CHECK (sender_user_id <> receiver_user_id),

                                 CONSTRAINT chk_session_rating_points_range
                                     CHECK (points >= 1.0 AND points <= 5.0),

                                 CONSTRAINT chk_session_rating_points_half_steps
                                     CHECK ((points * 2) = FLOOR(points * 2))
);

CREATE INDEX idx_session_ratings_session_id
    ON session_ratings(session_id);

CREATE INDEX idx_session_ratings_sender_user_id
    ON session_ratings(sender_user_id);

CREATE INDEX idx_session_ratings_receiver_user_id
    ON session_ratings(receiver_user_id);

CREATE INDEX idx_session_ratings_status
    ON session_ratings(status);

CREATE INDEX idx_session_ratings_receiver_status
    ON session_ratings(receiver_user_id, status);