# 공통 여백과 간격 디자인

이 문서는 여러 화면과 컴포넌트가 함께 쓰는 여백·간격 값을 소유한다. 다른 디자인 문서는 같은 값을 숫자로 다시 적지 않고 이 문서의 이름으로 참조한다. 여백이 아닌 값과 속성이 둘 이상 모인 묶음은 [공통 스타일](./styles.md)이 소유한다.

이 문서는 사용자 행위를 정의하지 않는 공통 디자인 값만 다루므로 기준 스펙을 두지 않는다.

## 값

| 이름 | 값 | 쓰는 곳 |
| --- | --- | --- |
| 화면 가로 여백 | `16dp` | 화면 본문과 표시 영역 좌우 바깥 여백 |
| 화면 세로 여백 | `16dp` | 화면 본문과 표시 영역 위아래 바깥 여백 |
| 항목 간격 | `8dp` | 목록과 격자에서 같은 종류의 항목 사이 |
| 구성요소 간격 | `12dp` | 한 화면 안에서 서로 다른 입력이나 영역 사이 |
| 칩 영역 높이 | `150dp` | 선택한 대상을 칩으로 늘어놓는 입력 영역의 최소 높이 또는 고정 높이 |
| 고정 지도 높이 | `240dp` | 높이가 정해지지 않은 자리에 지도를 둘 때 지도 영역의 높이 |
| 선택 목록 높이 | `288dp` | 대화상자로 여는 선택 목록에서 목록이 차지하는 자리의 높이 |
| 선택 대화상자 여백 | 가장자리 `24dp`, 제목 아래 `16dp`, 내용 아래 `24dp` | 대화상자로 여는 선택 목록의 제목과 내용 둘레. Material 3 AlertDialog의 기본 여백과 같다 |
| Bottom Sheet 가로 여백 | `24dp` | Bottom Sheet 제목, 내용 영역, 선택 줄의 좌우 안쪽 여백 |
| Bottom Sheet 제목 세로 여백 | `12dp` | Bottom Sheet 제목의 위아래 안쪽 여백 |
| Bottom Sheet 아래 여백 | `16dp` | Bottom Sheet 내용 마지막 줄 아래 여백 |
| 카드 안쪽 여백 | `16dp` | 카드 가장자리와 카드 내용 사이 |
| 카드 줄 간격 | `4dp` | 카드 안에서 제목 줄과 그 아래 보조 줄 사이 |
| 컬러 원형 표시 크기 | `8dp` | 저장된 컬러로 채운 작은 원형 표시의 지름 |
| 컬러 원형 표시 간격 | `12dp` | 카드에서 컬러 원형 표시와 옆에 놓인 글 묶음 사이 |
| 캘린더 아이템 간격 | `2dp` | 캘린더 한 주의 아이템 영역과 Timetable 종일 영역의 가장자리 안쪽 여백, 아이템 사이 가로세로 간격 |
| 진행 표시 크기 | `24dp` | 버튼이나 아이콘 자리에서 동작이 진행 중임을 알리는 원형 진행 표시의 지름 |
| 플로팅 액션 버튼 아래 여백 | `88dp` | 플로팅 액션 버튼이 떠 있는 동안 목록과 격자 내용의 아래쪽 여백 |

## 사용 기준

같은 종류의 항목을 반복해 늘어놓는 목록, 격자, 칩 배치에는 `항목 간격`을 쓴다.

제목 입력과 설명 입력처럼 서로 다른 구성요소를 세로나 가로로 이어 배치할 때는 `구성요소 간격`을 쓴다.

화면 본문이 표시 영역 가장자리에 닿지 않게 두는 바깥 여백에는 `화면 가로 여백`과 `화면 세로 여백`을 쓴다.

선택한 대상을 칩으로 늘어놓는 입력 영역에는 `칩 영역 높이`를 쓴다. 안쪽 여백을 포함한 영역 전체가 이 높이를 기준으로 삼는다. 칩 영역이 이 높이를 어떻게 쓰는지는 칩 영역을 담는 자리가 높이를 정하는지에 따라 갈린다.

담는 자리가 높이를 정하지 않으면 `칩 영역 높이`를 최소 높이로 쓴다. 칩이 한 줄이어도 이 높이를 지키고, 칩이 늘어 칩 전체 높이가 이 높이를 넘으면 칩 영역이 그만큼 커진다. 칩 영역은 스크롤하지 않으며, 뒤에 이어지는 요소는 커진 만큼 밀린다.

담는 자리가 높이를 정하면 `칩 영역 높이`를 고정 높이로 쓴다. 칩이 늘어도 영역 높이가 바뀌지 않고, 칩 전체 높이가 영역을 넘으면 칩 영역 안에서 세로로 스크롤한다.

칩 영역을 담는 자리가 이미 세로로 스크롤하는 곳에서 칩 영역에 다시 스크롤을 두지 않는다. 같은 방향으로 스크롤하는 영역이 겹치면 사용자가 무엇을 잡았는지에 따라 밀거나 던지는 조작의 결과가 달라져, 칩 영역을 잡고 던졌을 때 화면은 움직이지 않고 칩만 움직이는 일이 생긴다.

칩 영역의 안쪽 여백은 좌우에 `화면 가로 여백`, 위아래에 `화면 세로 여백`을 쓴다. 스크롤하는 칩 영역에서는 이 여백이 칩과 함께 움직이므로, 스크롤하지 않은 상태에서는 첫 줄 위에, 끝까지 내리면 마지막 줄 아래에 `화면 세로 여백`만큼의 빈 자리가 남는다.

Bottom Sheet의 제목과 내용은 `Bottom Sheet 가로 여백`으로 좌우를 맞추고, 제목의 위아래에는 `Bottom Sheet 제목 세로 여백`, 내용 마지막 줄 아래에는 `Bottom Sheet 아래 여백`을 쓴다. 이 값들이 어떤 요소에 함께 묶여 쓰이는지는 [공통 스타일](./styles.md)의 `Bottom Sheet 제목`, `Bottom Sheet 내용`, `Bottom Sheet 영역`, `Bottom Sheet 선택 줄`이 소유한다.

카드 가장자리와 내용 사이에는 `카드 안쪽 여백`을 쓴다. 카드 종류가 달라도 같은 값을 쓰며, 묶음은 [공통 스타일](./styles.md)의 `카드 내용`이 소유한다. 다른 값을 쓰는 카드는 그 카드의 디자인 문서가 예외로 정한다. 지금 예외는 [MoreHome 화면 디자인](./more-home.md)의 계정 카드뿐이다.

대화상자로 여는 선택 목록은 확인 버튼이 없어도 제목과 내용 둘레에 `선택 대화상자 여백`을 쓴다. 제목은 위와 좌우에 가장자리 여백을 두고 아래에 제목 아래 여백을 두며, 내용은 좌우에 가장자리 여백을 두고 아래에 내용 아래 여백을 둔다.

대화상자로 여는 선택 목록의 목록 자리에는 `선택 목록 높이`를 쓴다. 항목 수나 검색 결과 수가 바뀌어도 대화상자의 크기가 바뀌지 않는다. 표시 영역이 좁아 그 높이를 다 쓸 수 없으면 쓸 수 있는 높이까지만 차지한다.

버튼이나 아이콘 자리에서 동작이 진행 중임을 알릴 때는 아이콘 자리를 원형 진행 표시로 바꾸고 `진행 표시 크기`로 그린다. 아이콘과 같은 크기라 전환해도 자리가 흔들리지 않는다.

플로팅 액션 버튼을 표시하는 화면의 목록과 격자는 버튼이 보이는 동안 내용 아래쪽에 `화면 세로 여백` 대신 `플로팅 액션 버튼 아래 여백`을 둔다. Material 3 기본 플로팅 액션 버튼의 높이 `56dp`, 버튼과 화면 가장자리 사이 `16dp`, `화면 세로 여백`을 더한 값이라, 끝까지 스크롤하면 마지막 항목이 버튼 위쪽에 놓여 가리지 않는다. 버튼을 감춘 동안에는 `화면 세로 여백`으로 되돌린다.

여기에 없는 크기가 필요하면 그 값을 쓰는 디자인 문서에서 정하고, 두 곳 이상에서 같은 값을 쓰게 되면 이 문서로 옮긴다.

## 이 문서를 참조하는 문서

- [CalendarWeekOfMonth 컴포넌트](./calendar-week-of-month.md)
- [칩 이름 표시](./chip.md)
- [ContactAdd 화면](./contact-add.md)
- [ContactDetail 화면](./contact-detail.md)
- [ContactHome 화면](./contact-home.md)
- [설명 입력 컴포넌트](./description-input.md)
- [DiaryColorInput 컴포넌트](./diary-color-input.md)
- [DiaryDateTimeInput 컴포넌트](./diary-date-time-input.md)
- [항목 추가 화면 공통](./entity-add.md)
- [항목 상세 메모 탭 공통](./entity-detail-memo.md)
- [FileAdd 화면](./file-add.md)
- [FileHome 화면](./file-home.md)
- [HolidayHome 화면](./holiday-home.md)
- [목록 빈 상태](./list-empty-state.md)
- [목록 정렬](./list-sort.md)
- [Login 화면](./login.md)
- [메모 본문 배치](./memo-form.md)
- [메모 Gemini 작성 도우미](./memo-gemini.md)
- [MemoHome 목록](./memo-home.md)
- [메모 장소 카드 컴포넌트](./memo-place-card.md)
- [메모 웹 입력 컴포넌트](./memo-web-input.md)
- [MoreHome 화면](./more-home.md)
- [MusicAdd 화면](./music-add.md)
- [MusicDetail 화면](./music-detail.md)
- [선택 목록 항목](./picker-row.md)
- [PlaceAdd 화면](./place-add.md)
- [장소 검색 다이얼로그](./place-search-dialog.md)
- [장소 보기 모드](./place-view-mode.md)
- [PlaylistHome 화면](./playlist-home.md)
- [ProfileImageEdit 화면](./profile-image-edit.md)
- [QrAdd 화면](./qr-add.md)
- [QrHome 화면](./qr-home.md)
- [RoutineAdd 화면](./routine-add.md)
- [SearchHome 화면](./search-home.md)
- [SettingBrowser 화면](./setting-browser.md)
- [SettingDownload 화면](./setting-download.md)
- [SettingGemini 화면](./setting-gemini.md)
- [SettingHoliday 화면](./setting-holiday.md)
- [SettingMap 화면](./setting-map.md)
- [공통 스타일](./styles.md)
- [TagAdd 화면](./tag-add.md)
- [TagDetail 메모 탭](./tag-detail-memo.md)
- [TagDetail 화면](./tag-detail.md)
- [태그 필터](./tag-filter.md)
- [TagHome 목록](./tag-home.md)
- [태그 선택 입력 공통](./tag-select-input.md)
- [Timetable 컴포넌트](./timetable.md)
- [WebDetail 화면](./web-detail.md)
- [WebHome 화면](./web-home.md)
