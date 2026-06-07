CREATE TABLE skill_embeddings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    skill_text VARCHAR(255) NOT NULL,
    skill_type VARCHAR(50) NOT NULL,
    embedding_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_skill_embeddings_user_skill_text_type UNIQUE (user_id, skill_text, skill_type)
);

CREATE INDEX idx_skill_embeddings_user_id
    ON skill_embeddings(user_id);

CREATE INDEX idx_skill_embeddings_skill_type
    ON skill_embeddings(skill_type);
