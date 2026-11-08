-- 디데이는 기준일과 반복 간격으로 기념일을 세는 항목이고, 다른 종류를 참조하지 않는 독립된 항목으로 주고받는다.
-- 반복 간격은 간격마다 days나 months 키 하나만 가진 객체의 배열로 디데이 행에 함께 담고, 받은 목록으로 통째로 바꾼다.
create table public.dday (
    id              uuid        primary key,
    title           text        not null,
    description     text        not null,
    color           bigint      not null,
    base_date       date        not null,
    counts_base_day boolean     not null,
    repeats         jsonb       not null,
    end_date        date,
    overflow_policy text        not null,
    is_deleted      boolean     not null,
    updated_at      timestamptz not null,
    created_at      timestamptz not null,
    constraint dday_repeats_array_check check (jsonb_typeof(repeats) = 'array'),
    constraint dday_end_date_check check (end_date is null or end_date >= base_date),
    constraint dday_overflow_policy_check check (overflow_policy in ('clamp_to_month_end', 'skip', 'next_month_first_day'))
);

create table public.account_dday (
    account_id uuid   not null references public.account (id) on delete cascade,
    dday_id    uuid   not null references public.dday (id) on delete cascade,
    usn        bigint not null,
    primary key (account_id, dday_id)
);

create index account_dday_dday_id_idx on public.account_dday (dday_id);
create index account_dday_account_id_usn_idx on public.account_dday (account_id, usn);

alter table public.dday enable row level security;
alter table public.account_dday enable row level security;

grant select on table public.dday to authenticated;
grant select on table public.account_dday to authenticated;

create policy account_dday_select_own
    on public.account_dday
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy dday_select_linked
    on public.dday
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_dday
            where account_dday.dday_id = dday.id
                and account_dday.account_id = (select auth.uid())
        )
    );

create or replace function public.push_ddays(items jsonb)
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
    end loop;

    for item in
        select value
        from jsonb_array_elements(items)
    loop
        item_id := (item ->> 'id')::uuid;
        item_detail := item -> 'detail';

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

revoke all on function public.push_ddays(jsonb) from public;
revoke all on function public.push_ddays(jsonb) from anon;
grant execute on function public.push_ddays(jsonb) to authenticated;

revoke all on function public.pull_ddays(bigint) from public;
revoke all on function public.pull_ddays(bigint) from anon;
grant execute on function public.pull_ddays(bigint) to authenticated;

-- 디데이와 태그의 연결은 다른 항목과 태그의 연결과 같은 구조로 둔다.
create table public.dday_tag (
    dday_id    uuid        not null references public.dday (id) on delete cascade,
    tag_id     uuid        not null references public.tag (id) on delete cascade,
    is_deleted boolean     not null,
    updated_at timestamptz not null,
    created_at timestamptz not null,
    primary key (dday_id, tag_id)
);

create index dday_tag_tag_id_idx on public.dday_tag (tag_id);

create table public.account_dday_tag (
    account_id uuid        not null references public.account (id) on delete cascade,
    dday_id    uuid        not null,
    tag_id     uuid        not null,
    usn        bigint      not null,
    primary key (account_id, dday_id, tag_id),
    foreign key (dday_id, tag_id) references public.dday_tag (dday_id, tag_id) on delete cascade
);

create index account_dday_tag_dday_id_tag_id_idx on public.account_dday_tag (dday_id, tag_id);
create index account_dday_tag_account_id_usn_idx on public.account_dday_tag (account_id, usn);

alter table public.dday_tag enable row level security;
alter table public.account_dday_tag enable row level security;

grant select on table public.dday_tag to authenticated;
grant select on table public.account_dday_tag to authenticated;

create policy account_dday_tag_select_own
    on public.account_dday_tag
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy dday_tag_select_linked
    on public.dday_tag
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_dday_tag
            where account_dday_tag.dday_id = dday_tag.dday_id
                and account_dday_tag.tag_id = dday_tag.tag_id
                and account_dday_tag.account_id = (select auth.uid())
        )
    );

create or replace function public.push_dday_tags(items jsonb)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    affected_count integer := 0;
    item jsonb;
    item_dday_id uuid;
    item_tag_id uuid;
    is_account_linked boolean;
    is_relation_changed boolean;
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
        item_dday_id := (item ->> 'ddayId')::uuid;
        item_tag_id := (item ->> 'tagId')::uuid;

        if not exists (
            select 1
            from public.account_dday
            where account_id = current_account_id
                and dday_id = item_dday_id
        ) or not exists (
            select 1
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_tag_id
        ) then
            raise exception 'D-day tag is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        is_account_linked := exists (
            select 1
            from public.account_dday_tag
            where account_id = current_account_id
                and dday_id = item_dday_id
                and tag_id = item_tag_id
        );

        if exists (
            select 1
            from public.dday_tag
            where dday_id = item_dday_id
                and tag_id = item_tag_id
        ) then
            update public.dday_tag
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where dday_id = item_dday_id
                and tag_id = item_tag_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            is_relation_changed := found;
        else
            insert into public.dday_tag (
                dday_id,
                tag_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_dday_id,
                item_tag_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            );

            is_relation_changed := true;
        end if;

        if not is_account_linked then
            insert into public.account_dday_tag (account_id, dday_id, tag_id, usn)
            values (
                current_account_id,
                item_dday_id,
                item_tag_id,
                public.next_account_usn(current_account_id)
            );
        end if;

        if is_relation_changed then
            for linked_account_id in
                select account_id
                from public.account_dday_tag
                where dday_id = item_dday_id
                    and tag_id = item_tag_id
                order by account_id
            loop
                update public.account_dday_tag
                set usn = public.next_account_usn(linked_account_id)
                where account_id = linked_account_id
                    and dday_id = item_dday_id
                    and tag_id = item_tag_id;
            end loop;
        end if;

        if is_relation_changed or not is_account_linked then
            affected_count := affected_count + 1;
        end if;
    end loop;

    return affected_count;
end;
$$;

create or replace function public.pull_dday_tags(usn_cursor bigint)
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
            account_dday_tag.usn as usn,
            jsonb_build_object(
                'ddayTag', jsonb_build_object(
                    'ddayId', dday_tag.dday_id,
                    'tagId', dday_tag.tag_id,
                    'isDeleted', dday_tag.is_deleted,
                    'updatedAt', dday_tag.updated_at,
                    'createdAt', dday_tag.created_at
                ),
                'usn', account_dday_tag.usn
            ) as item
        from public.account_dday_tag
        inner join public.dday_tag
            on dday_tag.dday_id = account_dday_tag.dday_id
                and dday_tag.tag_id = account_dday_tag.tag_id
        where account_dday_tag.account_id = current_account_id
            and account_dday_tag.usn > usn_cursor
        order by account_dday_tag.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_dday_tags(jsonb) from public;
revoke all on function public.push_dday_tags(jsonb) from anon;
grant execute on function public.push_dday_tags(jsonb) to authenticated;

revoke all on function public.pull_dday_tags(bigint) from public;
revoke all on function public.pull_dday_tags(bigint) from anon;
grant execute on function public.pull_dday_tags(bigint) to authenticated;

-- 지운 지 오래된 디데이와 연결을 정리하는 대상과 탈퇴 시 지우는 대상에 더한다.
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
        'memo_contact',
        'tag_link',
        'web_tag',
        'place_tag',
        'contact_tag',
        'checklist_tag',
        'music_tag',
        'qr_tag',
        'dday_tag',
        'memo',
        'place',
        'web',
        'contact',
        'music',
        'qr',
        'checklist_item',
        'checklist',
        'dday'
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

create or replace function public.delete_account(account_id uuid)
    returns void
    language plpgsql
    security definer
    set search_path = ''
as $$
begin
    delete from public.memo_tag as target
    using public.account_memo_tag as owned
    where owned.account_id = delete_account.account_id
      and target.memo_id = owned.memo_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_memo_tag as other
          where other.memo_id = target.memo_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.memo_place as target
    using public.account_memo_place as owned
    where owned.account_id = delete_account.account_id
      and target.memo_id = owned.memo_id and target.place_id = owned.place_id
      and not exists (
          select 1
          from public.account_memo_place as other
          where other.memo_id = target.memo_id and other.place_id = target.place_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.memo_web as target
    using public.account_memo_web as owned
    where owned.account_id = delete_account.account_id
      and target.memo_id = owned.memo_id and target.web_id = owned.web_id
      and not exists (
          select 1
          from public.account_memo_web as other
          where other.memo_id = target.memo_id and other.web_id = target.web_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.memo_contact as target
    using public.account_memo_contact as owned
    where owned.account_id = delete_account.account_id
      and target.memo_id = owned.memo_id and target.contact_id = owned.contact_id
      and not exists (
          select 1
          from public.account_memo_contact as other
          where other.memo_id = target.memo_id and other.contact_id = target.contact_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.tag_link as target
    using public.account_tag_link as owned
    where owned.account_id = delete_account.account_id
      and target.from_tag_id = owned.from_tag_id and target.to_tag_id = owned.to_tag_id
      and not exists (
          select 1
          from public.account_tag_link as other
          where other.from_tag_id = target.from_tag_id and other.to_tag_id = target.to_tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.web_tag as target
    using public.account_web_tag as owned
    where owned.account_id = delete_account.account_id
      and target.web_id = owned.web_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_web_tag as other
          where other.web_id = target.web_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.place_tag as target
    using public.account_place_tag as owned
    where owned.account_id = delete_account.account_id
      and target.place_id = owned.place_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_place_tag as other
          where other.place_id = target.place_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.contact_tag as target
    using public.account_contact_tag as owned
    where owned.account_id = delete_account.account_id
      and target.contact_id = owned.contact_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_contact_tag as other
          where other.contact_id = target.contact_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.checklist_tag as target
    using public.account_checklist_tag as owned
    where owned.account_id = delete_account.account_id
      and target.checklist_id = owned.checklist_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_checklist_tag as other
          where other.checklist_id = target.checklist_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.music_tag as target
    using public.account_music_tag as owned
    where owned.account_id = delete_account.account_id
      and target.music_id = owned.music_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_music_tag as other
          where other.music_id = target.music_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.qr_tag as target
    using public.account_qr_tag as owned
    where owned.account_id = delete_account.account_id
      and target.qr_id = owned.qr_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_qr_tag as other
          where other.qr_id = target.qr_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.dday_tag as target
    using public.account_dday_tag as owned
    where owned.account_id = delete_account.account_id
      and target.dday_id = owned.dday_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_dday_tag as other
          where other.dday_id = target.dday_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.checklist_item as target
    using public.account_checklist_item as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.checklist_item_id
      and not exists (
          select 1
          from public.account_checklist_item as other
          where other.checklist_item_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.checklist as target
    using public.account_checklist as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.checklist_id
      and not exists (
          select 1
          from public.account_checklist as other
          where other.checklist_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.memo as target
    using public.account_memo as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.memo_id
      and not exists (
          select 1
          from public.account_memo as other
          where other.memo_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.tag as target
    using public.account_tag as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.tag_id
      and not exists (
          select 1
          from public.account_tag as other
          where other.tag_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.place as target
    using public.account_place as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.place_id
      and not exists (
          select 1
          from public.account_place as other
          where other.place_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.web as target
    using public.account_web as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.web_id
      and not exists (
          select 1
          from public.account_web as other
          where other.web_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.contact as target
    using public.account_contact as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.contact_id
      and not exists (
          select 1
          from public.account_contact as other
          where other.contact_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.music as target
    using public.account_music as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.music_id
      and not exists (
          select 1
          from public.account_music as other
          where other.music_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.qr as target
    using public.account_qr as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.qr_id
      and not exists (
          select 1
          from public.account_qr as other
          where other.qr_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.dday as target
    using public.account_dday as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.dday_id
      and not exists (
          select 1
          from public.account_dday as other
          where other.dday_id = target.id
            and other.account_id <> delete_account.account_id
      );

    -- 로그인 정보, 연결된 로그인 수단과 세션을 지우면 account와 그에 매달린 행이 cascade로 지워진다.
    delete from auth.users as target
    where target.id = delete_account.account_id;
end;
$$;

revoke execute on function public.delete_account(uuid) from public, anon, authenticated;
grant execute on function public.delete_account(uuid) to service_role;
