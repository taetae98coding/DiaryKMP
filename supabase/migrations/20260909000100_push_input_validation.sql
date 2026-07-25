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

        if (item -> 'detail' ->> 'isAllDay' is null) <> (item -> 'detail' ->> 'start' is null)
            or (item -> 'detail' ->> 'isAllDay' is null) <> (item -> 'detail' ->> 'endInclusive' is null) then
            raise exception 'isAllDay, start and endInclusive must be set together.' using errcode = '22023';
        end if;

        if (item -> 'detail' ->> 'start')::timestamp > (item -> 'detail' ->> 'endInclusive')::timestamp then
            raise exception 'start must not be after endInclusive.' using errcode = '22023';
        end if;

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

        if (item -> 'detail' ->> 'latitude')::double precision not between -90 and 90 then
            raise exception 'latitude must be between -90 and 90.' using errcode = '22023';
        end if;

        if (item -> 'detail' ->> 'longitude')::double precision not between -180 and 180 then
            raise exception 'longitude must be between -180 and 180.' using errcode = '22023';
        end if;

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
