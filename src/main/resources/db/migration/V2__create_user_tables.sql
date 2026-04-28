-- User table
CREATE TABLE app_user (
                          id UUID PRIMARY KEY,
                          username VARCHAR(50) NOT NULL,
                          email VARCHAR(320) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL
);

-- Offered skills
CREATE TABLE user_offered_skills (
                                     user_id UUID NOT NULL,
                                     skill VARCHAR(100) NOT NULL,
                                     CONSTRAINT fk_offered_user
                                         FOREIGN KEY (user_id)
                                             REFERENCES app_user(id)
                                             ON DELETE CASCADE
);

-- Wanted skills
CREATE TABLE user_wanted_skills (
                                    user_id UUID NOT NULL,
                                    skill VARCHAR(100) NOT NULL,
                                    CONSTRAINT fk_wanted_user
                                        FOREIGN KEY (user_id)
                                            REFERENCES app_user(id)
                                            ON DELETE CASCADE
);
