create table if not exists auth_sessions (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    refresh_token_hash text not null,
    device_name varchar(100),
    ip_address varchar(100),
    user_agent text,
    is_active boolean not null default true,
    expires_at timestamptz not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create unique index if not exists uq_auth_sessions_refresh_token_hash on auth_sessions(refresh_token_hash);
create index if not exists idx_auth_sessions_user_id on auth_sessions(user_id);
create index if not exists idx_auth_sessions_active on auth_sessions(is_active, expires_at);

drop trigger if exists trg_auth_sessions_updated_at on auth_sessions;
create trigger trg_auth_sessions_updated_at before update on auth_sessions for each row execute function set_updated_at();
