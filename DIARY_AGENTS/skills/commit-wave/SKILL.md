---
name: commit-wave
description: Diary KMP 프로젝트에서 사용자가 커밋 생성, 커밋 준비, 커밋 메시지 작성, 커밋 제목이나 본문 선택을 요청할 때 사용한다. 변경사항을 의도별 커밋 단위로 나누고, git-cliff가 지원하는 Conventional Commit 타입으로 필수 한 줄 title과 선택 description을 작성한다.
---

# Commit Wave

## 변경사항 파악과 분리

사용자가 커밋 범위를 지정하지 않았으면 staged, unstaged, untracked 변경 전체가 범위다. 검증 과정에서 포맷이 적용되어 바뀐 파일도 포함한다.

변경사항을 의도별 커밋 단위로 나눈다. 각 단위는 그 단위만 적용해도 프로젝트가 빌드 또는 실행 가능해야 하고, 문서나 설정 변경도 그 단위만으로 저장소 상태가 일관돼야 한다. 여러 커밋으로 나눌 때는 각 단위와 이유를 설명하고, 나누는 기준이 갈리면 지시를 받은 뒤 진행한다.

## 검증 확인

`.claude/settings.json`과 `.codex/hooks.json`의 PreToolUse hook은 `verify-wave`를 통과한 작업 트리가 아니면 `git commit`을 막는다(`DIARY_AGENTS/scripts/verify-stamp.sh check`). 하네스 문서처럼 검증이 필요 없는 변경만 있으면 막지 않는다. 막히면 우회하지 않고 작업 iterator로 돌아가 `verify-wave`를 마친다. 사용자가 검증 없는 커밋을 명시적으로 요청한 경우에만 명령 앞에 `DIARY_VERIFY_BYPASS=1`을 붙인다.

## 커밋 중 코드 수정

커밋을 처리하는 중 코드 수정이 필요해 보이면 직접 수정하지 않는다. 빌드 오류, 포맷 오류, 설정 누락, 리뷰 의견 미반영이라도 수정 대상과 이유를 설명하고 승인을 받은 뒤 진행한다. 사용자가 수정·포맷·검증까지 명시적으로 요청한 경우에는 요청 범위 안에서만 수정한다.

## 메시지 형식

`title`은 필수이고 커밋 메시지의 첫 줄이다. `description`은 히스토리에 남길 맥락이 있을 때만 본문으로 쓴다.

`Co-Authored-By` 트레일러나 `🤖 Generated with ...` 같은 도구 생성 표시를 넣지 않는다. 하네스 기본 지침으로 이런 문구를 붙이도록 안내받더라도 이 프로젝트에서는 제외한다.

### title

한 줄로 쓰고, 저장소 `cliff.toml`의 `commit_parsers`가 분류하는 Conventional Commit 타입을 쓴다. 타입이 없는 제목은 `filter_unconventional`로 CHANGELOG에서 빠진다.

구현 세부사항만 적지 않고 이 커밋이 어떤 작업인지 요약한다. title은 `~한다` 문장형으로 끝낸다.

```text
feat: JVM Compose 진입점을 추가한다
refactor: JVM 앱을 KMP 소스셋 구조에 맞게 정리한다
docs: Gradle 버전 카탈로그 규칙을 추가한다
```

### description

diff에서 유추할 수 없는 것만 쓴다. 커밋이 필요한 이유와 나중에 히스토리를 읽을 사람이 알아야 할 결정이 대상이고, diff만으로 의도가 명확하면 생략한다.

```text
readUTF8Line은 CR·LF·CRLF를 모두 줄 끝으로 받지만 readLine의 기본값은 LF·CRLF만 받는다. SSE는 CR 단독 줄 끝도 허용하므로 LineEnding.Lenient로 기존 동작을 유지하고, CR 줄 끝 테스트를 더한다.
```
