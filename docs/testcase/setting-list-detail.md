# Setting 목록·상세 배치 테스트 케이스

기준 스펙: [Setting 목록·상세 배치 스펙](../spec/client/setting-list-detail.md)

## feature

### TC-SETTING-LIST-DETAIL-FEATURE-002: 설정 항목을 선택하면 해당 설정 화면이 현재 상세가 된다

- 근거: `feature > 상세 영역의 구성`
- Given: 목록과 상세를 함께 사용하고 있고 상세는 선택 전 상태다.
- When: 사용자가 테스트 데이터의 설정 항목을 선택한다.
- Then: 설정 목록은 유지되고 테스트 데이터의 설정 화면이 현재 상세가 된다.
- 테스트 데이터:

| 설정 항목 | 상세 화면 |
| --- | --- |
| `공휴일` | SettingHoliday 화면 |
| `지도` | SettingMap 화면 |
| `Gemini` | SettingGemini 화면 |
| `브라우저` | SettingBrowser 화면 |
| `다운로드` | SettingDownload 화면 |

### TC-SETTING-LIST-DETAIL-FEATURE-003: 다른 설정 항목을 선택하면 현재 상세 화면을 교체한다

- 근거: `feature > 상세 영역의 구성`
- Given: 목록과 상세를 함께 사용하고 있고 테스트 데이터의 현재 상세가 선택되어 있다.
- When: 사용자가 설정 목록에서 테스트 데이터의 다른 설정 항목을 선택한다.
- Then: 현재 상세가 새로 선택한 설정 화면으로 교체된다.
- 테스트 데이터:

| 현재 상세 화면 | 다른 설정 항목 | 교체 후 상세 화면 |
| --- | --- | --- |
| SettingHoliday 화면 | `지도` | SettingMap 화면 |
| SettingMap 화면 | `공휴일` | SettingHoliday 화면 |
| SettingHoliday 화면 | `Gemini` | SettingGemini 화면 |
| SettingGemini 화면 | `지도` | SettingMap 화면 |
| SettingGemini 화면 | `브라우저` | SettingBrowser 화면 |
| SettingGemini 화면 | `다운로드` | SettingDownload 화면 |
| SettingBrowser 화면 | `공휴일` | SettingHoliday 화면 |
| SettingBrowser 화면 | `다운로드` | SettingDownload 화면 |
| SettingDownload 화면 | `지도` | SettingMap 화면 |

### TC-SETTING-LIST-DETAIL-FEATURE-011: 현재 상세인 설정 항목을 다시 선택하면 아무것도 바뀌지 않는다

- 근거: `feature > 상세 영역의 구성`
- Given: 목록과 상세를 함께 사용하고 있고 테스트 데이터의 설정 화면이 현재 상세다.
- When: 사용자가 설정 목록에서 현재 상세와 같은 설정 항목을 다시 선택한다.
- Then: 설정 목록과 현재 상세가 그대로 유지되고, 같은 상세가 전환 이력에 한 번 더 쌓이지 않으며, 그 설정 화면에서 입력 중이던 내용도 그대로 남는다.
- 테스트 데이터:

| 현재 상세 화면 | 다시 선택하는 설정 항목 |
| --- | --- |
| SettingHoliday 화면 | `공휴일` |
| SettingMap 화면 | `지도` |
| SettingGemini 화면 | `Gemini` |
| SettingBrowser 화면 | `브라우저` |
| SettingDownload 화면 | `다운로드` |

### TC-SETTING-LIST-DETAIL-FEATURE-005: 선택 전 상태에는 조작 가능한 동작이 없다

- 근거: `feature > 상세 영역의 구성`
- Given: 목록과 상세를 함께 사용할 수 있는 환경이고 설정 항목을 선택하지 않았다.
- When: 사용자가 설정 화면에 진입한다.
- Then: 상세는 선택 전 상태로 표시되고, 선택 전 상태에는 사용자가 실행할 수 있는 동작이 없다.

### TC-SETTING-LIST-DETAIL-FEATURE-009: 설정 화면에 다시 진입하면 상세 선택을 초기화한다

- 근거: `domain > 유지와 초기화 기준`
- Given: 목록과 상세를 함께 사용하고 있고 상세 화면을 선택했다.
- When: 사용자가 `더보기` 화면으로 돌아갔다가 설정 화면에 다시 진입한다.
- Then: 상세 선택이 초기화되어 선택 전 상태로 표시된다.

### TC-SETTING-LIST-DETAIL-FEATURE-010: 시스템 뒤로가기를 사용하면 상세가 선택 전 상태로 돌아간다

- 근거: `feature > 뒤로가기`
- Given: 목록과 상세를 함께 사용하고 있고 상세 화면이 선택되어 있다.
- When: 사용자가 시스템 뒤로가기를 사용한다.
- Then: 설정 목록은 유지되고 상세는 선택 전 상태로 돌아간다.

### TC-SETTING-LIST-DETAIL-FEATURE-012: 선택 전 상태에서 시스템 뒤로가기를 사용하면 `더보기`로 돌아간다

- 근거: `feature > 뒤로가기`
- Given: 목록과 상세를 함께 사용하고 있고 상세는 선택 전 상태다.
- When: 사용자가 시스템 뒤로가기를 사용한다.
- Then: 설정 화면 전체를 떠나 `더보기` 화면으로 돌아간다.

### TC-SETTING-LIST-DETAIL-FEATURE-013: 세부 설정이 단독으로 표시될 때 뒤로가면 설정 목록으로 돌아간다

- 근거: `feature > 뒤로가기`
- Given: 목록과 상세를 함께 사용할 수 없는 창에서 설정 목록이 표시되어 있고, 사용자가 테스트 데이터의 설정 항목을 선택해 그 설정 화면이 단독으로 표시되어 있다.
- When: 사용자가 시스템 뒤로가기를 사용한다.
- Then: 설정 화면이 닫히고 설정 목록이 표시되며, 설정 화면 전체를 떠나지 않는다.
- 테스트 데이터:

| 설정 항목 | 단독으로 표시된 설정 화면 |
| --- | --- |
| `공휴일` | SettingHoliday 화면 |
| `지도` | SettingMap 화면 |
| `Gemini` | SettingGemini 화면 |
| `브라우저` | SettingBrowser 화면 |
| `다운로드` | SettingDownload 화면 |

공통 내비게이션을 계속 제공하지 않는 결과는 [TopLevelNavigation 테스트 케이스](./top-level-navigation.md)의 TC-TOP-LEVEL-NAVIGATION-FEATURE-004가 다룬다.

### TC-SETTING-LIST-DETAIL-FEATURE-014: 화면이 재생성되어도 상세 선택을 유지한다

- 근거: `domain > 유지와 초기화 기준`, [목록·상세 배치 공통 스펙](../spec/client/list-detail-pane.md)의 `domain > 유지와 초기화 기준`
- Given: 목록과 상세를 함께 사용하고 있고 사용자가 설정 항목을 선택해 그 설정 화면이 현재 상세다.
- When: 화면이 시스템에 의해 재생성된다.
- Then: 설정 목록과 같은 설정 화면이 계속 함께 표시되고, 시스템 뒤로가기를 사용하면 상세가 선택 전 상태로 돌아간다.

### TC-SETTING-LIST-DETAIL-FEATURE-015: 앱이 백그라운드에서 돌아와도 상세 선택을 유지한다

- 근거: `domain > 유지와 초기화 기준`, [목록·상세 배치 공통 스펙](../spec/client/list-detail-pane.md)의 `domain > 유지와 초기화 기준`
- Given: 목록과 상세를 함께 사용하고 있고 사용자가 설정 항목을 선택해 그 설정 화면이 현재 상세다.
- When: 앱이 백그라운드로 갔다가 시스템이 앱을 종료하지 않은 채 다시 앞으로 돌아온다.
- Then: 설정 목록과 같은 설정 화면이 계속 함께 표시되고, 시스템 뒤로가기를 사용하면 상세가 선택 전 상태로 돌아간다.

## 작성하지 않는 이유

- 설정 목록의 뒤로가기 동작으로 상세 선택 여부와 관계없이 `더보기` 화면으로 돌아가는 결과는 [SettingHome 테스트 케이스](./setting-home.md)의 TC-SETTING-HOME-FEATURE-003에서 다룬다.
- 두 영역의 표시 비율 조절과 조절 후 유지, 화면 재생성·백그라운드 복귀 후 비율 유지, 재진입·앱 재실행 후 기본 상태 초기화는 실제 화면 너비와 실행 경계를 함께 제어하고 관찰해야 한다. 현재 테스트 환경에서는 결정적으로 자동화할 수 없으며, 화면 크기와 실행 경계를 제어하는 통합 UI 테스트 환경이 갖춰지면 자동화한다.
- 화면 재생성 후 상세 선택 유지는 TC-SETTING-LIST-DETAIL-FEATURE-014가, 백그라운드 복귀 후 상세 선택 유지는 TC-SETTING-LIST-DETAIL-FEATURE-015가 다룬다. 앱을 종료하고 다시 실행한 뒤 상세 선택이 초기화되는 것은 앱 프로세스를 새로 시작해야 판정할 수 있어 현재 테스트 환경에서는 결정적으로 자동화할 수 없다. 앱 재실행을 제어하는 통합 테스트 환경이 갖춰지면 자동화한다.
