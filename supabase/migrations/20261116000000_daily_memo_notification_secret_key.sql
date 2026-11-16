-- 일일 메모 알림 예약 작업이 Edge Function에 보내는 키를 Authorization 헤더에서 apikey 헤더로 옮긴다.
-- secret API 키(sb_secret_...)는 JWT가 아니어서 Authorization: Bearer로 보내지 않는다.
-- 배포 전에 Vault에 함수가 대조하는 default secret API 키를 넣어야 한다. project_url은 이미 있는 값을 그대로 쓴다.
--   select vault.create_secret('<secret API key, sb_secret_...>', 'secret_key');
-- 이 비밀이 없으면 이 작업은 매 회차 실패하고 알림은 나가지 않는다.
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
            'apikey', (select decrypted_secret from vault.decrypted_secrets where name = 'secret_key')
        ),
        body := '{}'::jsonb,
        timeout_milliseconds := 60000
    )
    $cron$
);
