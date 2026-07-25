-- 전화번호는 순서 있는 목록이지만 번호 단위로 조회하거나 변경하지 않으므로 연락처의 jsonb 컬럼 하나로 저장한다.
create table public.contact (
    id                   uuid        primary key,
    name                 text        not null,
    description          text        not null,
    height_centimeter    double precision,
    foot_size_millimeter integer,
    birthday             date,
    birthday_calendar    text,
    phone_number_list    jsonb       not null default '[]'::jsonb,
    is_deleted           boolean     not null,
    updated_at           timestamptz not null,
    created_at           timestamptz not null,
    check (jsonb_typeof(phone_number_list) = 'array'),
    -- 달력 구분은 생일 날짜에 붙는 값이므로 둘은 항상 함께 있거나 함께 비어 있다.
    check ((birthday is null) = (birthday_calendar is null)),
    check (birthday_calendar is null or birthday_calendar in ('solar', 'lunar'))
);

create table public.account_contact (
    account_id uuid   not null references public.account (id) on delete cascade,
    contact_id uuid   not null references public.contact (id) on delete cascade,
    usn        bigint not null,
    primary key (account_id, contact_id)
);

create index account_contact_contact_id_idx on public.account_contact (contact_id);
create index account_contact_account_id_usn_idx on public.account_contact (account_id, usn);

alter table public.contact enable row level security;
alter table public.account_contact enable row level security;

grant select on table public.contact to authenticated;
grant select on table public.account_contact to authenticated;

create policy account_contact_select_own
    on public.account_contact
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy contact_select_linked
    on public.contact
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_contact
            where account_contact.contact_id = contact.id
                and account_contact.account_id = (select auth.uid())
        )
    );

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
                phone_number_list = item_phone_number_list,
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
                phone_number_list,
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
                item_phone_number_list,
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
                        'phoneNumberList', contact.phone_number_list
                    ),
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

revoke all on function public.push_contacts(jsonb) from public;
revoke all on function public.push_contacts(jsonb) from anon;
grant execute on function public.push_contacts(jsonb) to authenticated;

revoke all on function public.pull_contacts(bigint) from public;
revoke all on function public.pull_contacts(bigint) from anon;
grant execute on function public.pull_contacts(bigint) to authenticated;
