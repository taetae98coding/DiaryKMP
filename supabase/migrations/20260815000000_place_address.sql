alter table public.place
    add column address text not null default '';

-- address 반영 전 클라이언트는 detail에 address 없이 push하므로 coalesce로 빈 주소를 유지한다.
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
                address = coalesce(item -> 'detail' ->> 'address', ''),
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
                address,
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
                coalesce(item -> 'detail' ->> 'address', ''),
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
                        'longitude', place.longitude,
                        'address', place.address
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
