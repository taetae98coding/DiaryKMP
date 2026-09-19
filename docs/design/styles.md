# 공통 스타일 디자인

이 문서는 여러 화면과 컴포넌트가 함께 쓰는 시각 속성 묶음에 이름을 붙여 소유한다. 여백과 간격 하나짜리 값은 [공통 여백과 간격](./dimens.md)이 소유하고, 이 문서는 속성이 둘 이상 모인 묶음과 모양·불투명도처럼 여백이 아닌 값을 소유한다. 다른 디자인 문서는 같은 속성을 다시 적지 않고 이 문서의 이름으로 참조한다.

이 문서는 사용자 행위를 정의하지 않는 공통 디자인 값만 다루므로 기준 스펙을 두지 않는다.

## 스타일

| 이름 | 속성 | 쓰는 곳 |
| --- | --- | --- |
| Bottom Sheet 제목 | 폭 전체를 채우고, 좌우에 [Bottom Sheet 가로 여백](./dimens.md), 위아래에 [Bottom Sheet 제목 세로 여백](./dimens.md)을 안쪽 여백으로 둔다. 글자는 큰 제목 강조 스타일이다 | 필터, 정렬 선택, 표시 방식 선택, 표시 범위 Bottom Sheet의 제목 |
| Bottom Sheet 내용 | 폭 전체를 채우고, 마지막 줄 아래에 [Bottom Sheet 아래 여백](./dimens.md)을 둔다. 좌우 여백은 두지 않고 안에 놓이는 영역과 줄이 각자 둔다 | 제목 아래에서 스크롤하는 내용 영역 |
| Bottom Sheet 영역 | 폭 전체를 채우고, 좌우에 [Bottom Sheet 가로 여백](./dimens.md)을 안쪽 여백으로 두어 내용의 좌우를 제목과 맞춘다 | 유무 필터 영역, 태그 필터 소제목과 칩 영역, 영역 사이 구분선 |
| Bottom Sheet 선택 줄 | 최소 높이 `56dp`이고, 좌우에 [Bottom Sheet 가로 여백](./dimens.md)을 안쪽 여백으로 둔다. 누르는 동안 드러나는 배경은 여백을 넘어 Bottom Sheet의 좌우 끝까지 채운다 | 정렬, 표시 방식, 표시 범위를 고르는 줄 |
| 카드 내용 | 폭 전체를 채우고, 네 방향에 [카드 안쪽 여백](./dimens.md)을 둔다 | 메모, 태그, 장소, 웹, 연락처, 곡 카드와 SettingHoliday 공휴일 항목, SettingGemini 모델 선택 줄 |
| 흐림 | 불투명도 `38%` | 적용되지 않는 동안의 태그 필터 영역, 이동 중인 캘린더 메모 조각 |
| 캘린더 아이템 모양 | 모서리를 `4dp` 둥글게 깎은 사각형 | CalendarText의 배경, 캘린더 메모 이동 고스트의 그림자 |

## 사용 기준

같은 자리에 놓이는 요소가 좌우를 맞춰야 하면 각 요소에 여백 값을 따로 적지 않고 같은 스타일을 준다. `Bottom Sheet 제목`, `Bottom Sheet 영역`, `Bottom Sheet 선택 줄`이 같은 가로 여백을 쓰는 이유다.

줄 전체가 하나의 누를 수 있는 대상인 줄에는 `Bottom Sheet 선택 줄`을 쓴다. 최소 터치 크기만 지키면 되는 스위치 행은 이 스타일을 쓰지 않고 [필터 Bottom Sheet 디자인](./filter-bottom-sheet.md)의 `항목 줄의 누름 표현`만 따른다.

`흐림`은 요소를 숨기지 않고 지금 조작할 수 없거나 자리를 옮기는 중임을 알릴 때 쓴다. 비활성 상태를 Material 컴포넌트가 스스로 표현하는 곳에는 겹쳐 쓰지 않는다.

여기에 없는 묶음이 필요하면 그 묶음을 쓰는 디자인 문서에서 정하고, 두 문서 이상에서 같은 묶음을 쓰게 되면 이 문서로 옮긴다. 이름은 값이 아니라 쓰는 자리와 역할이 드러나게 짓는다. 옮기는 절차는 `design-wave` 스킬의 `공통 디자인 요소 이름 짓기`가 소유한다.

## 이 문서를 참조하는 문서

- [필터 Bottom Sheet](./filter-bottom-sheet.md)
- [목록 정렬](./list-sort.md)
- [WebDetail 화면](./web-detail.md)
- [TagDetail 화면](./tag-detail.md)
- [MemoHome 목록](./memo-home.md)
- [TagHome 목록](./tag-home.md)
- [WebHome 화면](./web-home.md)
- [장소 보기 모드](./place-view-mode.md)
- [CalendarText 컴포넌트](./calendar-text.md)
- [캘린더 메모 이동](./calendar-memo-move.md)
