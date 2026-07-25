-- 프로필 이미지 파일을 담는 버킷. 프로필 이미지는 인증 헤더를 붙일 수 없는
-- 이미지 로더가 URL만으로 바로 읽어야 하므로 공개 버킷으로 둔다.
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
    'profile-image',
    'profile-image',
    true,
    5242880,
    array['image/png', 'image/jpeg', 'image/webp', 'image/gif']
)
on conflict (id) do update
set
    public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

-- 파일 경로의 첫 번째 폴더를 계정 id로 고정해 자기 폴더 밖은 다루지 못하게 한다.
create policy profile_image_select_own
    on storage.objects
    for select
    to authenticated
    using (
        bucket_id = 'profile-image'
        and (storage.foldername(name))[1] = (select auth.uid())::text
    );

create policy profile_image_insert_own
    on storage.objects
    for insert
    to authenticated
    with check (
        bucket_id = 'profile-image'
        and (storage.foldername(name))[1] = (select auth.uid())::text
    );

create policy profile_image_update_own
    on storage.objects
    for update
    to authenticated
    using (
        bucket_id = 'profile-image'
        and (storage.foldername(name))[1] = (select auth.uid())::text
    )
    with check (
        bucket_id = 'profile-image'
        and (storage.foldername(name))[1] = (select auth.uid())::text
    );

create policy profile_image_delete_own
    on storage.objects
    for delete
    to authenticated
    using (
        bucket_id = 'profile-image'
        and (storage.foldername(name))[1] = (select auth.uid())::text
    );

-- account 테이블은 RLS만 켜져 있고 authenticated에 권한이 없으므로
-- 다른 갱신과 같이 security definer 함수로만 프로필 이미지를 바꾼다.
create or replace function public.update_profile_image(image_url text)
    returns text
    language plpgsql
    security definer
    set search_path = ''
as $$
declare
    current_account_id uuid := auth.uid();
    updated_image_url text;
begin
    if current_account_id is null then
        raise exception 'Authentication is required.' using errcode = '42501';
    end if;

    if image_url is null or btrim(image_url) = '' then
        raise exception 'image_url must not be blank.' using errcode = '22023';
    end if;

    update public.account
    set
        profile_image = image_url,
        updated_at = now()
    where id = current_account_id
    returning profile_image into updated_image_url;

    if updated_image_url is null then
        raise exception 'Account does not exist.' using errcode = '23503';
    end if;

    return updated_image_url;
end;
$$;

revoke all on function public.update_profile_image(text) from public;
revoke all on function public.update_profile_image(text) from anon;
grant execute on function public.update_profile_image(text) to authenticated;
