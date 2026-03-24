create extension if not exists pgcrypto;

create or replace function set_updated_at()
returns trigger as
$$
begin
    new.updated_at = now();
    return new;
end;
$$ language plpgsql;

create table if not exists users (
    id uuid primary key default gen_random_uuid(),
    email varchar(255) unique not null,
    password_hash text,
    auth_provider varchar(50) not null default 'email',
    name varchar(100),
    role varchar(20) not null default 'user',
    native_language varchar(10) not null default 'ko',
    target_language varchar(10) not null default 'en',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint chk_users_role check (role in ('user', 'admin')),
    constraint chk_users_auth_provider check (auth_provider in ('email', 'google', 'github', 'magic_link'))
);

create table if not exists series (
    id uuid primary key default gen_random_uuid(),
    title varchar(255) not null,
    slug varchar(255) unique not null,
    description text,
    thumbnail_url text,
    category varchar(100),
    source_language varchar(10) not null default 'ko',
    target_language varchar(10) not null default 'en',
    is_published boolean not null default false,
    created_by uuid references users(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists packs (
    id uuid primary key default gen_random_uuid(),
    series_id uuid not null references series(id) on delete cascade,
    title varchar(255) not null,
    description text,
    order_index integer not null default 0,
    is_published boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists learning_items (
    id uuid primary key default gen_random_uuid(),
    pack_id uuid not null references packs(id) on delete cascade,
    source_text text not null,
    target_text text not null,
    nuance_note text,
    part_of_speech varchar(50),
    tags text[] not null default '{}',
    difficulty varchar(20),
    example_sentence text,
    example_translation text,
    audio_url text,
    synonyms text[] not null default '{}',
    accepted_answers text[] not null default '{}',
    order_index integer not null default 0,
    is_published boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint chk_learning_items_difficulty check (difficulty is null or difficulty in ('beginner', 'intermediate', 'advanced'))
);

create table if not exists subscriptions (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    series_id uuid not null references series(id) on delete cascade,
    status varchar(20) not null default 'active',
    subscribed_at timestamptz not null default now(),
    unique (user_id, series_id),
    constraint chk_subscriptions_status check (status in ('active', 'paused', 'canceled'))
);

create table if not exists favorites (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    learning_item_id uuid not null references learning_items(id) on delete cascade,
    created_at timestamptz not null default now(),
    unique (user_id, learning_item_id)
);

create table if not exists user_answers (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    learning_item_id uuid not null references learning_items(id) on delete cascade,
    typed_answer text not null,
    normalized_answer text,
    is_correct boolean,
    response_time_ms integer,
    created_at timestamptz not null default now()
);

create table if not exists sentence_practices (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    learning_item_id uuid not null references learning_items(id) on delete cascade,
    sentence_text text not null,
    ai_feedback jsonb,
    ai_score numeric(5, 2),
    created_at timestamptz not null default now()
);

create table if not exists review_schedules (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    learning_item_id uuid not null references learning_items(id) on delete cascade,
    ease_factor numeric(4, 2) not null default 2.50,
    interval_days integer not null default 0,
    repetition_count integer not null default 0,
    lapse_count integer not null default 0,
    next_review_at timestamptz,
    last_reviewed_at timestamptz,
    last_result varchar(20),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (user_id, learning_item_id),
    constraint chk_review_schedules_last_result check (last_result is null or last_result in ('again', 'hard', 'good', 'easy'))
);

create table if not exists review_logs (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    learning_item_id uuid not null references learning_items(id) on delete cascade,
    review_schedule_id uuid references review_schedules(id) on delete set null,
    result varchar(20) not null,
    previous_interval_days integer,
    new_interval_days integer,
    previous_ease_factor numeric(4, 2),
    new_ease_factor numeric(4, 2),
    reviewed_at timestamptz not null default now(),
    constraint chk_review_logs_result check (result in ('again', 'hard', 'good', 'easy'))
);

create table if not exists series_progress (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    series_id uuid not null references series(id) on delete cascade,
    total_items integer not null default 0,
    learned_items integer not null default 0,
    due_items integer not null default 0,
    last_studied_at timestamptz,
    updated_at timestamptz not null default now(),
    unique (user_id, series_id)
);

create table if not exists content_import_jobs (
    id uuid primary key default gen_random_uuid(),
    uploaded_by uuid references users(id),
    file_url text,
    status varchar(20) not null default 'pending',
    result_summary jsonb,
    created_at timestamptz not null default now(),
    constraint chk_content_import_jobs_status check (status in ('pending', 'processing', 'completed', 'failed'))
);

create index if not exists idx_series_published on series(is_published);
create index if not exists idx_packs_series_id on packs(series_id);
create index if not exists idx_learning_items_pack_id on learning_items(pack_id);
create index if not exists idx_learning_items_published on learning_items(is_published);
create index if not exists idx_subscriptions_user_id on subscriptions(user_id);
create index if not exists idx_favorites_user_id on favorites(user_id);
create index if not exists idx_review_schedules_user_next_review on review_schedules(user_id, next_review_at);
create index if not exists idx_user_answers_user_item on user_answers(user_id, learning_item_id);
create index if not exists idx_sentence_practices_user_item on sentence_practices(user_id, learning_item_id);
create index if not exists idx_series_progress_user_series on series_progress(user_id, series_id);
create index if not exists idx_content_import_jobs_uploaded_by on content_import_jobs(uploaded_by);

drop trigger if exists trg_users_updated_at on users;
create trigger trg_users_updated_at before update on users for each row execute function set_updated_at();

drop trigger if exists trg_series_updated_at on series;
create trigger trg_series_updated_at before update on series for each row execute function set_updated_at();

drop trigger if exists trg_packs_updated_at on packs;
create trigger trg_packs_updated_at before update on packs for each row execute function set_updated_at();

drop trigger if exists trg_learning_items_updated_at on learning_items;
create trigger trg_learning_items_updated_at before update on learning_items for each row execute function set_updated_at();

drop trigger if exists trg_review_schedules_updated_at on review_schedules;
create trigger trg_review_schedules_updated_at before update on review_schedules for each row execute function set_updated_at();

drop trigger if exists trg_series_progress_updated_at on series_progress;
create trigger trg_series_progress_updated_at before update on series_progress for each row execute function set_updated_at();
