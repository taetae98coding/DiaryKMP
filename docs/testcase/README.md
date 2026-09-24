# 테스트 케이스 문서

테스트 케이스는 [스펙](../spec/README.md)이 확정한 입력과 외부에서 관찰 가능한 결과만 사용해, 코드를 보지 않고도 결과를 판정할 수 있는 블랙박스 관점으로 작성한다.

## 문서 소유권

- 실행 조건(`Given`), 하나의 행동이나 발생 조건(`When`), 관찰 가능한 결과(`Then`)
- 각 케이스의 고유 ID와 근거가 되는 스펙 절
- 조건과 기대 결과의 조합이 여러 개일 때의 테스트 데이터
- 스펙에는 있지만 현재 결정적으로 자동화할 수 없는 케이스의 `작성하지 않는 이유`

클래스, 함수, 레이어, 내부 상태, mock, fixture 같은 구현과 자동화 수단은 문서에 쓰지 않는다. 테스트 코드는 이 문서의 ID를 참조해 추적성을 유지한다.

## 케이스 항목

`Given`, `When`, `Then`, 고유 ID, `근거`는 모든 케이스에 쓴다. `테스트 데이터`와 `작성하지 않는 이유`는 필요할 때만 쓰고, `검증 대상` 항목은 쓰지 않는다.

`작성하지 않는 이유`에는 자동화하지 않는 이유와, 자동화하려면 무엇이 갖춰져야 하는지를 함께 적는다.

## 고유 ID

```text
TC-<SPEC>-<AREA>-<NNN>
```

- `SPEC`: 스펙 파일 slug의 대문자 kebab-case
- `AREA`: `FEATURE`, `DOMAIN`, `DATA` 중 하나
- `NNN`: 같은 `SPEC`과 `AREA`의 3자리 일련번호

삭제되거나 제거된 ID는 재사용하지 않는다.

## 목록

| 테스트 케이스 | 기준 스펙 |
| --- | --- |
| [계정 조회](./account.md) | [계정 조회](../spec/account.md) |
| [앱 로깅](./app-logging.md) | [앱 로깅](../spec/app-logging.md) |
| [CalendarBarText](./calendar-bar-text.md) | [CalendarBarText 컴포넌트](../spec/calendar-bar-text.md) |
| [CalendarHome](./calendar-home.md) | [CalendarHome](../spec/calendar-home.md) |
| [캘린더 메모 이동](./calendar-memo-move.md) | [캘린더 메모 이동](../spec/calendar-memo-move.md) |
| [캘린더 메모 표시](./calendar-memo.md) | [캘린더 메모 표시](../spec/calendar-memo.md) |
| [캘린더 연락처 생일 표시](./calendar-contact-birthday.md) | [캘린더 연락처 생일 표시](../spec/calendar-contact-birthday.md) |
| [Chrome 로그인 이어받기](./chrome-session-import.md) | [Chrome 로그인 이어받기](../spec/chrome-session-import.md) |
| [CalendarMonth](./calendar-month.md) | [CalendarMonth 컴포넌트](../spec/calendar-month.md) |
| [Calendar 날짜 선택](./calendar-select.md) | [Calendar 날짜 선택](../spec/calendar-select.md) |
| [CalendarText](./calendar-text.md) | [CalendarText 컴포넌트](../spec/calendar-text.md) |
| [CalendarWeekOfMonth](./calendar-week-of-month.md) | [CalendarWeekOfMonth 컴포넌트](../spec/calendar-week-of-month.md) |
| [Calendar](./calendar.md) | [Calendar 컴포넌트](../spec/calendar.md) |
| [ChecklistHome](./checklist-home.md) | [ChecklistHome 화면](../spec/checklist-home.md) |
| [ContactAdd](./contact-add.md) | [ContactAdd 화면](../spec/contact-add.md) |
| [ContactDetail](./contact-detail.md) | [ContactDetail 화면](../spec/contact-detail.md) |
| [ContactHome](./contact-home.md) | [ContactHome 화면](../spec/contact-home.md) |
| [Contact 목록·상세 배치](./contact-list-detail.md) | [Contact 목록·상세 배치](../spec/contact-list-detail.md) |
| [현재 위치 확인](./current-location.md) | [현재 위치 확인](../spec/current-location.md) |
| [일일 메모 알림](./daily-memo-notification.md) | [일일 메모 알림](../spec/daily-memo-notification.md) |
| [데이터 동기화](./data-sync.md) | [데이터 동기화](../spec/data-sync.md) |
| [DDayHome](./dday-home.md) | [DDayHome 화면](../spec/dday-home.md) |
| [설명 입력 컴포넌트](./description-input.md) | [설명 입력 컴포넌트](../spec/description-input.md) |
| [DiaryColorInput](./diary-color-input.md) | [DiaryColorInput 컴포넌트](../spec/diary-color-input.md) |
| [DiaryDateTimeInput](./diary-date-time-input.md) | [DiaryDateTimeInput 컴포넌트](../spec/diary-date-time-input.md) |
| [DiaryMap](./diary-map.md) | [DiaryMap 컴포넌트](../spec/diary-map.md) |
| [FCM 토큰 등록](./fcm-token.md) | [FCM 토큰 등록](../spec/fcm-token.md) |
| [FileHome](./file-home.md) | [FileHome 화면](../spec/file-home.md) |
| [Gemini 모델 목록 조회](./gemini-model-list.md) | [Gemini 모델 목록 조회](../spec/gemini-model-list.md) |
| [Google 장소 검색](./google-place-search.md) | [Google 장소 검색](../spec/google-place-search.md) |
| [공휴일 로컬 캐시](./holiday-database.md) | [공휴일 로컬 캐시](../spec/holiday-database.md) |
| [공휴일 동기화](./holiday-fetch.md) | [공휴일 동기화](../spec/holiday-fetch.md) |
| [HolidayHome](./holiday-home.md) | [HolidayHome 화면](../spec/holiday-home.md) |
| [공휴일 노출 설정](./holiday-visibility.md) | [공휴일 노출 설정](../spec/holiday-visibility.md) |
| [JVM 데이터베이스 저장](./jvm-database-storage.md) | [JVM 데이터베이스 저장](../spec/jvm-database-storage.md) |
| [위치 권한 요청](./location-permission.md) | [위치 권한 요청](../spec/location-permission.md) |
| [Login](./login.md) | [Login 화면](../spec/login.md) |
| [MemoAdd](./memo-add.md) | [MemoAdd 화면](../spec/memo-add.md) |
| [MemoDetail](./memo-detail.md) | [MemoDetail 화면](../spec/memo-detail.md) |
| [MemoFinishedList](./memo-finished-list.md) | [MemoFinishedList 화면](../spec/memo-finished-list.md) |
| [메모 Gemini 작성 도우미](./memo-gemini.md) | [메모 Gemini 작성 도우미](../spec/memo-gemini.md) |
| [MemoHome 목록](./memo-home.md) | [MemoHome 목록](../spec/memo-home.md) |
| [Memo 목록·상세 배치](./memo-list-detail.md) | [Memo 목록·상세 배치](../spec/memo-list-detail.md) |
| [메모 연락처](./memo-contact.md) | [메모 연락처](../spec/memo-contact.md) |
| [메모 연락처 입력 컴포넌트](./memo-contact-input.md) | [메모 연락처 입력 컴포넌트](../spec/memo-contact-input.md) |
| [메모 장소 카드](./memo-place-card.md) | [메모 장소 카드 컴포넌트](../spec/memo-place-card.md) |
| [메모 장소](./memo-place.md) | [메모 장소](../spec/memo-place.md) |
| [메모 대표 태그](./memo-primary-tag.md) | [메모 대표 태그](../spec/memo-primary-tag.md) |
| [메모 태그 입력 컴포넌트](./memo-tag-input.md) | [메모 태그 입력 컴포넌트](../spec/memo-tag-input.md) |
| [메모 태그](./memo-tag.md) | [메모 태그](../spec/memo-tag.md) |
| [메모 웹 입력 컴포넌트](./memo-web-input.md) | [메모 웹 입력 컴포넌트](../spec/memo-web-input.md) |
| [메모 웹](./memo-web.md) | [메모 웹](../spec/memo-web.md) |
| [항목 태그 입력 컴포넌트](./entity-tag-input.md) | [항목 태그 입력 컴포넌트](../spec/entity-tag-input.md) |
| [MoreHome](./more-home.md) | [MoreHome 화면](../spec/more-home.md) |
| [MusicAdd](./music-add.md) | [MusicAdd 화면](../spec/music-add.md) |
| [MusicDetail](./music-detail.md) | [MusicDetail 화면](../spec/music-detail.md) |
| [곡 다운로드](./music-download.md) | [곡 다운로드](../spec/music-download.md) |
| [네이버 장소 검색](./naver-place-search.md) | [네이버 장소 검색](../spec/naver-place-search.md) |
| [알림 권한 요청](./notification-permission.md) | [알림 권한 요청](../spec/notification-permission.md) |
| [PlaceAdd](./place-add.md) | [PlaceAdd 화면](../spec/place-add.md) |
| [PlaceDetail](./place-detail.md) | [PlaceDetail 화면](../spec/place-detail.md) |
| [PlaceHome](./place-home.md) | [PlaceHome 화면](../spec/place-home.md) |
| [장소 태그](./place-tag.md) | [장소 태그](../spec/place-tag.md) |
| [장소 검색 다이얼로그](./place-search-dialog.md) | [장소 검색 다이얼로그 컴포넌트](../spec/place-search-dialog.md) |
| [PlaylistHome](./playlist-home.md) | [PlaylistHome 화면](../spec/playlist-home.md) |
| [Playlist 목록·상세 배치](./playlist-list-detail.md) | [Playlist 목록·상세 배치](../spec/playlist-list-detail.md) |
| [프로필 이미지 변경](./profile-image.md) | [프로필 이미지 변경](../spec/profile-image.md) |
| [ProfileImageEdit](./profile-image-edit.md) | [ProfileImageEdit 화면](../spec/profile-image-edit.md) |
| [QrHome](./qr-home.md) | [QrHome 화면](../spec/qr-home.md) |
| [RoutineAdd](./routine-add.md) | [RoutineAdd 화면](../spec/routine-add.md) |
| [RoutineHome](./routine-home.md) | [RoutineHome 화면](../spec/routine-home.md) |
| [Routine 목록·상세 배치](./routine-list-detail.md) | [Routine 목록·상세 배치](../spec/routine-list-detail.md) |
| [SearchHome](./search-home.md) | [SearchHome 화면](../spec/search-home.md) |
| [화면 조회 로깅](./screen-view-logging.md) | [화면 조회 로깅](../spec/screen-view-logging.md) |
| [SettingBrowser](./setting-browser.md) | [SettingBrowser 화면](../spec/setting-browser.md) |
| [SettingGemini](./setting-gemini.md) | [SettingGemini 화면](../spec/setting-gemini.md) |
| [SettingHoliday](./setting-holiday.md) | [SettingHoliday 화면](../spec/setting-holiday.md) |
| [SettingHome](./setting-home.md) | [SettingHome 화면](../spec/setting-home.md) |
| [Setting 목록·상세 배치](./setting-list-detail.md) | [Setting 목록·상세 배치](../spec/setting-list-detail.md) |
| [SettingMap](./setting-map.md) | [SettingMap 화면](../spec/setting-map.md) |
| [SwipeToFinishAndDelete 컴포넌트](./swipe-to-finish-and-delete.md) | [SwipeToFinishAndDelete 컴포넌트](../spec/swipe-to-finish-and-delete.md) |
| [새로고침](./sync-refresh.md) | [새로고침](../spec/sync-refresh.md) |
| [TagAdd](./tag-add.md) | [TagAdd 화면](../spec/tag-add.md) |
| [TagDetail](./tag-detail.md) | [TagDetail 화면](../spec/tag-detail.md) |
| [TagDetail 메모 탭](./tag-detail-memo.md) | [TagDetail 메모 탭](../spec/tag-detail-memo.md) |
| [TagDetail 웹 탭](./tag-detail-web.md) | [TagDetail 웹 탭](../spec/tag-detail-web.md) |
| [TagDetail 장소 탭](./tag-detail-place.md) | [TagDetail 장소 탭](../spec/tag-detail-place.md) |
| [TagFinishedList](./tag-finished-list.md) | [TagFinishedList 화면](../spec/tag-finished-list.md) |
| [TagHome 목록](./tag-home.md) | [TagHome 목록](../spec/tag-home.md) |
| [태그 연결](./tag-link.md) | [태그 연결](../spec/tag-link.md) |
| [태그 연결 입력 컴포넌트](./tag-link-input.md) | [태그 연결 입력 컴포넌트](../spec/tag-link-input.md) |
| [Tag 목록·상세 배치](./tag-list-detail.md) | [Tag 목록·상세 배치](../spec/tag-list-detail.md) |
| [TagMemoFinishedList](./tag-memo-finished-list.md) | [TagMemoFinishedList 화면](../spec/tag-memo-finished-list.md) |
| [TagMemoFinishedList 목록·상세 배치](./tag-memo-finished-list-detail.md) | [TagMemoFinishedList 목록·상세 배치](../spec/tag-memo-finished-list-detail.md) |
| [TopLevelNavigation](./top-level-navigation.md) | [TopLevelNavigation](../spec/top-level-navigation.md) |
| [UseCase 실패 로깅](./usecase-failure-logging.md) | [UseCase 실패 로깅](../spec/usecase-failure-logging.md) |
| [WebAdd](./web-add.md) | [WebAdd 화면](../spec/web-add.md) |
| [WebDetail](./web-detail.md) | [WebDetail 화면](../spec/web-detail.md) |
| [WebHome](./web-home.md) | [WebHome 화면](../spec/web-home.md) |
| [웹 태그](./web-tag.md) | [웹 태그](../spec/web-tag.md) |
| [Web 목록·상세 배치](./web-list-detail.md) | [Web 목록·상세 배치](../spec/web-list-detail.md) |
| [현재 날씨 동기화](./weather-fetch.md) | [현재 날씨 동기화](../spec/weather-fetch.md) |

다음 공통 스펙은 전용 테스트 케이스 문서를 두지 않고, 그 스펙을 사용하는 화면의 문서에 케이스를 둔다.

- [더보기 준비 중 화면 공통 스펙](../spec/more-menu-placeholder.md) → [QrHome](./qr-home.md), [DDayHome](./dday-home.md), [ChecklistHome](./checklist-home.md), [FileHome](./file-home.md)
  - 네 화면이 같은 공통 규칙의 케이스를 각자의 제목 문구와 진입 항목에 맞춰 각각 갖는다.
- [태그 필터 스펙](../spec/tag-filter.md) → [MemoHome 목록](./memo-home.md), [CalendarHome](./calendar-home.md)
- [태그 선택 입력 공통 스펙](../spec/tag-select-input.md) → [메모 태그 입력 컴포넌트](./memo-tag-input.md), [항목 태그 입력 컴포넌트](./entity-tag-input.md), [태그 연결 입력 컴포넌트](./tag-link-input.md)
  - 세 입력 문서가 공통 스펙의 같은 절을 각자의 대상과 문구에 맞춰 갖는다.
- [페이지 조회 목록의 자리 표시 스펙](../spec/paged-list-placeholder.md) → [ContactHome](./contact-home.md), [TagHome 목록](./tag-home.md), [TagFinishedList](./tag-finished-list.md), [TagDetail 메모 탭](./tag-detail-memo.md), [TagDetail 웹 탭](./tag-detail-web.md), [TagDetail 장소 탭](./tag-detail-place.md), [TagMemoFinishedList](./tag-memo-finished-list.md), [MemoHome 목록](./memo-home.md), [MemoFinishedList](./memo-finished-list.md), [PlaceHome](./place-home.md), [SearchHome](./search-home.md), [메모 장소 카드](./memo-place-card.md), [메모 태그 입력 컴포넌트](./memo-tag-input.md), [메모 웹 입력 컴포넌트](./memo-web-input.md), [메모 연락처 입력 컴포넌트](./memo-contact-input.md), [태그 연결 입력 컴포넌트](./tag-link-input.md), [항목 태그 입력 컴포넌트](./entity-tag-input.md), [WebHome](./web-home.md), [PlaylistHome](./playlist-home.md)
- [목록 빈 상태 스펙](../spec/list-empty-state.md) → [ContactHome](./contact-home.md), [TagHome 목록](./tag-home.md), [TagFinishedList](./tag-finished-list.md), [TagDetail 메모 탭](./tag-detail-memo.md), [TagDetail 웹 탭](./tag-detail-web.md), [TagDetail 장소 탭](./tag-detail-place.md), [TagMemoFinishedList](./tag-memo-finished-list.md), [MemoHome 목록](./memo-home.md), [MemoFinishedList](./memo-finished-list.md), [RoutineHome](./routine-home.md), [PlaceHome](./place-home.md), [WebHome](./web-home.md), [PlaylistHome](./playlist-home.md)
- [목록 필터 반영 스펙](../spec/list-filter.md) → [TagHome 목록](./tag-home.md), [MemoHome 목록](./memo-home.md)
  - 두 문서가 각자의 필터와 목록 항목에 맞춰 같은 공통 규칙의 케이스를 각각 갖는다.
- [목록 정렬 스펙](../spec/list-sort.md) → [MemoHome 목록](./memo-home.md), [WebHome](./web-home.md), [ContactHome](./contact-home.md)
  - 메모 목록의 세 정렬은 [MemoHome 목록](./memo-home.md)이, 그 밖의 목록이 함께 쓰는 두 정렬은 [WebHome](./web-home.md)이 대표로 갖는다. 제목순 대신 이름순을 쓰는 연락처 목록은 [ContactHome](./contact-home.md)이 갖는다. 나머지 목록은 각자의 정렬 케이스를 두지 않는다.
- [입력 정지 대기 시간 스펙](../spec/input-idle-delay.md) → [메모 태그 입력 컴포넌트](./memo-tag-input.md), [PlaceAdd](./place-add.md), [장소 검색 다이얼로그 컴포넌트](./place-search-dialog.md)
- [검색어 일치 판정 스펙](../spec/search-match.md) → [SearchHome](./search-home.md), [메모 태그 입력 컴포넌트](./memo-tag-input.md), [메모 장소 카드](./memo-place-card.md), [메모 웹 입력 컴포넌트](./memo-web-input.md), [메모 연락처 입력 컴포넌트](./memo-contact-input.md), [태그 연결 입력 컴포넌트](./tag-link-input.md), [항목 태그 입력 컴포넌트](./entity-tag-input.md)
  - `검색어 반영 시점`의 케이스는 [메모 태그 입력 컴포넌트](./memo-tag-input.md)와 [SearchHome](./search-home.md)이 대표로 갖는다. 같은 정책을 쓰는 나머지 목록은 각자의 검색 케이스만 둔다.
- [장소 보기 모드 스펙](../spec/place-view-mode.md) → [PlaceHome](./place-home.md), [TagDetail 장소 탭](./tag-detail-place.md)
- [권한 요청 공통 스펙](../spec/permission.md) → [알림 권한 요청](./notification-permission.md), [위치 권한 요청](./location-permission.md)
  - 두 권한 문서가 공통 스펙의 요청 시점, 요청 기준, 요청 결과 케이스를 각자의 권한과 요청 지점에 맞춰 각각 갖는다.
  - 두 문서가 각자의 기본 보기 모드와 노출 대상 장소, 현재 위치 확인 시점에 맞춰 같은 공통 규칙의 케이스를 각각 갖는다.
- [항목 상세 화면 공통 스펙](../spec/entity-detail.md) → [ContactDetail](./contact-detail.md), [MemoDetail](./memo-detail.md), [MusicDetail](./music-detail.md), [PlaceDetail](./place-detail.md), [TagDetail](./tag-detail.md), [WebDetail](./web-detail.md)
  - 각 상세 화면 문서가 공통 스펙의 절을 자기 화면의 입력과 동작에 맞춰 각각 갖는다.
- [목록·상세 배치 공통 스펙](../spec/list-detail-pane.md) → [Contact](./contact-list-detail.md), [Memo](./memo-list-detail.md), [Playlist](./playlist-list-detail.md), [Routine](./routine-list-detail.md), [Setting](./setting-list-detail.md), [Tag](./tag-list-detail.md), [TagMemoFinishedList](./tag-memo-finished-list-detail.md), [Web](./web-list-detail.md) 목록·상세 배치
