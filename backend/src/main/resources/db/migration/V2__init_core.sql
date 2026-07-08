-- V2 core schema. Column names are snake_case; enums are stored as varchar.
-- Timestamps are timestamptz (Instant / UTC). Keep this in sync with the JPA entities;
-- Hibernate runs with ddl-auto=validate and will fail startup on drift.

create table users (
    id             bigserial primary key,
    name           varchar(255) not null,
    email          varchar(255) not null unique,
    password_hash  varchar(255),
    role           varchar(255) not null,
    status         varchar(255) not null,
    last_active_at timestamptz,
    created_at     timestamptz  not null,
    updated_at     timestamptz  not null
);

create table projects (
    id            bigserial primary key,
    project_key   varchar(255) not null unique,
    name          varchar(255) not null,
    description   text,
    issue_counter integer      not null default 0,
    created_at    timestamptz  not null,
    updated_at    timestamptz  not null
);

create table project_members (
    project_id bigint not null references projects (id) on delete cascade,
    user_id    bigint not null references users (id) on delete cascade,
    primary key (project_id, user_id)
);

create table epics (
    id          bigserial primary key,
    project_id  bigint       not null references projects (id),
    epic_key    varchar(255) not null,
    name        varchar(255) not null,
    color       varchar(255),
    description text,
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null,
    unique (project_id, epic_key)
);

create table sprints (
    id         bigserial primary key,
    project_id bigint       not null references projects (id),
    name       varchar(255) not null,
    goal       text,
    status     varchar(255) not null,
    start_date date,
    end_date   date,
    created_at timestamptz  not null,
    updated_at timestamptz  not null
);

create table labels (
    id         bigserial primary key,
    name       varchar(255) not null unique,
    color      varchar(255),
    created_at timestamptz  not null,
    updated_at timestamptz  not null
);

create table issues (
    id           bigserial primary key,
    project_id   bigint       not null references projects (id),
    issue_key    varchar(255) not null unique,
    title        varchar(255) not null,
    description  text,
    status       varchar(255) not null,
    priority     varchar(255) not null,
    issue_type   varchar(255) not null,
    story_points integer,
    due_date     date,
    assignee_id  bigint references users (id),
    reporter_id  bigint references users (id),
    sprint_id    bigint references sprints (id),
    epic_id      bigint references epics (id),
    created_at   timestamptz  not null,
    updated_at   timestamptz  not null
);

create table issue_labels (
    issue_id bigint not null references issues (id) on delete cascade,
    label_id bigint not null references labels (id) on delete cascade,
    primary key (issue_id, label_id)
);

create table subtasks (
    id          bigserial primary key,
    issue_id    bigint       not null references issues (id) on delete cascade,
    title       varchar(255) not null,
    done        boolean      not null default false,
    assignee_id bigint references users (id),
    order_index integer      not null default 0,
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null
);

create table comments (
    id         bigserial primary key,
    issue_id   bigint      not null references issues (id) on delete cascade,
    author_id  bigint      not null references users (id),
    body       text        not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table activity_logs (
    id         bigserial primary key,
    issue_id   bigint       not null references issues (id) on delete cascade,
    actor_id   bigint references users (id),
    action     varchar(255) not null,
    field      varchar(255),
    old_value  text,
    new_value  text,
    created_at timestamptz  not null,
    updated_at timestamptz  not null
);

-- Foreign-key indexes for common lookups.
create index idx_epics_project on epics (project_id);
create index idx_sprints_project on sprints (project_id);
create index idx_issues_project on issues (project_id);
create index idx_issues_assignee on issues (assignee_id);
create index idx_issues_sprint on issues (sprint_id);
create index idx_issues_epic on issues (epic_id);
create index idx_subtasks_issue on subtasks (issue_id);
create index idx_comments_issue on comments (issue_id);
create index idx_activity_logs_issue on activity_logs (issue_id);
