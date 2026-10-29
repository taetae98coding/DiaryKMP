# CalendarMonth 컴포넌트 스펙

이 문서는 캘린더에서 지정한 달의 여섯 주와 기간 아이템을 표시하는 CalendarMonth의 사용자 관점 동작과 정책을 다룬다. 이 컴포넌트를 어느 화면에 배치할지와 날짜 선택 같은 상호작용은 이 문서의 범위가 아니다. 표시만 하는 컴포넌트로 사용자 데이터의 생성이나 저장이 없어 `data` 영역은 생략한다.

디자인: [CalendarMonth 컴포넌트 디자인](../../design/calendar-month.md)

## feature

### 여섯 주 표시

CalendarMonth는 배치하는 화면이 지정한 달의 여섯 주를 표시한다.

각 주의 날짜와 기간 아이템 표시 정책은 [CalendarWeekOfMonth 컴포넌트 스펙](./calendar-week-of-month.md)을 따른다.

### 아이템 표시

배치하는 화면이 지정한 아이템은 여섯 주 각각에 전달된다.

각 주에는 전달받은 아이템 중 그 주와 하루라도 겹치는 아이템만 표시된다.

### 아이템 스크롤 되돌리기

배치하는 화면이 아이템 영역의 스크롤을 맨 위로 되돌리도록 요청하면 여섯 주 모두가 그 요청을 받는다.

각 주의 되돌림 결과는 [CalendarWeekOfMonth 컴포넌트 스펙](./calendar-week-of-month.md)의 아이템 스크롤 되돌리기를 따른다.

## domain

### 여섯 주 고정

CalendarMonth는 달과 관계없이 항상 여섯 주를 표시한다. 표시할 주는 지정한 달의 0번째 주부터 5번째 주까지다.

달의 주 수가 여섯보다 적으면 남는 주는 다음 달 날짜로만 구성된다. 주요 날짜를 포함한 이웃 달 날짜의 구분 정책은 [CalendarWeekOfMonth 컴포넌트 스펙](./calendar-week-of-month.md)의 이웃 달 날짜 구분을 따른다.

### 여러 주에 걸친 아이템

기간이 여러 주에 걸친 아이템은 겹치는 각 주에 [CalendarWeekOfMonth 컴포넌트 스펙](./calendar-week-of-month.md)의 아이템 기간 자르기에 따라 나뉘어 표시된다.

표시 중인 달의 여섯 주와 하루도 겹치지 않는 아이템은 어느 주에도 표시되지 않는다.
