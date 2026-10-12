-- 생일을 모르고 태어난 해만 아는 연락처를 위해 태어난 연도를 따로 두고, 생일이 있으면 생일의 연도와 같게 유지한다.
alter table public.contact
    add column birth_year integer;

-- 기기도 같은 기준으로 채우므로 내용이 바뀐 것으로 보지 않아 usn과 updated_at을 그대로 둔다.
update public.contact
set birth_year = extract(year from birthday)::integer
where birthday is not null;

alter table public.contact
    add constraint contact_birth_year_matches_birthday
        check (birthday is null or birth_year = extract(year from birthday)::integer);

create or replace function public.push_contacts(items jsonb)
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

create or replace function public.pull_contacts(usn_cursor bigint)
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
