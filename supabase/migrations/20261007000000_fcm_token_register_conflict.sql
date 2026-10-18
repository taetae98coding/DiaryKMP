-- register_fcm_token의 on conflict (token)이 파라미터 token과 컬럼 token 중 어느 것인지 정하지 못해
-- 42702(column reference "token" is ambiguous)로 실패해 왔다. 충돌 대상을 기본 키 제약 이름으로 지정해 모호함을 없앤다.
-- create or replace는 기존 실행 권한을 유지한다.
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
    on conflict on constraint fcm_token_pkey do update
    set
        account_id = excluded.account_id,
        time_zone  = excluded.time_zone,
        language   = excluded.language,
        updated_at = now();
end;
$$;
