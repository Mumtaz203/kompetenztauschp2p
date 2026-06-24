CREATE TABLE session_completion_response (
                                             id UUID PRIMARY KEY,
                                             session_id UUID NOT NULL,
                                             user_id UUID NOT NULL,
                                             answer VARCHAR(50) NOT NULL,
                                             reason VARCHAR(1000),
                                             created_at TIMESTAMP NOT NULL,
                                             updated_at TIMESTAMP NOT NULL,

                                             CONSTRAINT fk_session_completion_response_session
                                                 FOREIGN KEY (session_id)
                                                     REFERENCES skill_session(id)
                                                     ON DELETE CASCADE,

                                             CONSTRAINT fk_session_completion_response_user
                                                 FOREIGN KEY (user_id)
                                                     REFERENCES app_user(id)
                                                     ON DELETE CASCADE,

                                             CONSTRAINT uq_session_completion_response_session_user
                                                 UNIQUE (session_id, user_id)

);

CREATE INDEX idx_session_completion_response_session_id
    ON session_completion_response(session_id);

CREATE INDEX idx_session_completion_response_user_id
    ON session_completion_response(user_id);

CREATE TABLE private_session_report (
                                        id UUID PRIMARY KEY,
                                        session_id UUID NOT NULL,
                                        reporter_user_id UUID NOT NULL,
                                        reported_user_id UUID NOT NULL,
                                        reason_code VARCHAR(100) NOT NULL,
                                        description VARCHAR(2000),
                                        created_at TIMESTAMP NOT NULL,

                                        CONSTRAINT fk_private_session_report_session
                                            FOREIGN KEY (session_id)
                                                REFERENCES skill_session(id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT fk_private_session_report_reporter
                                            FOREIGN KEY (reporter_user_id)
                                                REFERENCES app_user(id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT fk_private_session_report_reported
                                            FOREIGN KEY (reported_user_id)
                                                REFERENCES app_user(id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT chk_private_session_report_different_users
                                            CHECK (reporter_user_id <> reported_user_id)
);
CREATE INDEX idx_private_session_report_reported_user_id
    ON private_session_report(reported_user_id);

CREATE INDEX idx_private_session_report_session_id
    ON private_session_report(session_id);