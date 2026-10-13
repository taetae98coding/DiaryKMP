-- Firebase가 기기 수신 정보를 등록 토큰에서 설치 ID(FID)로 바꾸므로 두 방식을 함께 저장한다.
-- 이전 버전 앱은 방식 없이 토큰을 계속 보내므로 기존 행과 기존 함수는 'token' 방식으로 남는다.
-- 값 열의 이름은 이전 버전 앱이 부르는 함수와 맞추려고 token 그대로 둔다.
alter table public.fcm_token
    add column kind text not null default 'token'
        constraint fcm_token_kind_check check (kind in ('token', 'installation_id'));

-- 기존 register_fcm_token이 on conflict on constraint fcm_token_pkey를 쓰므로 같은 이름으로 다시 만든다.
alter table public.fcm_token drop constraint fcm_token_pkey;
alter table public.fcm_token add constraint fcm_token_pkey primary key (kind, token);

-- 이전 버전 앱의 해제와 발송 실패 정리가 토큰 방식의 행만 지우도록 방식을 한정한다.
-- create or replace는 기존 실행 권한을 유지한다.
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
    where fcm_token.kind = 'token'
        and fcm_token.token = unregister_fcm_token.token;
end;
$$;

-- 업데이트한 기기는 자기의 이전 토큰을 알 수 없어 해제하지 못하므로, 설치 ID를 등록할 때 같은 계정의 토큰 방식 행을 모두 지운다.
-- 저장과 정리는 한 함수 안에서 함께 반영되거나 함께 취소된다.
create or replace function public.register_fcm_installation_id(installation_id text, time_zone text, language text)
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

    if installation_id is null or btrim(installation_id) = '' then
        raise exception 'installation_id must not be blank.' using errcode = '22023';
    end if;

    if time_zone is null or btrim(time_zone) = '' then
        raise exception 'time_zone must not be blank.' using errcode = '22023';
    end if;

    if language is null or btrim(language) = '' then
        raise exception 'language must not be blank.' using errcode = '22023';
    end if;

    insert into public.fcm_token (kind, token, account_id, time_zone, language)
    values (
        'installation_id',
        register_fcm_installation_id.installation_id,
        current_account_id,
        register_fcm_installation_id.time_zone,
        register_fcm_installation_id.language
    )
    on conflict on constraint fcm_token_pkey do update
    set
        account_id = excluded.account_id,
        time_zone  = excluded.time_zone,
        language   = excluded.language,
        updated_at = now();

    delete from public.fcm_token
    where fcm_token.kind = 'token'
        and fcm_token.account_id = current_account_id;
end;
$$;

revoke all on function public.register_fcm_installation_id(text, text, text) from public;
revoke all on function public.register_fcm_installation_id(text, text, text) from anon;
grant execute on function public.register_fcm_installation_id(text, text, text) to authenticated;

-- 로그아웃한 게스트 기기가 보내므로 세션 없이 설치 ID만으로 지운다. 없는 설치 ID는 바꾸지 않고 정상 종료한다.
create or replace function public.unregister_fcm_installation_id(installation_id text)
    returns void
    language plpgsql
    security definer
    set search_path = ''
as $$
begin
    if installation_id is null or btrim(installation_id) = '' then
        raise exception 'installation_id must not be blank.' using errcode = '22023';
    end if;

    delete from public.fcm_token
    where fcm_token.kind = 'installation_id'
        and fcm_token.token = unregister_fcm_installation_id.installation_id;
end;
$$;

revoke all on function public.unregister_fcm_installation_id(text) from public;
revoke all on function public.unregister_fcm_installation_id(text) from anon;
grant execute on function public.unregister_fcm_installation_id(text) to anon, authenticated, service_role;

-- 발송 함수가 방식에 맞춰 대상을 고르도록 방식을 함께 돌려준다. 반환 열이 바뀌므로 지우고 다시 만든다.
drop function public.list_due_daily_memo_notification_tokens();

-- 발송 회차는 15분 단위다. 현재 시각을 15분 경계로 내린 회차 시각이 수신 정보의 시간대로 08:00인 행만 고른다.
-- 30분·45분 단위 오프셋 시간대도 15분 경계에 놓이므로 회차 하나가 그 시간대의 8시를 빠뜨리지 않는다.
-- 시간대 이름이 PostgreSQL이 모르는 값이면 at time zone이 실패하므로 아는 이름만 대상으로 한다.
-- expires_at은 수신 정보의 시간대로 그날이 끝나는 시점이며, 그때까지 도착하지 못한 알림은 버린다.
create function public.list_due_daily_memo_notification_tokens()
    returns table (
        kind       text,
        token      text,
        account_id uuid,
        time_zone  text,
        language   text,
        local_date date,
        expires_at timestamptz
    )
    language sql
    security definer
    set search_path = ''
    stable
as $$
    with slot as (
        select
            date_trunc('hour', now())
                + make_interval(mins => (floor(extract(minute from now()) / 15) * 15)::integer) as at
    )
    select
        fcm_token.kind,
        fcm_token.token,
        fcm_token.account_id,
        fcm_token.time_zone,
        fcm_token.language,
        (slot.at at time zone fcm_token.time_zone)::date as local_date,
        (((slot.at at time zone fcm_token.time_zone)::date + 1)::timestamp at time zone fcm_token.time_zone) as expires_at
    from public.fcm_token
    cross join slot
    where fcm_token.time_zone in (select name from pg_catalog.pg_timezone_names)
        and (slot.at at time zone fcm_token.time_zone)::time = time '08:00'
    order by fcm_token.kind, fcm_token.token;
$$;

revoke all on function public.list_due_daily_memo_notification_tokens() from public;
revoke all on function public.list_due_daily_memo_notification_tokens() from anon;
revoke all on function public.list_due_daily_memo_notification_tokens() from authenticated;
grant execute on function public.list_due_daily_memo_notification_tokens() to service_role;
