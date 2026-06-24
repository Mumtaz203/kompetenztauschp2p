-- Development-only demo data. All UUIDs and emails are deliberately reserved for demo use.
-- The repeatable migration makes edits to this file take effect on the next Flyway run.

INSERT INTO app_user (id, username, email, password)
SELECT
    ('00000000-0000-4000-8000-' || lpad(n::text, 12, '0'))::uuid,
    'Demo User ' || lpad(n::text, 2, '0'),
    'demo.user' || lpad(n::text, 2, '0') || '@example.test',
    '$2a$10$U3iPqX8JIUEJE8AvBHUDiOo8qhVcknEbKhV016s6ddMmBg1wxGhQC'
FROM generate_series(1, 50) AS n
ON CONFLICT (id) DO UPDATE
SET password = EXCLUDED.password
WHERE app_user.email = EXCLUDED.email
  AND app_user.email LIKE 'demo.user%@example.test';

WITH skill_profiles(profile, offered, wanted, offered_variant, wanted_variant) AS (
    VALUES
        (1,  ARRAY['Java', 'Spring Boot', 'PostgreSQL'], ARRAY['React', 'TypeScript'], ARRAY['REST APIs'], ARRAY['Figma']),
        (2,  ARRAY['React', 'TypeScript', 'Figma'], ARRAY['Java', 'Spring Boot'], ARRAY['CSS'], ARRAY['PostgreSQL']),
        (3,  ARRAY['Python', 'Data analysis', 'SQL'], ARRAY['Machine learning basics', 'Statistics'], ARRAY['Excel'], ARRAY['Presentation skills']),
        (4,  ARRAY['Docker', 'Linux', 'CI/CD'], ARRAY['Kubernetes', 'Monitoring'], ARRAY['Git'], ARRAY['Networking basics']),
        (5,  ARRAY['Project management', 'Scrum', 'Excel'], ARRAY['Agile coaching', 'Presentation skills'], ARRAY['Public speaking'], ARRAY['Data analysis']),
        (6,  ARRAY['UX research', 'Wireframing', 'Design systems'], ARRAY['Figma', 'React'], ARRAY['UX design'], ARRAY['CSS']),
        (7,  ARRAY['Machine learning basics', 'Python', 'Statistics'], ARRAY['SQL', 'Data analysis'], ARRAY['Jupyter notebooks'], ARRAY['Docker']),
        (8,  ARRAY['DevOps', 'Kubernetes', 'Monitoring'], ARRAY['Docker', 'CI/CD'], ARRAY['Linux'], ARRAY['Networking basics']),
        (9,  ARRAY['Kotlin', 'Java', 'Unit testing'], ARRAY['Spring Boot', 'REST APIs'], ARRAY['Git'], ARRAY['PostgreSQL']),
        (10, ARRAY['PostgreSQL', 'SQL', 'Data analysis'], ARRAY['Python', 'Excel'], ARRAY['Database design'], ARRAY['Machine learning basics']),
        (11, ARRAY['React', 'CSS', 'UX design'], ARRAY['TypeScript', 'Figma'], ARRAY['Design systems'], ARRAY['Wireframing']),
        (12, ARRAY['Linux', 'Networking basics', 'Docker'], ARRAY['DevOps', 'Monitoring'], ARRAY['CI/CD'], ARRAY['Kubernetes']),
        (13, ARRAY['German conversation', 'English conversation', 'Public speaking'], ARRAY['Spanish conversation', 'Presentation skills'], ARRAY['Agile coaching'], ARRAY['Project management']),
        (14, ARRAY['Photography', 'Video editing', 'Figma'], ARRAY['UX design', 'Design systems'], ARRAY['Wireframing'], ARRAY['React']),
        (15, ARRAY['Cooking', 'Spanish conversation', 'Presentation skills'], ARRAY['English conversation', 'German conversation'], ARRAY['Public speaking'], ARRAY['Project management']),
        (16, ARRAY['Spring Boot', 'REST APIs', 'Unit testing'], ARRAY['Java', 'PostgreSQL'], ARRAY['Docker'], ARRAY['CI/CD']),
        (17, ARRAY['TypeScript', 'React', 'REST APIs'], ARRAY['CSS', 'UX research'], ARRAY['Figma'], ARRAY['Design systems']),
        (18, ARRAY['Python', 'Machine learning basics', 'Data analysis'], ARRAY['Statistics', 'SQL'], ARRAY['Jupyter notebooks'], ARRAY['PostgreSQL']),
        (19, ARRAY['Kubernetes', 'CI/CD', 'Docker'], ARRAY['Linux', 'DevOps'], ARRAY['Monitoring'], ARRAY['Networking basics']),
        (20, ARRAY['Scrum', 'Agile coaching', 'Project management'], ARRAY['Excel', 'Public speaking'], ARRAY['Presentation skills'], ARRAY['Data analysis']),
        (21, ARRAY['Figma', 'Design systems', 'UX design'], ARRAY['Wireframing', 'UX research'], ARRAY['CSS'], ARRAY['React']),
        (22, ARRAY['Java', 'Kotlin', 'Git'], ARRAY['Unit testing', 'Spring Boot'], ARRAY['REST APIs'], ARRAY['Docker']),
        (23, ARRAY['SQL', 'PostgreSQL', 'Database design'], ARRAY['Data analysis', 'Python'], ARRAY['Excel'], ARRAY['Machine learning basics']),
        (24, ARRAY['Monitoring', 'Linux', 'Networking basics'], ARRAY['Kubernetes', 'CI/CD'], ARRAY['DevOps'], ARRAY['Docker']),
        (25, ARRAY['English conversation', 'German conversation', 'Spanish conversation'], ARRAY['Public speaking', 'Presentation skills'], ARRAY['Cooking'], ARRAY['Agile coaching'])
), demo_users AS (
    SELECT ('00000000-0000-4000-8000-' || lpad(n::text, 12, '0'))::uuid AS user_id, n
    FROM generate_series(1, 50) AS n
), skills(user_id, skill) AS (
    SELECT demo_users.user_id, generated.skill
    FROM demo_users
    JOIN skill_profiles profile ON profile.profile = ((demo_users.n - 1) % 25) + 1
    CROSS JOIN LATERAL unnest(profile.offered || CASE WHEN demo_users.n > 25 THEN profile.offered_variant ELSE ARRAY[]::text[] END) AS generated(skill)
)
INSERT INTO user_offered_skills (user_id, skill)
SELECT s.user_id, s.skill
FROM skills s
WHERE NOT EXISTS (
    SELECT 1 FROM user_offered_skills existing
    WHERE existing.user_id = s.user_id AND existing.skill = s.skill
);

WITH skill_profiles(profile, offered, wanted, offered_variant, wanted_variant) AS (
    VALUES
        (1,  ARRAY['Java', 'Spring Boot', 'PostgreSQL'], ARRAY['React', 'TypeScript'], ARRAY['REST APIs'], ARRAY['Figma']),
        (2,  ARRAY['React', 'TypeScript', 'Figma'], ARRAY['Java', 'Spring Boot'], ARRAY['CSS'], ARRAY['PostgreSQL']),
        (3,  ARRAY['Python', 'Data analysis', 'SQL'], ARRAY['Machine learning basics', 'Statistics'], ARRAY['Excel'], ARRAY['Presentation skills']),
        (4,  ARRAY['Docker', 'Linux', 'CI/CD'], ARRAY['Kubernetes', 'Monitoring'], ARRAY['Git'], ARRAY['Networking basics']),
        (5,  ARRAY['Project management', 'Scrum', 'Excel'], ARRAY['Agile coaching', 'Presentation skills'], ARRAY['Public speaking'], ARRAY['Data analysis']),
        (6,  ARRAY['UX research', 'Wireframing', 'Design systems'], ARRAY['Figma', 'React'], ARRAY['UX design'], ARRAY['CSS']),
        (7,  ARRAY['Machine learning basics', 'Python', 'Statistics'], ARRAY['SQL', 'Data analysis'], ARRAY['Jupyter notebooks'], ARRAY['Docker']),
        (8,  ARRAY['DevOps', 'Kubernetes', 'Monitoring'], ARRAY['Docker', 'CI/CD'], ARRAY['Linux'], ARRAY['Networking basics']),
        (9,  ARRAY['Kotlin', 'Java', 'Unit testing'], ARRAY['Spring Boot', 'REST APIs'], ARRAY['Git'], ARRAY['PostgreSQL']),
        (10, ARRAY['PostgreSQL', 'SQL', 'Data analysis'], ARRAY['Python', 'Excel'], ARRAY['Database design'], ARRAY['Machine learning basics']),
        (11, ARRAY['React', 'CSS', 'UX design'], ARRAY['TypeScript', 'Figma'], ARRAY['Design systems'], ARRAY['Wireframing']),
        (12, ARRAY['Linux', 'Networking basics', 'Docker'], ARRAY['DevOps', 'Monitoring'], ARRAY['CI/CD'], ARRAY['Kubernetes']),
        (13, ARRAY['German conversation', 'English conversation', 'Public speaking'], ARRAY['Spanish conversation', 'Presentation skills'], ARRAY['Agile coaching'], ARRAY['Project management']),
        (14, ARRAY['Photography', 'Video editing', 'Figma'], ARRAY['UX design', 'Design systems'], ARRAY['Wireframing'], ARRAY['React']),
        (15, ARRAY['Cooking', 'Spanish conversation', 'Presentation skills'], ARRAY['English conversation', 'German conversation'], ARRAY['Public speaking'], ARRAY['Project management']),
        (16, ARRAY['Spring Boot', 'REST APIs', 'Unit testing'], ARRAY['Java', 'PostgreSQL'], ARRAY['Docker'], ARRAY['CI/CD']),
        (17, ARRAY['TypeScript', 'React', 'REST APIs'], ARRAY['CSS', 'UX research'], ARRAY['Figma'], ARRAY['Design systems']),
        (18, ARRAY['Python', 'Machine learning basics', 'Data analysis'], ARRAY['Statistics', 'SQL'], ARRAY['Jupyter notebooks'], ARRAY['PostgreSQL']),
        (19, ARRAY['Kubernetes', 'CI/CD', 'Docker'], ARRAY['Linux', 'DevOps'], ARRAY['Monitoring'], ARRAY['Networking basics']),
        (20, ARRAY['Scrum', 'Agile coaching', 'Project management'], ARRAY['Excel', 'Public speaking'], ARRAY['Presentation skills'], ARRAY['Data analysis']),
        (21, ARRAY['Figma', 'Design systems', 'UX design'], ARRAY['Wireframing', 'UX research'], ARRAY['CSS'], ARRAY['React']),
        (22, ARRAY['Java', 'Kotlin', 'Git'], ARRAY['Unit testing', 'Spring Boot'], ARRAY['REST APIs'], ARRAY['Docker']),
        (23, ARRAY['SQL', 'PostgreSQL', 'Database design'], ARRAY['Data analysis', 'Python'], ARRAY['Excel'], ARRAY['Machine learning basics']),
        (24, ARRAY['Monitoring', 'Linux', 'Networking basics'], ARRAY['Kubernetes', 'CI/CD'], ARRAY['DevOps'], ARRAY['Docker']),
        (25, ARRAY['English conversation', 'German conversation', 'Spanish conversation'], ARRAY['Public speaking', 'Presentation skills'], ARRAY['Cooking'], ARRAY['Agile coaching'])
), demo_users AS (
    SELECT ('00000000-0000-4000-8000-' || lpad(n::text, 12, '0'))::uuid AS user_id, n
    FROM generate_series(1, 50) AS n
), skills(user_id, skill) AS (
    SELECT demo_users.user_id, generated.skill
    FROM demo_users
    JOIN skill_profiles profile ON profile.profile = ((demo_users.n - 1) % 25) + 1
    CROSS JOIN LATERAL unnest(profile.wanted || CASE WHEN demo_users.n > 25 THEN profile.wanted_variant ELSE ARRAY[]::text[] END) AS generated(skill)
)
INSERT INTO user_wanted_skills (user_id, skill)
SELECT s.user_id, s.skill
FROM skills s
WHERE NOT EXISTS (
    SELECT 1 FROM user_wanted_skills existing
    WHERE existing.user_id = s.user_id AND existing.skill = s.skill
);

INSERT INTO match_requests (id, sender_id, receiver_id, status, created_at, updated_at)
VALUES
    ('10000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000002', 'ACCEPTED', TIMESTAMP '2026-01-10 09:00:00', TIMESTAMP '2026-01-10 10:00:00'),
    ('10000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000003', '00000000-0000-4000-8000-000000000004', 'ACCEPTED', TIMESTAMP '2026-01-15 11:00:00', TIMESTAMP '2026-01-15 11:30:00'),
    ('10000000-0000-4000-8000-000000000003', '00000000-0000-4000-8000-000000000005', '00000000-0000-4000-8000-000000000006', 'ACCEPTED', TIMESTAMP '2026-02-01 14:00:00', TIMESTAMP '2026-02-01 14:15:00'),
    ('10000000-0000-4000-8000-000000000004', '00000000-0000-4000-8000-000000000007', '00000000-0000-4000-8000-000000000008', 'ACCEPTED', TIMESTAMP '2026-02-10 16:00:00', TIMESTAMP '2026-02-10 16:30:00'),
    ('10000000-0000-4000-8000-000000000005', '00000000-0000-4000-8000-000000000009', '00000000-0000-4000-8000-000000000010', 'PENDING', TIMESTAMP '2026-03-01 09:00:00', TIMESTAMP '2026-03-01 09:00:00'),
    ('10000000-0000-4000-8000-000000000006', '00000000-0000-4000-8000-000000000011', '00000000-0000-4000-8000-000000000012', 'ACCEPTED', TIMESTAMP '2026-03-05 10:00:00', TIMESTAMP '2026-03-05 10:20:00'),
    ('10000000-0000-4000-8000-000000000007', '00000000-0000-4000-8000-000000000013', '00000000-0000-4000-8000-000000000014', 'ACCEPTED', TIMESTAMP '2026-03-08 13:00:00', TIMESTAMP '2026-03-08 13:30:00'),
    ('10000000-0000-4000-8000-000000000008', '00000000-0000-4000-8000-000000000015', '00000000-0000-4000-8000-000000000016', 'REJECTED', TIMESTAMP '2026-03-12 08:30:00', TIMESTAMP '2026-03-12 09:00:00')
ON CONFLICT DO NOTHING;

INSERT INTO skill_session (id, matching_request_id, requester_user_id, receiver_user_id, status, created_at, accepted_at, completed_at, rating_window_opened_at, rating_window_ends_at)
VALUES
    ('20000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000002', 'RATING_CLOSED', TIMESTAMP '2026-01-10 10:00:00', TIMESTAMP '2026-01-10 10:00:00', TIMESTAMP '2026-01-20 17:00:00', TIMESTAMP '2026-01-20 17:00:00', TIMESTAMP '2026-01-27 17:00:00'),
    ('20000000-0000-4000-8000-000000000002', '10000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000003', '00000000-0000-4000-8000-000000000004', 'COMPLETED', TIMESTAMP '2026-01-15 11:30:00', TIMESTAMP '2026-01-15 11:30:00', TIMESTAMP '2026-02-03 18:00:00', NULL, NULL),
    ('20000000-0000-4000-8000-000000000003', '10000000-0000-4000-8000-000000000003', '00000000-0000-4000-8000-000000000005', '00000000-0000-4000-8000-000000000006', 'ACTIVE', TIMESTAMP '2026-02-01 14:15:00', TIMESTAMP '2026-02-01 14:15:00', NULL, NULL, NULL),
    ('20000000-0000-4000-8000-000000000004', '10000000-0000-4000-8000-000000000004', '00000000-0000-4000-8000-000000000007', '00000000-0000-4000-8000-000000000008', 'RATING_OPEN', TIMESTAMP '2026-02-10 16:30:00', TIMESTAMP '2026-02-10 16:30:00', TIMESTAMP '2026-03-10 16:00:00', TIMESTAMP '2026-03-10 16:00:00', TIMESTAMP '2026-03-17 16:00:00'),
    ('20000000-0000-4000-8000-000000000005', '10000000-0000-4000-8000-000000000006', '00000000-0000-4000-8000-000000000011', '00000000-0000-4000-8000-000000000012', 'RATING_CLOSED', TIMESTAMP '2026-03-05 10:20:00', TIMESTAMP '2026-03-05 10:20:00', TIMESTAMP '2026-03-15 17:00:00', TIMESTAMP '2026-03-15 17:00:00', TIMESTAMP '2026-03-22 17:00:00'),
    ('20000000-0000-4000-8000-000000000006', '10000000-0000-4000-8000-000000000007', '00000000-0000-4000-8000-000000000013', '00000000-0000-4000-8000-000000000014', 'RATING_CLOSED', TIMESTAMP '2026-03-08 13:30:00', TIMESTAMP '2026-03-08 13:30:00', TIMESTAMP '2026-03-18 18:00:00', TIMESTAMP '2026-03-18 18:00:00', TIMESTAMP '2026-03-25 18:00:00')
ON CONFLICT DO NOTHING;

INSERT INTO conversations (id, user1_id, user2_id, created_at, last_message_at)
VALUES
    ('30000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000002', TIMESTAMP '2026-01-10 09:15:00', TIMESTAMP '2026-01-19 18:05:00'),
    ('30000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000003', '00000000-0000-4000-8000-000000000004', TIMESTAMP '2026-01-15 12:00:00', TIMESTAMP '2026-02-03 18:20:00'),
    ('30000000-0000-4000-8000-000000000003', '00000000-0000-4000-8000-000000000005', '00000000-0000-4000-8000-000000000006', TIMESTAMP '2026-02-01 14:30:00', TIMESTAMP '2026-02-02 15:00:00'),
    ('30000000-0000-4000-8000-000000000004', '00000000-0000-4000-8000-000000000009', '00000000-0000-4000-8000-000000000010', TIMESTAMP '2026-03-01 09:10:00', TIMESTAMP '2026-03-01 09:25:00')
ON CONFLICT DO NOTHING;

INSERT INTO messages (id, conversation_id, sender_id, recipient_id, content, sent_at, is_read)
VALUES
    ('40000000-0000-4000-8000-000000000001', '30000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000002', 'Hi, I would like to practise PostgreSQL joins.', TIMESTAMP '2026-01-10 09:15:00', TRUE),
    ('40000000-0000-4000-8000-000000000002', '30000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000001', 'Great, lets plan a short demo session.', TIMESTAMP '2026-01-19 18:05:00', TRUE),
    ('40000000-0000-4000-8000-000000000003', '30000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000003', '00000000-0000-4000-8000-000000000004', 'Thanks for the React walkthrough.', TIMESTAMP '2026-02-03 18:20:00', TRUE),
    ('40000000-0000-4000-8000-000000000004', '30000000-0000-4000-8000-000000000003', '00000000-0000-4000-8000-000000000005', '00000000-0000-4000-8000-000000000006', 'Looking forward to our Docker session.', TIMESTAMP '2026-02-02 15:00:00', FALSE),
    ('40000000-0000-4000-8000-000000000005', '30000000-0000-4000-8000-000000000004', '00000000-0000-4000-8000-000000000009', '00000000-0000-4000-8000-000000000010', 'Would you be open to an English conversation exchange?', TIMESTAMP '2026-03-01 09:25:00', FALSE),
    ('40000000-0000-4000-8000-000000000006', '30000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000004', '00000000-0000-4000-8000-000000000003', 'Absolutely, Tuesday evening works well for me.', TIMESTAMP '2026-02-03 18:25:00', TRUE),
    ('40000000-0000-4000-8000-000000000007', '30000000-0000-4000-8000-000000000003', '00000000-0000-4000-8000-000000000006', '00000000-0000-4000-8000-000000000005', 'I will bring a small Docker Compose example.', TIMESTAMP '2026-02-02 15:10:00', FALSE),
    ('40000000-0000-4000-8000-000000000008', '30000000-0000-4000-8000-000000000004', '00000000-0000-4000-8000-000000000010', '00000000-0000-4000-8000-000000000009', 'Yes, lets start with a short introduction.', TIMESTAMP '2026-03-01 09:35:00', FALSE)
ON CONFLICT DO NOTHING;

INSERT INTO session_ratings (id, session_id, sender_user_id, receiver_user_id, status, points, comment, created_at, published_at)
VALUES
    ('50000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000002', 'PUBLISHED', 4.5, 'Clear explanations and useful examples.', TIMESTAMP '2026-01-20 17:30:00', TIMESTAMP '2026-01-27 17:01:00'),
    ('50000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000001', 'PUBLISHED', 5.0, 'Prepared questions and a friendly exchange.', TIMESTAMP '2026-01-20 17:35:00', TIMESTAMP '2026-01-27 17:01:00'),
    ('50000000-0000-4000-8000-000000000003', '20000000-0000-4000-8000-000000000004', '00000000-0000-4000-8000-000000000007', '00000000-0000-4000-8000-000000000008', 'PENDING', 4.0, 'Helpful session; waiting for the other rating.', TIMESTAMP '2026-03-10 16:30:00', NULL),
    ('50000000-0000-4000-8000-000000000004', '20000000-0000-4000-8000-000000000005', '00000000-0000-4000-8000-000000000011', '00000000-0000-4000-8000-000000000012', 'PUBLISHED', 4.5, 'The SQL examples were practical and easy to follow.', TIMESTAMP '2026-03-15 17:15:00', TIMESTAMP '2026-03-22 17:01:00'),
    ('50000000-0000-4000-8000-000000000005', '20000000-0000-4000-8000-000000000005', '00000000-0000-4000-8000-000000000012', '00000000-0000-4000-8000-000000000011', 'PUBLISHED', 5.0, 'A great exchange with thoughtful questions.', TIMESTAMP '2026-03-15 17:20:00', TIMESTAMP '2026-03-22 17:01:00'),
    ('50000000-0000-4000-8000-000000000006', '20000000-0000-4000-8000-000000000006', '00000000-0000-4000-8000-000000000013', '00000000-0000-4000-8000-000000000014', 'PUBLISHED', 4.0, 'Useful feedback and a well structured session.', TIMESTAMP '2026-03-18 18:15:00', TIMESTAMP '2026-03-25 18:01:00'),
    ('50000000-0000-4000-8000-000000000007', '20000000-0000-4000-8000-000000000006', '00000000-0000-4000-8000-000000000014', '00000000-0000-4000-8000-000000000013', 'PUBLISHED', 4.5, 'Friendly and well prepared skill exchange.', TIMESTAMP '2026-03-18 18:20:00', TIMESTAMP '2026-03-25 18:01:00')
ON CONFLICT DO NOTHING;

-- Do not seed skill_embeddings: embedding_json must contain a real model vector.
-- Use POST /internal/embeddings/backfill locally after enabling embeddings and backfill.
