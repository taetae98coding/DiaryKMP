# Supabase 규칙

## 마이그레이션

- dev나 real에 배포한 마이그레이션 파일은 고치지 않는다. 바꿀 내용은 새 마이그레이션으로 더한다. 아직 어디에도 배포하지 않은 마이그레이션은 고치거나 합쳐도 된다.
- 마이그레이션을 더하거나 고쳤으면 배포 전에 `supabase/scripts/plpgsql-check.sh`를 통과시킨다. PL/pgSQL 본문은 함수를 만들 때가 아니라 처음 실행할 때 검사되므로, 배포가 성공해도 함수가 동작한다는 뜻이 아니다.

## PL/pgSQL 함수

- 파라미터나 `returns table`의 컬럼이 본문에서 쓰는 테이블의 컬럼과 이름이 같으면, 본문의 SQL 문에서 파라미터는 `함수이름.파라미터`로, 컬럼은 `테이블.컬럼`으로 한정한다.
- `on conflict`의 충돌 대상 컬럼은 한정할 수 없으므로, 컬럼과 같은 이름의 파라미터가 있으면 `on conflict on constraint <제약 이름>`으로 쓴다.

```sql
create or replace function public.register_fcm_token(token text, time_zone text, language text)
...
    insert into public.fcm_token (token, account_id, time_zone, language)
    values (register_fcm_token.token, current_account_id, register_fcm_token.time_zone, register_fcm_token.language)
    on conflict on constraint fcm_token_pkey do update
```

- `security definer` 함수에는 `set search_path = ''`를 두고 모든 객체를 스키마로 한정한다.
- 함수를 만든 뒤 `public`과 `anon`의 실행 권한을 회수하고, 호출할 역할에만 `grant execute`한다. Supabase는 `public` 스키마의 새 함수에 `anon`과 `authenticated` 실행 권한을 기본으로 준다.

## Edge Function 인증

- 앱은 세션이 없으면 `apikey` 헤더만 보내고 `Authorization`을 붙이지 않는다. 세션 없이 받는 처리는 `createPublicClient`로, 로그인 세션이 필요한 처리는 `createUserClient`로 데이터베이스를 부른다.
- 함수를 직접 호출해 확인할 때는 앱과 같은 헤더를 쓴다. 게스트 요청은 `apikey`만, 사용자 요청은 `apikey`와 사용자 세션의 `Authorization`을 보낸다. `Authorization: Bearer <apikey>`를 직접 붙이면 앱에서는 실패하는 요청이 성공한다.
