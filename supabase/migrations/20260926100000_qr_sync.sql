-- QR은 제목, 설명과 QR 그림에 담기는 값을 가진다.
-- 제목과 값이 비어 있을 수 없는지는 앱이 판정하므로 서버 스키마에서는 형식만 고정한다.
-- 값의 줄바꿈과 앞뒤 공백도 값의 일부이므로 받은 그대로 저장한다.
create table public.qr (
    id          uuid        primary key,
    title       text        not null,
    description text        not null,
    value       text        not null,
    is_deleted  boolean     not null,
    updated_at  timestamptz not null,
    created_at  timestamptz not null
);

create table public.account_qr (
    account_id uuid   not null references public.account (id) on delete cascade,
    qr_id      uuid   not null references public.qr (id) on delete cascade,
    usn        bigint not null,
    primary key (account_id, qr_id)
);

create index account_qr_qr_id_idx on public.account_qr (qr_id);
create index account_qr_account_id_usn_idx on public.account_qr (account_id, usn);

alter table public.qr enable row level security;
alter table public.account_qr enable row level security;

grant select on table public.qr to authenticated;
grant select on table public.account_qr to authenticated;

create policy account_qr_select_own
    on public.account_qr
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy qr_select_linked
    on public.qr
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_qr
            where account_qr.qr_id = qr.id
                and account_qr.account_id = (select auth.uid())
        )
    );

create or replace function public.push_qrs(items jsonb)
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
            from public.account_qr
            where account_id = current_account_id
                and qr_id = item_id
        ) then
            update public.qr
            set
                title = item -> 'detail' ->> 'title',
                description = item -> 'detail' ->> 'description',
                value = item -> 'detail' ->> 'value',
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where id = item_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            if found then
                for linked_account_id in
                    select account_id
                    from public.account_qr
                    where qr_id = item_id
                    order by account_id
                loop
                    update public.account_qr
                    set usn = public.next_account_usn(linked_account_id)
                    where account_id = linked_account_id
                        and qr_id = item_id;
                end loop;

                affected_count := affected_count + 1;
            end if;
        else
            inserted_id := null;

            insert into public.qr (
                id,
                title,
                description,
                value,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_id,
                item -> 'detail' ->> 'title',
                item -> 'detail' ->> 'description',
                item -> 'detail' ->> 'value',
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            )
            on conflict (id) do nothing
            returning id into inserted_id;

            if inserted_id is null then
                raise exception 'QR is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;

            insert into public.account_qr (account_id, qr_id, usn)
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

create or replace function public.pull_qrs(usn_cursor bigint)
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
            account_qr.usn as usn,
            jsonb_build_object(
                'qr', jsonb_build_object(
                    'id', qr.id,
                    'detail', jsonb_build_object(
                        'title', qr.title,
                        'description', qr.description,
                        'value', qr.value
                    ),
                    'isDeleted', qr.is_deleted,
                    'updatedAt', qr.updated_at,
                    'createdAt', qr.created_at
                ),
                'usn', account_qr.usn
            ) as item
        from public.account_qr
        inner join public.qr on qr.id = account_qr.qr_id
        where account_qr.account_id = current_account_id
            and account_qr.usn > usn_cursor
        order by account_qr.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_qrs(jsonb) from public;
revoke all on function public.push_qrs(jsonb) from anon;
grant execute on function public.push_qrs(jsonb) to authenticated;

revoke all on function public.pull_qrs(bigint) from public;
revoke all on function public.pull_qrs(bigint) from anon;
grant execute on function public.pull_qrs(bigint) to authenticated;

-- QR도 보존 기간이 지나면 함께 제거되도록 영구 제거 대상에 더한다.
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
        'memo_contact',
        'tag_link',
        'web_tag',
        'place_tag',
        'memo',
        'place',
        'web',
        'contact',
        'music',
        'qr'
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
