-- Dev-only manual search test data.
-- This script is idempotent and deletes/recreates only the listed manual test users.

DELETE FROM skill_embeddings
WHERE user_id IN (
    '11111111-1111-1111-1111-111111111101',
    '11111111-1111-1111-1111-111111111102',
    '11111111-1111-1111-1111-111111111103',
    '11111111-1111-1111-1111-111111111104',
    '11111111-1111-1111-1111-111111111105',
    '11111111-1111-1111-1111-111111111106',
    '11111111-1111-1111-1111-111111111107',
    '11111111-1111-1111-1111-111111111108',
    '11111111-1111-1111-1111-111111111109',
    '11111111-1111-1111-1111-111111111110'
);

DELETE FROM app_user
WHERE username IN (
    'java_sql_user',
    'java_exact_user',
    'sql_exact_user',
    'javascript_partial_user',
    'mysql_partial_user',
    'postgres_partial_user',
    'spring_java_user',
    'python_user',
    'ai_user',
    'german_user'
)
OR email LIKE 'manual-search-%@example.com';

INSERT INTO app_user (id, username, email, password) VALUES
    ('11111111-1111-1111-1111-111111111101', 'java_sql_user', 'manual-search-java-sql@example.com', 'manual-test-password'),
    ('11111111-1111-1111-1111-111111111102', 'java_exact_user', 'manual-search-java@example.com', 'manual-test-password'),
    ('11111111-1111-1111-1111-111111111103', 'sql_exact_user', 'manual-search-sql@example.com', 'manual-test-password'),
    ('11111111-1111-1111-1111-111111111104', 'javascript_partial_user', 'manual-search-javascript@example.com', 'manual-test-password'),
    ('11111111-1111-1111-1111-111111111105', 'mysql_partial_user', 'manual-search-mysql@example.com', 'manual-test-password'),
    ('11111111-1111-1111-1111-111111111106', 'postgres_partial_user', 'manual-search-postgres@example.com', 'manual-test-password'),
    ('11111111-1111-1111-1111-111111111107', 'spring_java_user', 'manual-search-spring-java@example.com', 'manual-test-password'),
    ('11111111-1111-1111-1111-111111111108', 'python_user', 'manual-search-python@example.com', 'manual-test-password'),
    ('11111111-1111-1111-1111-111111111109', 'ai_user', 'manual-search-ai@example.com', 'manual-test-password'),
    ('11111111-1111-1111-1111-111111111110', 'german_user', 'manual-search-german@example.com', 'manual-test-password');

INSERT INTO user_offered_skills (user_id, skill) VALUES
    ('11111111-1111-1111-1111-111111111101', 'Java'),
    ('11111111-1111-1111-1111-111111111101', 'SQL'),
    ('11111111-1111-1111-1111-111111111102', 'Java'),
    ('11111111-1111-1111-1111-111111111103', 'SQL'),
    ('11111111-1111-1111-1111-111111111104', 'JavaScript'),
    ('11111111-1111-1111-1111-111111111105', 'MySQL'),
    ('11111111-1111-1111-1111-111111111106', 'PostgreSQL'),
    ('11111111-1111-1111-1111-111111111107', 'Spring Boot'),
    ('11111111-1111-1111-1111-111111111107', 'Java'),
    ('11111111-1111-1111-1111-111111111108', 'Python'),
    ('11111111-1111-1111-1111-111111111109', 'Artificial Intelligence'),
    ('11111111-1111-1111-1111-111111111109', 'Machine Learning'),
    ('11111111-1111-1111-1111-111111111110', 'Deutsch'),
    ('11111111-1111-1111-1111-111111111110', 'Englisch');
