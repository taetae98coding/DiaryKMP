-- 일일 알림은 오늘의 메모에 더해 오늘의 루틴과 오늘의 디데이를 담는다. 아래 함수는 모두 service role만 호출한다.
-- 메모만 담던 때의 이름(daily_memo_notification)을 daily_notification으로 옮긴다. Edge Function도 v1-daily-notification-send로 옮기므로,
-- 새 함수를 먼저 배포한 뒤 이 마이그레이션을 적용하고, 그다음 옛 v1-daily-memo-notification-send를 지운다.
-- 마이그레이션을 먼저 적용하면 새 함수가 배포될 때까지의 회차는 알림을 보내지 못한다.

-- 발송 회차는 15분 단위다. 현재 시각을 15분 경계로 내린 회차 시각이 수신 정보의 시간대로 08:00인 행만 고른다.
-- 30분·45분 단위 오프셋 시간대도 15분 경계에 놓이므로 회차 하나가 그 시간대의 8시를 빠뜨리지 않는다.
-- 시간대 이름이 PostgreSQL이 모르는 값이면 at time zone이 실패하므로 아는 이름만 대상으로 한다.
-- expires_at은 수신 정보의 시간대로 그날이 끝나는 시점이며, 그때까지 도착하지 못한 알림은 버린다.
create function public.list_due_daily_notification_tokens()
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

revoke all on function public.list_due_daily_notification_tokens() from public;
revoke all on function public.list_due_daily_notification_tokens() from anon;
revoke all on function public.list_due_daily_notification_tokens() from authenticated;
grant execute on function public.list_due_daily_notification_tokens() to service_role;

drop function public.list_due_daily_memo_notification_tokens();

-- 오늘의 루틴은 알림이 발생하는 날을 오늘로 삼은 '오늘 할 루틴'이다. 완료되지 않았고, 시작 날짜가 그 날과 같거나 앞이며,
-- 그 날의 요일이 주기가 정한 하는 요일에 드는 루틴이다. 평일만은 월~금, 주말만은 토·일이고 나머지 주기는 모든 요일이다.
-- 이번 주기는 달력 기준이다. 매일·평일만·주말만은 그 날, 매주는 그 날이 속한 일요일~토요일, 매월은 그 달 전체다.
-- 합계는 이번 주기에 든 삭제되지 않은 기록의 양을 모두 더한 값이며, 그 날보다 뒤의 기록도 이번 주기에 들면 더한다.
create or replace function public.list_daily_routines(target_account_id uuid, target_date date)
    returns table (
        title        text,
        cycle        text,
        unit         text,
        goal_amount  integer,
        period_total bigint
    )
    language sql
    security definer
    set search_path = ''
    stable
as $$
    with today_routine as (
        select
            routine.id,
            routine.title,
            routine.cycle,
            routine.unit,
            routine.goal_amount,
            case routine.cycle
                when 'FREQ=WEEKLY;WKST=SU' then target_date - extract(dow from target_date)::integer
                when 'FREQ=MONTHLY' then make_date(extract(year from target_date)::integer, extract(month from target_date)::integer, 1)
                else target_date
            end as period_start,
            case routine.cycle
                when 'FREQ=WEEKLY;WKST=SU' then target_date - extract(dow from target_date)::integer + 6
                when 'FREQ=MONTHLY' then (make_date(extract(year from target_date)::integer, extract(month from target_date)::integer, 1) + interval '1 month')::date - 1
                else target_date
            end as period_end
        from public.routine
        join public.account_routine
            on account_routine.routine_id = routine.id
        where account_routine.account_id = target_account_id
            and not routine.is_finished
            and not routine.is_deleted
            and routine.start_date <= target_date
            and case routine.cycle
                when 'FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR' then extract(isodow from target_date) between 1 and 5
                when 'FREQ=DAILY;BYDAY=SA,SU' then extract(isodow from target_date) in (6, 7)
                else true
            end
    )
    select
        today_routine.title,
        today_routine.cycle,
        today_routine.unit,
        today_routine.goal_amount,
        coalesce((
            select sum(routine_record.amount)
            from public.routine_record
            join public.account_routine_record
                on account_routine_record.routine_record_id = routine_record.id
            where account_routine_record.account_id = target_account_id
                and routine_record.routine_id = today_routine.id
                and not routine_record.is_deleted
                and routine_record.date between today_routine.period_start and today_routine.period_end
        ), 0)::bigint as period_total
    from today_routine
    order by today_routine.title asc, today_routine.id asc;
$$;

revoke all on function public.list_daily_routines(uuid, date) from public;
revoke all on function public.list_daily_routines(uuid, date) from anon;
revoke all on function public.list_daily_routines(uuid, date) from authenticated;
grant execute on function public.list_daily_routines(uuid, date) to service_role;

-- 기념일은 반복 간격과 달력에 없는 날짜 처리로 그때그때 정하므로, 그 날이 기준일 당일이나 기념일인지는 Edge Function이 계산한다.
-- 여기서는 계정의 삭제되지 않은 디데이 중 기준일이 그 날과 같거나 앞인 것만 돌려준다. 기준일보다 앞에는 기념일이 없다.
-- 정렬은 캘린더 디데이 조회와 같이 제목 오름차순, 식별값 오름차순이다.
create or replace function public.list_daily_dday_candidates(target_account_id uuid, target_date date)
    returns table (
        id              uuid,
        title           text,
        base_date       date,
        counts_base_day boolean,
        repeats         jsonb,
        end_date        date,
        overflow_policy text
    )
    language sql
    security definer
    set search_path = ''
    stable
as $$
    select
        dday.id,
        dday.title,
        dday.base_date,
        dday.counts_base_day,
        dday.repeats,
        dday.end_date,
        dday.overflow_policy
    from public.dday
    join public.account_dday
        on account_dday.dday_id = dday.id
    where account_dday.account_id = target_account_id
        and not dday.is_deleted
        and dday.base_date <= target_date
    order by dday.title asc, dday.id asc;
$$;

revoke all on function public.list_daily_dday_candidates(uuid, date) from public;
revoke all on function public.list_daily_dday_candidates(uuid, date) from anon;
revoke all on function public.list_daily_dday_candidates(uuid, date) from authenticated;
grant execute on function public.list_daily_dday_candidates(uuid, date) to service_role;

-- 예약 작업도 새 이름으로 옮기고 새 Edge Function을 부른다. 키와 주소는 이전과 같이 Vault의 project_url과 secret_key를 읽는다.
select cron.unschedule('send-daily-memo-notification')
where exists (
    select 1
    from cron.job
    where jobname = 'send-daily-memo-notification'
);

select cron.unschedule('send-daily-notification')
where exists (
    select 1
    from cron.job
    where jobname = 'send-daily-notification'
);

select cron.schedule(
    'send-daily-notification',
    '*/15 * * * *',
    $cron$
    select net.http_post(
        url := (select decrypted_secret from vault.decrypted_secrets where name = 'project_url')
            || '/functions/v1/v1-daily-notification-send',
        headers := jsonb_build_object(
            'Content-Type', 'application/json',
            'apikey', (select decrypted_secret from vault.decrypted_secrets where name = 'secret_key')
        ),
        body := '{}'::jsonb,
        timeout_milliseconds := 60000
    )
    $cron$
);
