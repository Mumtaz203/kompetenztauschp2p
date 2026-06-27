ALTER TABLE app_user
    ADD COLUMN private_report_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN internally_flagged BOOLEAN NOT NULL DEFAULT FALSE;
