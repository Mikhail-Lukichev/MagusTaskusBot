-- liquibase formatted sql

-- changeset mlukichev:1
CREATE TABLE task (
    id BIGINT NOT NULL PRIMARY KEY,
    chat_id TEXT,
    message TEXT,
    notification_time TIMESTAMP
)