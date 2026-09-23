-- 메모와 연락처의 연결은 두 항목을 함께 가리키는 관계이므로,
-- 메모와 웹 항목의 연결과 같은 구조로 연결 테이블과 계정 연결 테이블을 나눠 둔다.
create table public.memo_contact (
    memo_id    uuid        not null references public.memo (id) on delete cascade,
    contact_id uuid        not null references public.contact (id) on delete cascade,
    is_deleted boolean     not null,
    updated_at timestamptz not null,
    created_at timestamptz not null,
    primary key (memo_id, contact_id)
);

create index memo_contact_contact_id_idx on public.memo_contact (contact_id);

create table public.account_memo_contact (
    account_id uuid   not null references public.account (id) on delete cascade,
    memo_id    uuid   not null,
    contact_id uuid   not null,
    usn        bigint not null,
    primary key (account_id, memo_id, contact_id),
    foreign key (memo_id, contact_id) references public.memo_contact (memo_id, contact_id) on delete cascade
);

create index account_memo_contact_memo_id_contact_id_idx on public.account_memo_contact (memo_id, contact_id);
create index account_memo_contact_account_id_usn_idx on public.account_memo_contact (account_id, usn);

alter table public.memo_contact enable row level security;
alter table public.account_memo_contact enable row level security;

grant select on table public.memo_contact to authenticated;
grant select on table public.account_memo_contact to authenticated;

create policy account_memo_contact_select_own
    on public.account_memo_contact
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy memo_contact_select_linked
    on public.memo_contact
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_memo_contact
            where account_memo_contact.memo_id = memo_contact.memo_id
                and account_memo_contact.contact_id = memo_contact.contact_id
                and account_memo_contact.account_id = (select auth.uid())
        )
    );

create or replace function public.push_memo_contacts(items jsonb)
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
    item_contact_id uuid;
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
        item_contact_id := (item ->> 'contactId')::uuid;

        if not exists (
            select 1
            from public.account_memo
            where account_id = current_account_id
                and memo_id = item_memo_id
        ) or not exists (
            select 1
            from public.account_contact
            where account_id = current_account_id
                and contact_id = item_contact_id
        ) then
            raise exception 'memo contact link is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        is_account_linked := exists (
            select 1
            from public.account_memo_contact
            where account_id = current_account_id
                and memo_id = item_memo_id
                and contact_id = item_contact_id
        );

        if exists (
            select 1
            from public.memo_contact
            where memo_id = item_memo_id
                and contact_id = item_contact_id
        ) then
            update public.memo_contact
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where memo_id = item_memo_id
                and contact_id = item_contact_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            is_relation_changed := found;
        else
            insert into public.memo_contact (
                memo_id,
                contact_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_memo_id,
                item_contact_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            );

            is_relation_changed := true;
        end if;

        if not is_account_linked then
            insert into public.account_memo_contact (account_id, memo_id, contact_id, usn)
            values (
                current_account_id,
                item_memo_id,
                item_contact_id,
                public.next_account_usn(current_account_id)
            );
        end if;

        if is_relation_changed then
            for linked_account_id in
                select account_id
                from public.account_memo_contact
                where memo_id = item_memo_id
                    and contact_id = item_contact_id
                order by account_id
            loop
                update public.account_memo_contact
                set usn = public.next_account_usn(linked_account_id)
                where account_id = linked_account_id
                    and memo_id = item_memo_id
                    and contact_id = item_contact_id;
            end loop;
        end if;

        if is_relation_changed or not is_account_linked then
            affected_count := affected_count + 1;
        end if;
    end loop;

    return affected_count;
end;
$$;

create or replace function public.pull_memo_contacts(usn_cursor bigint)
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
            account_memo_contact.usn as usn,
            jsonb_build_object(
                'memoContact', jsonb_build_object(
                    'memoId', memo_contact.memo_id,
                    'contactId', memo_contact.contact_id,
                    'isDeleted', memo_contact.is_deleted,
                    'updatedAt', memo_contact.updated_at,
                    'createdAt', memo_contact.created_at
                ),
                'usn', account_memo_contact.usn
            ) as item
        from public.account_memo_contact
        inner join public.memo_contact
            on memo_contact.memo_id = account_memo_contact.memo_id
                and memo_contact.contact_id = account_memo_contact.contact_id
        where account_memo_contact.account_id = current_account_id
            and account_memo_contact.usn > usn_cursor
        order by account_memo_contact.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_memo_contacts(jsonb) from public;
revoke all on function public.push_memo_contacts(jsonb) from anon;
grant execute on function public.push_memo_contacts(jsonb) to authenticated;

revoke all on function public.pull_memo_contacts(bigint) from public;
revoke all on function public.pull_memo_contacts(bigint) from anon;
grant execute on function public.pull_memo_contacts(bigint) to authenticated;
