-- 파일 하나의 정보를 돌려준다. 다른 계정의 파일과 없는 파일을 구분하지 않도록 둘 다 null을 돌려준다.
create or replace function public.get_file(file_id uuid)
    returns jsonb
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    file_row public.file;
begin
    if current_account_id is null then
        raise exception 'Authentication is required.' using errcode = '42501';
    end if;

    select *
    into file_row
    from public.file
    where file.id = get_file.file_id
        and file.account_id = current_account_id;

    if not found then
        return null;
    end if;

    return jsonb_build_object(
        'id', file_row.id,
        'name', file_row.name,
        'title', file_row.title,
        'description', file_row.description,
        'mimeType', file_row.mime_type,
        'size', file_row.size,
        'createdAt', file_row.created_at
    );
end;
$$;

revoke all on function public.get_file(uuid) from public;
revoke all on function public.get_file(uuid) from anon;
grant execute on function public.get_file(uuid) to authenticated;

-- 이름, 제목, 설명을 한 번에 바꾼다. 내용과 보관 위치, 형식, 크기, 올린 시각은 바꾸지 않는다.
-- 다른 계정의 파일과 없는 파일은 get_file과 같이 null을 돌려준다.
create or replace function public.update_file(
    file_id uuid,
    file_name text,
    file_title text,
    file_description text
)
    returns jsonb
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    updated public.file;
begin
    if current_account_id is null then
        raise exception 'Authentication is required.' using errcode = '42501';
    end if;

    if file_name is null or btrim(file_name) = '' or char_length(file_name) > 255 then
        raise exception 'file_name must not be blank and must be at most 255 characters.' using errcode = '22023';
    end if;

    if file_title is null or btrim(file_title) = '' then
        raise exception 'file_title must not be blank.' using errcode = '22023';
    end if;

    update public.file
    set
        name = update_file.file_name,
        title = update_file.file_title,
        description = coalesce(update_file.file_description, '')
    where file.id = update_file.file_id
        and file.account_id = current_account_id
    returning * into updated;

    if not found then
        return null;
    end if;

    return jsonb_build_object(
        'id', updated.id,
        'name', updated.name,
        'title', updated.title,
        'description', updated.description,
        'mimeType', updated.mime_type,
        'size', updated.size,
        'createdAt', updated.created_at
    );
end;
$$;

revoke all on function public.update_file(uuid, text, text, text) from public;
revoke all on function public.update_file(uuid, text, text, text) from anon;
grant execute on function public.update_file(uuid, text, text, text) to authenticated;

-- 내려받을 임시 주소를 만들 보관 위치를 돌려준다. 앱에는 위치를 알리지 않고 서버 함수만 쓴다.
create or replace function public.get_file_path(file_id uuid)
    returns text
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    found_path text;
begin
    if current_account_id is null then
        raise exception 'Authentication is required.' using errcode = '42501';
    end if;

    select file.path
    into found_path
    from public.file
    where file.id = get_file_path.file_id
        and file.account_id = current_account_id;

    return found_path;
end;
$$;

revoke all on function public.get_file_path(uuid) from public;
revoke all on function public.get_file_path(uuid) from anon;
grant execute on function public.get_file_path(uuid) to authenticated;
