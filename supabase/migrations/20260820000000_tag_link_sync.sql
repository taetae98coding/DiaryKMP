-- 태그 연결은 출발 태그와 도착 태그를 함께 가리키는 단방향 관계이므로,
-- 메모와 태그의 연결과 같은 구조로 연결 테이블과 계정 연결 테이블을 나눠 둔다.
create table public.tag_link (
    from_tag_id uuid        not null references public.tag (id) on delete cascade,
    to_tag_id   uuid        not null references public.tag (id) on delete cascade,
    is_deleted  boolean     not null,
    updated_at  timestamptz not null,
    created_at  timestamptz not null,
    primary key (from_tag_id, to_tag_id),
    check (from_tag_id <> to_tag_id)
);

create index tag_link_to_tag_id_idx on public.tag_link (to_tag_id);

create table public.account_tag_link (
    account_id  uuid   not null references public.account (id) on delete cascade,
    from_tag_id uuid   not null,
    to_tag_id   uuid   not null,
    usn         bigint not null,
    primary key (account_id, from_tag_id, to_tag_id),
    foreign key (from_tag_id, to_tag_id) references public.tag_link (from_tag_id, to_tag_id) on delete cascade
);

create index account_tag_link_from_tag_id_to_tag_id_idx on public.account_tag_link (from_tag_id, to_tag_id);
create index account_tag_link_account_id_usn_idx on public.account_tag_link (account_id, usn);

alter table public.tag_link enable row level security;
alter table public.account_tag_link enable row level security;

grant select on table public.tag_link to authenticated;
grant select on table public.account_tag_link to authenticated;

create policy account_tag_link_select_own
    on public.account_tag_link
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy tag_link_select_linked
    on public.tag_link
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_tag_link
            where account_tag_link.from_tag_id = tag_link.from_tag_id
                and account_tag_link.to_tag_id = tag_link.to_tag_id
                and account_tag_link.account_id = (select auth.uid())
        )
    );

create or replace function public.push_tag_links(items jsonb)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    affected_count integer := 0;
    item jsonb;
    item_from_tag_id uuid;
    item_to_tag_id uuid;
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
        item_from_tag_id := (item ->> 'fromTagId')::uuid;
        item_to_tag_id := (item ->> 'toTagId')::uuid;

        if item_from_tag_id = item_to_tag_id then
            raise exception 'Tag link must point to another tag.' using errcode = '22023';
        end if;

        if not exists (
            select 1
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_from_tag_id
        ) or not exists (
            select 1
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_to_tag_id
        ) then
            raise exception 'Tag link is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        is_account_linked := exists (
            select 1
            from public.account_tag_link
            where account_id = current_account_id
                and from_tag_id = item_from_tag_id
                and to_tag_id = item_to_tag_id
        );

        if exists (
            select 1
            from public.tag_link
            where from_tag_id = item_from_tag_id
                and to_tag_id = item_to_tag_id
        ) then
            update public.tag_link
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where from_tag_id = item_from_tag_id
                and to_tag_id = item_to_tag_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            is_relation_changed := found;
        else
            insert into public.tag_link (
                from_tag_id,
                to_tag_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_from_tag_id,
                item_to_tag_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            );

            is_relation_changed := true;
        end if;

        if not is_account_linked then
            insert into public.account_tag_link (account_id, from_tag_id, to_tag_id, usn)
            values (
                current_account_id,
                item_from_tag_id,
                item_to_tag_id,
                public.next_account_usn(current_account_id)
            );
        end if;

        if is_relation_changed then
            for linked_account_id in
                select account_id
                from public.account_tag_link
                where from_tag_id = item_from_tag_id
                    and to_tag_id = item_to_tag_id
                order by account_id
            loop
                update public.account_tag_link
                set usn = public.next_account_usn(linked_account_id)
                where account_id = linked_account_id
                    and from_tag_id = item_from_tag_id
                    and to_tag_id = item_to_tag_id;
            end loop;
        end if;

        if is_relation_changed or not is_account_linked then
            affected_count := affected_count + 1;
        end if;
    end loop;

    return affected_count;
end;
$$;

create or replace function public.pull_tag_links(usn_cursor bigint)
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
            account_tag_link.usn as usn,
            jsonb_build_object(
                'tagLink', jsonb_build_object(
                    'fromTagId', tag_link.from_tag_id,
                    'toTagId', tag_link.to_tag_id,
                    'isDeleted', tag_link.is_deleted,
                    'updatedAt', tag_link.updated_at,
                    'createdAt', tag_link.created_at
                ),
                'usn', account_tag_link.usn
            ) as item
        from public.account_tag_link
        inner join public.tag_link
            on tag_link.from_tag_id = account_tag_link.from_tag_id
                and tag_link.to_tag_id = account_tag_link.to_tag_id
        where account_tag_link.account_id = current_account_id
            and account_tag_link.usn > usn_cursor
        order by account_tag_link.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_tag_links(jsonb) from public;
revoke all on function public.push_tag_links(jsonb) from anon;
grant execute on function public.push_tag_links(jsonb) to authenticated;

revoke all on function public.pull_tag_links(bigint) from public;
revoke all on function public.pull_tag_links(bigint) from anon;
grant execute on function public.pull_tag_links(bigint) to authenticated;
