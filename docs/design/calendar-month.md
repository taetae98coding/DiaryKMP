# CalendarMonth 컴포넌트 디자인

기준 스펙: [CalendarMonth 컴포넌트 스펙](../spec/calendar-month.md)

## 여섯 주 배치

첫 번째 주부터 여섯 번째 주까지 위에서 아래로 배치한다.

여섯 주는 사용할 수 있는 세로 공간을 같은 높이로 나누어 차지한다.

각 주의 구체적인 배치와 표현은 [CalendarWeekOfMonth 컴포넌트 디자인](calendar-week-of-month.md)을 따른다.

## 여러 주에 걸친 아이템

스펙에 따라 각 주로 나뉜 아이템 조각의 날짜 칸과 줄 배치는 [CalendarWeekOfMonth 컴포넌트 디자인](calendar-week-of-month.md)의 아이템 배치를 따른다.

## 아이템 스크롤 되돌림

아이템 영역의 스크롤을 맨 위로 되돌릴 때 여섯 주는 같은 시점에 함께 되돌아간다. 되돌리는 표현은 [CalendarWeekOfMonth 컴포넌트 디자인](calendar-week-of-month.md)의 아이템 스크롤 되돌림을 따른다.
