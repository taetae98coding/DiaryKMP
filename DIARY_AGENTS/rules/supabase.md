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

## 동기화 계약의 버전

이미 출시된 앱은 업데이트하기 전까지 자신이 아는 Edge Function과 요청 형식을 계속 쓴다. 계약을 바꿀 때는 출시된 모든 버전의 앱이 계속 동작해야 한다.

**기존 버전을 확장할 수 있으면 확장하고, 확장할 수 없을 때만 새 버전을 만든다.** 버전이 늘 때마다 Edge Function, DB 함수, 요청 스키마가 한 벌씩 늘어 같은 처리를 여러 곳에서 고쳐야 하기 때문이다.

### 확장할 수 있는 경우

다음을 모두 만족하면 같은 버전의 Edge Function, DB 함수, 요청 스키마를 고친다.

- 새 정보가 응답에만 더해지거나, 요청에서 빠져도 서버가 정할 수 있다. 출시된 앱은 supabase-kt의 `ignoreUnknownKeys = true`로 응답의 모르는 키를 무시한다.
- 요청에 키가 없을 때의 처리가 정해져 있다. 새로 만들 때는 컬럼 기본값을 쓰고, 이미 있는 행을 고칠 때는 저장된 값을 바꾸지 않는다. 키가 없을 때 기본값으로 덮어쓰면 이전 앱이 항목을 고칠 때 새 앱이 기록한 값이 지워진다.
- 키가 있을 때의 검사가 이전 앱의 요청을 거절하지 않는다.

```sql
-- 고칠 때: 키가 없으면 저장된 값을 유지한다.
how_we_met = coalesce(item -> 'detail' ->> 'howWeMet', how_we_met),
-- 새로 만들 때: 키가 없으면 컬럼 기본값과 같은 값을 쓴다.
coalesce(item -> 'detail' ->> 'howWeMet', ''),
```

요청 스키마에서는 새 키를 `.optional()`로 받는다. 키가 없는 것과 값이 `null`인 것은 다르다. 앱이 `null`로 지운 값을 키 없음으로 처리하지 않도록, 둘을 구분해야 하는 값은 `item -> 'detail' ? 'birthYear'`처럼 키가 있는지로 가른다.

### 확장할 수 없는 경우

다음처럼 이전 앱의 요청이나 응답 해석이 깨지면 새 버전을 만든다.

- 이미 있는 키의 타입이나 의미가 바뀐다.
- 이전 앱이 보내지 않는 키를 반드시 받아야 한다.
- 응답에서 이전 앱이 읽는 키를 빼거나 형식을 바꾼다.

새 버전은 서버에서만 이름을 나눈다. Edge Function은 `v2-sync-push-contact`, DB 함수는 `push_contacts_v2`, `supabase/functions/_shared`의 요청 스키마는 `contactPushRequestSchemaV2`처럼 버전을 붙이고, 하나뿐인 버전의 스키마에는 버전을 붙이지 않는다(`contactPushRequestSchema`). 이전 버전은 이전 앱이 쓰는 동안 남긴다. 그 버전을 쓰는 앱이 내부 테스트 빌드뿐이어서 사용자가 지원을 끝내도 된다고 정하면 지운다.

앱은 버전을 엔티티에 나누지 않는다. `core:network`의 원격 엔티티는 항상 현재 형식 하나(`ContactRemoteEntity`)만 두고, 호출할 함수 이름의 버전만 바꾼다.

### 앱 원격 엔티티의 기본값

서버가 키가 있는지로 이전 앱을 구분하므로, 앱은 요청의 모든 키를 값과 무관하게 보내야 한다. supabase-kt의 Json은 `encodeDefaults = false`라, 원격 엔티티 프로퍼티에 기본값이 있으면 그 값과 같을 때 키를 빼고 보낸다. 그러면 사용자가 지운 값이 키 없음으로 처리되어 서버에 반영되지 않는다.

- 요청에 담는 원격 엔티티의 프로퍼티에는 기본값을 두지 않는다.
- 요청을 직렬화하는 테스트는 빈 문자열과 `null`인 값으로도 모든 키가 남는지 확인한다.
