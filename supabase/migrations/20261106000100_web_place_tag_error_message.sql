-- 웹 항목·장소 태그 연결의 소유 검사 오류 문구에 섞인 한국어를 다른 함수와 같은 영어 문구로 바꾼다.
-- 동작은 바꾸지 않으며, create or replace는 기존 실행 권한을 유지한다.

create or replace function public.push_web_tags(items jsonb)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    affected_count integer := 0;
    item jsonb;
    item_web_id uuid;
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
        item_web_id := (item ->> 'webId')::uuid;
        item_tag_id := (item ->> 'tagId')::uuid;

        if not exists (
            select 1
            from public.account_web
            where account_id = current_account_id
                and web_id = item_web_id
        ) or not exists (
            select 1
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_tag_id
        ) then
            raise exception 'Web tag is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        is_account_linked := exists (
            select 1
            from public.account_web_tag
            where account_id = current_account_id
                and web_id = item_web_id
                and tag_id = item_tag_id
        );

        if exists (
            select 1
            from public.web_tag
            where web_id = item_web_id
                and tag_id = item_tag_id
        ) then
            update public.web_tag
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where web_id = item_web_id
                and tag_id = item_tag_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            is_relation_changed := found;
        else
            insert into public.web_tag (
                web_id,
                tag_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_web_id,
                item_tag_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            );

            is_relation_changed := true;
        end if;

        if not is_account_linked then
            insert into public.account_web_tag (account_id, web_id, tag_id, usn)
            values (
                current_account_id,
                item_web_id,
                item_tag_id,
                public.next_account_usn(current_account_id)
            );
        end if;

        if is_relation_changed then
            for linked_account_id in
                select account_id
                from public.account_web_tag
                where web_id = item_web_id
                    and tag_id = item_tag_id
                order by account_id
            loop
                update public.account_web_tag
                set usn = public.next_account_usn(linked_account_id)
                where account_id = linked_account_id
                    and web_id = item_web_id
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

create or replace function public.push_place_tags(items jsonb)
    returns integer
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    affected_count integer := 0;
    item jsonb;
    item_place_id uuid;
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
        item_place_id := (item ->> 'placeId')::uuid;
        item_tag_id := (item ->> 'tagId')::uuid;

        if not exists (
            select 1
            from public.account_place
            where account_id = current_account_id
                and place_id = item_place_id
        ) or not exists (
            select 1
            from public.account_tag
            where account_id = current_account_id
                and tag_id = item_tag_id
        ) then
            raise exception 'Place tag is not linked to the authenticated account.'
                using errcode = '42501';
        end if;

        is_account_linked := exists (
            select 1
            from public.account_place_tag
            where account_id = current_account_id
                and place_id = item_place_id
                and tag_id = item_tag_id
        );

        if exists (
            select 1
            from public.place_tag
            where place_id = item_place_id
                and tag_id = item_tag_id
        ) then
            update public.place_tag
            set
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where place_id = item_place_id
                and tag_id = item_tag_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            is_relation_changed := found;
        else
            insert into public.place_tag (
                place_id,
                tag_id,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_place_id,
                item_tag_id,
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            );

            is_relation_changed := true;
        end if;

        if not is_account_linked then
            insert into public.account_place_tag (account_id, place_id, tag_id, usn)
            values (
                current_account_id,
                item_place_id,
                item_tag_id,
                public.next_account_usn(current_account_id)
            );
        end if;

        if is_relation_changed then
            for linked_account_id in
                select account_id
                from public.account_place_tag
                where place_id = item_place_id
                    and tag_id = item_tag_id
                order by account_id
            loop
                update public.account_place_tag
                set usn = public.next_account_usn(linked_account_id)
                where account_id = linked_account_id
                    and place_id = item_place_id
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
