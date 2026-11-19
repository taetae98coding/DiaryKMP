-- 디데이도 메모처럼 연결된 태그 하나를 대표 태그로 가진다.
-- 대표 태그가 생기기 전 버전의 앱은 v1 Edge Function으로 대표 태그 없이 올리고, 현재 앱은 v2 Edge Function으로 대표 태그를 반드시 담아 올린다.
-- v1의 push_ddays는 그대로 두어 대표 태그를 바꾸지 않는다. 고칠 때는 저장된 대표 태그를 유지하고, 새로 만들 때는 컬럼 기본값(null)을 쓴다.
-- 내려받기는 v1 하나로 대표 태그를 더해 내려주고, 이전 버전의 앱은 모르는 키를 무시한다.
alter table public.dday
    add column primary_tag_id uuid references public.tag (id) on delete set null;

create index dday_primary_tag_id_idx on public.dday (primary_tag_id);

create or replace function public.push_ddays_v2(items jsonb)
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
    item_detail jsonb;
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

    -- 반영하기 전에 모든 항목을 확인해, 하나라도 어긋나면 아무것도 반영하지 않고 요청 전체를 거절한다.
    for item in
        select value
        from jsonb_array_elements(items)
    loop
        item_detail := item -> 'detail';

        if jsonb_typeof(item_detail -> 'repeats') is distinct from 'array' then
            raise exception 'repeats must be an array.' using errcode = '22023';
        end if;

        -- CASE로 순서를 고정해 객체가 아닌 값에 키 함수를 부르지 않는다.
        if exists (
            select 1
            from jsonb_array_elements(item_detail -> 'repeats') as repeat_entry(value)
            where case
                when jsonb_typeof(repeat_entry.value) <> 'object' then true
                when (select count(*) from jsonb_object_keys(repeat_entry.value)) <> 1 then true
                when not (repeat_entry.value ? 'days' or repeat_entry.value ? 'months') then true
                when jsonb_typeof(coalesce(repeat_entry.value -> 'days', repeat_entry.value -> 'months')) <> 'number' then true
                else (coalesce(repeat_entry.value ->> 'days', repeat_entry.value ->> 'months'))::numeric not between 1 and 2147483647
                    or (coalesce(repeat_entry.value ->> 'days', repeat_entry.value ->> 'months'))::numeric
                        <> trunc((coalesce(repeat_entry.value ->> 'days', repeat_entry.value ->> 'months'))::numeric)
            end
        ) then
            raise exception 'Each repeat must have exactly one of days or months as a positive integer.' using errcode = '22023';
        end if;

        if (
            select count(*) <> count(distinct repeat_entry.value)
            from jsonb_array_elements(item_detail -> 'repeats') as repeat_entry(value)
        ) then
            raise exception 'repeats must not contain the same interval twice.' using errcode = '22023';
        end if;

        if jsonb_typeof(item_detail -> 'endDate') = 'string'
            and (item_detail ->> 'endDate')::date < (item_detail ->> 'baseDate')::date then
            raise exception 'endDate must not be before baseDate.' using errcode = '22023';
        end if;

        if (item_detail ->> 'overflowPolicy') is null
            or (item_detail ->> 'overflowPolicy') not in ('clamp_to_month_end', 'skip', 'next_month_first_day') then
            raise exception 'overflowPolicy must be one of clamp_to_month_end, skip or next_month_first_day.' using errcode = '22023';
        end if;

        -- 서버에 남아 있는데 계정과 연결되지 않은 대표 태그는 권한 위반이므로 요청 전체를 거부한다.
        item_primary_tag_id := (item ->> 'primaryTagId')::uuid;

        if item_primary_tag_id is not null
            and exists (
                select 1
                from public.tag
                where id = item_primary_tag_id
            )
            and not exists (
                select 1
                from public.account_tag
                where account_id = current_account_id
                    and tag_id = item_primary_tag_id
            ) then
            raise exception 'Primary tag is not linked to the authenticated account.'
                using errcode = '42501';
        end if;
    end loop;

    for item in
        select value
        from jsonb_array_elements(items)
    loop
        item_id := (item ->> 'id')::uuid;
        item_detail := item -> 'detail';
        item_primary_tag_id := (item ->> 'primaryTagId')::uuid;

        -- 대표 태그가 서버에 아예 없으면 영구 제거된 태그이므로, 업로드를 막지 않고 지정 해제로 반영한다.
        if item_primary_tag_id is not null and not exists (
            select 1
            from public.tag
            where id = item_primary_tag_id
        ) then
            item_primary_tag_id := null;
        end if;

        if exists (
            select 1
            from public.account_dday
            where account_id = current_account_id
                and dday_id = item_id
        ) then
            update public.dday
            set
                title = item_detail ->> 'title',
                description = item_detail ->> 'description',
                color = (item_detail ->> 'color')::bigint,
                base_date = (item_detail ->> 'baseDate')::date,
                counts_base_day = (item_detail ->> 'countsBaseDay')::boolean,
                repeats = item_detail -> 'repeats',
                end_date = (item_detail ->> 'endDate')::date,
                overflow_policy = item_detail ->> 'overflowPolicy',
                primary_tag_id = item_primary_tag_id,
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where id = item_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            if found then
                for linked_account_id in
                    select account_id
                    from public.account_dday
                    where dday_id = item_id
                    order by account_id
                loop
                    update public.account_dday
                    set usn = public.next_account_usn(linked_account_id)
                    where account_id = linked_account_id
                        and dday_id = item_id;
                end loop;

                affected_count := affected_count + 1;
            end if;
        else
            inserted_id := null;

            insert into public.dday (
                id,
                title,
                description,
                color,
                base_date,
                counts_base_day,
                repeats,
                end_date,
                overflow_policy,
                primary_tag_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_id,
                item_detail ->> 'title',
                item_detail ->> 'description',
                (item_detail ->> 'color')::bigint,
                (item_detail ->> 'baseDate')::date,
                (item_detail ->> 'countsBaseDay')::boolean,
                item_detail -> 'repeats',
                (item_detail ->> 'endDate')::date,
                item_detail ->> 'overflowPolicy',
                item_primary_tag_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            )
            on conflict (id) do nothing
            returning id into inserted_id;

            if inserted_id is null then
                raise exception 'D-day is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;

            insert into public.account_dday (account_id, dday_id, usn)
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

revoke all on function public.push_ddays_v2(jsonb) from public;
revoke all on function public.push_ddays_v2(jsonb) from anon;
grant execute on function public.push_ddays_v2(jsonb) to authenticated;

create or replace function public.pull_ddays(usn_cursor bigint)
    returns jsonb
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    result jsonb;
begin
    if current_account_id is null then
        raise exception 'Authentication is required.' using errcode = '42501';
    end if;

    select coalesce(jsonb_agg(page.item order by page.usn), '[]'::jsonb)
    into result
    from (
        select
            account_dday.usn as usn,
            jsonb_build_object(
                'dday', jsonb_build_object(
                    'id', dday.id,
                    'detail', jsonb_build_object(
                        'title', dday.title,
                        'description', dday.description,
                        'color', dday.color,
                        'baseDate', dday.base_date,
                        'countsBaseDay', dday.counts_base_day,
                        'repeats', dday.repeats,
                        'endDate', dday.end_date,
                        'overflowPolicy', dday.overflow_policy
                    ),
                    'primaryTagId', dday.primary_tag_id,
                    'isDeleted', dday.is_deleted,
                    'updatedAt', dday.updated_at,
                    'createdAt', dday.created_at
                ),
                'usn', account_dday.usn
            ) as item
        from public.account_dday
        inner join public.dday on dday.id = account_dday.dday_id
        where account_dday.account_id = current_account_id
            and account_dday.usn > usn_cursor
        order by account_dday.usn
        limit 100
    ) as page;

    return result;
end;
$$;

-- 영구 제거하는 태그를 대표 태그로 가진 디데이도 지우기 전에 해제하고 순번을 올린다.
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
    affected_dday_id_list uuid[];
    affected_dday_id uuid;
    linked_account_id uuid;
begin
    foreach target_table in array array[
        'memo_tag',
        'memo_place',
        'memo_web',
        'memo_contact',
        'tag_link',
        'web_tag',
        'place_tag',
        'contact_tag',
        'checklist_tag',
        'music_tag',
        'qr_tag',
        'dday_tag',
        'routine_tag',
        'memo',
        'place',
        'web',
        'contact',
        'music',
        'qr',
        'checklist_item',
        'checklist',
        'dday',
        'routine_record',
        'routine'
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

    -- 태그 행을 지우면 외래 키가 memo.primary_tag_id와 dday.primary_tag_id를 null로 바꾸지만 순번을 올리지 않아
    -- 기기가 해제를 내려받지 못한다. 순번을 올려 정상 변경으로 전달하기 위해 지우기 전에 직접 해제한다.
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

    select array_agg(id)
    into affected_dday_id_list
    from public.dday
    where primary_tag_id = any (purge_tag_id_list);

    if affected_dday_id_list is not null then
        update public.dday
        set primary_tag_id = null
        where id = any (affected_dday_id_list);

        for affected_dday_id, linked_account_id in
            select account_dday.dday_id, account_dday.account_id
            from public.account_dday
            where account_dday.dday_id = any (affected_dday_id_list)
            order by account_dday.account_id, account_dday.dday_id
        loop
            update public.account_dday
            set usn = public.next_account_usn(linked_account_id)
            where account_id = linked_account_id
                and dday_id = affected_dday_id;
        end loop;
    end if;

    delete from public.tag
    where id = any (purge_tag_id_list);

    get diagnostics deleted_count = row_count;
    purged_count := purged_count + deleted_count;

    return purged_count;
end;
$$;
