-- 일일 메모 알림은 서버가 등록된 FCM 토큰으로 보낸다. 15분마다 pg_cron이 Edge Function을 깨우고,
-- Edge Function은 아래 함수로 이번 회차에 오전 8시가 된 토큰과 그 계정의 오늘의 메모를 읽어 FCM으로 보낸다.
-- 아래 함수는 모두 service role만 호출한다. 앱은 토큰을 읽을 일이 없고 다른 계정의 메모를 볼 수 없어야 한다.

-- 마지막 등록 뒤 오래 지난 토큰은 앱을 지웠거나 쓰지 않는 기기의 것이므로 보내지 않고 지운다.
create or replace function public.purge_stale_fcm_tokens(retention interval default '60 days')
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    purged_count integer;
begin
    delete from public.fcm_token
    where updated_at < now() - retention;

    get diagnostics purged_count = row_count;

    return purged_count;
end;
$$;

revoke all on function public.purge_stale_fcm_tokens(interval) from public;
revoke all on function public.purge_stale_fcm_tokens(interval) from anon;
revoke all on function public.purge_stale_fcm_tokens(interval) from authenticated;
grant execute on function public.purge_stale_fcm_tokens(interval) to service_role;

-- 발송 회차는 15분 단위다. 현재 시각을 15분 경계로 내린 회차 시각이 토큰의 시간대로 08:00인 토큰만 고른다.
-- 30분·45분 단위 오프셋 시간대도 15분 경계에 놓이므로 회차 하나가 그 시간대의 8시를 빠뜨리지 않는다.
-- 시간대 이름이 PostgreSQL이 모르는 값이면 at time zone이 실패하므로 아는 이름만 대상으로 한다.
-- expires_at은 토큰 시간대로 그날이 끝나는 시점이며, 그때까지 도착하지 못한 알림은 버린다.
create or replace function public.list_due_daily_memo_notification_tokens()
    returns table (
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
    order by fcm_token.token;
$$;

revoke all on function public.list_due_daily_memo_notification_tokens() from public;
revoke all on function public.list_due_daily_memo_notification_tokens() from anon;
revoke all on function public.list_due_daily_memo_notification_tokens() from authenticated;
grant execute on function public.list_due_daily_memo_notification_tokens() to service_role;

-- 메모의 기간은 시간대 없는 벽시계 값이므로 토큰 시간대의 날짜와 날짜 단위로 겹침을 비교한다.
-- 정렬은 캘린더 메모 조회와 같다. 종일 메모가 앞에 오고, 종일은 시작 이른 순·종료 늦은 순, 시각 있는 메모는 시작 이른 순·종료 이른 순이다.
create or replace function public.list_daily_memos(target_account_id uuid, target_date date)
    returns table (title text)
    language sql
    security definer
    set search_path = ''
    stable
as $$
    select memo.title
    from public.memo
    join public.account_memo
        on account_memo.memo_id = memo.id
    where account_memo.account_id = target_account_id
        and memo.start_at is not null
        and memo.end_inclusive is not null
        and not memo.is_finished
        and not memo.is_deleted
        and memo.start_at::date <= target_date
        and memo.end_inclusive::date >= target_date
    order by
        memo.is_all_day desc,
        memo.start_at asc,
        case when memo.is_all_day then memo.end_inclusive end desc,
        case when not memo.is_all_day then memo.end_inclusive end asc,
        memo.title asc,
        memo.id asc;
$$;

revoke all on function public.list_daily_memos(uuid, date) from public;
revoke all on function public.list_daily_memos(uuid, date) from anon;
revoke all on function public.list_daily_memos(uuid, date) from authenticated;
grant execute on function public.list_daily_memos(uuid, date) to service_role;

-- FCM이 더 이상 유효하지 않다고 답한 토큰은 service role이 해제한다. 앱이 쓰는 해제 함수를 그대로 쓴다.
grant execute on function public.unregister_fcm_token(text) to service_role;

create extension if not exists pg_net;

-- Edge Function 주소와 service role 키는 마이그레이션에 둘 수 없으므로 Vault에서 읽는다.
-- 배포 전에 Vault에 다음 두 비밀을 넣어야 한다. Edge 런타임의 SUPABASE_SERVICE_ROLE_KEY는 옛 JWT가 아니라
-- 새 형식의 secret API 키(sb_secret_...)이므로, 함수가 대조하는 값과 같은 그 키를 넣는다.
--   select vault.create_secret('https://<project-ref>.supabase.co', 'project_url');
--   select vault.create_secret('<secret API key, sb_secret_...>', 'service_role_key');
-- 두 비밀이 없으면 이 작업은 매 회차 실패하고 알림은 나가지 않는다.
select cron.unschedule('send-daily-memo-notification')
where exists (
    select 1
    from cron.job
    where jobname = 'send-daily-memo-notification'
);

select cron.schedule(
    'send-daily-memo-notification',
    '*/15 * * * *',
    $cron$
    select net.http_post(
        url := (select decrypted_secret from vault.decrypted_secrets where name = 'project_url')
            || '/functions/v1/v1-daily-memo-notification-send',
        headers := jsonb_build_object(
            'Content-Type', 'application/json',
            'Authorization', 'Bearer ' || (select decrypted_secret from vault.decrypted_secrets where name = 'service_role_key')
        ),
        body := '{}'::jsonb,
        timeout_milliseconds := 60000
    )
    $cron$
);
