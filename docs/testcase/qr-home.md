# QrHome 테스트 케이스

기준 스펙: [QrHome 화면 스펙](../spec/client/qr-home.md)

QrHome 화면의 진입, 화면 내용, 뒤로가기는 [더보기 준비 중 화면 공통 스펙](../spec/client/more-menu-placeholder.md)에 위임되어 있다.

## feature

### TC-QR-HOME-FEATURE-001: 상단 바에 화면 제목을 표시한다

- 근거: `feature > 화면 내용과 행동`
- Given: 사용자 언어가 한국어다.
- When: 사용자가 QrHome 화면을 확인한다.
- Then: 상단 바에 제목 `QR` 문구가 표시된다.

### TC-QR-HOME-FEATURE-002: 제목과 뒤로가기 외에 선택할 수 있는 동작을 두지 않는다

- 근거: `feature > 화면 내용과 행동`
- Given: QrHome 화면이 표시되어 있다.
- When: 사용자가 화면에서 할 수 있는 동작을 확인한다.
- Then: 뒤로가기 외에 선택할 수 있는 동작이 없다.

### TC-QR-HOME-FEATURE-004: 제목 외의 정보와 준비 중 안내를 표시하지 않는다

- 근거: `feature > 화면 내용과 행동`
- Given: 사용자 언어가 한국어다.
- When: 사용자가 QrHome 화면을 확인한다.
- Then: 화면에 보이는 글은 제목 `QR` 하나뿐이고, 기능이 아직 준비 중이라는 안내나 그 밖의 정보가 표시되지 않는다.

### TC-QR-HOME-FEATURE-003: 뒤로가기를 선택하면 이전 화면으로 돌아간다

- 근거: `feature > 화면 내용과 행동`
- Given: 사용자가 `더보기`의 `QR` 바로가기로 QrHome 화면에 들어와 있다.
- When: 사용자가 뒤로가기를 선택한다.
- Then: 이전 화면으로 돌아가는 동작이 한 번 실행된다.
