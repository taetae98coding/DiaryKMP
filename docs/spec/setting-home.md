# SettingHome 화면 스펙

이 문서는 사용자가 `더보기`의 설정 동작에서 진입하는 SettingHome 화면의 내용과 행동을 다룬다. `더보기`의 설정 동작 표시와 선택은 [MoreHome 화면 스펙](./more-home.md)에서, 세부 화면에서의 공통 내비게이션 노출 규칙은 [TopLevelNavigation 스펙](./top-level-navigation.md)에서 다룬다. 설정 목록과 세부 설정을 함께 사용할 때의 선택·뒤로가기 정책은 [Setting 목록·상세 배치 스펙](./setting-list-detail.md)을 따르고, 각 설정 항목에서 이어지는 화면은 [SettingHoliday 화면 스펙](./setting-holiday.md), [SettingMap 화면 스펙](./setting-map.md), [SettingGemini 화면 스펙](./setting-gemini.md), [SettingBrowser 화면 스펙](./setting-browser.md)에서 다룬다.

디자인: [SettingHome 화면 디자인](../design/setting-home.md)

이번 범위는 설정 항목을 제공하고 해당 설정 화면으로 이동하는 것까지다. SettingHome에서는 설정 값을 직접 변경하거나 사용자 데이터를 저장하지 않는다.

## feature

### 진입과 이동

사용자는 `더보기` 화면에서 설정 동작을 선택해 SettingHome 화면으로 이동한다.

SettingHome 화면은 `더보기`에서 이어지는 세부 화면이며 설정 목록을 제공한다.

SettingHome 화면이 단독으로 표시되거나 상세 설정과 함께 표시되는 동안에는 공통 내비게이션을 제공하지 않는다. 설정 목록과 상세 설정의 표시 관계는 [Setting 목록·상세 배치 스펙](./setting-list-detail.md)을 따른다.

### 설정 항목

사용자는 다음 설정 항목을 선언된 순서대로 확인하고 각각 선택할 수 있다.

1. `공휴일`
2. `지도`
3. `Gemini`
4. `브라우저`

`공휴일`을 선택하면 [SettingHoliday 화면](./setting-holiday.md)으로 이동한다.

`지도`를 선택하면 [SettingMap 화면](./setting-map.md)으로 이동한다.

`Gemini`를 선택하면 [SettingGemini 화면](./setting-gemini.md)으로 이동한다.

`브라우저`를 선택하면 [SettingBrowser 화면](./setting-browser.md)으로 이동한다.

`브라우저`는 [Chrome 로그인 이어받기 스펙](./chrome-session-import.md)의 `제공 환경`에서만 제공한다. 제공하지 않는 환경에서는 `브라우저`를 목록에 두지 않고, 나머지 항목은 같은 순서로 제공한다.

현재 환경에서 제공하는 항목을 확인하기 전에는 설정 항목을 제공하지 않으며 별도 로딩 또는 오류 상태로 전환하지 않는다.

### 뒤로가기

사용자가 SettingHome의 뒤로가기 동작을 실행하면 SettingHome이 단독으로 표시되는지 상세 설정과 함께 표시되는지, 상세 설정을 선택했는지와 관계없이 설정 화면 전체를 떠나 `더보기` 화면으로 돌아간다.

`더보기` 화면으로 돌아가면 공통 내비게이션을 다시 제공한다. 상세 설정과 함께 표시되는 상태에서 시스템 뒤로가기를 사용한 결과는 [Setting 목록·상세 배치 스펙](./setting-list-detail.md)을 따른다.

## domain

### 설정 항목과 순서

설정 항목은 설정 화면에서 이어지는 세부 설정 영역을 가리키며 이동 대상 화면을 하나씩 가진다.

이동 대상 화면이 없는 항목과 현재 환경에서 제공하지 않는 항목은 SettingHome에 제공하지 않는다.
