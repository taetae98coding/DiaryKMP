# SettingHome 테스트 케이스

기준 스펙: [SettingHome 화면 스펙](../spec/client/setting-home.md)

`더보기`에서 설정 동작을 선택해 SettingHome으로 이동하는 케이스는 [MoreHome 테스트 케이스](./more-home.md)에서 다룬다. 설정 목록과 상세 설정이 함께 표시되는 상태의 시스템 뒤로가기 결과는 [Setting 목록·상세 배치 테스트 케이스](./setting-list-detail.md)에서, 각 설정 항목에서 이어지는 화면의 케이스는 [SettingHoliday 테스트 케이스](./setting-holiday.md), [SettingMap 테스트 케이스](./setting-map.md), [SettingGemini 테스트 케이스](./setting-gemini.md), [SettingBrowser 테스트 케이스](./setting-browser.md), [SettingDownload 테스트 케이스](./setting-download.md)에서 다룬다.

## feature

### TC-SETTING-HOME-FEATURE-003: 표시 상태와 관계없이 뒤로가기 동작을 실행하면 더보기로 돌아간다

- 근거: `feature > 뒤로가기`
- Given: 설정 화면이 테스트 데이터의 표시 상태로 나타나 있다.
- When: 사용자가 SettingHome의 뒤로가기 동작을 실행한다.
- Then: 설정 화면 전체를 떠나 `더보기` 화면으로 돌아간다.
- 테스트 데이터:

| 표시 상태 |
| --- |
| SettingHome이 단독으로 표시되고 상세 설정을 선택하지 않음 |
| SettingHome과 선택 전 상세 영역이 함께 표시됨 |
| SettingHome과 SettingHoliday가 함께 표시됨 |
| SettingHome과 SettingMap이 함께 표시됨 |
| SettingHome과 SettingGemini가 함께 표시됨 |
| SettingHome과 SettingBrowser가 함께 표시됨 |
| SettingHome과 SettingDownload가 함께 표시됨 |

`더보기` 화면으로 돌아간 뒤 공통 내비게이션이 다시 제공되는 결과는 [TopLevelNavigation 테스트 케이스](./top-level-navigation.md)의 TC-TOP-LEVEL-NAVIGATION-FEATURE-005가 다룬다.

### TC-SETTING-HOME-FEATURE-005: 설정 항목을 표시한다

- 근거: `feature > 설정 항목`
- Given: `브라우저`와 `다운로드` 항목을 제공하는 환경에서 제공하는 항목이 확인된 상태로 SettingHome 화면이 표시되어 있다.
- When: 사용자가 SettingHome 화면 본문을 확인한다.
- Then: `공휴일`, `지도`, `Gemini`, `브라우저`, `다운로드` 항목이 표시된다.

### TC-SETTING-HOME-FEATURE-006: 각 설정 항목을 선택할 수 있다

- 근거: `feature > 설정 항목`
- Given: `브라우저`와 `다운로드` 항목을 제공하는 환경에서 제공하는 항목이 확인된 상태로 SettingHome 화면이 표시되어 있다.
- When: 사용자가 화면 본문을 확인한다.
- Then: `공휴일`, `지도`, `Gemini`, `브라우저`, `다운로드` 항목을 각각 선택할 수 있다.

### TC-SETTING-HOME-FEATURE-007: 공휴일 항목을 선택하면 SettingHoliday 화면으로 이동한다

- 근거: `feature > 설정 항목`
- Given: SettingHome 화면이 표시되어 있다.
- When: 사용자가 `공휴일` 항목을 선택한다.
- Then: SettingHoliday 화면으로 이동하는 동작이 한 번 실행되고, 다른 설정 화면으로 이동하지 않는다.

### TC-SETTING-HOME-FEATURE-008: 지도 항목을 선택하면 SettingMap 화면으로 이동한다

- 근거: `feature > 설정 항목`
- Given: SettingHome 화면이 표시되어 있다.
- When: 사용자가 `지도` 항목을 선택한다.
- Then: SettingMap 화면으로 이동하는 동작이 한 번 실행되고, 다른 설정 화면으로 이동하지 않는다.

### TC-SETTING-HOME-FEATURE-009: Gemini 항목을 선택하면 SettingGemini 화면으로 이동한다

- 근거: `feature > 설정 항목`
- Given: SettingHome 화면이 표시되어 있다.
- When: 사용자가 `Gemini` 항목을 선택한다.
- Then: SettingGemini 화면으로 이동하는 동작이 한 번 실행되고, 다른 설정 화면으로 이동하지 않는다.

### TC-SETTING-HOME-FEATURE-010: 브라우저 항목을 선택하면 SettingBrowser 화면으로 이동한다

- 근거: `feature > 설정 항목`
- Given: `브라우저` 항목을 제공하는 환경에서 SettingHome 화면이 표시되어 있다.
- When: 사용자가 `브라우저` 항목을 선택한다.
- Then: SettingBrowser 화면으로 이동하는 동작이 한 번 실행되고, 다른 설정 화면으로 이동하지 않는다.

### TC-SETTING-HOME-FEATURE-013: 다운로드 항목을 선택하면 SettingDownload 화면으로 이동한다

- 근거: `feature > 설정 항목`
- Given: `다운로드` 항목을 제공하는 환경에서 SettingHome 화면이 표시되어 있다.
- When: 사용자가 `다운로드` 항목을 선택한다.
- Then: SettingDownload 화면으로 이동하는 동작이 한 번 실행되고, 다른 설정 화면으로 이동하지 않는다.

### TC-SETTING-HOME-FEATURE-011: 브라우저 항목을 제공하지 않는 환경에서는 그 항목을 표시하지 않는다

- 근거: `feature > 설정 항목`, `domain > 설정 항목과 순서`
- Given: `브라우저` 항목을 제공하지 않고 `다운로드` 항목은 제공하는 환경에서 제공하는 항목이 확인된 상태로 SettingHome 화면이 표시되어 있다.
- When: 사용자가 SettingHome 화면 본문을 확인한다.
- Then: `공휴일`, `지도`, `Gemini`, `다운로드` 항목만 이 순서로 표시되고 `브라우저` 항목은 표시되지 않는다.

### TC-SETTING-HOME-FEATURE-014: 다운로드 항목을 제공하지 않는 환경에서는 그 항목을 표시하지 않는다

- 근거: `feature > 설정 항목`, `domain > 설정 항목과 순서`
- Given: `브라우저`와 `다운로드` 항목을 모두 제공하지 않는 환경에서 제공하는 항목이 확인된 상태로 SettingHome 화면이 표시되어 있다.
- When: 사용자가 SettingHome 화면 본문을 확인한다.
- Then: `공휴일`, `지도`, `Gemini` 항목만 이 순서로 표시되고 `브라우저`와 `다운로드` 항목은 표시되지 않는다.

### TC-SETTING-HOME-FEATURE-012: 제공하는 항목을 확인하기 전에는 항목을 제공하지 않는다

- 근거: `feature > 설정 항목`
- Given: 현재 환경에서 제공하는 항목을 아직 확인하지 못한 상태다.
- When: 사용자가 SettingHome 화면 본문을 확인한다.
- Then: 설정 항목이 표시되지 않고 별도 로딩 또는 오류 상태도 제공되지 않는다.

### TC-SETTING-HOME-FEATURE-015: 제공하는 항목을 확인하지 못하면 항목을 제공하지 않는다

- 근거: `feature > 설정 항목`
- Given: 테스트 데이터의 제공 여부 중 하나를 확인하지 못했다.
- When: 사용자가 SettingHome 화면 본문을 확인한다.
- Then: 설정 항목이 표시되지 않고 별도 로딩 또는 오류 상태도 제공되지 않는다.
- 테스트 데이터:

| 확인하지 못한 제공 여부 |
| --- |
| `브라우저` 항목 제공 여부 |
| `다운로드` 항목 제공 여부 |

### TC-SETTING-HOME-FEATURE-016: 화면을 보는 동안에는 항목 목록이 바뀌지 않는다

- 근거: `feature > 목록의 갱신`
- Given: `다운로드` 항목을 제공하는 환경으로 확인되어 SettingHome 화면에 다섯 항목이 표시되어 있고, 이후 다시 확인하면 `다운로드` 항목을 제공하지 않는 것으로 제어되어 있다.
- When: 사용자가 화면을 떠나지 않고 계속 본다.
- Then: 다섯 항목이 그대로 표시된다.

### TC-SETTING-HOME-FEATURE-017: 화면을 떠났다가 다시 들어오면 제공하는 항목을 다시 확인한다

- 근거: `feature > 목록의 갱신`
- Given: `다운로드` 항목을 제공하는 환경으로 확인되어 SettingHome 화면에 다섯 항목이 표시된 뒤 사용자가 화면을 떠났고, 이후 다시 확인하면 `다운로드` 항목을 제공하지 않는 것으로 제어되어 있다.
- When: 사용자가 SettingHome 화면에 다시 들어온다.
- Then: `다운로드` 항목을 뺀 네 항목이 표시된다.

### TC-SETTING-HOME-FEATURE-018: 다른 앱에 다녀온 지 5초가 지나 돌아오면 제공하는 항목을 다시 확인한다

- 근거: `feature > 목록의 갱신`
- Given: `다운로드` 항목을 제공하는 환경으로 확인되어 SettingHome 화면에 다섯 항목이 표시되어 있고, 이후 다시 확인하면 `다운로드` 항목을 제공하지 않는 것으로 제어되어 있다.
- When: 사용자가 다른 앱으로 떠났다가 5초가 지난 뒤 돌아온다.
- Then: `다운로드` 항목을 뺀 네 항목이 표시된다.

### TC-SETTING-HOME-FEATURE-019: 다른 앱에 다녀온 지 5초 안에 돌아오면 확인해 둔 항목을 그대로 둔다

- 근거: `feature > 목록의 갱신`
- Given: `다운로드` 항목을 제공하는 환경으로 확인되어 SettingHome 화면에 다섯 항목이 표시되어 있고, 이후 다시 확인하면 `다운로드` 항목을 제공하지 않는 것으로 제어되어 있다.
- When: 사용자가 다른 앱으로 떠났다가 5초가 지나기 전에 돌아온다.
- Then: 다섯 항목이 그대로 표시되고 제공하는 항목을 다시 확인하지 않는다.

### TC-SETTING-HOME-FEATURE-020: 단독으로 표시된 세부 설정에 다녀온 지 5초 안에 돌아오면 확인해 둔 항목을 그대로 둔다

- 근거: `feature > 목록의 갱신`
- Given: `다운로드` 항목을 제공하는 환경으로 확인되어 SettingHome 화면이 단독으로 다섯 항목을 표시하고 있고, 이후 다시 확인하면 `다운로드` 항목을 제공하지 않는 것으로 제어되어 있다.
- When: 사용자가 세부 설정을 골라 설정 목록이 가려졌다가 5초가 지나기 전에 설정 목록으로 돌아온다.
- Then: 다섯 항목이 그대로 표시되고 제공하는 항목을 다시 확인하지 않는다.

### TC-SETTING-HOME-FEATURE-021: 단독으로 표시된 세부 설정에 다녀온 지 5초가 지나 돌아오면 제공하는 항목을 다시 확인한다

- 근거: `feature > 목록의 갱신`
- Given: `다운로드` 항목을 제공하는 환경으로 확인되어 SettingHome 화면이 단독으로 다섯 항목을 표시하고 있고, 이후 다시 확인하면 `다운로드` 항목을 제공하지 않는 것으로 제어되어 있다.
- When: 사용자가 세부 설정을 골라 설정 목록이 가려졌다가 5초가 지난 뒤 설정 목록으로 돌아온다.
- Then: `다운로드` 항목을 뺀 네 항목이 표시된다.

### TC-SETTING-HOME-FEATURE-022: 설정 목록과 세부 설정이 함께 표시되는 동안에는 세부 설정을 골라도 항목을 다시 확인하지 않는다

- 근거: `feature > 목록의 갱신`
- Given: `다운로드` 항목을 제공하는 환경으로 확인되어 설정 목록과 세부 설정이 함께 표시되고 있고, 이후 다시 확인하면 `다운로드` 항목을 제공하지 않는 것으로 제어되어 있다.
- When: 사용자가 세부 설정을 고르고 5초가 지나도록 설정 목록을 함께 본다.
- Then: 다섯 항목이 그대로 표시되고 제공하는 항목을 다시 확인하지 않는다.

## domain

### TC-SETTING-HOME-DOMAIN-001: 설정 항목을 선언된 순서대로 배치한다

- 근거: `domain > 설정 항목과 순서`
- Given: `브라우저`와 `다운로드` 항목을 제공하는 환경에서 제공하는 항목이 확인된 상태로 SettingHome 화면이 표시되어 있다.
- When: 사용자가 화면 본문의 항목 순서를 확인한다.
- Then: `공휴일`, `지도`, `Gemini`, `브라우저`, `다운로드` 순서로 배치되고, 그 밖의 설정 항목은 표시되지 않는다.
