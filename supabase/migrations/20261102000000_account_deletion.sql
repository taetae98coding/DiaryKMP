-- 탈퇴한 계정과 그 계정만 연결된 항목·연결을 한 번에 지운다.
-- 다른 계정과도 연결된 항목·연결은 남기고, 계정과의 연결은 account 삭제의 cascade로 지운다.
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
