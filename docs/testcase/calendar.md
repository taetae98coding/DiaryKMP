# Calendar 테스트 케이스

기준 스펙: [Calendar 컴포넌트 스펙](../spec/client/calendar.md)

## feature

### TC-CALENDAR-FEATURE-001: 시작 달로 지정한 달의 캘린더를 여섯 주로 표시한다

- 근거: `feature > 달 표시`
- Given: 2026년 7월이 시작 달로 지정된다.
- When: Calendar가 표시된다.
- Then: 첫 주의 일요일인 6월 28일부터 여섯 번째 주의 토요일인 8월 8일까지 42개의 날짜 숫자가 순서대로 표시된다.

### TC-CALENDAR-FEATURE-002: 시작 달을 지정하지 않으면 오늘이 속한 달을 표시한다

- 근거: `feature > 시작 달`, `domain > 오늘 기준`
- Given: 시작 달이 지정되지 않는다.
- When: Calendar가 표시된다.
- Then: 사용자의 현재 시간대 기준 오늘이 속한 달의 1일이 포함된 주의 일요일부터 42개의 날짜 숫자가 순서대로 표시된다.

### TC-CALENDAR-FEATURE-003: 다음 달로 이동한다

- 근거: `feature > 달 이동`
- Given: 2026년 7월의 캘린더가 표시되어 있다.
- When: 사용자가 다음 달로 이동한다.
- Then: 2026년 8월의 캘린더가 표시된다.

### TC-CALENDAR-FEATURE-004: 이전 달로 이동한다

- 근거: `feature > 달 이동`
- Given: 2026년 7월의 캘린더가 표시되어 있다.
- When: 사용자가 이전 달로 이동한다.
- Then: 2026년 6월의 캘린더가 표시된다.

### TC-CALENDAR-FEATURE-005: 1년 1월에서 이전 달로 이동할 수 없다

- 근거: `domain > 이동 범위`
- Given: 1년 1월의 캘린더가 표시되어 있다.
- When: 사용자가 이전 달로 이동한다.
- Then: 1년 1월의 캘린더가 그대로 표시된다.

### TC-CALENDAR-FEATURE-006: 화면이 재생성되어도 보던 달이 유지된다

- 근거: `domain > 보던 달 유지`
- Given: 2026년 7월이 시작 달로 지정된 캘린더에서 사용자가 다음 달로 이동해 2026년 8월이 표시되어 있다.
- When: 화면이 재생성된다.
- Then: 2026년 8월의 캘린더가 표시된다.

### TC-CALENDAR-FEATURE-007: 일곱 요일이 일요일부터 토요일까지 순서대로 표시된다

- 근거: `feature > 요일 표시`
- Given: Calendar가 표시될 수 있다.
- When: Calendar가 표시된다.
- Then: 일요일부터 토요일까지 일곱 요일이 순서대로 표시된다.

### TC-CALENDAR-FEATURE-008: 달을 이동해도 요일 표시는 변하지 않는다

- 근거: `feature > 요일 표시`
- Given: 캘린더에 일요일부터 토요일까지 요일이 표시되어 있다.
- When: 사용자가 다음 달로 이동한다.
- Then: 요일은 일요일부터 토요일까지 같은 순서로 유지된다.

### TC-CALENDAR-FEATURE-009: 배치하는 화면이 지정한 아이템이 표시된다

- 근거: `feature > 아이템 표시`
- Given: 2026년 7월이 시작 달로 지정되고, 7월 6일부터 8일까지의 아이템 하나가 지정된다.
- When: Calendar가 표시된다.
- Then: 해당 아이템의 내용이 표시된다.

### TC-CALENDAR-FEATURE-010: 아이템을 지정하지 않으면 날짜만 표시된다

- 근거: `feature > 아이템 표시`
- Given: 2026년 7월이 시작 달로 지정되고, 아이템이 지정되지 않는다.
- When: Calendar가 표시된다.
- Then: 42개의 날짜 숫자만 표시되고 다른 아이템 내용은 표시되지 않는다.

### TC-CALENDAR-FEATURE-011: 아이템 지정이 바뀌면 표시가 갱신된다

- 근거: `domain > 아이템 지정`
- Given: 2026년 7월이 시작 달로 지정된 캘린더에 7월 6일부터 8일까지의 아이템 하나가 표시되어 있다.
- When: 배치하는 화면이 아이템을 다른 내용의 아이템으로 바꿔 지정한다.
- Then: 사용자가 별도 조작을 하지 않아도 바뀐 아이템의 내용이 표시되고 이전 아이템의 내용은 표시되지 않는다.

### TC-CALENDAR-FEATURE-012: 달을 이동하면 이동한 달과 겹치는 아이템만 표시된다

- 근거: `domain > 아이템 지정`
- Given: 2026년 7월이 시작 달로 지정된 캘린더에 7월 6일부터 8일까지의 아이템과 8월 3일부터 5일까지의 아이템이 지정되어 있다.
- When: 사용자가 다음 달로 이동한다.
- Then: 2026년 8월의 여섯 주와 겹치는 8월 아이템의 내용이 표시되고, 겹치지 않는 7월 아이템의 내용은 표시되지 않는다.

### 작성하지 않는 이유

배치하는 화면이 지정한 주요 날짜의 강조, 지정을 바꿨을 때의 강조 갱신, 지정하지 않았을 때 강조가 없는 결과, 중복 지정과 표시 중인 달에 없는 날짜 지정의 처리는 실제 화면의 의미 표현을 판정해야 하므로 블랙박스 유닛 테스트 케이스로 작성하지 않는다. 의미 표현을 결정적으로 식별할 수 있는 테스트 환경이 갖춰지면 검증한다.

아이템이 각 주의 어느 날짜 칸에 어떤 크기로 놓이는지는 [CalendarWeekOfMonth 테스트 케이스](calendar-week-of-month.md)의 작성하지 않는 이유를 따른다. Calendar 수준에서는 지정한 아이템이 표시되는지만 TC-CALENDAR-FEATURE-009부터 TC-CALENDAR-FEATURE-012로 확인한다.

아이템 영역의 스크롤을 맨 위로 되돌리는 요청이 각 주에 전달되는 것은 [CalendarMonth 테스트 케이스](calendar-month.md)의 TC-CALENDAR-MONTH-FEATURE-003과 [CalendarWeekOfMonth 테스트 케이스](calendar-week-of-month.md)의 TC-CALENDAR-WEEK-OF-MONTH-FEATURE-011로 확인하므로, Calendar 수준에서 같은 결과를 다시 검증하는 케이스는 두지 않는다.
