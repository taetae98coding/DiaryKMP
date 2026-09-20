-- 삭제된 항목은 다른 기기가 그 삭제를 내려받을 수 있도록 삭제 상태로 남겨 두고, 60일이 지나면 영구히 제거한다.
-- 보존 기간은 기기가 강제 전체 재동기화를 시작하는 30일보다 길어야 한다. 그래야 마지막 실행 이후에 생긴
-- 삭제가 기기에 닿기 전에 사라지지 않는다.

create or replace function public.push_memos(items jsonb)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    affected_count integer := 0;
    inserted_id uuid;
    item jsonb;
    item_id uuid;
    item_primary_tag_id uuid;
    linked_account_id uuid;
begin
    if current_account_id is null then
        raise exception 'Authentication is required.' using errcode = '42501';
    end if;

    if items is null or jsonb_typeof(items) <> 'array' then
        raise exception 'items must be an array.' using errcode = '22023';
    end if;

    for item in
        select value
        from jsonb_array_elements(items)
    loop
        item_id := (item ->> 'id')::uuid;
        item_primary_tag_id := (item ->> 'primaryTagId')::uuid;

        if (item -> 'detail' ->> 'isAllDay' is null) <> (item -> 'detail' ->> 'start' is null)
            or (item -> 'detail' ->> 'isAllDay' is null) <> (item -> 'detail' ->> 'endInclusive' is null) then
            raise exception 'isAllDay, start and endInclusive must be set together.' using errcode = '22023';
        end if;

        if (item -> 'detail' ->> 'start')::timestamp > (item -> 'detail' ->> 'endInclusive')::timestamp then
            raise exception 'start must not be after endInclusive.' using errcode = '22023';
        end if;

        -- 대표 태그가 서버에 아예 없으면 영구 제거된 태그이므로, 업로드를 막지 않고 지정 해제로 반영한다.
        -- 서버에 남아 있는데 계정과 연결되지 않은 태그는 권한 위반이므로 그대로 거부한다.
        if item_primary_tag_id is not null then
            if not exists (
                select 1
                from public.tag
                where id = item_primary_tag_id
            ) then
                item_primary_tag_id := null;
            elsif not exists (
                select 1
                from public.account_tag
                where account_id = current_account_id
                    and tag_id = item_primary_tag_id
            ) then
                raise exception 'Primary tag is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;
        end if;

        if exists (
            select 1
            from public.account_memo
            where account_id = current_account_id
                and memo_id = item_id
        ) then
            update public.memo
            set
                title = item -> 'detail' ->> 'title',
                description = item -> 'detail' ->> 'description',
                color = (item -> 'detail' ->> 'color')::bigint,
                is_all_day = (item -> 'detail' ->> 'isAllDay')::boolean,
                start_at = (item -> 'detail' ->> 'start')::timestamp,
                end_inclusive = (item -> 'detail' ->> 'endInclusive')::timestamp,
                primary_tag_id = item_primary_tag_id,
                is_finished = (item ->> 'isFinished')::boolean,
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where id = item_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            if found then
                for linked_account_id in
                    select account_id
                    from public.account_memo
                    where memo_id = item_id
                    order by account_id
                loop
                    update public.account_memo
                    set usn = public.next_account_usn(linked_account_id)
                    where account_id = linked_account_id
                        and memo_id = item_id;
                end loop;

                affected_count := affected_count + 1;
            end if;
        else
            inserted_id := null;

            insert into public.memo (
                id,
                title,
                description,
                color,
                is_all_day,
                start_at,
                end_inclusive,
                primary_tag_id,
                is_finished,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_id,
                item -> 'detail' ->> 'title',
                item -> 'detail' ->> 'description',
                (item -> 'detail' ->> 'color')::bigint,
                (item -> 'detail' ->> 'isAllDay')::boolean,
                (item -> 'detail' ->> 'start')::timestamp,
                (item -> 'detail' ->> 'endInclusive')::timestamp,
                item_primary_tag_id,
                (item ->> 'isFinished')::boolean,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            )
            on conflict (id) do nothing
            returning id into inserted_id;

            if inserted_id is null then
                raise exception 'Memo is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;

            insert into public.account_memo (account_id, memo_id, usn)
            values (
                current_account_id,
                inserted_id,
                public.next_account_usn(current_account_id)
            );

            affected_count := affected_count + 1;
        end if;
    end loop;

    return affected_count;
end;
$$;

-- 삭제 상태로 보존 기간이 지난 항목을 영구히 제거한다.
-- 연결은 가리키는 엔티티가 사라지면 함께 사라지므로 연결을 먼저 정리해 엔티티 삭제가 끌고 가는 범위를 줄인다.
-- 태그는 메모가 대표 태그로 참조하므로 마지막에 따로 다룬다.
--
-- batch_size는 한 번의 실행이 종류마다 지우는 상한이다. 함수 전체가 한 트랜잭션이라 트랜잭션 크기를
-- 줄이지는 못하고, 한 번의 실행 시간이 예측 범위를 벗어나지 않게 막는 역할만 한다.
create or replace function public.purge_deleted_entities(
    retention interval default '60 days',
    batch_size integer default 50000
)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    threshold timestamptz := now() - retention;
    purged_count integer := 0;
    deleted_count integer;
    target_table text;
    purge_tag_id_list uuid[];
    affected_memo_id_list uuid[];
    affected_memo_id uuid;
    linked_account_id uuid;
begin
    foreach target_table in array array[
        'memo_tag',
        'memo_place',
        'memo_web',
        'tag_link',
        'web_tag',
        'place_tag',
        'memo',
        'place',
        'web',
        'contact',
        'music'
    ]
    loop
        execute format(
            'delete from public.%1$I where ctid in ('
            || 'select ctid from public.%1$I where is_deleted and updated_at < $1 limit $2'
            || ')',
            target_table
        ) using threshold, batch_size;

        get diagnostics deleted_count = row_count;
        purged_count := purged_count + deleted_count;
    end loop;

    select array_agg(target.id)
    into purge_tag_id_list
    from (
        select id
        from public.tag
        where is_deleted
            and updated_at < threshold
        limit batch_size
    ) as target;

    if purge_tag_id_list is null then
        return purged_count;
    end if;

    -- 태그 행을 지우면 외래 키가 memo.primary_tag_id를 null로 바꾸지만 순번을 올리지 않아 기기가 해제를
    -- 내려받지 못한다. 순번을 올려 정상 변경으로 전달하기 위해 지우기 전에 직접 해제한다.
    select array_agg(id)
    into affected_memo_id_list
    from public.memo
    where primary_tag_id = any (purge_tag_id_list);

    if affected_memo_id_list is not null then
        update public.memo
        set primary_tag_id = null
        where id = any (affected_memo_id_list);

        for affected_memo_id, linked_account_id in
            select account_memo.memo_id, account_memo.account_id
            from public.account_memo
            where account_memo.memo_id = any (affected_memo_id_list)
            order by account_memo.account_id, account_memo.memo_id
        loop
            update public.account_memo
            set usn = public.next_account_usn(linked_account_id)
            where account_id = linked_account_id
                and memo_id = affected_memo_id;
        end loop;
    end if;

    delete from public.tag
    where id = any (purge_tag_id_list);

    get diagnostics deleted_count = row_count;
    purged_count := purged_count + deleted_count;

    return purged_count;
end;
$$;

revoke all on function public.purge_deleted_entities(interval, integer) from public;
revoke all on function public.purge_deleted_entities(interval, integer) from anon;
revoke all on function public.purge_deleted_entities(interval, integer) from authenticated;

create extension if not exists pg_cron;

-- 정상 상태의 하루 삭제량은 batch_size에 한참 못 미치므로 하루 한 번으로 충분하다.
-- 사용이 적은 시각에 돌도록 03:00 KST에 맞춘다. pg_cron의 시각 기준은 서버 시간대인 UTC다.
select cron.unschedule('purge-deleted-entities')
where exists (
    select 1
    from cron.job
    where jobname = 'purge-deleted-entities'
);

select cron.schedule(
    'purge-deleted-entities',
    '0 18 * * *',
    $cron$select public.purge_deleted_entities()$cron$
);
