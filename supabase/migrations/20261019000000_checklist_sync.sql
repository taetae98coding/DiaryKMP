-- 체크리스트는 주제의 제목과 설명을 가진다. 제목이 비어 있을 수 없는지는 앱이 판정하므로 서버 스키마에서는 형식만 고정한다.
create table public.checklist (
    id          uuid        primary key,
    title       text        not null,
    description text        not null,
    is_deleted  boolean     not null,
    updated_at  timestamptz not null,
    created_at  timestamptz not null
);

create table public.account_checklist (
    account_id   uuid   not null references public.account (id) on delete cascade,
    checklist_id uuid   not null references public.checklist (id) on delete cascade,
    usn          bigint not null,
    primary key (account_id, checklist_id)
);

create index account_checklist_checklist_id_idx on public.account_checklist (checklist_id);
create index account_checklist_account_id_usn_idx on public.account_checklist (account_id, usn);

-- 체크 항목은 체크리스트 하나에 속하고, 여러 기기의 서로 다른 항목 변경이 함께 남도록 항목마다 따로 주고받는다.
-- 속한 체크리스트는 처음 만들 때 정해지고 바뀌지 않으며, 체크리스트가 영구히 제거되면 항목도 함께 제거된다.
create table public.checklist_item (
    id           uuid        primary key,
    checklist_id uuid        not null references public.checklist (id) on delete cascade,
    content      text        not null,
    is_checked   boolean     not null,
    is_deleted   boolean     not null,
    updated_at   timestamptz not null,
    created_at   timestamptz not null
);

create index checklist_item_checklist_id_idx on public.checklist_item (checklist_id);

create table public.account_checklist_item (
    account_id        uuid   not null references public.account (id) on delete cascade,
    checklist_item_id uuid   not null references public.checklist_item (id) on delete cascade,
    usn               bigint not null,
    primary key (account_id, checklist_item_id)
);

create index account_checklist_item_checklist_item_id_idx on public.account_checklist_item (checklist_item_id);
create index account_checklist_item_account_id_usn_idx on public.account_checklist_item (account_id, usn);

alter table public.checklist enable row level security;
alter table public.account_checklist enable row level security;
alter table public.checklist_item enable row level security;
alter table public.account_checklist_item enable row level security;

grant select on table public.checklist to authenticated;
grant select on table public.account_checklist to authenticated;
grant select on table public.checklist_item to authenticated;
grant select on table public.account_checklist_item to authenticated;

create policy account_checklist_select_own
    on public.account_checklist
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy checklist_select_linked
    on public.checklist
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_checklist
            where account_checklist.checklist_id = checklist.id
                and account_checklist.account_id = (select auth.uid())
        )
    );

create policy account_checklist_item_select_own
    on public.account_checklist_item
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy checklist_item_select_linked
    on public.checklist_item
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_checklist_item
            where account_checklist_item.checklist_item_id = checklist_item.id
                and account_checklist_item.account_id = (select auth.uid())
        )
    );

create or replace function public.push_checklists(items jsonb)
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

        if exists (
            select 1
            from public.account_checklist
            where account_id = current_account_id
                and checklist_id = item_id
        ) then
            update public.checklist
            set
                title = item -> 'detail' ->> 'title',
                description = item -> 'detail' ->> 'description',
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where id = item_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            if found then
                for linked_account_id in
                    select account_id
                    from public.account_checklist
                    where checklist_id = item_id
                    order by account_id
                loop
                    update public.account_checklist
                    set usn = public.next_account_usn(linked_account_id)
                    where account_id = linked_account_id
                        and checklist_id = item_id;
                end loop;

                affected_count := affected_count + 1;
            end if;
        else
            inserted_id := null;

            insert into public.checklist (
                id,
                title,
                description,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_id,
                item -> 'detail' ->> 'title',
                item -> 'detail' ->> 'description',
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            )
            on conflict (id) do nothing
            returning id into inserted_id;

            if inserted_id is null then
                raise exception 'Checklist is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;

            insert into public.account_checklist (account_id, checklist_id, usn)
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

create or replace function public.pull_checklists(usn_cursor bigint)
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
            account_checklist.usn as usn,
            jsonb_build_object(
                'checklist', jsonb_build_object(
                    'id', checklist.id,
                    'detail', jsonb_build_object(
                        'title', checklist.title,
                        'description', checklist.description
                    ),
                    'isDeleted', checklist.is_deleted,
                    'updatedAt', checklist.updated_at,
                    'createdAt', checklist.created_at
                ),
                'usn', account_checklist.usn
            ) as item
        from public.account_checklist
        inner join public.checklist on checklist.id = account_checklist.checklist_id
        where account_checklist.account_id = current_account_id
            and account_checklist.usn > usn_cursor
        order by account_checklist.usn
        limit 100
    ) as page;

    return result;
end;
$$;

-- 체크 항목은 속한 체크리스트가 요청한 계정과 연결되어 있을 때만 반영한다. 연결의 두 항목을 확인하는 것과 같은 이유로,
-- 다른 계정의 체크리스트에 항목을 끼워 넣지 못하게 한다.
create or replace function public.push_checklist_items(items jsonb)
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
    item_checklist_id uuid;
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
        item_checklist_id := (item ->> 'checklistId')::uuid;

        if not exists (
            select 1
            from public.account_checklist
            where account_id = current_account_id
                and checklist_id = item_checklist_id
        ) then
            raise exception 'Checklist is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        if exists (
            select 1
            from public.account_checklist_item
            where account_id = current_account_id
                and checklist_item_id = item_id
        ) then
            -- 속한 체크리스트는 처음 만들 때 정해지므로 바꾸지 않는다.
            update public.checklist_item
            set
                content = item ->> 'content',
                is_checked = (item ->> 'isChecked')::boolean,
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where id = item_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            if found then
                for linked_account_id in
                    select account_id
                    from public.account_checklist_item
                    where checklist_item_id = item_id
                    order by account_id
                loop
                    update public.account_checklist_item
                    set usn = public.next_account_usn(linked_account_id)
                    where account_id = linked_account_id
                        and checklist_item_id = item_id;
                end loop;

                affected_count := affected_count + 1;
            end if;
        else
            inserted_id := null;

            insert into public.checklist_item (
                id,
                checklist_id,
                content,
                is_checked,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_id,
                item_checklist_id,
                item ->> 'content',
                (item ->> 'isChecked')::boolean,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            )
            on conflict (id) do nothing
            returning id into inserted_id;

            if inserted_id is null then
                raise exception 'Checklist item is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;

            insert into public.account_checklist_item (account_id, checklist_item_id, usn)
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

create or replace function public.pull_checklist_items(usn_cursor bigint)
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
            account_checklist_item.usn as usn,
            jsonb_build_object(
                'checklistItem', jsonb_build_object(
                    'id', checklist_item.id,
                    'checklistId', checklist_item.checklist_id,
                    'content', checklist_item.content,
                    'isChecked', checklist_item.is_checked,
                    'isDeleted', checklist_item.is_deleted,
                    'updatedAt', checklist_item.updated_at,
                    'createdAt', checklist_item.created_at
                ),
                'usn', account_checklist_item.usn
            ) as item
        from public.account_checklist_item
        inner join public.checklist_item on checklist_item.id = account_checklist_item.checklist_item_id
        where account_checklist_item.account_id = current_account_id
            and account_checklist_item.usn > usn_cursor
        order by account_checklist_item.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_checklists(jsonb) from public;
revoke all on function public.push_checklists(jsonb) from anon;
grant execute on function public.push_checklists(jsonb) to authenticated;

revoke all on function public.pull_checklists(bigint) from public;
revoke all on function public.pull_checklists(bigint) from anon;
grant execute on function public.pull_checklists(bigint) to authenticated;

revoke all on function public.push_checklist_items(jsonb) from public;
revoke all on function public.push_checklist_items(jsonb) from anon;
grant execute on function public.push_checklist_items(jsonb) to authenticated;

revoke all on function public.pull_checklist_items(bigint) from public;
revoke all on function public.pull_checklist_items(bigint) from anon;
grant execute on function public.pull_checklist_items(bigint) to authenticated;

-- 체크 항목과 체크리스트도 보존 기간이 지나면 함께 제거되도록 영구 제거 대상에 더한다.
-- 체크 항목을 먼저 지워 체크리스트 삭제가 끌고 가는 범위를 줄인다.
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
        'memo',
        'place',
        'web',
        'contact',
        'music',
        'qr',
        'checklist_item',
        'checklist'
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
