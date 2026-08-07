# CalendarBarText 컴포넌트 스펙

이 문서는 캘린더 아이템 안에 색상 바와 짧은 글을 함께 표시하는 공용 컴포넌트 CalendarBarText의 사용자 관점 동작을 다룬다. 배경 위에 글을 얹어 표시하는 형태는 [CalendarText 컴포넌트 스펙](./calendar-text.md)에서 다루며, 어느 아이템에 어떤 형태를 사용할지는 배치하는 화면의 스펙에서 정한다. 아이템의 구체적인 배치는 [CalendarWeekOfMonth 컴포넌트 디자인](../design/calendar-week-of-month.md)에서 다룬다. 표시만 하는 컴포넌트로 사용자 데이터의 생성이나 저장이 없어 `data` 영역은 생략한다.

디자인: [CalendarBarText 컴포넌트 디자인](../design/calendar-bar-text.md)

## feature

### 글 표시

CalendarBarText는 배치하는 화면이 지정한 글을 색상 바와 함께 표시한다.

### 색상 구분

사용자는 글 옆의 색상 바로 그 아이템이 어떤 색상에 해당하는지 구분할 수 있다.

### 넘치는 글 확인

글이 한 번에 다 보이지 않는 경우에도 사용자는 뒷부분이 생략되지 않은 전체 글을 확인할 수 있다.

## domain

### 표시 내용

CalendarBarText는 배치하는 화면이 지정한 글을 줄이거나 다른 내용으로 바꾸지 않는다.

### 색상 결정

배치하는 화면은 색상 바의 색을 지정할 수 있다. 지정하지 않으면 CalendarBarText가 배치된 화면의 기본 글자색을 사용한다.

글자색은 지정한 색상과 무관하게 CalendarBarText가 배치된 화면의 기본 글자색을 사용한다. 색상 정보는 색상 바가 전달하므로 글은 배경과 관계없이 항상 읽을 수 있는 대비를 유지한다.
