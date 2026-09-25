# CalendarText 컴포넌트 스펙

이 문서는 캘린더 아이템 안에 짧은 글을 표시하는 공용 컴포넌트 CalendarText의 사용자 관점 동작을 다룬다. 이 컴포넌트를 어느 아이템에 사용할지는 배치하는 화면의 스펙에서 정하고, 아이템의 구체적인 배치는 [CalendarWeekOfMonth 컴포넌트 디자인](../../design/calendar-week-of-month.md)에서 다룬다. 표시만 하는 컴포넌트로 사용자 데이터의 생성이나 저장이 없어 `data` 영역은 생략한다.

디자인: [CalendarText 컴포넌트 디자인](../../design/calendar-text.md)

## feature

### 글 표시

CalendarText는 배치하는 화면이 지정한 글을 표시한다.

### 넘치는 글 확인

글이 한 번에 다 보이지 않는 경우에도 사용자는 뒷부분이 생략되지 않은 전체 글을 확인할 수 있다.

## domain

### 표시 내용

CalendarText는 배치하는 화면이 지정한 글을 줄이거나 다른 내용으로 바꾸지 않는다.

### 색상 결정

배치하는 화면은 CalendarText의 배경색을 지정할 수 있다. 지정하지 않으면 CalendarText가 배치된 화면의 기본 글자색을 배경색으로 사용한다.

글자색은 배경색과 충분히 구분되어 읽을 수 있어야 한다.
