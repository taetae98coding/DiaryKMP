-- 루틴은 되풀이해서 하려는 일 하나를 정해 두고 한 만큼을 날짜별 기록으로 쌓는 항목이고, 다른 종류를 참조하지 않는 독립된 항목으로 주고받는다.
-- 주기는 iCalendar(RFC 5545) 반복 규칙 문자열 세 값만, 단위는 세 값만 받는다.
create table public.routine (
    id                uuid        primary key,
    title             text        not null,
    description       text        not null,
    color             bigint      not null,
    start_date        date        not null,
    cycle             text        not null,
    unit              text        not null,
    goal_amount       integer,
    amount_per_record integer     not null,
    is_finished       boolean     not null,
    is_deleted        boolean     not null,
    updated_at        timestamptz not null,
    created_at        timestamptz not null,
    constraint routine_cycle_check check (cycle in ('FREQ=DAILY', 'FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR', 'FREQ=DAILY;BYDAY=SA,SU', 'FREQ=WEEKLY;WKST=SU', 'FREQ=MONTHLY')),
    constraint routine_unit_check check (char_length(unit) <= 10),
    constraint routine_goal_amount_check check (goal_amount is null or goal_amount between 1 and 9999999),
    constraint routine_amount_per_record_check check (amount_per_record between 1 and 9999999)
);

create table public.account_routine (
    account_id uuid   not null references public.account (id) on delete cascade,
    routine_id uuid   not null references public.routine (id) on delete cascade,
    usn        bigint not null,
    primary key (account_id, routine_id)
);

create index account_routine_routine_id_idx on public.account_routine (routine_id);
create index account_routine_account_id_usn_idx on public.account_routine (account_id, usn);

-- 루틴 기록은 루틴 하나에 속하고, 여러 기기에서 넣은 서로 다른 기록이 함께 남도록 기록마다 따로 주고받는다.
-- 속한 루틴은 처음 만들 때 정해지고 바뀌지 않으며, 루틴이 영구히 제거되면 기록도 함께 제거된다.
create table public.routine_record (
    id         uuid        primary key,
    routine_id uuid        not null references public.routine (id) on delete cascade,
    date       date        not null,
    amount     integer     not null,
    is_deleted boolean     not null,
    updated_at timestamptz not null,
    created_at timestamptz not null,
    constraint routine_record_amount_check check (amount between 1 and 9999999)
);

create index routine_record_routine_id_idx on public.routine_record (routine_id);

create table public.account_routine_record (
    account_id        uuid   not null references public.account (id) on delete cascade,
    routine_record_id uuid   not null references public.routine_record (id) on delete cascade,
    usn               bigint not null,
    primary key (account_id, routine_record_id)
);

create index account_routine_record_routine_record_id_idx on public.account_routine_record (routine_record_id);
create index account_routine_record_account_id_usn_idx on public.account_routine_record (account_id, usn);

alter table public.routine enable row level security;
alter table public.account_routine enable row level security;
alter table public.routine_record enable row level security;
alter table public.account_routine_record enable row level security;

grant select on table public.routine to authenticated;
grant select on table public.account_routine to authenticated;
grant select on table public.routine_record to authenticated;
grant select on table public.account_routine_record to authenticated;

create policy account_routine_select_own
    on public.account_routine
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy routine_select_linked
    on public.routine
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_routine
            where account_routine.routine_id = routine.id
                and account_routine.account_id = (select auth.uid())
        )
    );

create policy account_routine_record_select_own
    on public.account_routine_record
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy routine_record_select_linked
    on public.routine_record
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_routine_record
            where account_routine_record.routine_record_id = routine_record.id
                and account_routine_record.account_id = (select auth.uid())
        )
    );

create or replace function public.push_routines(items jsonb)
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

        if (item_detail ->> 'cycle') is null
            or (item_detail ->> 'cycle') not in ('FREQ=DAILY', 'FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR', 'FREQ=DAILY;BYDAY=SA,SU', 'FREQ=WEEKLY;WKST=SU', 'FREQ=MONTHLY') then
            raise exception 'cycle must be one of FREQ=DAILY, FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR, FREQ=DAILY;BYDAY=SA,SU, FREQ=WEEKLY;WKST=SU or FREQ=MONTHLY.' using errcode = '22023';
        end if;

        if (item_detail ->> 'unit') is null
            or char_length(item_detail ->> 'unit') > 10 then
            raise exception 'unit must be at most 10 characters.' using errcode = '22023';
        end if;

        -- CASE로 순서를 고정해 숫자가 아닌 값을 숫자로 바꾸지 않는다.
        if (case
            when item_detail -> 'goalAmount' is null then false
            when jsonb_typeof(item_detail -> 'goalAmount') = 'null' then false
            when jsonb_typeof(item_detail -> 'goalAmount') <> 'number' then true
            else (item_detail ->> 'goalAmount')::numeric not between 1 and 9999999
                or (item_detail ->> 'goalAmount')::numeric <> trunc((item_detail ->> 'goalAmount')::numeric)
        end) then
            raise exception 'goalAmount must be null or an integer between 1 and 9999999.' using errcode = '22023';
        end if;

        if (case
            when jsonb_typeof(item_detail -> 'amountPerRecord') is distinct from 'number' then true
            else (item_detail ->> 'amountPerRecord')::numeric not between 1 and 9999999
                or (item_detail ->> 'amountPerRecord')::numeric <> trunc((item_detail ->> 'amountPerRecord')::numeric)
        end) then
            raise exception 'amountPerRecord must be an integer between 1 and 9999999.' using errcode = '22023';
        end if;

        if jsonb_typeof(item -> 'isFinished') is distinct from 'boolean' then
            raise exception 'isFinished must be a boolean.' using errcode = '22023';
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
            from public.account_routine
            where account_id = current_account_id
                and routine_id = item_id
        ) then
            update public.routine
            set
                title = item_detail ->> 'title',
                description = item_detail ->> 'description',
                color = (item_detail ->> 'color')::bigint,
                start_date = (item_detail ->> 'startDate')::date,
                cycle = item_detail ->> 'cycle',
                unit = item_detail ->> 'unit',
                goal_amount = (item_detail ->> 'goalAmount')::integer,
                amount_per_record = (item_detail ->> 'amountPerRecord')::integer,
                is_finished = (item ->> 'isFinished')::boolean,
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where id = item_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            if found then
                for linked_account_id in
                    select account_id
                    from public.account_routine
                    where routine_id = item_id
                    order by account_id
                loop
                    update public.account_routine
                    set usn = public.next_account_usn(linked_account_id)
                    where account_id = linked_account_id
                        and routine_id = item_id;
                end loop;

                affected_count := affected_count + 1;
            end if;
        else
            inserted_id := null;

            insert into public.routine (
                id,
                title,
                description,
                color,
                start_date,
                cycle,
                unit,
                goal_amount,
                amount_per_record,
                is_finished,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_id,
                item_detail ->> 'title',
                item_detail ->> 'description',
                (item_detail ->> 'color')::bigint,
                (item_detail ->> 'startDate')::date,
                item_detail ->> 'cycle',
                item_detail ->> 'unit',
                (item_detail ->> 'goalAmount')::integer,
                (item_detail ->> 'amountPerRecord')::integer,
                (item ->> 'isFinished')::boolean,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            )
            on conflict (id) do nothing
            returning id into inserted_id;

            if inserted_id is null then
                raise exception 'Routine is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;

            insert into public.account_routine (account_id, routine_id, usn)
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

create or replace function public.pull_routines(usn_cursor bigint)
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
            account_routine.usn as usn,
            jsonb_build_object(
                'routine', jsonb_build_object(
                    'id', routine.id,
                    'detail', jsonb_build_object(
                        'title', routine.title,
                        'description', routine.description,
                        'color', routine.color,
                        'startDate', routine.start_date,
                        'cycle', routine.cycle,
                        'unit', routine.unit,
                        'goalAmount', routine.goal_amount,
                        'amountPerRecord', routine.amount_per_record
                    ),
                    'isFinished', routine.is_finished,
                    'isDeleted', routine.is_deleted,
                    'updatedAt', routine.updated_at,
                    'createdAt', routine.created_at
                ),
                'usn', account_routine.usn
            ) as item
        from public.account_routine
        inner join public.routine on routine.id = account_routine.routine_id
        where account_routine.account_id = current_account_id
            and account_routine.usn > usn_cursor
        order by account_routine.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_routines(jsonb) from public;
revoke all on function public.push_routines(jsonb) from anon;
grant execute on function public.push_routines(jsonb) to authenticated;

revoke all on function public.pull_routines(bigint) from public;
revoke all on function public.pull_routines(bigint) from anon;
grant execute on function public.pull_routines(bigint) to authenticated;

-- 루틴 기록은 속한 루틴이 요청한 계정과 연결되어 있을 때만 반영한다. 연결의 두 항목을 확인하는 것과 같은 이유로,
-- 다른 계정의 루틴에 기록을 끼워 넣지 못하게 한다. 속한 루틴이 삭제되었거나 완료된 상태인지와 기록의 날짜가 루틴의 시작 날짜 이후인지는 확인하지 않는다.
create or replace function public.push_routine_records(items jsonb)
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
    item_routine_id uuid;
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
        -- CASE로 순서를 고정해 숫자가 아닌 값을 숫자로 바꾸지 않는다.
        if (case
            when jsonb_typeof(item -> 'amount') is distinct from 'number' then true
            else (item ->> 'amount')::numeric not between 1 and 9999999
                or (item ->> 'amount')::numeric <> trunc((item ->> 'amount')::numeric)
        end) then
            raise exception 'amount must be an integer between 1 and 9999999.' using errcode = '22023';
        end if;
    end loop;

    for item in
        select value
        from jsonb_array_elements(items)
    loop
        item_id := (item ->> 'id')::uuid;
        item_routine_id := (item ->> 'routineId')::uuid;

        if not exists (
            select 1
            from public.account_routine
            where account_id = current_account_id
                and routine_id = item_routine_id
        ) then
            raise exception 'Routine is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        if exists (
            select 1
            from public.account_routine_record
            where account_id = current_account_id
                and routine_record_id = item_id
        ) then
            -- 속한 루틴, 날짜와 양은 처음 만들 때 정해지므로 바꾸지 않는다.
            update public.routine_record
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where id = item_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            if found then
                for linked_account_id in
                    select account_id
                    from public.account_routine_record
                    where routine_record_id = item_id
                    order by account_id
                loop
                    update public.account_routine_record
                    set usn = public.next_account_usn(linked_account_id)
                    where account_id = linked_account_id
                        and routine_record_id = item_id;
                end loop;

                affected_count := affected_count + 1;
            end if;
        else
            inserted_id := null;

            insert into public.routine_record (
                id,
                routine_id,
                date,
                amount,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_id,
                item_routine_id,
                (item ->> 'date')::date,
                (item ->> 'amount')::integer,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            )
            on conflict (id) do nothing
            returning id into inserted_id;

            if inserted_id is null then
                raise exception 'Routine record is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;

            insert into public.account_routine_record (account_id, routine_record_id, usn)
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

create or replace function public.pull_routine_records(usn_cursor bigint)
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
            account_routine_record.usn as usn,
            jsonb_build_object(
                'routineRecord', jsonb_build_object(
                    'id', routine_record.id,
                    'routineId', routine_record.routine_id,
                    'date', routine_record.date,
                    'amount', routine_record.amount,
                    'isDeleted', routine_record.is_deleted,
                    'updatedAt', routine_record.updated_at,
                    'createdAt', routine_record.created_at
                ),
                'usn', account_routine_record.usn
            ) as item
        from public.account_routine_record
        inner join public.routine_record on routine_record.id = account_routine_record.routine_record_id
        where account_routine_record.account_id = current_account_id
            and account_routine_record.usn > usn_cursor
        order by account_routine_record.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_routine_records(jsonb) from public;
revoke all on function public.push_routine_records(jsonb) from anon;
grant execute on function public.push_routine_records(jsonb) to authenticated;

revoke all on function public.pull_routine_records(bigint) from public;
revoke all on function public.pull_routine_records(bigint) from anon;
grant execute on function public.pull_routine_records(bigint) to authenticated;

-- 루틴과 태그의 연결은 다른 항목과 태그의 연결과 같은 구조로 둔다.
create table public.routine_tag (
    routine_id uuid        not null references public.routine (id) on delete cascade,
    tag_id     uuid        not null references public.tag (id) on delete cascade,
    is_deleted boolean     not null,
    updated_at timestamptz not null,
    created_at timestamptz not null,
    primary key (routine_id, tag_id)
);

create index routine_tag_tag_id_idx on public.routine_tag (tag_id);

create table public.account_routine_tag (
    account_id uuid        not null references public.account (id) on delete cascade,
    routine_id uuid        not null,
    tag_id     uuid        not null,
    usn        bigint      not null,
    primary key (account_id, routine_id, tag_id),
    foreign key (routine_id, tag_id) references public.routine_tag (routine_id, tag_id) on delete cascade
);

create index account_routine_tag_routine_id_tag_id_idx on public.account_routine_tag (routine_id, tag_id);
create index account_routine_tag_account_id_usn_idx on public.account_routine_tag (account_id, usn);

alter table public.routine_tag enable row level security;
alter table public.account_routine_tag enable row level security;

grant select on table public.routine_tag to authenticated;
grant select on table public.account_routine_tag to authenticated;

create policy account_routine_tag_select_own
    on public.account_routine_tag
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy routine_tag_select_linked
    on public.routine_tag
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_routine_tag
            where account_routine_tag.routine_id = routine_tag.routine_id
                and account_routine_tag.tag_id = routine_tag.tag_id
                and account_routine_tag.account_id = (select auth.uid())
        )
    );

create or replace function public.push_routine_tags(items jsonb)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    affected_count integer := 0;
    item jsonb;
    item_routine_id uuid;
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
        item_routine_id := (item ->> 'routineId')::uuid;
        item_tag_id := (item ->> 'tagId')::uuid;

        if not exists (
            select 1
            from public.account_routine
            where account_id = current_account_id
                and routine_id = item_routine_id
        ) or not exists (
            select 1
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_tag_id
        ) then
            raise exception 'Routine tag is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        is_account_linked := exists (
            select 1
            from public.account_routine_tag
            where account_id = current_account_id
                and routine_id = item_routine_id
                and tag_id = item_tag_id
        );

        if exists (
            select 1
            from public.routine_tag
            where routine_id = item_routine_id
                and tag_id = item_tag_id
        ) then
            update public.routine_tag
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where routine_id = item_routine_id
                and tag_id = item_tag_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            is_relation_changed := found;
        else
            insert into public.routine_tag (
                routine_id,
                tag_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_routine_id,
                item_tag_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            );

            is_relation_changed := true;
        end if;

        if not is_account_linked then
            insert into public.account_routine_tag (account_id, routine_id, tag_id, usn)
            values (
                current_account_id,
                item_routine_id,
                item_tag_id,
                public.next_account_usn(current_account_id)
            );
        end if;

        if is_relation_changed then
            for linked_account_id in
                select account_id
                from public.account_routine_tag
                where routine_id = item_routine_id
                    and tag_id = item_tag_id
                order by account_id
            loop
                update public.account_routine_tag
                set usn = public.next_account_usn(linked_account_id)
                where account_id = linked_account_id
                    and routine_id = item_routine_id
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

create or replace function public.pull_routine_tags(usn_cursor bigint)
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
            account_routine_tag.usn as usn,
            jsonb_build_object(
                'routineTag', jsonb_build_object(
                    'routineId', routine_tag.routine_id,
                    'tagId', routine_tag.tag_id,
                    'isDeleted', routine_tag.is_deleted,
                    'updatedAt', routine_tag.updated_at,
                    'createdAt', routine_tag.created_at
                ),
                'usn', account_routine_tag.usn
            ) as item
        from public.account_routine_tag
        inner join public.routine_tag
            on routine_tag.routine_id = account_routine_tag.routine_id
                and routine_tag.tag_id = account_routine_tag.tag_id
        where account_routine_tag.account_id = current_account_id
            and account_routine_tag.usn > usn_cursor
        order by account_routine_tag.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_routine_tags(jsonb) from public;
revoke all on function public.push_routine_tags(jsonb) from anon;
grant execute on function public.push_routine_tags(jsonb) to authenticated;

revoke all on function public.pull_routine_tags(bigint) from public;
revoke all on function public.pull_routine_tags(bigint) from anon;
grant execute on function public.pull_routine_tags(bigint) to authenticated;

-- 지운 지 오래된 루틴과 루틴 기록, 연결을 정리하는 대상과 탈퇴 시 지우는 대상에 더한다.
-- 루틴 기록을 먼저 지워 루틴 삭제가 끌고 가는 범위를 줄인다.
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

    delete from public.routine_tag as target
    using public.account_routine_tag as owned
    where owned.account_id = delete_account.account_id
      and target.routine_id = owned.routine_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_routine_tag as other
          where other.routine_id = target.routine_id and other.tag_id = target.tag_id
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

    delete from public.routine_record as target
    using public.account_routine_record as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.routine_record_id
      and not exists (
          select 1
          from public.account_routine_record as other
          where other.routine_record_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.routine as target
    using public.account_routine as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.routine_id
      and not exists (
          select 1
          from public.account_routine as other
          where other.routine_id = target.id
            and other.account_id <> delete_account.account_id
      );

    -- 로그인 정보, 연결된 로그인 수단과 세션을 지우면 account와 그에 매달린 행이 cascade로 지워진다.
    delete from auth.users as target
    where target.id = delete_account.account_id;
end;
$$;

revoke execute on function public.delete_account(uuid) from public, anon, authenticated;
grant execute on function public.delete_account(uuid) to service_role;
