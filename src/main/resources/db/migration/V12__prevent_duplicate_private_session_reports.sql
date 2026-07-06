DELETE FROM private_session_report
WHERE id IN (
    SELECT id
    FROM (
        SELECT
            id,
            ROW_NUMBER() OVER (
                PARTITION BY session_id, reporter_user_id, reported_user_id
                ORDER BY created_at ASC, id ASC
            ) AS duplicate_rank
        FROM private_session_report
    ) ranked_reports
    WHERE duplicate_rank > 1
);

ALTER TABLE private_session_report
    ADD CONSTRAINT uk_private_session_report_once_per_pair
        UNIQUE (session_id, reporter_user_id, reported_user_id);
