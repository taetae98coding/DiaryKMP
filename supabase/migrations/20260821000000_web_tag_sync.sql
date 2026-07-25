-- 웹 항목과 태그의 연결은 두 항목을 함께 가리키는 관계이므로,
-- 메모와 태그의 연결과 같은 구조로 연결 테이블과 계정 연결 테이블을 나눠 둔다.
create table public.web_tag (
    web_id   uuid        not null references public.web (id) on delete cascade,
    tag_id     uuid        not null references public.tag (id) on delete cascade,
    is_deleted boolean     not null,
    updated_at timestamptz not null,
    created_at timestamptz not null,
    primary key (web_id, tag_id)
);

create index web_tag_tag_id_idx on public.web_tag (tag_id);

create table public.account_web_tag (
    account_id uuid   not null references public.account (id) on delete cascade,
    web_id   uuid   not null,
    tag_id     uuid   not null,
    usn        bigint not null,
    primary key (account_id, web_id, tag_id),
    foreign key (web_id, tag_id) references public.web_tag (web_id, tag_id) on delete cascade
);

create index account_web_tag_web_id_tag_id_idx on public.account_web_tag (web_id, tag_id);
create index account_web_tag_account_id_usn_idx on public.account_web_tag (account_id, usn);

alter table public.web_tag enable row level security;
alter table public.account_web_tag enable row level security;

grant select on table public.web_tag to authenticated;
grant select on table public.account_web_tag to authenticated;

create policy account_web_tag_select_own
    on public.account_web_tag
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy web_tag_select_linked
    on public.web_tag
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_web_tag
            where account_web_tag.web_id = web_tag.web_id
                and account_web_tag.tag_id = web_tag.tag_id
                and account_web_tag.account_id = (select auth.uid())
        )
    );

create or replace function public.push_web_tags(items jsonb)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    affected_count integer := 0;
    item jsonb;
    item_web_id uuid;
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
        item_web_id := (item ->> 'webId')::uuid;
        item_tag_id := (item ->> 'tagId')::uuid;

        if not exists (
            select 1
            from public.account_web
            where account_id = current_account_id
                and web_id = item_web_id
        ) or not exists (
            select 1
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_tag_id
        ) then
            raise exception '웹 항목 tag link is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        is_account_linked := exists (
            select 1
            from public.account_web_tag
            where account_id = current_account_id
                and web_id = item_web_id
                and tag_id = item_tag_id
        );

        if exists (
            select 1
            from public.web_tag
            where web_id = item_web_id
                and tag_id = item_tag_id
        ) then
            update public.web_tag
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where web_id = item_web_id
                and tag_id = item_tag_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            is_relation_changed := found;
        else
            insert into public.web_tag (
                web_id,
                tag_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_web_id,
                item_tag_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            );

            is_relation_changed := true;
        end if;

        if not is_account_linked then
            insert into public.account_web_tag (account_id, web_id, tag_id, usn)
            values (
                current_account_id,
                item_web_id,
                item_tag_id,
                public.next_account_usn(current_account_id)
            );
        end if;

        if is_relation_changed then
            for linked_account_id in
                select account_id
                from public.account_web_tag
                where web_id = item_web_id
                    and tag_id = item_tag_id
                order by account_id
            loop
                update public.account_web_tag
                set usn = public.next_account_usn(linked_account_id)
                where account_id = linked_account_id
                    and web_id = item_web_id
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

create or replace function public.pull_web_tags(usn_cursor bigint)
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
            account_web_tag.usn as usn,
            jsonb_build_object(
                'webTag', jsonb_build_object(
                    'webId', web_tag.web_id,
                    'tagId', web_tag.tag_id,
                    'isDeleted', web_tag.is_deleted,
                    'updatedAt', web_tag.updated_at,
                    'createdAt', web_tag.created_at
                ),
                'usn', account_web_tag.usn
            ) as item
        from public.account_web_tag
        inner join public.web_tag
            on web_tag.web_id = account_web_tag.web_id
                and web_tag.tag_id = account_web_tag.tag_id
        where account_web_tag.account_id = current_account_id
            and account_web_tag.usn > usn_cursor
        order by account_web_tag.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_web_tags(jsonb) from public;
revoke all on function public.push_web_tags(jsonb) from anon;
grant execute on function public.push_web_tags(jsonb) to authenticated;

revoke all on function public.pull_web_tags(bigint) from public;
revoke all on function public.pull_web_tags(bigint) from anon;
grant execute on function public.pull_web_tags(bigint) to authenticated;
