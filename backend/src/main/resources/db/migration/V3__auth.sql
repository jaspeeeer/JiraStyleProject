-- V3: password-reset / invitation tokens. One row per issued reset or invite link.

create table password_reset_tokens (
    id         bigserial primary key,
    user_id    bigint       not null references users (id) on delete cascade,
    token      varchar(255) not null unique,
    expires_at timestamptz  not null,
    used_at    timestamptz,
    created_at timestamptz  not null,
    updated_at timestamptz  not null
);

create index idx_password_reset_tokens_user on password_reset_tokens (user_id);
