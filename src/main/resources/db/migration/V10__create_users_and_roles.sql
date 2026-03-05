CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- (Because of @ElementCollection)
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    roles VARCHAR(50) NOT NULL,
    
    -- If the user is deleted, automatically delete their roles
    CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_users_username ON users(username);