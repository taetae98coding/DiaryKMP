# MoreHome 테스트 케이스

기준 스펙: [MoreHome 화면 스펙](../spec/more-home.md)

## feature

### TC-MORE-HOME-FEATURE-003: 확인 중 상태에서는 라벨과 계정 동작을 표시하지 않는다

- 근거: `feature > 계정 상태와 행동`
- Given: 계정 정보가 확인 중이다.
- When: 사용자가 계정 상태를 확인한다.
- Then: 게스트 라벨과 이메일이 표시되지 않고, 로그인 동작과 로그아웃 동작도 선택할 수 없다.

### TC-MORE-HOME-FEATURE-004: 게스트 상태에서 게스트 라벨과 로그인 동작을 표시한다

- 근거: `feature > 계정 상태와 행동`
- Given: 로그인한 사용자 정보가 없어 계정 상태가 게스트다.
- When: 사용자가 계정 상태를 확인한다.
- Then: 게스트 상태가 표시되고 로그인 동작을 선택할 수 있다.

### TC-MORE-HOME-FEATURE-005: 사용자 상태에서 이메일과 로그아웃 동작을 표시한다

- 근거: `feature > 계정 상태와 행동`
- Given: 이메일 `diary@example.com`을 가진 로그인 사용자 정보가 있다.
- When: 사용자가 계정 상태를 확인한다.
- Then: `diary@example.com`이 표시되고 게스트 상태는 표시되지 않으며 로그아웃 동작을 선택할 수 있다.

### TC-MORE-HOME-FEATURE-006: 로그인 동작을 선택하면 Login 화면으로 이동한다

- 근거: `feature > 계정 상태와 행동`
- Given: 계정 상태가 게스트로 표시되어 있다.
- When: 사용자가 로그인 동작을 선택한다.
- Then: Login 화면으로 이동한다.

### TC-MORE-HOME-FEATURE-007: 로그아웃에 성공하면 게스트 상태로 바뀐다

- 근거: `feature > 계정 상태와 행동`
- Given: 계정 상태가 사용자로 표시되어 있고 로그아웃이 성공하도록 제어되어 있다.
- When: 사용자가 로그아웃 동작을 선택한다.
- Then: 계정 상태가 게스트로 바뀌고 로그인 동작을 선택할 수 있다.

### TC-MORE-HOME-FEATURE-008: 한글이 아닌 이름을 앞에 두고 가나다순으로 표시한다

- 근거: `feature > 기능 바로가기`, `domain > 기능 바로가기 순서`
- Given: MoreHome 화면이 표시되어 있다.
- When: 사용자가 MoreHome 화면을 확인한다.
- Then: `QR`, `검색`, `디데이`, `연락처`, `웹`, `장소`, `체크리스트`, `파일`, `플레이리스트`, `황금연휴`가 해당 순서로 표시된다.

### TC-MORE-HOME-FEATURE-009: 각 메뉴 항목을 선택할 수 있다

- 근거: `feature > 기능 바로가기`
- Given: MoreHome 화면이 표시되어 있다.
- When: 사용자가 메뉴 항목을 확인한다.
- Then: `QR`, `검색`, `디데이`, `연락처`, `웹`, `장소`, `체크리스트`, `파일`, `플레이리스트`, `황금연휴` 항목을 각각 선택할 수 있다.

### TC-MORE-HOME-FEATURE-010: 장소 항목을 선택하면 PlaceHome 화면으로 이동한다

- 근거: `feature > 기능 바로가기`
- Given: MoreHome 화면이 표시되어 있다.
- When: 사용자가 `장소` 항목을 선택한다.
- Then: PlaceHome 화면으로 이동하는 동작이 한 번 실행된다.

### TC-MORE-HOME-FEATURE-012: 계정 상태와 관계없이 설정 동작을 표시한다

- 근거: `feature > 화면 내용`
- Given: 계정 상태가 테스트 데이터의 상태다.
- When: 사용자가 MoreHome 화면에서 가능한 동작을 확인한다.
- Then: 설정 이동을 선택할 수 있다.
- 테스트 데이터:

| 계정 상태 |
| --- |
| 확인 중 |
| 게스트 |
| 사용자 |

### TC-MORE-HOME-FEATURE-013: 설정 동작을 선택하면 SettingHome 화면으로 이동한다

- 근거: `feature > 화면 내용`
- Given: MoreHome 화면이 표시되어 있다.
- When: 사용자가 설정 이동을 선택한다.
- Then: SettingHome 화면으로 이동하는 동작이 한 번 실행된다.

### TC-MORE-HOME-FEATURE-014: 황금연휴 항목을 선택하면 HolidayHome 화면으로 이동한다

- 근거: `feature > 기능 바로가기`
- Given: MoreHome 화면이 표시되어 있다.
- When: 사용자가 `황금연휴` 항목을 선택한다.
- Then: HolidayHome 화면으로 이동하는 동작이 한 번 실행된다.

### TC-MORE-HOME-FEATURE-015: 검색 항목을 선택하면 SearchHome 화면으로 이동한다

- 근거: `feature > 기능 바로가기`
- Given: MoreHome 화면이 표시되어 있다.
- When: 사용자가 `검색` 항목을 선택한다.
- Then: SearchHome 화면으로 이동하는 동작이 한 번 실행된다.

### TC-MORE-HOME-FEATURE-016: 웹 항목을 선택하면 WebHome 화면으로 이동한다

- 근거: `feature > 기능 바로가기`
- Given: MoreHome 화면이 표시되어 있다.
- When: 사용자가 `웹` 항목을 선택한다.
- Then: WebHome 화면으로 이동하는 동작이 한 번 실행된다.

### TC-MORE-HOME-FEATURE-017: 사용자 상태에서 프로필을 선택하면 사진 선택 도구를 연다

- 근거: `feature > 프로필 사진 선택`
- Given: 계정 상태가 사용자로 표시되어 있다.
- When: 사용자가 프로필을 선택한다.
- Then: 기기의 사진 선택 도구를 여는 요청이 한 번 이루어진다.

### TC-MORE-HOME-FEATURE-018: 사진을 고르면 고른 사진을 프로필 이미지로 반영하도록 요청한다

- 근거: `feature > 프로필 사진 선택`
- Given: 계정 상태가 사용자로 표시되어 있고 사진 선택 도구가 열려 있다.
- When: 사용자가 사진을 고른다.
- Then: 고른 사진의 위치로 프로필 이미지 반영을 요청하는 동작이 한 번 실행된다.

### TC-MORE-HOME-FEATURE-019: 연락처 항목을 선택하면 ContactHome 화면으로 이동한다

- 근거: `feature > 기능 바로가기`
- Given: MoreHome 화면이 표시되어 있다.
- When: 사용자가 `연락처` 항목을 선택한다.
- Then: ContactHome 화면으로 이동하는 동작이 한 번 실행된다.

### TC-MORE-HOME-FEATURE-020: 사진 선택을 취소하면 프로필 이미지 반영을 요청하지 않는다

- 근거: `feature > 프로필 사진 선택`
- Given: 계정 상태가 사용자로 표시되어 있고 사진 선택 도구가 열려 있다.
- When: 사용자가 사진을 고르지 않고 선택을 취소한다.
- Then: 프로필 이미지 반영을 요청하는 동작이 실행되지 않는다.

### TC-MORE-HOME-FEATURE-021: QR 항목을 선택하면 QrHome 화면으로 이동한다

- 근거: `feature > 기능 바로가기`
- Given: MoreHome 화면이 표시되어 있다.
- When: 사용자가 `QR` 항목을 선택한다.
- Then: QrHome 화면으로 이동하는 동작이 한 번 실행된다.

### TC-MORE-HOME-FEATURE-022: 디데이 항목을 선택하면 DDayHome 화면으로 이동한다

- 근거: `feature > 기능 바로가기`
- Given: MoreHome 화면이 표시되어 있다.
- When: 사용자가 `디데이` 항목을 선택한다.
- Then: DDayHome 화면으로 이동하는 동작이 한 번 실행된다.

### TC-MORE-HOME-FEATURE-023: 체크리스트 항목을 선택하면 ChecklistHome 화면으로 이동한다

- 근거: `feature > 기능 바로가기`
- Given: MoreHome 화면이 표시되어 있다.
- When: 사용자가 `체크리스트` 항목을 선택한다.
- Then: ChecklistHome 화면으로 이동하는 동작이 한 번 실행된다.

### TC-MORE-HOME-FEATURE-024: 파일 항목을 선택하면 FileHome 화면으로 이동한다

- 근거: `feature > 기능 바로가기`
- Given: MoreHome 화면이 표시되어 있다.
- When: 사용자가 `파일` 항목을 선택한다.
- Then: FileHome 화면으로 이동하는 동작이 한 번 실행된다.

### TC-MORE-HOME-FEATURE-025: 플레이리스트 항목을 선택하면 PlaylistHome 화면으로 이동한다

- 근거: `feature > 기능 바로가기`
- Given: MoreHome 화면이 표시되어 있다.
- When: 사용자가 `플레이리스트` 항목을 선택한다.
- Then: PlaylistHome 화면으로 이동하는 동작이 한 번 실행된다.

## domain

### TC-MORE-HOME-DOMAIN-001: 로그인한 사용자 정보가 없으면 게스트 상태로 표시한다

- 근거: `domain > 계정 표시 상태`
- Given: 로그인한 사용자 정보가 없다.
- When: 사용자가 MoreHome 화면을 확인한다.
- Then: 계정 상태가 게스트로 표시되고 로그인 동작이 제공된다.

### TC-MORE-HOME-DOMAIN-002: 로그인한 사용자 정보가 있으면 사용자 상태로 표시한다

- 근거: `domain > 계정 표시 상태`
- Given: 이메일과 프로필 이미지를 가진 로그인 사용자 정보가 있다.
- When: 사용자가 MoreHome 화면을 확인한다.
- Then: 계정 상태가 사용자로 표시되고 해당 사용자 정보의 이메일과 프로필 이미지가 사용되며 로그아웃 동작이 제공된다.

### TC-MORE-HOME-DOMAIN-003: 계정 정보를 확인하기 전이나 확인에 실패하면 확인 중 상태를 유지한다

- 근거: `domain > 계정 표시 상태`
- Given: 계정 정보가 테스트 데이터의 상태다.
- When: 사용자가 MoreHome 화면을 확인한다.
- Then: 오류 안내 없이 계정 상태가 확인 중으로 유지되고 게스트나 사용자 상태로 확정되지 않는다.
- 테스트 데이터:

| 계정 정보 상태 |
| --- |
| 아직 계정 정보를 받지 못함 |
| 계정 정보 확인에 실패함 |

### TC-MORE-HOME-DOMAIN-004: 확인 중과 게스트 상태에서는 프로필을 선택해도 사진 선택 도구를 열지 않는다

- 근거: `domain > 프로필 사진 선택 조건`
- Given: 계정 상태가 테스트 데이터의 상태다.
- When: 사용자가 프로필을 누른다.
- Then: 사진 선택 도구를 여는 요청이 이루어지지 않는다.
- 테스트 데이터:

| 계정 상태 |
| --- |
| 확인 중 |
| 게스트 |

### TC-MORE-HOME-DOMAIN-005: 프로필 이미지 반영에 실패해도 계정 표시가 바뀌지 않는다

- 근거: `feature > 프로필 사진 선택`
- Given: 계정 상태가 사용자로 표시되어 있다.
- When: 프로필 이미지 반영 요청이 실패한다.
- Then: 오류 안내 없이 계정 상태와 표시하는 계정 정보가 그대로 유지된다.

## data

### TC-MORE-HOME-DATA-001: 로그아웃하면 저장된 로그인 세션이 제거된다

- 근거: `data > 로그아웃 시 데이터 처리`
- Given: 로그인 세션이 저장되어 있다.
- When: 사용자가 로그아웃한다.
- Then: 저장된 로그인 세션을 제거하는 요청이 한 번 이루어진다.

### TC-MORE-HOME-DATA-002: 고른 사진으로 저장된 프로필 이미지를 바꾸도록 요청한다

- 근거: `data > 프로필 이미지 반영 요청`
- Given: 계정 상태가 사용자다.
- When: 사용자가 사진 선택에서 사진을 고른다.
- Then: 고른 사진의 위치로 저장된 사용자 정보의 프로필 이미지를 바꾸는 요청이 한 번 이루어진다.
