# Login 화면 디자인

기준 스펙: [Login 화면 스펙](../spec/login.md)

## 화면 구조

Login 화면은 앱의 공통 내비게이션 영역 없이 단독으로 표시한다.

화면 상단에는 로그인 화면임을 알리는 상단 바와 뒤로가기 버튼을 표시한다. 본문 중앙에는 Apple 로그인 버튼과 Google 로그인 버튼을 같은 크기로 가로로 나란히 배치하며, Apple 로그인 버튼을 앞에 둔다.

두 로그인 버튼은 모든 플랫폼에서 같은 구성으로 표시한다.

상단 바의 뒤로가기 버튼이나 시스템 뒤로가기를 사용하면 진입하기 전 화면으로 돌아간다.

## 로그인 버튼

Google 로그인 버튼은 Google 공식 가이드의 아이콘 모드 버튼으로 표시한다. 표준 색상의 Google `G` 로고는 색상, 비율, 형태를 바꾸지 않는다.

Apple 로그인 버튼은 Apple 공식 가이드가 허용하는 로고 전용 버튼으로 표시한다. 버튼 전체 색은 흰색 또는 검은색을 유지하고 Apple 로고의 비율과 형태를 바꾸지 않는다.

두 로고 모두 장식 요소나 로고 자체만으로 사용하지 않는다. 로그인 행동임을 알 수 있는 경계와 터치 영역을 가진 버튼 안에 배치한다.

## 상태와 피드백

앱 로그인 요청을 처리하는 동안에는 로그인 버튼 대신 진행 표시를 보여 준다. 진행 중에도 상단 바와 뒤로가기 버튼은 계속 표시한다.

로그인에 실패하면 실패 안내를 스낵바로 표시한다. 실패 후에는 진행 표시를 종료하고 로그인 버튼을 다시 표시한다.

## 문구와 접근성

한국어 환경과 그 외 기본 환경에서 다음 문구를 사용한다.

| 항목 | 한국어 | 그 외 기본 |
| --- | --- | --- |
| 상단 바 제목 | `로그인` | `Login` |
| Google 로그인 버튼 접근성 이름 | `Google로 로그인` | `Sign in with Google` |
| Apple 로그인 버튼 접근성 이름 | `Apple로 로그인` | `Sign in with Apple` |
| 로그인 실패 안내 | `로그인에 실패했습니다.` | `Sign-in failed.` |

## 참고

- [Sign in with Google Branding Guidelines](https://developers.google.com/identity/branding-guidelines)
- [Sign in with Apple Buttons](https://developer.apple.com/design/human-interface-guidelines/sign-in-with-apple#Buttons)
