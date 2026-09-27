-- 파일마다 사용자가 붙인 제목과 설명을 보관한다. 제목을 보관하기 전에 올린 파일은 파일 이름을 제목으로 삼는다.
alter table public.file add column title text;

update public.file set title = name;

alter table public.file
    alter column title set not null,
    add constraint file_title_check check (btrim(title) <> '');

alter table public.file add column description text not null default '';

-- 제목과 설명을 받지 않던 이전 서버 함수도 그대로 부를 수 있게 둘 다 기본값을 두고, 제목이 없으면 파일 이름을 쓴다.
drop function public.insert_file(uuid, text, text, bigint, text);

create function public.insert_file(
    file_id uuid,
    file_name text,
    file_mime_type text,
    file_size bigint,
    file_path text,
    file_title text default null,
    file_description text default ''
)
    returns jsonb
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    inserted public.file;
begin
    if current_account_id is null then
        raise exception 'Authentication is required.' using errcode = '42501';
    end if;

    -- 경로는 버킷 정책과 같이 계정 폴더 안이어야 다른 계정의 파일을 자기 목록에 등록하지 못한다.
    if file_path is null or split_part(file_path, '/', 1) <> current_account_id::text then
        raise exception 'file_path must be inside the account folder.' using errcode = '22023';
    end if;

    insert into public.file (id, account_id, name, title, description, mime_type, size, path)
    values (
        file_id,
        current_account_id,
        file_name,
        coalesce(file_title, file_name),
        coalesce(file_description, ''),
        file_mime_type,
        file_size,
        file_path
    )
    returning * into inserted;

    return jsonb_build_object(
        'id', inserted.id,
        'name', inserted.name,
        'title', inserted.title,
        'description', inserted.description,
        'mimeType', inserted.mime_type,
        'size', inserted.size,
        'createdAt', inserted.created_at
    );
end;
$$;

revoke all on function public.insert_file(uuid, text, text, bigint, text, text, text) from public;
revoke all on function public.insert_file(uuid, text, text, bigint, text, text, text) from anon;
grant execute on function public.insert_file(uuid, text, text, bigint, text, text, text) to authenticated;

create or replace function public.list_files(
    created_at_cursor timestamptz,
    id_cursor uuid,
    page_size integer
)
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

    if page_size is null or page_size < 1 or page_size > 100 then
        raise exception 'page_size must be between 1 and 100.' using errcode = '22023';
    end if;

    if (created_at_cursor is null) <> (id_cursor is null) then
        raise exception 'created_at_cursor and id_cursor must be set together.' using errcode = '22023';
    end if;

    select coalesce(jsonb_agg(page.item order by page.created_at desc, page.id desc), '[]'::jsonb)
    into result
    from (
        select
            file.created_at as created_at,
            file.id as id,
            jsonb_build_object(
                'id', file.id,
                'name', file.name,
                'title', file.title,
                'description', file.description,
                'mimeType', file.mime_type,
                'size', file.size,
                'createdAt', file.created_at
            ) as item
        from public.file
        where file.account_id = current_account_id
            and (
                created_at_cursor is null
                or (file.created_at, file.id) < (created_at_cursor, id_cursor)
            )
        order by file.created_at desc, file.id desc
        limit page_size
    ) as page;

    return result;
end;
$$;
