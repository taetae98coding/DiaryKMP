-- 곡은 들을 수 있는 YouTube 영상 링크와 그 영상의 썸네일 주소를 함께 갖는다.
-- 링크가 YouTube 영상 주소인지는 앱이 판정하므로 서버 스키마에서는 형식만 고정한다.
-- 썸네일은 불러오기로만 채워지는 선택 값이고, 이 마이그레이션 이전에 추가된 곡은 두 값이 없으므로 빈 문자열로 남는다.
alter table public.music
    add column link text not null default '',
    add column thumbnail text not null default '';

create or replace function public.push_musics(items jsonb)
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
            from public.account_music
            where account_id = current_account_id
                and music_id = item_id
        ) then
            update public.music
            set
                link = item -> 'detail' ->> 'link',
                title = item -> 'detail' ->> 'title',
                artist = item -> 'detail' ->> 'artist',
                thumbnail = item -> 'detail' ->> 'thumbnail',
                is_deleted = (item ->> 'isDeleted')::boolean,
                updated_at = (item ->> 'updatedAt')::timestamptz
            where id = item_id
                and (item ->> 'updatedAt')::timestamptz > updated_at;

            if found then
                for linked_account_id in
                    select account_id
                    from public.account_music
                    where music_id = item_id
                    order by account_id
                loop
                    update public.account_music
                    set usn = public.next_account_usn(linked_account_id)
                    where account_id = linked_account_id
                        and music_id = item_id;
                end loop;

                affected_count := affected_count + 1;
            end if;
        else
            inserted_id := null;

            insert into public.music (
                id,
                link,
                title,
                artist,
                thumbnail,
                is_deleted,
                updated_at,
                created_at
            )
            values (
                item_id,
                item -> 'detail' ->> 'link',
                item -> 'detail' ->> 'title',
                item -> 'detail' ->> 'artist',
                item -> 'detail' ->> 'thumbnail',
                (item ->> 'isDeleted')::boolean,
                (item ->> 'updatedAt')::timestamptz,
                (item ->> 'createdAt')::timestamptz
            )
            on conflict (id) do nothing
            returning id into inserted_id;

            if inserted_id is null then
                raise exception 'Music is not linked to the authenticated account.'
                    using errcode = '42501';
            end if;

            insert into public.account_music (account_id, music_id, usn)
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

create or replace function public.pull_musics(usn_cursor bigint)
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
            account_music.usn as usn,
            jsonb_build_object(
                'music', jsonb_build_object(
                    'id', music.id,
                    'detail', jsonb_build_object(
                        'link', music.link,
                        'title', music.title,
                        'artist', music.artist,
                        'thumbnail', music.thumbnail
                    ),
                    'isDeleted', music.is_deleted,
                    'updatedAt', music.updated_at,
                    'createdAt', music.created_at
                ),
                'usn', account_music.usn
            ) as item
        from public.account_music
        inner join public.music on music.id = account_music.music_id
        where account_music.account_id = current_account_id
            and account_music.usn > usn_cursor
        order by account_music.usn
        limit 100
    ) as page;

    return result;
end;
$$;
