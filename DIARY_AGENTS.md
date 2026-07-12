# Diary KMP 지침

이 문서는 에이전트 지침의 단일 진입점이다. 루트의 `AGENTS.md`, `CLAUDE.md`는 이 문서를 가리키는 심볼릭 링크이고, `.claude/skills`와 `.codex/skills`는 스킬 원본인 `DIARY_AGENTS/skills`를 가리키는 심볼릭 링크다. 지침을 추가할 때 사본을 만들지 않고 원본만 수정한다.

이 문서에는 코드와 프로젝트 구조에서 유추할 수 없는 판단 기준만 둔다. 모듈 구성, 의존성, 소스셋, 적용된 플러그인처럼 `settings.gradle.kts`와 `build-logic`이 이미 표현하는 사실은 여기에 옮겨 적지 않고 코드에서 확인한다. 세부 규칙과 절차는 아래 표의 문서가 소유하며, 이 문서는 어디를 읽어야 하는지만 알려준다.

## 사용자 확인

- 요청이 잘못된 정보나 오래된 지식에 기반한다고 판단되면, 잘못된 점을 설명하고 다음 지시를 받은 뒤 진행한다.
- 구현 방향에 명확한 trade-off가 있으면, 주요 선택지의 장단점을 설명하고 사용자의 선택을 받은 뒤 진행한다.
- 스펙, 디자인, 테스트 케이스에 공백이나 충돌이 있으면 추정으로 메우지 않고 결정을 받은 뒤 진행한다.

## 작업 iterator

사용자가 작업을 지시하면 별도 확인 없이 다음 순서로 진행한다. 각 단계는 앞 단계의 산출물이 확정된 뒤에 시작한다.

`spec-wave` → `design-wave` → `testcase-wave` → 구현·수정 → `testcode-wave` → `verify-wave`

- 변경 대상에 따라 시작 단계가 달라진다. 스펙이 바뀌면 `spec-wave`부터, 코드만 바뀌면 `testcode-wave`부터 시작한다.
- 해당하지 않는 단계는 건너뛴다.
- 진행 중 앞 단계의 산출물이 바뀌면 그 단계로 돌아가 뒤 단계를 다시 수행한다.
- 코드를 변경했다면 `verify-wave`는 반드시 수행한다.
- 각 단계의 절차, 판단 기준, 완료 조건은 해당 스킬 문서가 소유한다. 각 스킬은 다른 스킬의 수행 순서를 다시 적지 않고, 순서는 이 문서가 소유한다.
- 문서를 다루는 wave가 공통으로 따르는 절차는 [wave.md](DIARY_AGENTS/wave.md)가 소유한다.

## 종료 iterator

사용자가 커밋, 머지 또는 작업 종료를 요청했을 때만 다음 순서로 진행한다.

`rebase-wave` → `commit-wave` → `merge-wave`

- 작업 iterator가 끝났다고 이어서 자동으로 시작하지 않는다. 사용자의 1차 검증이 끝난 뒤 요청을 받고 시작한다.
- 요청 범위 밖의 단계는 수행하지 않는다. 커밋만 요청받으면 `rebase-wave`와 `merge-wave`를 수행하지 않는다.
- `rebase-wave`가 컨플릭 없이 끝나면 별도 확인 없이 `commit-wave`와 `merge-wave`까지 이어서 수행한다.
- `rebase-wave`에서 컨플릭을 해결했으면 그 결과를 보고하고 사용자 승인을 받은 뒤에만 `commit-wave`로 넘어간다.
- 코드가 바뀐 뒤 `verify-wave`를 통과하지 않았으면 이 iterator를 시작하지 않고 작업 iterator로 돌아가 검증부터 마친다.

## 코드 규칙

**코드를 작성하거나 리뷰하기 전에 변경 대상에 해당하는 규칙 문서를 읽는다.** 규칙 본문은 각 문서가 소유하고, 이 표에는 라우팅만 둔다.

| 변경 대상 | 규칙 문서 |
| --- | --- |
| 모든 Kotlin 코드 | [kotlin.md](DIARY_AGENTS/rules/kotlin.md) |
| Composable, UI Event·Effect, 레이아웃 | [compose.md](DIARY_AGENTS/rules/compose.md) |
| ViewModel | [viewmodel.md](DIARY_AGENTS/rules/viewmodel.md) |
| `:domain:*`의 UseCase와 Repository 인터페이스 | [domain.md](DIARY_AGENTS/rules/domain.md) |
| `:data:*`와 DataSource | [data.md](DIARY_AGENTS/rules/data.md) |
| Room Entity, DAO, `@Query` | [room.md](DIARY_AGENTS/rules/room.md) |
| `build.gradle.kts`와 버전 카탈로그 | [gradle.md](DIARY_AGENTS/rules/gradle.md) |

## 제품 문서

제품 문서는 `docs`에 두고, 문서 종류별 소유 범위와 목록은 [docs/README.md](docs/README.md)가 소유한다. 문서를 읽거나 쓰기 전에 그 문서를 먼저 확인한다.

같은 내용을 두 문서에 적지 않고 소유한 문서를 링크한다. 문서가 충돌하면 스펙을 기준으로 한다.

## 참고 우선순위

아키텍처 판단과 코드 작성의 근거는 다음 순서로 삼고, 기준이 서로 충돌하면 앞선 항목을 우선한다.

1. [Android Developers](https://developer.android.com/) — Android, Compose, Kotlin, Gradle, 앱 아키텍처의 공식 권장사항
2. [Now in Android](https://github.com/android/nowinandroid) — 앱 아키텍처, 모듈 구성, 네이밍, Gradle 설정, 코드 스타일
3. [DroidKaigi conference-app-2026](https://github.com/DroidKaigi/conference-app-2026) — Android·iOS·JVM·wasmJs를 대상으로 하는 KMP 모듈 구성과 Compose UI 구성
4. [KotlinConf App](https://github.com/JetBrains/kotlinconf-app) — 플랫폼별 앱 구성과 공유 UI 구성
