# SUPABASE.md

## Scope
Supabase is used only for explicit `SEND IT INTO THE LITTLE WORLD` Wish submission.

No Auth, analytics, admin panel, remote content, photo storage, or tracking.

Owner reads rows manually in Supabase Dashboard/Table Editor.

## Client credentials
APK may contain:
- Supabase URL
- `sb_publishable_...` publishable key

Never bundle:
- `sb_secret_...`
- service-role key
- database password
- personal access token

Security comes from grants + RLS, not hiding a public mobile key.

## Table
`public.wishes`

Columns:
- `id uuid primary key default gen_random_uuid()`
- `request_id uuid not null unique`
- `message text not null`
- `created_at timestamptz not null default now()`

## SQL
```sql
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
```

Create **no** SELECT, UPDATE, or DELETE policies for anon.

## REST
Endpoint:
`https://<PROJECT_REF>.supabase.co/rest/v1/wishes`

Headers:
- `apikey: <SUPABASE_PUBLISHABLE_KEY>`
- `Content-Type: application/json`
- `Prefer: return=minimal`

Payload:
```json
{
  "request_id": "<uuid>",
  "message": "<wish>"
}
```

No manual `Authorization: Bearer <publishable-key>` header is required for this no-Auth design.

## request_id
Generate one UUID for the logical Send action and persist it before first request.
All retries use the same UUID.

If first insert succeeded but response was lost, the same `request_id` causes a unique conflict instead of a duplicate row.

A confirmed PostgreSQL unique violation (`23505`) for the same persisted request is treated as logical success.

Do **not** treat every HTTP 409 as success without parsing the error.

## Local states
- NONE
- DRAFT
- SEALED
- KEPT_LOCAL
- PENDING_SEND
- SENT

Rules:
- SEALED is not upload consent.
- Keep Local -> KEPT_LOCAL, never upload/worker.
- Send -> persist PENDING_SEND before network.
- Online success -> SENT.
- Temporary failure -> keep PENDING_SEND + schedule retry + continue finale.
- Duplicate same request_id -> SENT.

## WorkManager
Unique work name:
`little_blue_world_pending_wish`

Constraint:
network connected.

Worker reads pending payload from DataStore; do not duplicate Wish text inside WorkManager input.

Temporary failures (offline/timeout/5xx) -> retry.
Permanent configuration/payload errors -> do not loop forever; preserve local data and log only non-sensitive technical context.

No Android notification for background success.

## Data minimization
Server intentionally stores only:
- request_id
- message
- created_at
- server-generated row id

Do not intentionally collect location, Android ID, advertising ID, contacts, phone, email, device model, or analytics events.

Never log the Wish body.

## Threat model
Publishable key can be extracted from the APK. That is expected.
Strict INSERT-only permissions prevent reading/editing/deleting Wishes, but direct public INSERT is not general anti-spam protection.

This is acceptable for a one-recipient sideloaded v1. If app becomes public or abuse occurs, escalate with server-side rate/quota controls or an Edge Function. Never solve spam by placing a secret/service-role credential in the APK.

## Manual release tests
Using publishable key:
- valid INSERT succeeds
- SELECT denied
- UPDATE denied
- DELETE denied
- attempt to set created_at denied by column grant
- blank message rejected
- 501-char message rejected
- duplicate request_id does not create two rows
- online Send yields one row
- offline Send -> close app -> reconnect -> eventually one row
- Keep Local -> reconnect/restart -> zero rows
- no Wish message appears in logs
- delete development test rows before final handoff
