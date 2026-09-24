---
name: supabase-push
description: Diary KMP 프로젝트에서 사용자가 Supabase config push, Supabase 설정 배포, "supabase push"를 요청할 때 사용한다. dev/real flavor를 반드시 명시적으로 확인하고, local.properties와 flavor별 iOS xcconfig에서 Google·Apple OAuth 값을 읽어 config.toml의 env(...) 값을 비어 있지 않은 프로세스 환경으로 새로 구성한 뒤 올바른 Supabase 프로젝트에 push한다.
---

# Supabase Push

## Flavor 확인

현재 사용자 요청에 `dev` 또는 `real`이 명시되어 있는지 먼저 확인한다.

- 둘 중 하나가 명시되어 있으면 해당 flavor를 사용한다.
- flavor가 없거나 둘 다 언급되어 대상이 모호하면 `dev와 real 중 어느 flavor로 push할까요?`라고 질문하고 실행을 중단한다.
- linked project, `config.toml`의 기본 `project_id`, 현재 브랜치, 이전 대화만으로 flavor를 추론하지 않는다.
- `dev`, `real` 외의 flavor를 임의로 매핑하지 않는다.

사용자가 flavor와 함께 push를 명시적으로 요청했다면 추가 확인 없이 실행한다.

## 실행

저장소 루트에서 다음 helper를 실행한다.

```shell
python3 DIARY_AGENTS/skills/supabase-push/scripts/push_supabase_config.py --flavor dev
```

`real` 요청에는 `--flavor real`을 사용한다. 진단만 요청받은 경우에만 `--dry-run`을 추가한다. 실제 push 요청을 `--dry-run`으로 대신하지 않는다.

helper는 다음을 보장한다.

- dev는 루트 `project_id`, real은 `[remotes.real].project_id`를 읽고 기대 project ref와 대조한다.
- 항상 `supabase config push --project-ref <ref>`를 실행해 linked project에 의존하지 않는다.
- `local.properties`와 선택한 `iosApp/Config/DevLocal.xcconfig` 또는 `RealLocal.xcconfig`를 shell로 source하지 않고 데이터로 파싱한다.
- `config.toml`의 활성 `env(...)` 참조를 검사하고, 지원하지 않는 참조나 누락·공백 값이 있으면 Supabase CLI 호출 전에 중단한다.
- 상속받은 동일 이름의 환경변수를 제거하고 현재 child process용 값으로 새로 설정한다. `.env`, 임시 secret 파일, shell history 또는 `config.toml`에는 값을 쓰지 않는다.
- secret과 client ID 값을 stdout/stderr 또는 명령 인자에 출력하지 않는다.

## 값 구성

Google provider client ID 목록은 중복을 제거해 다음 순서로 구성한다.

1. `<flavor>.wasm.googleCredentialsClientId` 웹 client ID
2. 선택한 xcconfig의 `GID_CLIENT_ID` iOS client ID
3. `<flavor>.jvm.googleCredentialsClientId` JVM client ID

웹 client ID가 `<flavor>.android.googleCredentialsServerClientId` 및 xcconfig의 `GID_SERVER_CLIENT_ID`와 모두 같은지 검증한다. `GID_REVERSED_CLIENT_ID`는 URL scheme 값이므로 목록에 넣지 않는다.

`SUPABASE_AUTH_EXTERNAL_GOOGLE_CLIENT_SECRET`에는 첫 번째 웹 client ID에 대응하는 `<flavor>.wasm.googleSecret`만 사용한다. 다른 flavor의 secret이나 기존 process environment 값으로 대체하지 않는다. 키가 없거나 비어 있으면 필요한 키 이름만 알리고 push를 중단한다.

Apple provider 값은 `local.properties`에서만 읽는다.

- `SUPABASE_AUTH_EXTERNAL_APPLE_CLIENT_ID`: `<flavor>.apple.clientIds`의 콤마 구분 목록 뒤에 `<flavor>.apple.webClientId`를 이어 붙이고 중복을 제거해 사용한다. 네이티브 Sign in with Apple은 `id_token`의 `aud`가 앱 bundle id이므로 해당 flavor의 bundle id를 포함하고, Android·JVM·WASM의 웹 Sign in with Apple은 `aud`가 Services ID이므로 `webClientId`를 포함한다. 각 항목은 reverse-DNS 형식이어야 한다.

키가 없거나 비어 있으면 필요한 키 이름만 알리고 push를 중단한다.

Apple secret은 웹 OAuth 흐름 전용이라 `config.toml`에 선언하지 않는다. CLI 설정 검증도 apple·google provider는 secret 필수 검사에서 제외하므로 별도 키를 요구하지 않는다.

`<flavor>.jvm.googleSecret`과 Edge Function의 `GOOGLE_CLIENT_SECRETS`는 authorization-code 교환용 별도 설정이다. 사용자가 별도로 요청하지 않으면 이 스킬에서 `supabase secrets set`, DB push, migration push 또는 Function deploy를 실행하지 않는다.

## 결과 보고

성공 시 flavor와 project ref, push 성공 여부만 보고한다. 실패 시 누락된 파일·키 또는 검증 실패 이유를 값 없이 보고한다. local property, xcconfig, 생성된 환경변수의 실제 값은 어떤 경우에도 답변에 포함하지 않는다.
