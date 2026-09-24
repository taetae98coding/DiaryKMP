-- 기기가 서버 푸시 알림을 받기 위해 등록한 FCM 토큰. 토큰 하나가 한 건이며,
-- 발송 시각과 문구를 정하려면 기기의 시간대와 언어가 함께 필요하다.
create table public.fcm_token (
    token      text        primary key,
    account_id uuid        not null references public.account (id) on delete cascade,
    time_zone  text        not null,
    language   text        not null,
    updated_at timestamptz not null default now(),
    created_at timestamptz not null default now()
);

create index fcm_token_account_id_idx on public.fcm_token (account_id);

-- 앱이 토큰을 읽을 일은 없으므로 authenticated에 권한을 주지 않고 아래 함수로만 다룬다.
alter table public.fcm_token enable row level security;

-- 같은 토큰이 다른 계정으로 오면 연결 계정을 바꾼다. 같은 기기에서 계정을 바꿔 로그인한 경우다.
create or replace function public.register_fcm_token(token text, time_zone text, language text)
    returns void
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
begin
    if current_account_id is null then
        raise exception 'Authentication is required.' using errcode = '42501';
    end if;

    if token is null or btrim(token) = '' then
        raise exception 'token must not be blank.' using errcode = '22023';
    end if;

    if time_zone is null or btrim(time_zone) = '' then
        raise exception 'time_zone must not be blank.' using errcode = '22023';
    end if;

    if language is null or btrim(language) = '' then
        raise exception 'language must not be blank.' using errcode = '22023';
    end if;

    insert into public.fcm_token (token, account_id, time_zone, language)
    values (register_fcm_token.token, current_account_id, register_fcm_token.time_zone, register_fcm_token.language)
    on conflict (token) do update
    set
        account_id = excluded.account_id,
        time_zone  = excluded.time_zone,
        language   = excluded.language,
        updated_at = now();
end;
$$;

revoke all on function public.register_fcm_token(text, text, text) from public;
revoke all on function public.register_fcm_token(text, text, text) from anon;
grant execute on function public.register_fcm_token(text, text, text) to authenticated;

-- 로그아웃한 게스트 기기가 보내므로 세션 없이 토큰만으로 지운다. 없는 토큰은 바꾸지 않고 정상 종료한다.
create or replace function public.unregister_fcm_token(token text)
    returns void
    language plpgsql
    security definer
    set search_path = ''
as $$
begin
    if token is null or btrim(token) = '' then
        raise exception 'token must not be blank.' using errcode = '22023';
    end if;

    delete from public.fcm_token
    where fcm_token.token = unregister_fcm_token.token;
end;
$$;

revoke all on function public.unregister_fcm_token(text) from public;
grant execute on function public.unregister_fcm_token(text) to anon, authenticated;
