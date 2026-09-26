# QrHome 테스트 케이스

기준 스펙: [QrHome 화면 스펙](../spec/client/qr-home.md)

## feature

### TC-QR-HOME-FEATURE-001: 상단 바에 화면 제목을 표시한다

- 근거: `feature > 화면 내용`
- Given: 사용자 언어가 한국어다.
- When: 사용자가 QrHome 화면을 확인한다.
- Then: 상단 바에 제목 `QR` 문구가 표시된다.

### TC-QR-HOME-FEATURE-004: 제목 외의 정보와 준비 중 안내를 표시하지 않는다

- 근거: `feature > 화면 내용`
- Given: 사용자 언어가 한국어다.
- When: 사용자가 QrHome 화면을 확인한다.
- Then: 화면에 보이는 글은 제목 `QR` 하나뿐이고, 기능이 아직 준비 중이라는 안내나 그 밖의 정보가 표시되지 않는다.

### TC-QR-HOME-FEATURE-012: 선택할 수 있는 동작은 뒤로가기와 QR 추가뿐이다

- 근거: `feature > 화면 내용`
- Given: QrHome 화면이 표시되어 있고, 사용자 언어가 테스트 데이터의 언어다.
- When: 사용자가 화면에서 할 수 있는 동작을 확인한다.
- Then: 선택할 수 있는 동작은 뒤로가기와 QR 추가 둘뿐이고, QR 추가는 테스트 데이터의 접근성 이름으로 제공된다.
- 테스트 데이터:

  | 언어 | QR 추가 접근성 이름 |
  | --- | --- |
  | 한국어 | `QR 추가` |
  | 그 외 기본 | `Add QR code` |

### TC-QR-HOME-FEATURE-013: QR 추가를 선택하면 QrAdd 화면으로 이동한다

- 근거: `feature > QR 추가`
- Given: QrHome 화면이 표시되어 있다.
- When: 사용자가 QR 추가를 선택한다.
- Then: QrAdd 화면으로 이동하는 동작이 한 번 실행된다.

### TC-QR-HOME-FEATURE-014: 데스크톱 앱과 웹에서도 QR 추가를 표시하고 선택하면 QrAdd 화면으로 이동한다

- 근거: `feature > QR 추가`
- Given: 테스트 데이터의 환경에서 QrHome 화면이 표시되어 있다.
- When: 사용자가 QR 추가를 선택한다.
- Then: QR 추가가 표시되어 있고, QrAdd 화면으로 이동하는 동작이 한 번 실행된다.
- 테스트 데이터:

  | 환경 |
  | --- |
  | 데스크톱 앱 |
  | 웹 |

- 작성하지 않는 이유: 화면 표시와 선택은 데스크톱 앱과 웹의 화면 테스트로만 관찰할 수 있고, 현재 테스트 환경은 Android 화면 테스트만 실행한다. Android에서의 결과는 TC-QR-HOME-FEATURE-013이 확인한다. 데스크톱 앱이나 웹에서 화면 테스트를 실행할 수 있는 환경이 제공되면 자동화한다.

### TC-QR-HOME-FEATURE-003: 뒤로가기를 선택하면 이전 화면으로 돌아간다

- 근거: `feature > 뒤로가기`
- Given: 사용자가 `더보기`의 `QR` 바로가기로 QrHome 화면에 들어와 있다.
- When: 사용자가 뒤로가기를 선택한다.
- Then: 이전 화면으로 돌아가는 동작이 한 번 실행된다.

## 작성하지 않는 케이스

- QrAdd 화면에서 돌아왔을 때 이 화면이 떠나기 전 모습 그대로 보이는 결과와 `domain > 유지할 사용자 진행 상태`는 따로 작성하지 않는다. 이 화면에는 사용자가 바꿀 수 있는 내용이 없어 떠나기 전과 돌아온 뒤, 재생성 전과 후를 구분할 관찰 결과가 없다.
- QR 스캔 시작과 카메라 권한 안내를 다루던 TC-QR-HOME-FEATURE-005부터 TC-QR-HOME-FEATURE-011까지와 TC-QR-HOME-DOMAIN-002부터 TC-QR-HOME-DOMAIN-004까지는 그 동작이 QrAdd 화면으로 옮겨가 폐기했다. 옮겨간 결과는 [QrAdd 테스트 케이스](./qr-add.md)가 확인한다.
