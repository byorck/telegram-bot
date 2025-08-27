--liquibase formatted sql

--changeset byorck:1
CREATE TABLE notification_task
(
    id                    BIGSERIAL PRIMARY KEY,
    chat_id               BIGSERIAL    NOT NULL,
    notification_text     VARCHAR(255) NOT NULL,
    notification_datetime TIMESTAMP    NOT NULL
);
--rollback DROP TABLE notification_task;
