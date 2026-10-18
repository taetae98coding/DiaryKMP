-- 태어난 연도가 생기기 전 버전의 앱은 v1 Edge Function으로 이전 형식을, 현재 앱은 v2 Edge Function으로 태어난 연도를 담는 형식을 주고받는다.
-- v1은 생일이 있으면 그 연도를 태어난 연도로 저장하고, 생일이 없으면 저장된 태어난 연도를 바꾸지 않는다.

create or replace function public.push_contacts_v1(items jsonb)
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
    item_phone_number_list jsonb;
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
        item_phone_number_list := coalesce(item -> 'detail' -> 'phoneNumberList', '[]'::jsonb);

        if jsonb_typeof(item_phone_number_list) <> 'array' then
            raise exception 'phoneNumberList must be an array.' using errcode = '22023';
        end if;

        if (item -> 'detail' ->> 'birthday' is null) <> (item -> 'detail' ->> 'birthdayCalendar' is null) then
            raise exception 'birthday and birthdayCalendar must be set together.' using errcode = '22023';
        end if;

        if item -> 'detail' ->> 'birthdayCalendar' is not null
            and item -> 'detail' ->> 'birthdayCalendar' not in ('solar', 'lunar') then
            raise exception 'birthdayCalendar must be solar or lunar.' using errcode = '22023';
        end if;

        if exists (
            select 1
            from public.account_contact
            where account_id = current_account_id
                and contact_id = item_id
        ) then
            update public.contact
            set
                name = item -> 'detail' ->> 'name',
                description = item -> 'detail' ->> 'description',
                height_centimeter = (item -> 'detail' ->> 'heightCentimeter')::double precision,
                foot_size_millimeter = (item -> 'detail' ->> 'footSizeMillimeter')::integer,
                birthday = (item -> 'detail' ->> 'birthday')::date,
                birthday_calendar = (item -> 'detail' ->> 'birthdayCalendar'),
                birth_year = case
                    when item -> 'detail' ->> 'birthday' is not null
                        then extract(year from (item -> 'detail' ->> 'birthday')::date)::integer
                    else birth_year
                end,
                hometown = item -> 'detail' ->> 'hometown',
                phone_number_list = item_phone_number_list,
                is_favorite = (item ->> 'isFavorite')::boolean,
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where id = item_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            if found then
                for linked_account_id in
                    select account_id
                    from public.account_contact
                    where contact_id = item_id
                    order by account_id
                loop
                    update public.account_contact
                    set usn = public.next_account_usn(linked_account_id)
                    where account_id = linked_account_id
                        and contact_id = item_id;
                end loop;

                affected_count := affected_count + 1;
            end if;
        else
            inserted_id := null;

            insert into public.contact (
                id,
                name,
                description,
                height_centimeter,
                foot_size_millimeter,
                birthday,
                birthday_calendar,
                birth_year,
                hometown,
                phone_number_list,
                is_favorite,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_id,
                item -> 'detail' ->> 'name',
                item -> 'detail' ->> 'description',
                (item -> 'detail' ->> 'heightCentimeter')::double precision,
                (item -> 'detail' ->> 'footSizeMillimeter')::integer,
                (item -> 'detail' ->> 'birthday')::date,
                (item -> 'detail' ->> 'birthdayCalendar'),
                extract(year from (item -> 'detail' ->> 'birthday')::date)::integer,
                item -> 'detail' ->> 'hometown',
                item_phone_number_list,
                (item ->> 'isFavorite')::boolean,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            )
            on conflict (id) do nothing
            returning id into inserted_id;

            if inserted_id is null then
                raise exception 'Contact is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;

            insert into public.account_contact (account_id, contact_id, usn)
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

revoke all on function public.push_contacts_v1(jsonb) from public;
revoke all on function public.push_contacts_v1(jsonb) from anon;
grant execute on function public.push_contacts_v1(jsonb) to authenticated;

create or replace function public.push_contacts_v2(items jsonb)
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
    item_phone_number_list jsonb;
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
        item_phone_number_list := coalesce(item -> 'detail' -> 'phoneNumberList', '[]'::jsonb);

        if jsonb_typeof(item_phone_number_list) <> 'array' then
            raise exception 'phoneNumberList must be an array.' using errcode = '22023';
        end if;

        if (item -> 'detail' ->> 'birthday' is null) <> (item -> 'detail' ->> 'birthdayCalendar' is null) then
            raise exception 'birthday and birthdayCalendar must be set together.' using errcode = '22023';
        end if;

        if item -> 'detail' ->> 'birthdayCalendar' is not null
            and item -> 'detail' ->> 'birthdayCalendar' not in ('solar', 'lunar') then
            raise exception 'birthdayCalendar must be solar or lunar.' using errcode = '22023';
        end if;

        if item -> 'detail' ->> 'birthday' is not null
            and (item -> 'detail' ->> 'birthYear')::integer is distinct from
                extract(year from (item -> 'detail' ->> 'birthday')::date)::integer then
            raise exception 'birthYear must be the year of birthday.' using errcode = '22023';
        end if;

        if exists (
            select 1
            from public.account_contact
            where account_id = current_account_id
                and contact_id = item_id
        ) then
            update public.contact
            set
                name = item -> 'detail' ->> 'name',
                description = item -> 'detail' ->> 'description',
                height_centimeter = (item -> 'detail' ->> 'heightCentimeter')::double precision,
                foot_size_millimeter = (item -> 'detail' ->> 'footSizeMillimeter')::integer,
                birthday = (item -> 'detail' ->> 'birthday')::date,
                birthday_calendar = (item -> 'detail' ->> 'birthdayCalendar'),
                birth_year = (item -> 'detail' ->> 'birthYear')::integer,
                hometown = item -> 'detail' ->> 'hometown',
                phone_number_list = item_phone_number_list,
                is_favorite = (item ->> 'isFavorite')::boolean,
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where id = item_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            if found then
                for linked_account_id in
                    select account_id
                    from public.account_contact
                    where contact_id = item_id
                    order by account_id
                loop
                    update public.account_contact
                    set usn = public.next_account_usn(linked_account_id)
                    where account_id = linked_account_id
                        and contact_id = item_id;
                end loop;

                affected_count := affected_count + 1;
            end if;
        else
            inserted_id := null;

            insert into public.contact (
                id,
                name,
                description,
                height_centimeter,
                foot_size_millimeter,
                birthday,
                birthday_calendar,
                birth_year,
                hometown,
                phone_number_list,
                is_favorite,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_id,
                item -> 'detail' ->> 'name',
                item -> 'detail' ->> 'description',
                (item -> 'detail' ->> 'heightCentimeter')::double precision,
                (item -> 'detail' ->> 'footSizeMillimeter')::integer,
                (item -> 'detail' ->> 'birthday')::date,
                (item -> 'detail' ->> 'birthdayCalendar'),
                (item -> 'detail' ->> 'birthYear')::integer,
                item -> 'detail' ->> 'hometown',
                item_phone_number_list,
                (item ->> 'isFavorite')::boolean,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            )
            on conflict (id) do nothing
            returning id into inserted_id;

            if inserted_id is null then
                raise exception 'Contact is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;

            insert into public.account_contact (account_id, contact_id, usn)
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

revoke all on function public.push_contacts_v2(jsonb) from public;
revoke all on function public.push_contacts_v2(jsonb) from anon;
grant execute on function public.push_contacts_v2(jsonb) to authenticated;

create or replace function public.pull_contacts_v1(usn_cursor bigint)
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
            account_contact.usn as usn,
            jsonb_build_object(
                'contact', jsonb_build_object(
                    'id', contact.id,
                    'detail', jsonb_build_object(
                        'name', contact.name,
                        'description', contact.description,
                        'heightCentimeter', contact.height_centimeter,
                        'footSizeMillimeter', contact.foot_size_millimeter,
                        'birthday', contact.birthday,
                        'birthdayCalendar', contact.birthday_calendar,
                        'hometown', contact.hometown,
                        'phoneNumberList', contact.phone_number_list
                    ),
                    'isFavorite', contact.is_favorite,
                    'isDeleted', contact.is_deleted,
                    'updatedAt', contact.updated_at,
                    'createdAt', contact.created_at
                ),
                'usn', account_contact.usn
            ) as item
        from public.account_contact
        inner join public.contact on contact.id = account_contact.contact_id
        where account_contact.account_id = current_account_id
            and account_contact.usn > usn_cursor
        order by account_contact.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.pull_contacts_v1(bigint) from public;
revoke all on function public.pull_contacts_v1(bigint) from anon;
grant execute on function public.pull_contacts_v1(bigint) to authenticated;

create or replace function public.pull_contacts_v2(usn_cursor bigint)
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
            account_contact.usn as usn,
            jsonb_build_object(
                'contact', jsonb_build_object(
                    'id', contact.id,
                    'detail', jsonb_build_object(
                        'name', contact.name,
                        'description', contact.description,
                        'heightCentimeter', contact.height_centimeter,
                        'footSizeMillimeter', contact.foot_size_millimeter,
                        'birthday', contact.birthday,
                        'birthdayCalendar', contact.birthday_calendar,
                        'birthYear', contact.birth_year,
                        'hometown', contact.hometown,
                        'phoneNumberList', contact.phone_number_list
                    ),
                    'isFavorite', contact.is_favorite,
                    'isDeleted', contact.is_deleted,
                    'updatedAt', contact.updated_at,
                    'createdAt', contact.created_at
                ),
                'usn', account_contact.usn
            ) as item
        from public.account_contact
        inner join public.contact on contact.id = account_contact.contact_id
        where account_contact.account_id = current_account_id
            and account_contact.usn > usn_cursor
        order by account_contact.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.pull_contacts_v2(bigint) from public;
revoke all on function public.pull_contacts_v2(bigint) from anon;
grant execute on function public.pull_contacts_v2(bigint) to authenticated;
