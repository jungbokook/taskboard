create table if not exists tasks (
    id          bigserial primary key,
    title       varchar(255) not null,
    description text,
    done        boolean not null default false,
    created_at  timestamptz not null default now()
);