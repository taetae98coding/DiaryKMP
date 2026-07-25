create table public.place (
    id          uuid             primary key,
    title       text             not null,
    description text             not null,
    color       bigint           not null,
    latitude    double precision not null,
    longitude   double precision not null,
    is_deleted  boolean          not null,
    updated_at  timestamptz      not null,
    created_at  timestamptz      not null,
    check (latitude >= -90 and latitude <= 90),
    check (longitude >= -180 and longitude <= 180)
);

create table public.account_place (
    account_id uuid   not null references public.account (id) on delete cascade,
    place_id   uuid   not null references public.place (id) on delete cascade,
    usn        bigint not null,
    primary key (account_id, place_id)
);

create index account_place_place_id_idx on public.account_place (place_id);
create index account_place_account_id_usn_idx on public.account_place (account_id, usn);

create table public.memo_place (
    memo_id    uuid        not null references public.memo (id) on delete cascade,
    place_id   uuid        not null references public.place (id) on delete cascade,
    is_deleted boolean     not null,
    updated_at timestamptz not null,
    created_at timestamptz not null,
    primary key (memo_id, place_id)
);

create index memo_place_place_id_idx on public.memo_place (place_id);

create table public.account_memo_place (
    account_id uuid   not null references public.account (id) on delete cascade,
    memo_id    uuid   not null,
    place_id   uuid   not null,
    usn        bigint not null,
    primary key (account_id, memo_id, place_id),
    foreign key (memo_id, place_id) references public.memo_place (memo_id, place_id) on delete cascade
);

create index account_memo_place_memo_id_place_id_idx on public.account_memo_place (memo_id, place_id);
create index account_memo_place_account_id_usn_idx on public.account_memo_place (account_id, usn);

alter table public.place enable row level security;
alter table public.account_place enable row level security;
alter table public.memo_place enable row level security;
alter table public.account_memo_place enable row level security;

grant select on table public.place to authenticated;
grant select on table public.account_place to authenticated;
grant select on table public.memo_place to authenticated;
grant select on table public.account_memo_place to authenticated;

create policy account_place_select_own
    on public.account_place
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy account_memo_place_select_own
    on public.account_memo_place
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy place_select_linked
    on public.place
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_place
            where account_place.place_id = place.id
                and account_place.account_id = (select auth.uid())
        )
    );

create policy memo_place_select_linked
    on public.memo_place
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_memo_place
            where account_memo_place.memo_id = memo_place.memo_id
                and account_memo_place.place_id = memo_place.place_id
                and account_memo_place.account_id = (select auth.uid())
        )
    );

create or replace function public.push_places(items jsonb)
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
            from public.account_place
            where account_id = current_account_id
                and place_id = item_id
        ) then
            update public.place
            set
                title = item -> 'detail' ->> 'title',
                description = item -> 'detail' ->> 'description',
                color = (item -> 'detail' ->> 'color')::bigint,
                latitude = (item -> 'detail' ->> 'latitude')::double precision,
                longitude = (item -> 'detail' ->> 'longitude')::double precision,
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where id = item_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            if found then
                for linked_account_id in
                    select account_id
                    from public.account_place
                    where place_id = item_id
                    order by account_id
                loop
                    update public.account_place
                    set usn = public.next_account_usn(linked_account_id)
                    where account_id = linked_account_id
                        and place_id = item_id;
                end loop;

                affected_count := affected_count + 1;
            end if;
        else
            inserted_id := null;

            insert into public.place (
                id,
                title,
                description,
                color,
                latitude,
                longitude,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_id,
                item -> 'detail' ->> 'title',
                item -> 'detail' ->> 'description',
                (item -> 'detail' ->> 'color')::bigint,
                (item -> 'detail' ->> 'latitude')::double precision,
                (item -> 'detail' ->> 'longitude')::double precision,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            )
            on conflict (id) do nothing
            returning id into inserted_id;

            if inserted_id is null then
                raise exception 'Place is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;

            insert into public.account_place (account_id, place_id, usn)
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

-- 관계는 메모와 장소를 모두 소유한 계정만 올릴 수 있고, 관계 자체는 메모와 장소처럼
-- 여러 계정이 공유하므로 내용이 바뀌면 연결된 모든 계정의 순번을 올린다.
create or replace function public.push_memo_places(items jsonb)
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
    item_place_id uuid;
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
        item_place_id := (item ->> 'placeId')::uuid;

        if not exists (
            select 1
            from public.account_memo
            where account_id = current_account_id
                and memo_id = item_memo_id
        ) or not exists (
            select 1
            from public.account_place
            where account_id = current_account_id
                and place_id = item_place_id
        ) then
            raise exception 'Memo place is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        is_account_linked := exists (
            select 1
            from public.account_memo_place
            where account_id = current_account_id
                and memo_id = item_memo_id
                and place_id = item_place_id
        );

        if exists (
            select 1
            from public.memo_place
            where memo_id = item_memo_id
                and place_id = item_place_id
        ) then
            update public.memo_place
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where memo_id = item_memo_id
                and place_id = item_place_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            is_relation_changed := found;
        else
            insert into public.memo_place (
                memo_id,
                place_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_memo_id,
                item_place_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            );

            is_relation_changed := true;
        end if;

        if not is_account_linked then
            insert into public.account_memo_place (account_id, memo_id, place_id, usn)
            values (
                current_account_id,
                item_memo_id,
                item_place_id,
                public.next_account_usn(current_account_id)
            );
        end if;

        if is_relation_changed then
            for linked_account_id in
                select account_id
                from public.account_memo_place
                where memo_id = item_memo_id
                    and place_id = item_place_id
                order by account_id
            loop
                update public.account_memo_place
                set usn = public.next_account_usn(linked_account_id)
                where account_id = linked_account_id
                    and memo_id = item_memo_id
                    and place_id = item_place_id;
            end loop;
        end if;

        if is_relation_changed or not is_account_linked then
            affected_count := affected_count + 1;
        end if;
    end loop;

    return affected_count;
end;
$$;

revoke all on function public.push_places(jsonb) from public;
revoke all on function public.push_places(jsonb) from anon;
grant execute on function public.push_places(jsonb) to authenticated;

revoke all on function public.push_memo_places(jsonb) from public;
revoke all on function public.push_memo_places(jsonb) from anon;
grant execute on function public.push_memo_places(jsonb) to authenticated;

create or replace function public.pull_places(usn_cursor bigint)
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
            account_place.usn as usn,
            jsonb_build_object(
                'place', jsonb_build_object(
                    'id', place.id,
                    'detail', jsonb_build_object(
                        'title', place.title,
                        'description', place.description,
                        'color', place.color,
                        'latitude', place.latitude,
                        'longitude', place.longitude
                    ),
                    'isDeleted', place.is_deleted,
                    'updatedAt', place.updated_at,
                    'createdAt', place.created_at
                ),
                'usn', account_place.usn
            ) as item
        from public.account_place
        inner join public.place on place.id = account_place.place_id
        where account_place.account_id = current_account_id
            and account_place.usn > usn_cursor
        order by account_place.usn
        limit 100
    ) as page;

    return result;
end;
$$;

create or replace function public.pull_memo_places(usn_cursor bigint)
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
            account_memo_place.usn as usn,
            jsonb_build_object(
                'memoPlace', jsonb_build_object(
                    'memoId', memo_place.memo_id,
                    'placeId', memo_place.place_id,
                    'isDeleted', memo_place.is_deleted,
                    'updatedAt', memo_place.updated_at,
                    'createdAt', memo_place.created_at
                ),
                'usn', account_memo_place.usn
            ) as item
        from public.account_memo_place
        inner join public.memo_place
            on memo_place.memo_id = account_memo_place.memo_id
                and memo_place.place_id = account_memo_place.place_id
        where account_memo_place.account_id = current_account_id
            and account_memo_place.usn > usn_cursor
        order by account_memo_place.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.pull_places(bigint) from public;
revoke all on function public.pull_places(bigint) from anon;
grant execute on function public.pull_places(bigint) to authenticated;

revoke all on function public.pull_memo_places(bigint) from public;
revoke all on function public.pull_memo_places(bigint) from anon;
grant execute on function public.pull_memo_places(bigint) to authenticated;
