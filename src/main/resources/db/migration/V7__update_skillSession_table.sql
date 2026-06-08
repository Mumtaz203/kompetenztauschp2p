ALTER TABLE skill_session
    ADD COLUMN matching_request_id UUID;

ALTER TABLE skill_session
    ADD CONSTRAINT fk_skill_session_matching_request
        FOREIGN KEY (matching_request_id)
            REFERENCES match_requests(id)
            ON DELETE CASCADE;

CREATE UNIQUE INDEX idx_uq_skill_session_matching_request_id
    ON skill_session(matching_request_id);

ALTER TABLE skill_session
    ALTER COLUMN matching_request_id SET NOT NULL;