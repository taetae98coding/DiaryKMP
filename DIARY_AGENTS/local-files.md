# 로컬 전용 파일

Git이 추적하지 않고 저장소 밖에서 받아 각자 배치해야 하는 파일이다.

## 편입 기준

Git ignored이면서 없으면 빌드, 검증 또는 앱 실행이 성립하지 않는 파일만 넣는다. 빌드 산출물, IDE 설정, 캐시처럼 다시 만들 수 있는 파일은 넣지 않는다.

새 파일을 `.gitignore`에 추가할 때 이 기준에 해당하면 목록에도 함께 등록한다.

## 목록

| 경로 | 용도 |
| --- | --- |
| `local.properties` | Gradle 구성 단계에서 읽는 SDK 경로와 인증 키 |
| `iosApp/Config/DevLocal.xcconfig` | dev flavor iOS 빌드 설정과 인증 키 |
| `iosApp/Config/RealLocal.xcconfig` | real flavor iOS 빌드 설정과 인증 키 |
| `app/android/keystore/dev.jks` | dev flavor Android 서명 |
| `app/android/keystore/real.jks` | real flavor Android 서명 |
| `app/android/src/dev/google-services.json` | dev flavor Firebase Android 설정 |
| `app/android/src/real/google-services.json` | real flavor Firebase Android 설정 |
| `iosApp/Firebase/dev/GoogleService-Info.plist` | dev flavor Firebase iOS 설정 |
| `iosApp/Firebase/real/GoogleService-Info.plist` | real flavor Firebase iOS 설정 |

파일 내용과 인증 값은 어떤 경우에도 출력하지 않는다.

## 자동 복사

Claude Code 세션이 시작되면 `.claude/settings.json`의 SessionStart hook이 [copy-local-files.sh](scripts/copy-local-files.sh)를 실행해, 현재 worktree에 없는 목록의 파일을 기본 worktree(`git worktree list --porcelain`의 첫 항목)에서 복사한다. 이미 있는 파일은 덮어쓰지 않는다. 표의 모든 파일이 대상이다. 스크립트는 위 표의 첫 열을 읽으므로 목록은 이 표에서만 관리한다. 다른 에이전트나 직접 만든 worktree에서는 같은 스크립트를 한 번 실행한다.
