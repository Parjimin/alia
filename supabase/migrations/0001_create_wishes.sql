create table if not exists public.wishes (
    id uuid primary key default gen_random_uuid(),
    request_id uuid not null unique,
    message text not null,
    created_at timestamptz not null default now(),
    constraint wishes_message_length
        check (
            char_length(message) <= 500
            and char_length(btrim(message)) >= 1
        )
);

alter table public.wishes enable row level security;

revoke all privileges
on table public.wishes
from anon, authenticated;

grant usage
on schema public
to anon;

grant insert (request_id, message)
on table public.wishes
to anon;

drop policy if exists "little blue world may submit wishes"
on public.wishes;

create policy "little blue world may submit wishes"
on public.wishes
for insert
to anon
with check (
    request_id is not null
    and char_length(message) <= 500
    and char_length(btrim(message)) >= 1
);
