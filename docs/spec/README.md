# 스펙 문서

스펙은 제품이 보장하는 사용자 행위와 관찰 가능한 결과를 정의한다. 문서 종류 사이의 소유 범위와 우선순위는 [제품 문서](../README.md)를 따른다.

## 영역 구분

한 스펙 문서는 다음 영역으로 나뉘며, 실질적인 내용이 있는 영역만 작성한다.

- `feature`: 사용자가 할 수 있는 행동, 진입 조건, 성공·실패·빈 상태에서 보장할 결과
- `domain`: 화면과 무관하게 유지할 정책, 상태 전이, 정렬·선택·권한 규칙
- `data`: 저장, 조회, 동기화, 외부 데이터 경계에서 지킬 계약

하나의 요구사항이 행위와 표현을 함께 포함하면 행위와 결과는 스펙에, 그 행위를 제공하고 결과를 보여 주는 구체적인 방법은 [디자인 문서](../design/README.md)에 둔다.

## 목록

| 스펙 | 디자인 |
| --- | --- |
| [계정 조회](./account.md) | — |
| [앱 로깅](./app-logging.md) | — |
| [CalendarBarText 컴포넌트](./calendar-bar-text.md) | [CalendarBarText 디자인](../design/calendar-bar-text.md) |
| [CalendarHome](./calendar-home.md) | [CalendarHome 디자인](../design/calendar-home.md) |
| [Calendar 날짜 선택](./calendar-select.md) | [Calendar 날짜 선택 디자인](../design/calendar-select.md) |
| [캘린더 메모 이동](./calendar-memo-move.md) | [캘린더 메모 이동 디자인](../design/calendar-memo-move.md) |
| [캘린더 메모 표시](./calendar-memo.md) | — |
| [캘린더 연락처 생일 표시](./calendar-contact-birthday.md) | — |
| [CalendarMonth 컴포넌트](./calendar-month.md) | [CalendarMonth 디자인](../design/calendar-month.md) |
| [CalendarText 컴포넌트](./calendar-text.md) | [CalendarText 디자인](../design/calendar-text.md) |
| [CalendarWeekOfMonth 컴포넌트](./calendar-week-of-month.md) | [CalendarWeekOfMonth 디자인](../design/calendar-week-of-month.md) |
| [Calendar 컴포넌트](./calendar.md) | [Calendar 디자인](../design/calendar.md) |
| [ChecklistHome 화면](./checklist-home.md) | [ChecklistHome 디자인](../design/checklist-home.md) |
| [ContactAdd 화면](./contact-add.md) | [ContactAdd 디자인](../design/contact-add.md) |
| [ContactDetail 화면](./contact-detail.md) | [ContactDetail 디자인](../design/contact-detail.md) |
| [ContactHome 화면](./contact-home.md) | [ContactHome 디자인](../design/contact-home.md) |
| [Contact 목록·상세 배치](./contact-list-detail.md) | [Contact 목록·상세 배치 디자인](../design/contact-list-detail.md) |
| [현재 위치 확인](./current-location.md) | — |
| [일일 메모 알림](./daily-memo-notification.md) | [일일 메모 알림 디자인](../design/daily-memo-notification.md) |
| [데이터 동기화](./data-sync.md) | — |
| [DDayHome 화면](./dday-home.md) | [DDayHome 디자인](../design/dday-home.md) |
| [설명 입력 컴포넌트](./description-input.md) | [설명 입력 디자인](../design/description-input.md) |
| [DiaryColorInput 컴포넌트](./diary-color-input.md) | [DiaryColorInput 디자인](../design/diary-color-input.md) |
| [DiaryDateTimeInput 컴포넌트](./diary-date-time-input.md) | [DiaryDateTimeInput 디자인](../design/diary-date-time-input.md) |
| [DiaryMap 컴포넌트](./diary-map.md) | [DiaryMap 디자인](../design/diary-map.md) |
| [항목 추가 화면 공통](./entity-add.md) | — |
| [항목 상세 화면 공통](./entity-detail.md) | — |
| [항목 연결 공통](./entity-link.md) | — |
| [항목 태그 연결 공통](./entity-tag.md) | — |
| [FileHome 화면](./file-home.md) | [FileHome 디자인](../design/file-home.md) |
| [완료 목록 공통](./finished-list.md) | — |
| [Gemini 모델 목록 조회](./gemini-model-list.md) | — |
| [Google 장소 검색](./google-place-search.md) | — |
| [공휴일 로컬 캐시](./holiday-database.md) | — |
| [공휴일 동기화](./holiday-fetch.md) | — |
| [HolidayHome 화면](./holiday-home.md) | [HolidayHome 디자인](../design/holiday-home.md) |
| [공휴일 노출 설정](./holiday-visibility.md) | — |
| [입력 정지 대기 시간](./input-idle-delay.md) | — |
| [JVM 데이터베이스 저장](./jvm-database-storage.md) | — |
| [목록·상세 배치 공통](./list-detail-pane.md) | [목록·상세 배치 공통 디자인](../design/list-detail-pane.md) |
| [목록 빈 상태](./list-empty-state.md) | [목록 빈 상태 디자인](../design/list-empty-state.md) |
| [목록 정렬](./list-sort.md) | [목록 정렬 디자인](../design/list-sort.md) |
| [목록 필터 반영](./list-filter.md) | [필터 Bottom Sheet 디자인](../design/filter-bottom-sheet.md) |
| [위치 권한 요청](./location-permission.md) | — |
| [Login 화면](./login.md) | [Login 디자인](../design/login.md) |
| [MemoAdd 화면](./memo-add.md) | [MemoAdd 디자인](../design/memo-add.md) |
| [MemoDetail 화면](./memo-detail.md) | [MemoDetail 디자인](../design/memo-detail.md) |
| [MemoFinishedList 화면](./memo-finished-list.md) | [MemoFinishedList 디자인](../design/memo-finished-list.md) |
| [메모 Gemini 작성 도우미](./memo-gemini.md) | [메모 Gemini 작성 도우미 디자인](../design/memo-gemini.md) |
| [MemoHome 목록](./memo-home.md) | [MemoHome 디자인](../design/memo-home.md) |
| [Memo 목록·상세 배치](./memo-list-detail.md) | [Memo 목록·상세 배치 디자인](../design/memo-list-detail.md) |
| [메모 연락처](./memo-contact.md) | — |
| [메모 연락처 입력 컴포넌트](./memo-contact-input.md) | [메모 연락처 입력 디자인](../design/memo-contact-input.md) |
| [메모 장소](./memo-place.md) | — |
| [메모 장소 카드 컴포넌트](./memo-place-card.md) | [메모 장소 카드 디자인](../design/memo-place-card.md) |
| [메모 대표 태그](./memo-primary-tag.md) | — |
| [메모 태그 입력 컴포넌트](./memo-tag-input.md) | [메모 태그 입력 디자인](../design/memo-tag-input.md) |
| [메모 태그](./memo-tag.md) | — |
| [메모 웹 입력 컴포넌트](./memo-web-input.md) | [메모 웹 입력 디자인](../design/memo-web-input.md) |
| [메모 웹](./memo-web.md) | — |
| [페이지 조회 목록의 자리 표시](./paged-list-placeholder.md) | [페이지 조회 목록의 자리 표시 디자인](../design/paged-list-placeholder.md) |
| [MoreHome 화면](./more-home.md) | [MoreHome 디자인](../design/more-home.md) |
| [더보기 준비 중 화면 공통](./more-menu-placeholder.md) | [더보기 준비 중 화면 공통 디자인](../design/more-menu-placeholder.md) |
| [MusicAdd 화면](./music-add.md) | [MusicAdd 디자인](../design/music-add.md) |
| [MusicDetail 화면](./music-detail.md) | [MusicDetail 디자인](../design/music-detail.md) |
| [네이버 장소 검색](./naver-place-search.md) | — |
| [알림 권한 요청](./notification-permission.md) | — |
| [권한 요청 공통](./permission.md) | — |
| [PlaceAdd 화면](./place-add.md) | [PlaceAdd 디자인](../design/place-add.md) |
| [PlaceDetail 화면](./place-detail.md) | [PlaceDetail 디자인](../design/place-detail.md) |
| [PlaceHome 화면](./place-home.md) | [PlaceHome 디자인](../design/place-home.md) |
| [장소 보기 모드](./place-view-mode.md) | [장소 보기 모드 디자인](../design/place-view-mode.md) |
| [장소 태그](./place-tag.md) | — |
| [장소 검색 공통](./place-search.md) | — |
| [장소 검색 다이얼로그 컴포넌트](./place-search-dialog.md) | [장소 검색 다이얼로그 디자인](../design/place-search-dialog.md) |
| [PlaylistHome 화면](./playlist-home.md) | [PlaylistHome 디자인](../design/playlist-home.md) |
| [Playlist 목록·상세 배치](./playlist-list-detail.md) | [Playlist 목록·상세 배치 디자인](../design/playlist-list-detail.md) |
| [프로필 이미지 변경](./profile-image.md) | — |
| [ProfileImageEdit 화면](./profile-image-edit.md) | [ProfileImageEdit 디자인](../design/profile-image-edit.md) |
| [QrHome 화면](./qr-home.md) | [QrHome 디자인](../design/qr-home.md) |
| [RoutineAdd 화면](./routine-add.md) | [RoutineAdd 디자인](../design/routine-add.md) |
| [RoutineHome 화면](./routine-home.md) | [RoutineHome 디자인](../design/routine-home.md) |
| [Routine 목록·상세 배치](./routine-list-detail.md) | [Routine 목록·상세 배치 디자인](../design/routine-list-detail.md) |
| [SearchHome 화면](./search-home.md) | [SearchHome 디자인](../design/search-home.md) |
| [검색어 일치 판정](./search-match.md) | — |
| [화면 조회 로깅](./screen-view-logging.md) | — |
| [SettingGemini 화면](./setting-gemini.md) | [SettingGemini 디자인](../design/setting-gemini.md) |
| [SettingHoliday 화면](./setting-holiday.md) | [SettingHoliday 디자인](../design/setting-holiday.md) |
| [SettingHome 화면](./setting-home.md) | [SettingHome 디자인](../design/setting-home.md) |
| [Setting 목록·상세 배치](./setting-list-detail.md) | [Setting 목록·상세 배치 디자인](../design/setting-list-detail.md) |
| [SettingMap 화면](./setting-map.md) | [SettingMap 디자인](../design/setting-map.md) |
| [SwipeToFinishAndDelete 컴포넌트](./swipe-to-finish-and-delete.md) | [SwipeToFinishAndDelete 디자인](../design/swipe-to-finish-and-delete.md) |
| [새로고침](./sync-refresh.md) | [새로고침 디자인](../design/sync-refresh.md), [PlaceHome 디자인](../design/place-home.md) |
| [태그 필터](./tag-filter.md) | [태그 필터 디자인](../design/tag-filter.md), [필터 Bottom Sheet 디자인](../design/filter-bottom-sheet.md) |
| [TagAdd 화면](./tag-add.md) | [TagAdd 디자인](../design/tag-add.md), [태그 이모지 입력 디자인](../design/tag-emoji-input.md) |
| [TagDetail 화면](./tag-detail.md) | [TagDetail 디자인](../design/tag-detail.md), [태그 이모지 입력 디자인](../design/tag-emoji-input.md) |
| [TagDetail 메모 탭](./tag-detail-memo.md) | [TagDetail 메모 탭 디자인](../design/tag-detail-memo.md) |
| [TagDetail 웹 탭](./tag-detail-web.md) | [TagDetail 웹 탭 디자인](../design/tag-detail-web.md) |
| [TagDetail 장소 탭](./tag-detail-place.md) | [TagDetail 장소 탭 디자인](../design/tag-detail-place.md) |
| [TagFinishedList 화면](./tag-finished-list.md) | [TagFinishedList 디자인](../design/tag-finished-list.md) |
| [TagHome 목록](./tag-home.md) | [TagHome 디자인](../design/tag-home.md), [필터 Bottom Sheet 디자인](../design/filter-bottom-sheet.md) |
| [태그 연결](./tag-link.md) | — |
| [태그 연결 입력 컴포넌트](./tag-link-input.md) | [태그 연결 입력 디자인](../design/tag-link-input.md) |
| [항목 태그 입력 컴포넌트](./entity-tag-input.md) | [항목 태그 입력 디자인](../design/entity-tag-input.md) |
| [태그 선택 입력 공통](./tag-select-input.md) | [태그 선택 입력 공통 디자인](../design/tag-select-input.md) |
| [Tag 목록·상세 배치](./tag-list-detail.md) | [Tag 목록·상세 배치 디자인](../design/tag-list-detail.md) |
| [TagMemoFinishedList 화면](./tag-memo-finished-list.md) | [TagMemoFinishedList 디자인](../design/tag-memo-finished-list.md) |
| [TagMemoFinishedList 목록·상세 배치](./tag-memo-finished-list-detail.md) | [TagMemoFinishedList 목록·상세 배치 디자인](../design/tag-memo-finished-list-detail.md) |
| [TopLevelNavigation](./top-level-navigation.md) | [TopLevelNavigation 디자인](../design/top-level-navigation.md) |
| [UseCase 실패 로깅](./usecase-failure-logging.md) | — |
| [WebAdd 화면](./web-add.md) | [WebAdd 디자인](../design/web-add.md) |
| [WebDetail 화면](./web-detail.md) | [WebDetail 디자인](../design/web-detail.md) |
| [WebHome 화면](./web-home.md) | [WebHome 디자인](../design/web-home.md) |
| [웹 태그](./web-tag.md) | — |
| [Web 목록·상세 배치](./web-list-detail.md) | [Web 목록·상세 배치 디자인](../design/web-list-detail.md) |
| [현재 날씨 동기화](./weather-fetch.md) | — |
