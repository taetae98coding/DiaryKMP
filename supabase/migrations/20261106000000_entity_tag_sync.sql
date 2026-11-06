-- 연락처·체크리스트·곡·QR과 태그의 연결은 웹 항목·장소와 태그의 연결과 같은 관계이므로,
-- 같은 구조로 종류마다 연결 테이블과 계정 연결 테이블, push·pull 함수를 둔다.

-- contact_tag
create table public.contact_tag (
    contact_id uuid        not null references public.contact (id) on delete cascade,
    tag_id     uuid        not null references public.tag (id) on delete cascade,
    is_deleted boolean     not null,
    updated_at timestamptz not null,
    created_at timestamptz not null,
    primary key (contact_id, tag_id)
);

create index contact_tag_tag_id_idx on public.contact_tag (tag_id);

create table public.account_contact_tag (
    account_id uuid        not null references public.account (id) on delete cascade,
    contact_id uuid        not null,
    tag_id     uuid        not null,
    usn        bigint      not null,
    primary key (account_id, contact_id, tag_id),
    foreign key (contact_id, tag_id) references public.contact_tag (contact_id, tag_id) on delete cascade
);

create index account_contact_tag_contact_id_tag_id_idx on public.account_contact_tag (contact_id, tag_id);
create index account_contact_tag_account_id_usn_idx on public.account_contact_tag (account_id, usn);

alter table public.contact_tag enable row level security;
alter table public.account_contact_tag enable row level security;

grant select on table public.contact_tag to authenticated;
grant select on table public.account_contact_tag to authenticated;

create policy account_contact_tag_select_own
    on public.account_contact_tag
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy contact_tag_select_linked
    on public.contact_tag
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_contact_tag
            where account_contact_tag.contact_id = contact_tag.contact_id
                and account_contact_tag.tag_id = contact_tag.tag_id
                and account_contact_tag.account_id = (select auth.uid())
        )
    );

create or replace function public.push_contact_tags(items jsonb)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    affected_count integer := 0;
    item jsonb;
    item_contact_id uuid;
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
        item_contact_id := (item ->> 'contactId')::uuid;
        item_tag_id := (item ->> 'tagId')::uuid;

        if not exists (
            select 1
            from public.account_contact
            where account_id = current_account_id
                and contact_id = item_contact_id
        ) or not exists (
            select 1
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_tag_id
        ) then
            raise exception 'Contact tag is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        is_account_linked := exists (
            select 1
            from public.account_contact_tag
            where account_id = current_account_id
                and contact_id = item_contact_id
                and tag_id = item_tag_id
        );

        if exists (
            select 1
            from public.contact_tag
            where contact_id = item_contact_id
                and tag_id = item_tag_id
        ) then
            update public.contact_tag
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where contact_id = item_contact_id
                and tag_id = item_tag_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            is_relation_changed := found;
        else
            insert into public.contact_tag (
                contact_id,
                tag_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_contact_id,
                item_tag_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            );

            is_relation_changed := true;
        end if;

        if not is_account_linked then
            insert into public.account_contact_tag (account_id, contact_id, tag_id, usn)
            values (
                current_account_id,
                item_contact_id,
                item_tag_id,
                public.next_account_usn(current_account_id)
            );
        end if;

        if is_relation_changed then
            for linked_account_id in
                select account_id
                from public.account_contact_tag
                where contact_id = item_contact_id
                    and tag_id = item_tag_id
                order by account_id
            loop
                update public.account_contact_tag
                set usn = public.next_account_usn(linked_account_id)
                where account_id = linked_account_id
                    and contact_id = item_contact_id
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

create or replace function public.pull_contact_tags(usn_cursor bigint)
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
            account_contact_tag.usn as usn,
            jsonb_build_object(
                'contactTag', jsonb_build_object(
                    'contactId', contact_tag.contact_id,
                    'tagId', contact_tag.tag_id,
                    'isDeleted', contact_tag.is_deleted,
                    'updatedAt', contact_tag.updated_at,
                    'createdAt', contact_tag.created_at
                ),
                'usn', account_contact_tag.usn
            ) as item
        from public.account_contact_tag
        inner join public.contact_tag
            on contact_tag.contact_id = account_contact_tag.contact_id
                and contact_tag.tag_id = account_contact_tag.tag_id
        where account_contact_tag.account_id = current_account_id
            and account_contact_tag.usn > usn_cursor
        order by account_contact_tag.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_contact_tags(jsonb) from public;
revoke all on function public.push_contact_tags(jsonb) from anon;
grant execute on function public.push_contact_tags(jsonb) to authenticated;

revoke all on function public.pull_contact_tags(bigint) from public;
revoke all on function public.pull_contact_tags(bigint) from anon;
grant execute on function public.pull_contact_tags(bigint) to authenticated;

-- checklist_tag
create table public.checklist_tag (
    checklist_id uuid        not null references public.checklist (id) on delete cascade,
    tag_id       uuid        not null references public.tag (id) on delete cascade,
    is_deleted   boolean     not null,
    updated_at   timestamptz not null,
    created_at   timestamptz not null,
    primary key (checklist_id, tag_id)
);

create index checklist_tag_tag_id_idx on public.checklist_tag (tag_id);

create table public.account_checklist_tag (
    account_id   uuid        not null references public.account (id) on delete cascade,
    checklist_id uuid        not null,
    tag_id       uuid        not null,
    usn          bigint      not null,
    primary key (account_id, checklist_id, tag_id),
    foreign key (checklist_id, tag_id) references public.checklist_tag (checklist_id, tag_id) on delete cascade
);

create index account_checklist_tag_checklist_id_tag_id_idx on public.account_checklist_tag (checklist_id, tag_id);
create index account_checklist_tag_account_id_usn_idx on public.account_checklist_tag (account_id, usn);

alter table public.checklist_tag enable row level security;
alter table public.account_checklist_tag enable row level security;

grant select on table public.checklist_tag to authenticated;
grant select on table public.account_checklist_tag to authenticated;

create policy account_checklist_tag_select_own
    on public.account_checklist_tag
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy checklist_tag_select_linked
    on public.checklist_tag
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_checklist_tag
            where account_checklist_tag.checklist_id = checklist_tag.checklist_id
                and account_checklist_tag.tag_id = checklist_tag.tag_id
                and account_checklist_tag.account_id = (select auth.uid())
        )
    );

create or replace function public.push_checklist_tags(items jsonb)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    affected_count integer := 0;
    item jsonb;
    item_checklist_id uuid;
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
        item_checklist_id := (item ->> 'checklistId')::uuid;
        item_tag_id := (item ->> 'tagId')::uuid;

        if not exists (
            select 1
            from public.account_checklist
            where account_id = current_account_id
                and checklist_id = item_checklist_id
        ) or not exists (
            select 1
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_tag_id
        ) then
            raise exception 'Checklist tag is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        is_account_linked := exists (
            select 1
            from public.account_checklist_tag
            where account_id = current_account_id
                and checklist_id = item_checklist_id
                and tag_id = item_tag_id
        );

        if exists (
            select 1
            from public.checklist_tag
            where checklist_id = item_checklist_id
                and tag_id = item_tag_id
        ) then
            update public.checklist_tag
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where checklist_id = item_checklist_id
                and tag_id = item_tag_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            is_relation_changed := found;
        else
            insert into public.checklist_tag (
                checklist_id,
                tag_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_checklist_id,
                item_tag_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            );

            is_relation_changed := true;
        end if;

        if not is_account_linked then
            insert into public.account_checklist_tag (account_id, checklist_id, tag_id, usn)
            values (
                current_account_id,
                item_checklist_id,
                item_tag_id,
                public.next_account_usn(current_account_id)
            );
        end if;

        if is_relation_changed then
            for linked_account_id in
                select account_id
                from public.account_checklist_tag
                where checklist_id = item_checklist_id
                    and tag_id = item_tag_id
                order by account_id
            loop
                update public.account_checklist_tag
                set usn = public.next_account_usn(linked_account_id)
                where account_id = linked_account_id
                    and checklist_id = item_checklist_id
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

create or replace function public.pull_checklist_tags(usn_cursor bigint)
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
            account_checklist_tag.usn as usn,
            jsonb_build_object(
                'checklistTag', jsonb_build_object(
                    'checklistId', checklist_tag.checklist_id,
                    'tagId', checklist_tag.tag_id,
                    'isDeleted', checklist_tag.is_deleted,
                    'updatedAt', checklist_tag.updated_at,
                    'createdAt', checklist_tag.created_at
                ),
                'usn', account_checklist_tag.usn
            ) as item
        from public.account_checklist_tag
        inner join public.checklist_tag
            on checklist_tag.checklist_id = account_checklist_tag.checklist_id
                and checklist_tag.tag_id = account_checklist_tag.tag_id
        where account_checklist_tag.account_id = current_account_id
            and account_checklist_tag.usn > usn_cursor
        order by account_checklist_tag.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_checklist_tags(jsonb) from public;
revoke all on function public.push_checklist_tags(jsonb) from anon;
grant execute on function public.push_checklist_tags(jsonb) to authenticated;

revoke all on function public.pull_checklist_tags(bigint) from public;
revoke all on function public.pull_checklist_tags(bigint) from anon;
grant execute on function public.pull_checklist_tags(bigint) to authenticated;

-- music_tag
create table public.music_tag (
    music_id   uuid        not null references public.music (id) on delete cascade,
    tag_id     uuid        not null references public.tag (id) on delete cascade,
    is_deleted boolean     not null,
    updated_at timestamptz not null,
    created_at timestamptz not null,
    primary key (music_id, tag_id)
);

create index music_tag_tag_id_idx on public.music_tag (tag_id);

create table public.account_music_tag (
    account_id uuid        not null references public.account (id) on delete cascade,
    music_id   uuid        not null,
    tag_id     uuid        not null,
    usn        bigint      not null,
    primary key (account_id, music_id, tag_id),
    foreign key (music_id, tag_id) references public.music_tag (music_id, tag_id) on delete cascade
);

create index account_music_tag_music_id_tag_id_idx on public.account_music_tag (music_id, tag_id);
create index account_music_tag_account_id_usn_idx on public.account_music_tag (account_id, usn);

alter table public.music_tag enable row level security;
alter table public.account_music_tag enable row level security;

grant select on table public.music_tag to authenticated;
grant select on table public.account_music_tag to authenticated;

create policy account_music_tag_select_own
    on public.account_music_tag
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy music_tag_select_linked
    on public.music_tag
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_music_tag
            where account_music_tag.music_id = music_tag.music_id
                and account_music_tag.tag_id = music_tag.tag_id
                and account_music_tag.account_id = (select auth.uid())
        )
    );

create or replace function public.push_music_tags(items jsonb)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    affected_count integer := 0;
    item jsonb;
    item_music_id uuid;
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
        item_music_id := (item ->> 'musicId')::uuid;
        item_tag_id := (item ->> 'tagId')::uuid;

        if not exists (
            select 1
            from public.account_music
            where account_id = current_account_id
                and music_id = item_music_id
        ) or not exists (
            select 1
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_tag_id
        ) then
            raise exception 'Music tag is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        is_account_linked := exists (
            select 1
            from public.account_music_tag
            where account_id = current_account_id
                and music_id = item_music_id
                and tag_id = item_tag_id
        );

        if exists (
            select 1
            from public.music_tag
            where music_id = item_music_id
                and tag_id = item_tag_id
        ) then
            update public.music_tag
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where music_id = item_music_id
                and tag_id = item_tag_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            is_relation_changed := found;
        else
            insert into public.music_tag (
                music_id,
                tag_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_music_id,
                item_tag_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            );

            is_relation_changed := true;
        end if;

        if not is_account_linked then
            insert into public.account_music_tag (account_id, music_id, tag_id, usn)
            values (
                current_account_id,
                item_music_id,
                item_tag_id,
                public.next_account_usn(current_account_id)
            );
        end if;

        if is_relation_changed then
            for linked_account_id in
                select account_id
                from public.account_music_tag
                where music_id = item_music_id
                    and tag_id = item_tag_id
                order by account_id
            loop
                update public.account_music_tag
                set usn = public.next_account_usn(linked_account_id)
                where account_id = linked_account_id
                    and music_id = item_music_id
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

create or replace function public.pull_music_tags(usn_cursor bigint)
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
            account_music_tag.usn as usn,
            jsonb_build_object(
                'musicTag', jsonb_build_object(
                    'musicId', music_tag.music_id,
                    'tagId', music_tag.tag_id,
                    'isDeleted', music_tag.is_deleted,
                    'updatedAt', music_tag.updated_at,
                    'createdAt', music_tag.created_at
                ),
                'usn', account_music_tag.usn
            ) as item
        from public.account_music_tag
        inner join public.music_tag
            on music_tag.music_id = account_music_tag.music_id
                and music_tag.tag_id = account_music_tag.tag_id
        where account_music_tag.account_id = current_account_id
            and account_music_tag.usn > usn_cursor
        order by account_music_tag.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_music_tags(jsonb) from public;
revoke all on function public.push_music_tags(jsonb) from anon;
grant execute on function public.push_music_tags(jsonb) to authenticated;

revoke all on function public.pull_music_tags(bigint) from public;
revoke all on function public.pull_music_tags(bigint) from anon;
grant execute on function public.pull_music_tags(bigint) to authenticated;

-- qr_tag
create table public.qr_tag (
    qr_id      uuid        not null references public.qr (id) on delete cascade,
    tag_id     uuid        not null references public.tag (id) on delete cascade,
    is_deleted boolean     not null,
    updated_at timestamptz not null,
    created_at timestamptz not null,
    primary key (qr_id, tag_id)
);

create index qr_tag_tag_id_idx on public.qr_tag (tag_id);

create table public.account_qr_tag (
    account_id uuid        not null references public.account (id) on delete cascade,
    qr_id      uuid        not null,
    tag_id     uuid        not null,
    usn        bigint      not null,
    primary key (account_id, qr_id, tag_id),
    foreign key (qr_id, tag_id) references public.qr_tag (qr_id, tag_id) on delete cascade
);

create index account_qr_tag_qr_id_tag_id_idx on public.account_qr_tag (qr_id, tag_id);
create index account_qr_tag_account_id_usn_idx on public.account_qr_tag (account_id, usn);

alter table public.qr_tag enable row level security;
alter table public.account_qr_tag enable row level security;

grant select on table public.qr_tag to authenticated;
grant select on table public.account_qr_tag to authenticated;

create policy account_qr_tag_select_own
    on public.account_qr_tag
    for select
    to authenticated
    using ((select auth.uid()) = account_id);

create policy qr_tag_select_linked
    on public.qr_tag
    for select
    to authenticated
    using (
        exists (
            select 1
            from public.account_qr_tag
            where account_qr_tag.qr_id = qr_tag.qr_id
                and account_qr_tag.tag_id = qr_tag.tag_id
                and account_qr_tag.account_id = (select auth.uid())
        )
    );

create or replace function public.push_qr_tags(items jsonb)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    affected_count integer := 0;
    item jsonb;
    item_qr_id uuid;
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
        item_qr_id := (item ->> 'qrId')::uuid;
        item_tag_id := (item ->> 'tagId')::uuid;

        if not exists (
            select 1
            from public.account_qr
            where account_id = current_account_id
                and qr_id = item_qr_id
        ) or not exists (
            select 1
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_tag_id
        ) then
            raise exception 'QR tag is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        is_account_linked := exists (
            select 1
            from public.account_qr_tag
            where account_id = current_account_id
                and qr_id = item_qr_id
                and tag_id = item_tag_id
        );

        if exists (
            select 1
            from public.qr_tag
            where qr_id = item_qr_id
                and tag_id = item_tag_id
        ) then
            update public.qr_tag
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where qr_id = item_qr_id
                and tag_id = item_tag_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            is_relation_changed := found;
        else
            insert into public.qr_tag (
                qr_id,
                tag_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_qr_id,
                item_tag_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            );

            is_relation_changed := true;
        end if;

        if not is_account_linked then
            insert into public.account_qr_tag (account_id, qr_id, tag_id, usn)
            values (
                current_account_id,
                item_qr_id,
                item_tag_id,
                public.next_account_usn(current_account_id)
            );
        end if;

        if is_relation_changed then
            for linked_account_id in
                select account_id
                from public.account_qr_tag
                where qr_id = item_qr_id
                    and tag_id = item_tag_id
                order by account_id
            loop
                update public.account_qr_tag
                set usn = public.next_account_usn(linked_account_id)
                where account_id = linked_account_id
                    and qr_id = item_qr_id
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

create or replace function public.pull_qr_tags(usn_cursor bigint)
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
            account_qr_tag.usn as usn,
            jsonb_build_object(
                'qrTag', jsonb_build_object(
                    'qrId', qr_tag.qr_id,
                    'tagId', qr_tag.tag_id,
                    'isDeleted', qr_tag.is_deleted,
                    'updatedAt', qr_tag.updated_at,
                    'createdAt', qr_tag.created_at
                ),
                'usn', account_qr_tag.usn
            ) as item
        from public.account_qr_tag
        inner join public.qr_tag
            on qr_tag.qr_id = account_qr_tag.qr_id
                and qr_tag.tag_id = account_qr_tag.tag_id
        where account_qr_tag.account_id = current_account_id
            and account_qr_tag.usn > usn_cursor
        order by account_qr_tag.usn
        limit 100
    ) as page;

    return result;
end;
$$;

revoke all on function public.push_qr_tags(jsonb) from public;
revoke all on function public.push_qr_tags(jsonb) from anon;
grant execute on function public.push_qr_tags(jsonb) to authenticated;

revoke all on function public.pull_qr_tags(bigint) from public;
revoke all on function public.pull_qr_tags(bigint) from anon;
grant execute on function public.pull_qr_tags(bigint) to authenticated;


-- 지운 지 오래된 연결을 정리하는 대상과 탈퇴 시 지우는 대상에 새 연결 테이블을 더한다.
create or replace function public.purge_deleted_entities(
    retention interval default '60 days',
    batch_size integer default 50000
)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    threshold timestamptz := now() - retention;
    purged_count integer := 0;
    deleted_count integer;
    target_table text;
    purge_tag_id_list uuid[];
    affected_memo_id_list uuid[];
    affected_memo_id uuid;
    linked_account_id uuid;
begin
    foreach target_table in array array[
        'memo_tag',
        'memo_place',
        'memo_web',
        'memo_contact',
        'tag_link',
        'web_tag',
        'place_tag',
        'contact_tag',
        'checklist_tag',
        'music_tag',
        'qr_tag',
        'memo',
        'place',
        'web',
        'contact',
        'music',
        'qr',
        'checklist_item',
        'checklist'
    ]
    loop
        execute format(
            'delete from public.%1$I where ctid in ('
            || 'select ctid from public.%1$I where is_deleted and updated_at < $1 limit $2'
            || ')',
            target_table
        ) using threshold, batch_size;

        get diagnostics deleted_count = row_count;
        purged_count := purged_count + deleted_count;
    end loop;

    select array_agg(target.id)
    into purge_tag_id_list
    from (
        select id
        from public.tag
        where is_deleted
            and updated_at < threshold
        limit batch_size
    ) as target;

    if purge_tag_id_list is null then
        return purged_count;
    end if;

    -- 태그 행을 지우면 외래 키가 memo.primary_tag_id를 null로 바꾸지만 순번을 올리지 않아 기기가 해제를
    -- 내려받지 못한다. 순번을 올려 정상 변경으로 전달하기 위해 지우기 전에 직접 해제한다.
    select array_agg(id)
    into affected_memo_id_list
    from public.memo
    where primary_tag_id = any (purge_tag_id_list);

    if affected_memo_id_list is not null then
        update public.memo
        set primary_tag_id = null
        where id = any (affected_memo_id_list);

        for affected_memo_id, linked_account_id in
            select account_memo.memo_id, account_memo.account_id
            from public.account_memo
            where account_memo.memo_id = any (affected_memo_id_list)
            order by account_memo.account_id, account_memo.memo_id
        loop
            update public.account_memo
            set usn = public.next_account_usn(linked_account_id)
            where account_id = linked_account_id
                and memo_id = affected_memo_id;
        end loop;
    end if;

    delete from public.tag
    where id = any (purge_tag_id_list);

    get diagnostics deleted_count = row_count;
    purged_count := purged_count + deleted_count;

    return purged_count;
end;
$$;

create or replace function public.delete_account(account_id uuid)
    returns void
    language plpgsql
    security definer
    set search_path = ''
as $$
begin
    delete from public.memo_tag as target
    using public.account_memo_tag as owned
    where owned.account_id = delete_account.account_id
      and target.memo_id = owned.memo_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_memo_tag as other
          where other.memo_id = target.memo_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.memo_place as target
    using public.account_memo_place as owned
    where owned.account_id = delete_account.account_id
      and target.memo_id = owned.memo_id and target.place_id = owned.place_id
      and not exists (
          select 1
          from public.account_memo_place as other
          where other.memo_id = target.memo_id and other.place_id = target.place_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.memo_web as target
    using public.account_memo_web as owned
    where owned.account_id = delete_account.account_id
      and target.memo_id = owned.memo_id and target.web_id = owned.web_id
      and not exists (
          select 1
          from public.account_memo_web as other
          where other.memo_id = target.memo_id and other.web_id = target.web_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.memo_contact as target
    using public.account_memo_contact as owned
    where owned.account_id = delete_account.account_id
      and target.memo_id = owned.memo_id and target.contact_id = owned.contact_id
      and not exists (
          select 1
          from public.account_memo_contact as other
          where other.memo_id = target.memo_id and other.contact_id = target.contact_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.tag_link as target
    using public.account_tag_link as owned
    where owned.account_id = delete_account.account_id
      and target.from_tag_id = owned.from_tag_id and target.to_tag_id = owned.to_tag_id
      and not exists (
          select 1
          from public.account_tag_link as other
          where other.from_tag_id = target.from_tag_id and other.to_tag_id = target.to_tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.web_tag as target
    using public.account_web_tag as owned
    where owned.account_id = delete_account.account_id
      and target.web_id = owned.web_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_web_tag as other
          where other.web_id = target.web_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.place_tag as target
    using public.account_place_tag as owned
    where owned.account_id = delete_account.account_id
      and target.place_id = owned.place_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_place_tag as other
          where other.place_id = target.place_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.contact_tag as target
    using public.account_contact_tag as owned
    where owned.account_id = delete_account.account_id
      and target.contact_id = owned.contact_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_contact_tag as other
          where other.contact_id = target.contact_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.checklist_tag as target
    using public.account_checklist_tag as owned
    where owned.account_id = delete_account.account_id
      and target.checklist_id = owned.checklist_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_checklist_tag as other
          where other.checklist_id = target.checklist_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.music_tag as target
    using public.account_music_tag as owned
    where owned.account_id = delete_account.account_id
      and target.music_id = owned.music_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_music_tag as other
          where other.music_id = target.music_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.qr_tag as target
    using public.account_qr_tag as owned
    where owned.account_id = delete_account.account_id
      and target.qr_id = owned.qr_id and target.tag_id = owned.tag_id
      and not exists (
          select 1
          from public.account_qr_tag as other
          where other.qr_id = target.qr_id and other.tag_id = target.tag_id
            and other.account_id <> delete_account.account_id
      );

    delete from public.checklist_item as target
    using public.account_checklist_item as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.checklist_item_id
      and not exists (
          select 1
          from public.account_checklist_item as other
          where other.checklist_item_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.checklist as target
    using public.account_checklist as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.checklist_id
      and not exists (
          select 1
          from public.account_checklist as other
          where other.checklist_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.memo as target
    using public.account_memo as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.memo_id
      and not exists (
          select 1
          from public.account_memo as other
          where other.memo_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.tag as target
    using public.account_tag as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.tag_id
      and not exists (
          select 1
          from public.account_tag as other
          where other.tag_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.place as target
    using public.account_place as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.place_id
      and not exists (
          select 1
          from public.account_place as other
          where other.place_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.web as target
    using public.account_web as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.web_id
      and not exists (
          select 1
          from public.account_web as other
          where other.web_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.contact as target
    using public.account_contact as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.contact_id
      and not exists (
          select 1
          from public.account_contact as other
          where other.contact_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.music as target
    using public.account_music as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.music_id
      and not exists (
          select 1
          from public.account_music as other
          where other.music_id = target.id
            and other.account_id <> delete_account.account_id
      );

    delete from public.qr as target
    using public.account_qr as owned
    where owned.account_id = delete_account.account_id
      and target.id = owned.qr_id
      and not exists (
          select 1
          from public.account_qr as other
          where other.qr_id = target.id
            and other.account_id <> delete_account.account_id
      );

    -- 로그인 정보, 연결된 로그인 수단과 세션을 지우면 account와 그에 매달린 행이 cascade로 지워진다.
    delete from auth.users as target
    where target.id = delete_account.account_id;
end;
$$;

revoke execute on function public.delete_account(uuid) from public, anon, authenticated;
grant execute on function public.delete_account(uuid) to service_role;
