# RoutineHome 테스트 케이스

기준 스펙: [RoutineHome 화면 스펙](../spec/routine-home.md)

빈 상태의 판정과 행동은 [목록 빈 상태 스펙](../spec/list-empty-state.md)에, 당김으로 시작하는 동기화와 진행 표시는 [새로고침 스펙](../spec/sync-refresh.md)에 위임되어 있으며, 이 문서의 케이스는 RoutineHome에서 관찰하는 결과를 기준으로 한다. 공통 내비게이션에서 `루틴` 목적지로 이동하고 뒤로가 기본 목적지로 돌아오는 케이스는 [TopLevelNavigation 테스트 케이스](./top-level-navigation.md)에서, 루틴 추가 화면의 입력 케이스는 [RoutineAdd 테스트 케이스](./routine-add.md)에서, 목록과 상세를 함께 사용할 때의 케이스는 [Routine 목록·상세 배치 테스트 케이스](./routine-list-detail.md)에서, 화면과 무관한 새로고침 공통 규칙의 케이스는 [새로고침 테스트 케이스](./sync-refresh.md)에서 다룬다.

## feature

### TC-ROUTINE-HOME-FEATURE-001: 표시할 루틴이 없으면 빈 상태 안내를 표시한다

- 근거: `feature > 빈 상태`
- Given: 노출 기준을 만족하는 루틴이 하나도 없다.
- When: RoutineHome 화면이 표시된다.
- Then: 목록 자리에 아직 루틴이 없으며 새로 추가할 수 있음을 알리는 안내가 표시된다.

### TC-ROUTINE-HOME-FEATURE-002: 루틴 추가를 선택하면 RoutineAdd로 이동한다

- 근거: `feature > 루틴 추가로 이동`
- Given: RoutineHome 화면이 표시되어 있다.
- When: 사용자가 루틴 추가를 선택한다.
- Then: RoutineAdd 화면으로 한 번 이동한다.

### TC-ROUTINE-HOME-FEATURE-003: 추가 단축키를 사용하면 RoutineAdd로 이동한다

- 근거: `feature > 루틴 추가로 이동`
- Given: 물리 키보드를 사용할 수 있고 RoutineHome 화면이 표시되어 있다.
- When: 사용자가 추가 단축키를 누른다.
- Then: RoutineAdd 화면으로 한 번 이동한다.

### TC-ROUTINE-HOME-FEATURE-004: 빈 상태에서도 루틴 추가와 당겨서 새로고침을 실행할 수 있다

- 근거: `feature > 빈 상태`
- Given: RoutineHome 화면에 빈 상태 안내가 표시되어 있다.
- When: 사용자가 화면에서 실행할 수 있는 동작을 확인한다.
- Then: 루틴 추가 동작과 당겨서 새로고침 동작이 모두 제공된다.

### TC-ROUTINE-HOME-FEATURE-005: 새로고침이 진행되는 동안에도 빈 상태 안내를 유지한다

- 근거: `feature > 빈 상태`
- Given: RoutineHome 화면에 빈 상태 안내가 표시되어 있다.
- When: 새로고침이 진행 중임이 표시된다.
- Then: 빈 상태 안내가 그대로 표시된다.
