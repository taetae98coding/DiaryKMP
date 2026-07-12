# 로컬 전용 파일

Git이 추적하지 않고 저장소 밖에서 받아 각자 배치해야 하는 파일이다.

## 편입 기준

Git ignored이면서 없으면 빌드, 검증 또는 앱 실행이 성립하지 않는 파일만 넣는다. 빌드 산출물, IDE 설정, 캐시처럼 다시 만들 수 있는 파일은 넣지 않는다.

새 파일을 `.gitignore`에 추가할 때 이 기준에 해당하면 목록에도 함께 등록한다.

## 목록

| 경로 | 용도 | 복구 대상 |
| --- | --- | --- |
| `local.properties` | Gradle 구성 단계에서 읽는 SDK 경로와 인증 키 | O |
| `iosApp/Config/DevLocal.xcconfig` | dev flavor iOS 빌드 설정과 인증 키 | O |
| `iosApp/Config/RealLocal.xcconfig` | real flavor iOS 빌드 설정과 인증 키 | O |
| `app/android/keystore/dev.jks` | dev flavor Android 서명 | O |
| `app/android/keystore/real.jks` | real flavor Android 서명 | O |
| `app/android/src/dev/google-services.json` | dev flavor Firebase Android 설정 | — |
| `app/android/src/real/google-services.json` | real flavor Firebase Android 설정 | — |
| `iosApp/Firebase/dev/GoogleService-Info.plist` | dev flavor Firebase iOS 설정 | — |
| `iosApp/Firebase/real/GoogleService-Info.plist` | real flavor Firebase iOS 설정 | — |

파일 내용과 인증 값은 어떤 경우에도 출력하지 않는다.

## 복구 절차

검증 명령이 이 목록의 파일 누락 때문에 실패한 경우에만 복구를 한 번 수행한다. `복구 대상`이 `O`가 아닌 파일은 복구하지 않고 실패로 보고한다.

`git worktree list --porcelain`에서 `refs/heads/main`이 체크아웃된 worktree를 복사 원본으로 찾는다. 해당 worktree가 없거나 여러 개이면 복구하지 않고 실패로 보고한다.

복구 대상 경로를 모두 확인하고 현재 worktree에 없는 파일만 main worktree의 동일한 상대 경로에서 복사한다. 복사 전에 모든 누락 파일이 main worktree의 일반 파일인지 확인한다. 현재 worktree에 이미 있는 파일은 덮어쓰지 않는다.

복사한 각 파일이 원본과 바이트 단위로 같은지 확인한다. 모든 복사와 비교가 성공하면 검증 절차 전체를 처음부터 다시 실행한다. 복구 후에도 실패하거나 복사 또는 비교에 실패하면 추가 복구를 시도하지 않고 실패로 보고한다.
