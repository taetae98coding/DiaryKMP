# 디자인 문서

디자인 문서는 [스펙](../spec/README.md)이 정의한 사용자 행위와 제품 정책을 화면에서 제공하고 표현하는 구체적인 방법을 정의한다. 문서 종류 사이의 소유 범위와 우선순위는 [제품 문서](../README.md)를 따른다.

## 문서 소유권

- 컨트롤의 종류와 위치, 클릭·스와이프·키보드 같은 조작 수단
- 화면 구조, 적응형 배치, 크기와 간격
- 색상, 아이콘, 타이포그래피, 모션
- 로딩·빈 상태·오류·확인 결과의 구체적인 표시 방식
- 정확한 화면 문구와 접근성 이름

여러 문서가 함께 쓰는 여백과 간격 값은 [공통 여백과 간격](./dimens.md)이 소유한다. 다른 문서는 같은 값을 숫자로 다시 적지 않는다.

여러 문서가 함께 쓰는 시각 속성 묶음과 모양·불투명도 값은 [공통 스타일](./styles.md)이 이름을 붙여 소유한다. 다른 문서는 그 이름으로 참조하고, 코드는 같은 이름의 Style로 관리한다.

페이지 단위로 조회하는 목록에서 아직 준비되지 않은 자리의 표현은 [페이지 조회 목록의 자리 표시](./paged-list-placeholder.md)가 소유한다. 각 문서는 그 목록에서만 다른 비움 형태만 남긴다.

화면 본문을 차지하는 목록이 비어 있을 때의 표시 형태와 전환은 [목록 빈 상태](./list-empty-state.md)가 소유한다. 각 문서는 그 목록에서만 다른 아이콘과 문구만 남긴다.

사용자가 목록의 정렬 기준을 고르는 정렬 줄과 정렬 선택 Bottom Sheet의 구조와 문구는 [목록 정렬](./list-sort.md)이 소유한다. 각 문서는 그 화면에서 정렬 줄을 두는 자리만 남긴다.

아이콘만 표시하는 공통 버튼에 이름을 알려 주는 설명 표시는 [아이콘 버튼 설명](./icon-button-tooltip.md)이 소유한다. 각 문서는 그 버튼의 자리와 접근성 이름만 남긴다.

칩 하나가 이름을 표시하는 방식과 칩 하나가 차지할 수 있는 최대 폭은 [칩 이름 표시](./chip.md)가 소유한다. 각 문서는 그 칩에 무엇을 표시하는지와 색, 조작, 접근성 이름 문구만 남긴다.

대화상자로 여는 선택 목록에서 항목 하나의 해부구조, 글자와 간격, 조작, 접근성은 [선택 목록 항목](./picker-row.md)이 소유한다. 각 문서는 그 목록의 항목에 컬러 원형 표시, 보조 줄, 끝 쪽 버튼을 두는지와 그 내용만 남긴다.

목록 위 정렬 줄의 끝 쪽에서 짝이 되는 다른 목록으로 이동하는 진입 버튼의 해부구조와 조작, 접근성은 [목록 진입 버튼](./list-entry-button.md)이 소유한다. 각 문서는 그 버튼이 어느 목록으로 이동하는지와 라벨 문구만 남긴다.

조건에 따라 나타났다 사라지는 버튼이 나타나고 사라지는 표현은 [나타나고 사라지는 버튼](./button-visibility.md)이 소유한다. 각 문서는 그 버튼을 두는 조건과 자리만 남긴다.

화면 위쪽에 두는 검색 입력의 해부구조와 조작, 접근성 문구는 [검색 입력](./search-input.md)이 소유한다. 각 문서는 검색을 어디에 두고 결과를 어떻게 표시하는지와 자리 표시 문구만 남긴다.

항목의 제목을 한 줄로 입력하는 제목 입력의 구성, 라벨, 지우기 버튼, 소프트 키보드와 문구는 [제목 입력](./title-input.md)이 소유한다. 각 문서는 제목 입력을 본문 어느 자리에 두는지만 남긴다.

MemoAdd 화면과 MemoDetail 화면이 함께 쓰는 본문의 구성과 적응형 배치, 떠 있는 동작 버튼의 자리는 [메모 본문 배치](./memo-form.md)가 소유한다. 두 화면 문서는 화면마다 다른 진입별 표시와 버튼의 표시 조건, 문구만 남긴다.

두 화면이 함께 쓰는 작성 도우미의 여는 버튼과 다이얼로그 구조, 상태별 표시와 문구는 [메모 Gemini 작성 도우미](./memo-gemini.md)가 소유한다. 두 화면 문서는 그 버튼을 상단 바 어느 자리에 두는지만 남긴다.

태그를 고르는 입력들이 함께 쓰는 칩 영역과 추가 칩, 태그 선택 목록과 그 안의 검색·빈 상태·준비 상태 표현은 [태그 선택 입력 공통](./tag-select-input.md)이 소유한다. [항목 태그 입력 컴포넌트](./entity-tag-input.md), [메모 태그 입력 컴포넌트](./memo-tag-input.md), [태그 연결 입력 컴포넌트](./tag-link-input.md) 문서는 그 입력에서만 다른 칩 구성과 목록 항목의 추가 요소, 라벨과 문구만 남긴다.

WebAdd, WebDetail, PlaceAdd, PlaceDetail 화면 문서는 태그 입력을 폼의 어느 자리에 두는지와 화면마다 다른 반영 시점의 표현만 남긴다.

장소 목록을 목록 모드와 지도 모드로 함께 보여 주는 자리의 영역 분할과 적응형 배치, 보기 모드 전환 표현, 장소 격자와 장소 카드, 지도 핀은 [장소 보기 모드](./place-view-mode.md)가 소유한다. PlaceHome 화면과 TagDetail 장소 탭 문서는 전환 컨트롤을 두는 자리와 화면마다 다른 컨트롤, 문구만 남긴다.

새 항목 하나를 입력해 추가하는 화면들이 함께 쓰는 화면 구조, 표시 영역과 소프트 키보드 처리, 초점과 단축키, 진행과 피드백 표현은 [항목 추가 화면 공통](./entity-add.md)이 소유한다. 각 추가 화면 문서는 본문에 두는 입력의 종류와 순서, 그 화면에서만 다른 표현과 문구만 남긴다.

완료된 항목만 모아 보여 주는 목록 화면들이 함께 쓰는 화면 구조, 정렬 줄 자리, 목록과 빈 상태·새로고침 표현은 [완료 목록 공통](./finished-list.md)이 소유한다. 각 화면 문서는 상단 바 제목과 카드 표현, 제스처만 남긴다.

연락처·장소·웹 상세 화면의 메모 탭이 함께 쓰는 탭 안 구성, 메모 추가 버튼, 정렬 줄, 목록, 제스처와 피드백, 단축키, 문구는 [항목 상세 메모 탭 공통](./entity-detail-memo.md)이 소유한다. 각 상세 화면 문서는 탭 행과 페이지 영역의 배치, 떠 있는 버튼의 교체, 탭 접근성 이름만 남긴다.

여러 항목이 하나의 묶음으로 읽히는 세로 목록의 배경, 모양, 항목 사이 간격과 누름·선택 상태의 표현은 [묶음 목록 항목](./segmented-list-item.md)이 소유한다. 각 문서는 그 목록에 두는 항목과 순서, 문구만 남긴다.

각 디자인 문서는 상단에 `기준 스펙` 링크를 두고, 그 스펙이 소유한 행위와 정책을 다시 정의하지 않는다.

## 목록

- [나타나고 사라지는 버튼](./button-visibility.md)
- [CalendarBarText 컴포넌트](./calendar-bar-text.md)
- [CalendarHome](./calendar-home.md)
- [Calendar 날짜 선택](./calendar-select.md)
- [CalendarMonth 컴포넌트](./calendar-month.md)
- [CalendarText 컴포넌트](./calendar-text.md)
- [CalendarWeekOfMonth 컴포넌트](./calendar-week-of-month.md)
- [Calendar 컴포넌트](./calendar.md)
- [캘린더 메모 이동](./calendar-memo-move.md)
- [ChecklistHome 화면](./checklist-home.md)
- [칩 이름 표시](./chip.md)
- [ContactAdd 화면](./contact-add.md)
- [ContactDetail 화면](./contact-detail.md)
- [ContactHome 화면](./contact-home.md)
- [일일 메모 알림](./daily-memo-notification.md)
- [항목 추가 화면 공통](./entity-add.md)
- [항목 상세 메모 탭 공통](./entity-detail-memo.md)
- [Contact 목록·상세 배치](./contact-list-detail.md)
- [DDayHome 화면](./dday-home.md)
- [설명 입력 컴포넌트](./description-input.md)
- [DiaryColorInput 컴포넌트](./diary-color-input.md)
- [DiaryDateTimeInput 컴포넌트](./diary-date-time-input.md)
- [DiaryMap 컴포넌트](./diary-map.md)
- [공통 여백과 간격](./dimens.md)
- [공통 스타일](./styles.md)
- [필터 Bottom Sheet](./filter-bottom-sheet.md)
- [FileHome 화면](./file-home.md)
- [완료 목록 공통](./finished-list.md)
- [HolidayHome 화면](./holiday-home.md)
- [아이콘 버튼 설명](./icon-button-tooltip.md)
- [목록·상세 배치 공통](./list-detail-pane.md)
- [목록 빈 상태](./list-empty-state.md)
- [목록 정렬](./list-sort.md)
- [목록 진입 버튼](./list-entry-button.md)
- [항목 태그 입력 컴포넌트](./entity-tag-input.md)
- [Login 화면](./login.md)
- [MemoAdd 화면](./memo-add.md)
- [MemoDetail 화면](./memo-detail.md)
- [MemoFinishedList 화면](./memo-finished-list.md)
- [메모 본문 배치](./memo-form.md)
- [메모 Gemini 작성 도우미](./memo-gemini.md)
- [MemoHome 목록](./memo-home.md)
- [Memo 목록·상세 배치](./memo-list-detail.md)
- [메모 연락처 입력 컴포넌트](./memo-contact-input.md)
- [메모 장소 카드 컴포넌트](./memo-place-card.md)
- [메모 태그 입력 컴포넌트](./memo-tag-input.md)
- [메모 웹 입력 컴포넌트](./memo-web-input.md)
- [페이지 조회 목록의 자리 표시](./paged-list-placeholder.md)
- [선택 목록 항목](./picker-row.md)
- [MoreHome 화면](./more-home.md)
- [더보기 준비 중 화면 공통](./more-menu-placeholder.md)
- [MusicAdd 화면](./music-add.md)
- [MusicDetail 화면](./music-detail.md)
- [PlaceAdd 화면](./place-add.md)
- [PlaceDetail 화면](./place-detail.md)
- [PlaceHome 화면](./place-home.md)
- [장소 보기 모드](./place-view-mode.md)
- [장소 검색 다이얼로그](./place-search-dialog.md)
- [PlaylistHome 화면](./playlist-home.md)
- [Playlist 목록·상세 배치](./playlist-list-detail.md)
- [ProfileImageEdit 화면](./profile-image-edit.md)
- [QrAdd 화면](./qr-add.md)
- [QrHome 화면](./qr-home.md)
- [QrScan 화면](./qr-scan.md)
- [RoutineAdd 화면](./routine-add.md)
- [RoutineHome 화면](./routine-home.md)
- [Routine 목록·상세 배치](./routine-list-detail.md)
- [SearchHome 화면](./search-home.md)
- [검색 입력](./search-input.md)
- [묶음 목록 항목](./segmented-list-item.md)
- [SettingBrowser 화면](./setting-browser.md)
- [SettingDownload 화면](./setting-download.md)
- [SettingGemini 화면](./setting-gemini.md)
- [SettingHoliday 화면](./setting-holiday.md)
- [SettingHome 화면](./setting-home.md)
- [Setting 목록·상세 배치](./setting-list-detail.md)
- [SettingMap 화면](./setting-map.md)
- [SwipeToFinishAndDelete 컴포넌트](./swipe-to-finish-and-delete.md)
- [새로고침](./sync-refresh.md)
- [태그 필터](./tag-filter.md)
- [TagAdd 화면](./tag-add.md)
- [TagDetail 화면](./tag-detail.md)
- [TagDetail 메모 탭](./tag-detail-memo.md)
- [TagDetail 웹 탭](./tag-detail-web.md)
- [TagDetail 장소 탭](./tag-detail-place.md)
- [태그 이모지 입력 컴포넌트](./tag-emoji-input.md)
- [TagFinishedList 화면](./tag-finished-list.md)
- [TagHome 목록](./tag-home.md)
- [태그 연결 입력 컴포넌트](./tag-link-input.md)
- [태그 선택 입력 공통](./tag-select-input.md)
- [Tag 목록·상세 배치](./tag-list-detail.md)
- [제목 입력](./title-input.md)
- [TagMemoFinishedList 화면](./tag-memo-finished-list.md)
- [TagMemoFinishedList 목록·상세 배치](./tag-memo-finished-list-detail.md)
- [TopLevelNavigation](./top-level-navigation.md)
- [WebAdd 화면](./web-add.md)
- [WebDetail 화면](./web-detail.md)
- [WebHome 화면](./web-home.md)
- [Web 목록·상세 배치](./web-list-detail.md)
