-- 요청 헤더는 순서 있는 목록이지만 헤더 단위로 조회하거나 변경하지 않으므로 웹 항목의 jsonb 컬럼 하나로 저장한다.
create table public.web (
    id          uuid        primary key,
    title       text        not null,
    description text        not null,
    url         text        not null,
    header_list jsonb       not null default '[]'::jsonb,
    is_deleted  boolean     not null,
    updated_at  timestamptz not null,
    created_at  timestamptz not null,
    check (jsonb_typeof(header_list) = 'array')
);

create table public.account_web (
    account_id uuid   not null references public.account (id) on delete cascade,
    web_id     uuid   not null references public.web (id) on delete cascade,
    usn        bigint not null,
    primary key (account_id, web_id)
);

create index account_web_web_id_idx on public.account_web (web_id);
create index account_web_account_id_usn_idx on public.account_web (account_id, usn);

alter table public.web enable row level security;
alter table public.account_web enable row level security;

grant select on table public.web to authenticated;
grant select on table public.account_web to authenticated;

create policy account_web_select_own
    on public.account_web
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy web_select_linked
    on public.web
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_web
            where account_web.web_id = web.id
                and account_web.account_id = (select auth.uid())
        )
    );

create or replace function public.push_webs(items jsonb)
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
    item_header_list jsonb;
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
        item_header_list := coalesce(item -> 'detail' -> 'headerList', '[]'::jsonb);

        if jsonb_typeof(item_header_list) <> 'array' then
            raise exception 'headerList must be an array.' using errcode = '22023';
        end if;

        if exists (
            select 1
            from public.account_web
            where account_id = current_account_id
                and web_id = item_id
        ) then
            update public.web
            set
                title = item -> 'detail' ->> 'title',
                description = item -> 'detail' ->> 'description',
                url = item -> 'detail' ->> 'url',
                header_list = item_header_list,
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where id = item_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            if found then
                for linked_account_id in
                    select account_id
                    from public.account_web
                    where web_id = item_id
                    order by account_id
                loop
                    update public.account_web
                    set usn = public.next_account_usn(linked_account_id)
                    where account_id = linked_account_id
                        and web_id = item_id;
                end loop;

                affected_count := affected_count + 1;
            end if;
        else
            inserted_id := null;

            insert into public.web (
                id,
                title,
                description,
                url,
                header_list,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_id,
                item -> 'detail' ->> 'title',
                item -> 'detail' ->> 'description',
                item -> 'detail' ->> 'url',
                item_header_list,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            )
            on conflict (id) do nothing
            returning id into inserted_id;

            if inserted_id is null then
                raise exception 'Web is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;

            insert into public.account_web (account_id, web_id, usn)
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

create or replace function public.pull_webs(usn_cursor bigint)
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
            account_web.usn as usn,
            jsonb_build_object(
                'web', jsonb_build_object(
                    'id', web.id,
                    'detail', jsonb_build_object(
                        'title', web.title,
                        'description', web.description,
                        'url', web.url,
                        'headerList', web.header_list
                    ),
                    'isDeleted', web.is_deleted,
                    'updatedAt', web.updated_at,
                    'createdAt', web.created_at
                ),
                'usn', account_web.usn
            ) as item
        from public.account_web
        inner join public.web on web.id = account_web.web_id
        where account_web.account_id = current_account_id
            and account_web.usn > usn_cursor
        order by account_web.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_webs(jsonb) from public;
revoke all on function public.push_webs(jsonb) from anon;
grant execute on function public.push_webs(jsonb) to authenticated;

revoke all on function public.pull_webs(bigint) from public;
revoke all on function public.pull_webs(bigint) from anon;
grant execute on function public.pull_webs(bigint) to authenticated;
