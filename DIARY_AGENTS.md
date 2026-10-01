# Diary KMP 지침

`AGENTS.md`, `CLAUDE.md`는 이 문서를, `.claude/skills`, `.codex/skills`는 `DIARY_AGENTS/skills`를 가리키는 심볼릭 링크다. 사본을 만들지 않고 원본만 수정한다.

이 문서에는 코드와 `settings.gradle.kts`·`build-logic`에서 유추할 수 없는 판단 기준과 라우팅만 둔다.

## 사용자 확인

다음은 설명하고 사용자의 결정을 받은 뒤 진행한다.

- 요청이 잘못된 정보나 오래된 지식에 기반한 경우
- 구현 방향에 명확한 trade-off가 있는 경우. 주요 선택지의 장단점을 함께 설명한다.
- 스펙, 디자인, 테스트 케이스에 공백이나 충돌이 있는 경우. 추정으로 메우지 않는다.

보고 형식은 [confirm.md](DIARY_AGENTS/confirm.md)를 따른다.

## 작업 iterator

사용자가 작업을 지시하면 별도 확인 없이 다음 순서로 진행한다. 각 단계는 앞 단계의 산출물이 확정된 뒤에 시작한다.

`spec-wave` → `design-wave` → `testcase-wave` → 구현·수정 → `testcode-wave` → `verify-wave`

- 스펙이 바뀌면 `spec-wave`부터, 코드만 바뀌면 `testcode-wave`부터 시작하고, 해당하지 않는 단계는 건너뛴다.
- 진행 중 앞 단계의 산출물이 바뀌면 그 단계로 돌아가 뒤 단계를 다시 수행한다.
- 코드나 스펙·테스트 케이스 문서를 변경했다면 `verify-wave`는 반드시 수행한다.
- 단계별 절차는 각 스킬이, 단계 순서는 이 문서가, 문서 wave의 공통 절차는 [wave.md](DIARY_AGENTS/wave.md)가 소유한다.

## 종료 iterator

사용자가 커밋, 머지 또는 작업 종료를 요청했을 때만 다음 순서로 진행한다.

`rebase-wave` → `commit-wave` → `merge-wave`

- 작업 iterator가 끝나도 자동으로 시작하지 않는다.
- 요청 범위 밖의 단계는 수행하지 않는다. 커밋만 요청받으면 커밋만 한다.
- `rebase-wave`가 컨플릭 없이 끝나면 확인 없이 이어서 수행하고, 컨플릭을 해결했으면 결과를 보고해 승인을 받은 뒤 넘어간다.
- 코드가 바뀐 뒤 `verify-wave`를 통과하지 않았으면 작업 iterator로 돌아가 검증부터 마친다.

## 코드 규칙

**코드를 작성하거나 리뷰하기 전에 변경 대상에 해당하는 규칙 문서를 읽는다.**

| 변경 대상 | 규칙 문서 |
| --- | --- |
| 모든 Kotlin 코드 | [kotlin.md](DIARY_AGENTS/rules/kotlin.md) |
| Composable, UI Event·Effect, 레이아웃 | [compose.md](DIARY_AGENTS/rules/compose.md) |
| ViewModel | [viewmodel.md](DIARY_AGENTS/rules/viewmodel.md) |
| `:domain:*`의 UseCase와 Repository 인터페이스 | [domain.md](DIARY_AGENTS/rules/domain.md) |
| `:core:*`, `:data:*`, `:work:*`의 DataSource·Repository 구현·매퍼 | [data.md](DIARY_AGENTS/rules/data.md) |
| Room Entity, DAO, `@Query` | [room.md](DIARY_AGENTS/rules/room.md) |
| `CoroutineWorker`·`WorkRequest`, `BGTaskScheduler`, 코루틴 예약기 | [work.md](DIARY_AGENTS/rules/work.md) |
| `build.gradle.kts`와 버전 카탈로그 | [gradle.md](DIARY_AGENTS/rules/gradle.md) |

## 제품 문서

`docs`의 문서를 읽거나 쓰기 전에 소유 범위와 목록을 정한 [docs/README.md](docs/README.md)를 먼저 확인한다. 같은 내용을 두 문서에 적지 않고 소유한 문서를 링크하며, 문서가 충돌하면 스펙을 기준으로 한다.

## 참고 우선순위

아키텍처 판단과 코드 작성의 근거는 다음 순서로 삼고, 충돌하면 앞선 항목을 우선한다.

1. [Android Developers](https://developer.android.com/) — 공식 권장사항
2. [Now in Android](https://github.com/android/nowinandroid) — 아키텍처, 모듈, 네이밍, Gradle, 코드 스타일
3. [DroidKaigi conference-app-2026](https://github.com/DroidKaigi/conference-app-2026) — KMP 모듈과 Compose UI 구성
4. [KotlinConf App](https://github.com/JetBrains/kotlinconf-app) — 플랫폼별 앱 구성과 공유 UI
