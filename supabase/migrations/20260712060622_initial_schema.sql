create table public.account (
    id            uuid        primary key references auth.users (id) on delete cascade,
    email         text        not null unique,
    profile_image text,
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now(),
    sync_usn      bigint      not null default 0
);

alter table public.account enable row level security;

create or replace function public.handle_new_user()
    returns trigger
    language plpgsql
    security definer
    set search_path = ''
as $$
begin
    insert into public.account (id, email, profile_image)
    values (
        new.id,
        coalesce(new.email, new.raw_user_meta_data ->> 'email'),
        coalesce(new.raw_user_meta_data ->> 'avatar_url', new.raw_user_meta_data ->> 'picture')
    )
    on conflict (id) do nothing;

    return new;
end;
$$;

create trigger on_auth_user_created
    after insert on auth.users
    for each row
    execute function public.handle_new_user();

revoke execute on function public.handle_new_user() from anon, authenticated, public;

create table public.tag (
    id          uuid        primary key,
    emoji       text        not null,
    title       text        not null,
    description text        not null,
    color       bigint      not null,
    is_finished boolean     not null,
    is_deleted  boolean     not null,
    updated_at  timestamptz not null,
    created_at  timestamptz not null
);

create table public.memo (
    id             uuid        primary key,
    title          text        not null,
    description    text        not null,
    color          bigint      not null,
    is_all_day     boolean,
    start_at       timestamp,
    end_inclusive  timestamp,
    primary_tag_id uuid        references public.tag (id) on delete set null,
    is_finished    boolean     not null,
    is_deleted     boolean     not null,
    updated_at     timestamptz not null,
    created_at     timestamptz not null,
    check (
        (is_all_day is null and start_at is null and end_inclusive is null)
        or
        (is_all_day is not null and start_at is not null and end_inclusive is not null)
    ),
    check (start_at is null or start_at <= end_inclusive)
);

create index memo_primary_tag_id_idx on public.memo (primary_tag_id);

create table public.account_tag (
    account_id       uuid        not null references public.account (id) on delete cascade,
    tag_id           uuid        not null references public.tag (id) on delete cascade,
    usn              bigint      not null,
    primary key (account_id, tag_id)
);

create index account_tag_tag_id_idx on public.account_tag (tag_id);
create index account_tag_account_id_usn_idx on public.account_tag (account_id, usn);

create table public.account_memo (
    account_id       uuid        not null references public.account (id) on delete cascade,
    memo_id          uuid        not null references public.memo (id) on delete cascade,
    usn              bigint      not null,
    primary key (account_id, memo_id)
);

create index account_memo_memo_id_idx on public.account_memo (memo_id);
create index account_memo_account_id_usn_idx on public.account_memo (account_id, usn);

create table public.memo_tag (
    memo_id    uuid        not null references public.memo (id) on delete cascade,
    tag_id     uuid        not null references public.tag (id) on delete cascade,
    is_deleted boolean     not null,
    updated_at timestamptz not null,
    created_at timestamptz not null,
    primary key (memo_id, tag_id)
);

create index memo_tag_tag_id_idx on public.memo_tag (tag_id);

create table public.account_memo_tag (
    account_id uuid   not null references public.account (id) on delete cascade,
    memo_id    uuid   not null,
    tag_id     uuid   not null,
    usn        bigint not null,
    primary key (account_id, memo_id, tag_id),
    foreign key (memo_id, tag_id) references public.memo_tag (memo_id, tag_id) on delete cascade
);

create index account_memo_tag_memo_id_tag_id_idx on public.account_memo_tag (memo_id, tag_id);
create index account_memo_tag_account_id_usn_idx on public.account_memo_tag (account_id, usn);

alter table public.tag enable row level security;
alter table public.memo enable row level security;
alter table public.account_tag enable row level security;
alter table public.account_memo enable row level security;
alter table public.memo_tag enable row level security;
alter table public.account_memo_tag enable row level security;

grant select on table public.tag to authenticated;
grant select on table public.memo to authenticated;
grant select on table public.account_tag to authenticated;
grant select on table public.account_memo to authenticated;
grant select on table public.memo_tag to authenticated;
grant select on table public.account_memo_tag to authenticated;

create policy account_tag_select_own
    on public.account_tag
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy account_memo_select_own
    on public.account_memo
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy account_memo_tag_select_own
    on public.account_memo_tag
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy tag_select_linked
    on public.tag
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_tag
            where account_tag.tag_id = tag.id
                and account_tag.account_id = (select auth.uid())
        )
    );

create policy memo_select_linked
    on public.memo
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_memo
            where account_memo.memo_id = memo.id
                and account_memo.account_id = (select auth.uid())
        )
    );

create policy memo_tag_select_linked
    on public.memo_tag
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_memo_tag
            where account_memo_tag.memo_id = memo_tag.memo_id
                and account_memo_tag.tag_id = memo_tag.tag_id
                and account_memo_tag.account_id = (select auth.uid())
        )
    );

-- 계정 행을 갱신해 순번을 부여하므로 같은 계정의 동시 요청은 직렬화되고,
-- 낮은 순번이 항상 먼저 커밋되어 내려받기가 순번을 건너뛰지 않는다.
create or replace function public.next_account_usn(target_account_id uuid)
    returns bigint
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    next_usn bigint;
begin
    update public.account
    set sync_usn = sync_usn + 1
    where id = target_account_id
    returning sync_usn into next_usn;

    if next_usn is null then
        raise exception 'Account does not exist.' using errcode = '23503';
    end if;

    return next_usn;
end;
$$;

revoke all on function public.next_account_usn(uuid) from public;
revoke all on function public.next_account_usn(uuid) from anon;
revoke all on function public.next_account_usn(uuid) from authenticated;

create or replace function public.push_tags(items jsonb)
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
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_id
        ) then
            update public.tag
            set
                emoji = item -> 'detail' ->> 'emoji',
                title = item -> 'detail' ->> 'title',
                description = item -> 'detail' ->> 'description',
                color = (item -> 'detail' ->> 'color')::bigint,
                is_finished = (item ->> 'isFinished')::boolean,
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where id = item_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            if found then
                for linked_account_id in
                    select account_id
                    from public.account_tag
                    where tag_id = item_id
                    order by account_id
                loop
                    update public.account_tag
                    set usn = public.next_account_usn(linked_account_id)
                    where account_id = linked_account_id
                        and tag_id = item_id;
                end loop;

                affected_count := affected_count + 1;
            end if;
        else
            inserted_id := null;

            insert into public.tag (
                id,
                emoji,
                title,
                description,
                color,
                is_finished,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_id,
                item -> 'detail' ->> 'emoji',
                item -> 'detail' ->> 'title',
                item -> 'detail' ->> 'description',
                (item -> 'detail' ->> 'color')::bigint,
                (item ->> 'isFinished')::boolean,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            )
            on conflict (id) do nothing
            returning id into inserted_id;

            if inserted_id is null then
                raise exception 'Tag is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;

            insert into public.account_tag (account_id, tag_id, usn)
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

        if item_primary_tag_id is not null and not exists (
            select 1
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_primary_tag_id
        ) then
            raise exception 'Primary tag is not linked to the authenticated account.'
                using errcode = '42501';
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

-- 관계는 메모와 태그를 모두 소유한 계정만 올릴 수 있고, 관계 자체는 메모와 태그처럼
-- 여러 계정이 공유하므로 내용이 바뀌면 연결된 모든 계정의 순번을 올린다.
create or replace function public.push_memo_tags(items jsonb)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    affected_count integer := 0;
    item jsonb;
    item_memo_id uuid;
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
        item_memo_id := (item ->> 'memoId')::uuid;
        item_tag_id := (item ->> 'tagId')::uuid;

        if not exists (
            select 1
            from public.account_memo
            where account_id = current_account_id
                and memo_id = item_memo_id
        ) or not exists (
            select 1
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_tag_id
        ) then
            raise exception 'Memo tag is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        is_account_linked := exists (
            select 1
            from public.account_memo_tag
            where account_id = current_account_id
                and memo_id = item_memo_id
                and tag_id = item_tag_id
        );

        if exists (
            select 1
            from public.memo_tag
            where memo_id = item_memo_id
                and tag_id = item_tag_id
        ) then
            update public.memo_tag
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where memo_id = item_memo_id
                and tag_id = item_tag_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            is_relation_changed := found;
        else
            insert into public.memo_tag (
                memo_id,
                tag_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_memo_id,
                item_tag_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            );

            is_relation_changed := true;
        end if;

        if not is_account_linked then
            insert into public.account_memo_tag (account_id, memo_id, tag_id, usn)
            values (
                current_account_id,
                item_memo_id,
                item_tag_id,
                public.next_account_usn(current_account_id)
            );
        end if;

        if is_relation_changed then
            for linked_account_id in
                select account_id
                from public.account_memo_tag
                where memo_id = item_memo_id
                    and tag_id = item_tag_id
                order by account_id
            loop
                update public.account_memo_tag
                set usn = public.next_account_usn(linked_account_id)
                where account_id = linked_account_id
                    and memo_id = item_memo_id
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

revoke all on function public.push_tags(jsonb) from public;
revoke all on function public.push_tags(jsonb) from anon;
grant execute on function public.push_tags(jsonb) to authenticated;

revoke all on function public.push_memos(jsonb) from public;
revoke all on function public.push_memos(jsonb) from anon;
grant execute on function public.push_memos(jsonb) to authenticated;

revoke all on function public.push_memo_tags(jsonb) from public;
revoke all on function public.push_memo_tags(jsonb) from anon;
grant execute on function public.push_memo_tags(jsonb) to authenticated;

create or replace function public.pull_tags(usn_cursor bigint)
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
            account_tag.usn as usn,
            jsonb_build_object(
                'tag', jsonb_build_object(
                    'id', tag.id,
                    'detail', jsonb_build_object(
                        'emoji', tag.emoji,
                        'title', tag.title,
                        'description', tag.description,
                        'color', tag.color
                    ),
                    'isFinished', tag.is_finished,
                    'isDeleted', tag.is_deleted,
                    'updatedAt', tag.updated_at,
                    'createdAt', tag.created_at
                ),
                'usn', account_tag.usn
            ) as item
        from public.account_tag
        inner join public.tag on tag.id = account_tag.tag_id
        where account_tag.account_id = current_account_id
            and account_tag.usn > usn_cursor
        order by account_tag.usn
        limit 100
    ) as page;

    return result;
end;
$$;

create or replace function public.pull_memos(usn_cursor bigint)
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
            account_memo.usn as usn,
            jsonb_build_object(
                'memo', jsonb_build_object(
                    'id', memo.id,
                    'detail', jsonb_build_object(
                        'title', memo.title,
                        'description', memo.description,
                        'color', memo.color,
                        'isAllDay', memo.is_all_day,
                        'start', memo.start_at,
                        'endInclusive', memo.end_inclusive
                    ),
                    'primaryTagId', memo.primary_tag_id,
                    'isFinished', memo.is_finished,
                    'isDeleted', memo.is_deleted,
                    'updatedAt', memo.updated_at,
                    'createdAt', memo.created_at
                ),
                'usn', account_memo.usn
            ) as item
        from public.account_memo
        inner join public.memo on memo.id = account_memo.memo_id
        where account_memo.account_id = current_account_id
            and account_memo.usn > usn_cursor
        order by account_memo.usn
        limit 100
    ) as page;

    return result;
end;
$$;

create or replace function public.pull_memo_tags(usn_cursor bigint)
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
            account_memo_tag.usn as usn,
            jsonb_build_object(
                'memoTag', jsonb_build_object(
                    'memoId', memo_tag.memo_id,
                    'tagId', memo_tag.tag_id,
                    'isDeleted', memo_tag.is_deleted,
                    'updatedAt', memo_tag.updated_at,
                    'createdAt', memo_tag.created_at
                ),
                'usn', account_memo_tag.usn
            ) as item
        from public.account_memo_tag
        inner join public.memo_tag
            on memo_tag.memo_id = account_memo_tag.memo_id
                and memo_tag.tag_id = account_memo_tag.tag_id
        where account_memo_tag.account_id = current_account_id
            and account_memo_tag.usn > usn_cursor
        order by account_memo_tag.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.pull_tags(bigint) from public;
revoke all on function public.pull_tags(bigint) from anon;
grant execute on function public.pull_tags(bigint) to authenticated;

revoke all on function public.pull_memos(bigint) from public;
revoke all on function public.pull_memos(bigint) from anon;
grant execute on function public.pull_memos(bigint) to authenticated;

revoke all on function public.pull_memo_tags(bigint) from public;
revoke all on function public.pull_memo_tags(bigint) from anon;
grant execute on function public.pull_memo_tags(bigint) to authenticated;
