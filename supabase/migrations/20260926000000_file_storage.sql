-- 사용자가 올린 파일의 내용을 담는 버킷. 파일은 올린 계정만 다루므로 비공개로 두고,
-- 형식은 거르지 않으며 한 파일의 크기만 50MB로 제한한다.
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
    'file',
    'file',
    false,
    52428800,
    null
)
on conflict (id) do update
set
    public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

-- 파일 경로의 첫 번째 폴더를 계정 id로 고정해 자기 폴더 밖은 다루지 못하게 한다.
create policy file_select_own
    on storage.objects
    for select
    to authenticated
    using (
        bucket_id = 'file'
        and (storage.foldername(name))[1] = (select auth.uid())::text
    );

create policy file_insert_own
    on storage.objects
    for insert
    to authenticated
    with check (
        bucket_id = 'file'
        and (storage.foldername(name))[1] = (select auth.uid())::text
    );

create policy file_delete_own
    on storage.objects
    for delete
    to authenticated
    using (
        bucket_id = 'file'
        and (storage.foldername(name))[1] = (select auth.uid())::text
    );

-- 목록에 보이는 파일은 이 표에 등록된 것뿐이다. 버킷에 내용만 남은 파일은 목록에 나타나지 않는다.
-- 같은 이름을 여러 번 올릴 수 있으므로 이름에는 유일 제약을 두지 않는다.
create table public.file (
    id         uuid        primary key,
    account_id uuid        not null references public.account (id) on delete cascade,
    name       text        not null check (btrim(name) <> '' and char_length(name) <= 255),
    mime_type  text        not null,
    size       bigint      not null check (size >= 0 and size <= 52428800),
    path       text        not null unique,
    created_at timestamptz not null default now()
);

create index file_account_id_created_at_id_idx on public.file (account_id, created_at desc, id desc);

alter table public.file enable row level security;

revoke all on table public.file from anon;
revoke all on table public.file from authenticated;

create or replace function public.insert_file(
    file_id uuid,
    file_name text,
    file_mime_type text,
    file_size bigint,
    file_path text
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

    insert into public.file (id, account_id, name, mime_type, size, path)
    values (file_id, current_account_id, file_name, file_mime_type, file_size, file_path)
    returning * into inserted;

    return jsonb_build_object(
        'id', inserted.id,
        'name', inserted.name,
        'mimeType', inserted.mime_type,
        'size', inserted.size,
        'createdAt', inserted.created_at
    );
end;
$$;

revoke all on function public.insert_file(uuid, text, text, bigint, text) from public;
revoke all on function public.insert_file(uuid, text, text, bigint, text) from anon;
grant execute on function public.insert_file(uuid, text, text, bigint, text) to authenticated;

-- 마지막으로 받은 파일의 (올린 시각, id)를 기준으로 그다음을 돌려준다. 그 사이에 새 파일이 올라와도
-- 앞쪽에 쌓일 뿐이라 이미 받은 파일이 다시 오거나 받지 않은 파일이 빠지지 않는다.
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

revoke all on function public.list_files(timestamptz, uuid, integer) from public;
revoke all on function public.list_files(timestamptz, uuid, integer) from anon;
grant execute on function public.list_files(timestamptz, uuid, integer) to authenticated;
