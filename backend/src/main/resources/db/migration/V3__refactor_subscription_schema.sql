drop table if exists subscriptions;

create table if not exists subscription_plans (
    id uuid primary key default gen_random_uuid(),
    code varchar(50) unique not null,
    name varchar(100) not null,
    billing_interval varchar(20) not null,
    price_amount numeric(12, 2) not null default 0,
    currency varchar(10) not null default 'KRW',
    is_active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint chk_subscription_plans_interval check (billing_interval in ('monthly', 'yearly'))
);

create table if not exists user_subscriptions (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    plan_id uuid not null references subscription_plans(id),
    status varchar(20) not null,
    started_at timestamptz not null default now(),
    current_period_start timestamptz not null,
    current_period_end timestamptz not null,
    cancel_at_period_end boolean not null default false,
    canceled_at timestamptz,
    ended_at timestamptz,
    provider varchar(50) not null default 'internal',
    provider_customer_id varchar(100),
    provider_subscription_id varchar(100),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint chk_user_subscriptions_status check (status in ('trialing', 'active', 'past_due', 'canceled', 'expired', 'unpaid', 'pending'))
);

create table if not exists billing_transactions (
    id uuid primary key default gen_random_uuid(),
    user_subscription_id uuid references user_subscriptions(id) on delete set null,
    user_id uuid not null references users(id) on delete cascade,
    amount numeric(12, 2) not null default 0,
    currency varchar(10) not null default 'KRW',
    status varchar(20) not null,
    paid_at timestamptz,
    provider varchar(50) not null default 'internal',
    provider_payment_id varchar(100),
    provider_order_id varchar(100),
    metadata jsonb,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint chk_billing_transactions_status check (status in ('pending', 'paid', 'failed', 'refunded', 'canceled'))
);

create index if not exists idx_user_subscriptions_user_id on user_subscriptions(user_id);
create index if not exists idx_user_subscriptions_status_period on user_subscriptions(status, current_period_end);
create unique index if not exists uq_user_subscriptions_one_open on user_subscriptions(user_id)
    where status in ('trialing', 'active', 'past_due') and ended_at is null;
create index if not exists idx_billing_transactions_user_id on billing_transactions(user_id);
create index if not exists idx_billing_transactions_subscription_id on billing_transactions(user_subscription_id);

drop trigger if exists trg_subscription_plans_updated_at on subscription_plans;
create trigger trg_subscription_plans_updated_at before update on subscription_plans for each row execute function set_updated_at();

drop trigger if exists trg_user_subscriptions_updated_at on user_subscriptions;
create trigger trg_user_subscriptions_updated_at before update on user_subscriptions for each row execute function set_updated_at();

drop trigger if exists trg_billing_transactions_updated_at on billing_transactions;
create trigger trg_billing_transactions_updated_at before update on billing_transactions for each row execute function set_updated_at();

insert into subscription_plans (code, name, billing_interval, price_amount, currency, is_active)
values ('standard-monthly', 'Standard Monthly', 'monthly', 9900, 'KRW', true)
on conflict (code) do update
set name = excluded.name,
    billing_interval = excluded.billing_interval,
    price_amount = excluded.price_amount,
    currency = excluded.currency,
    is_active = excluded.is_active,
    updated_at = now();
