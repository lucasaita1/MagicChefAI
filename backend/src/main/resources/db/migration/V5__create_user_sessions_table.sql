-- Every issued JWT is bound to one row here (jti = id). Logout revokes the row,
-- so a token stops working immediately even before it expires.
CREATE TABLE user_sessions (
    id               UUID PRIMARY KEY,
    user_id          BIGINT                   NOT NULL,
    fingerprint_hash VARCHAR(64)              NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at       TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_sessions_expires ON user_sessions (expires_at);
