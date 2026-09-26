# 스펙 문서

스펙은 제품이 보장하는 사용자 행위와 관찰 가능한 결과를 정의한다. 문서 종류 사이의 소유 범위와 우선순위는 [제품 문서](../README.md)를 따른다.

## 폴더 구분

스펙은 그 계약을 누가 지키는지에 따라 세 폴더로 나눈다. 같은 대상의 규칙이 여러 폴더에 걸치면 폴더마다 같은 slug의 문서를 두고, 각 문서는 자기 폴더가 소유하는 부분만 적은 뒤 나머지를 링크한다.

| 폴더 | 소유 범위 | 예 |
| --- | --- | --- |
| [`common`](./common) | 클라이언트와 서버가 함께 지키는 도메인 개념과 계약. 항목과 연결의 정의, 상태와 시각의 의미, 동기화·등록 때 주고받는 정보와 그 의미 | 항목 연결, 대표 태그, 동기화 대상과 충돌 기준 |
| [`client`](./client) | 앱이 보장하는 행위와 결과. 화면과 컴포넌트, 로컬 저장, 백그라운드 작업, 앱이 직접 부르는 외부 서비스 | 화면, 동기화 계기, 공휴일·날씨 조회 |
| [`server`](./server) | 서버(Supabase의 데이터베이스와 함수, 예약 작업)가 보장하는 처리. 요청 검증, 저장과 접근 제한, 예약 발송, 외부 서비스 중계 | 동기화 요청 처리, 일일 알림 발송 |

- `common`의 계약을 바꾸면 그 문서를 참조하는 `client`와 `server` 문서를 함께 확인한다.
- 앱 안에서만 성립하는 규칙은 서버가 같은 값을 저장하더라도 `client`에 둔다. 서버가 검증하거나 강제하는 규칙만 `server`에, 양쪽이 같은 의미로 해석해야 하는 값만 `common`에 둔다.
- 디자인과 테스트 케이스는 폴더를 나누지 않고 같은 slug로 스펙을 찾는다. slug가 여러 폴더에 있으면 `client` → `common` → `server` 순으로 기준 스펙을 찾는다.

## 서술 수준

스펙은 기획자와 제품 이해관계자가 읽는다. 구현 수준까지 적지 않되, 사용자가 겪는 결과가 하나로 정해지도록 다음을 기획자의 언어로 빠뜨리지 않고 적는다.

- 내용이 갱신되는 방식: 다른 화면이나 다른 기기에서 바뀐 내용이 사용자가 아무것도 하지 않아도 바로 보이는지, 화면에 들어올 때만 새로 보이는지, 목록 끝에 다가가면 이어서 더 보이는지
- 새로고침 방식: 사용자가 목록을 당겨서 새로고침하는지, 화면에 들어오거나 앱으로 돌아오거나 로그인했을 때 저절로 새로고침되는지, 새로고침이 없는지, 새로고침 중에 다시 요청하면 어떻게 되는지
- 표시 조건: 불러오는 중, 빈 상태, 오류, 내용이 각각 어떤 조건에서 보이는지. 빈 상태는 "불러오기가 끝났고, 고른 필터와 검색어에 맞는 항목이 하나도 없을 때"처럼 판정 조건으로 적는다.
- 입력이 저장되는 시점: 입력할 때마다 저장되는지, 입력을 멈추면 저장되는지, 저장 버튼을 눌러야 저장되는지
- 앱을 다시 열거나 다른 앱에 다녀왔을 때 유지되는 것과 처음으로 돌아가는 것

다음 용어는 스펙에 쓰지 않는다.

- 프로그래밍 언어, 프레임워크, 라이브러리, 플랫폼 SDK가 제공하는 용어. 예: 코루틴, Flow, ViewModel, Composable, UseCase, Repository, Room, DAO, WorkManager, Worker, Intent, SwiftUI, Ktor, Koin, DTO, Result, null, FCM 토큰
- 클래스, 함수, 모듈, 테이블, 컬럼, API 필드와 엔드포인트 이름
- 구현 구조를 드러내는 표현. 예: 캐시, 캐시 무효화, 구독, 스트림, 콜백, 트랜잭션, 커서, 페이지 키, 업서트

같은 결과를 사용자가 인식하는 말로 바꿔 쓴다. 예: 캐시 → 기기에 저장해 둔 자료, FCM 토큰 → 푸시 알림 수신 정보, UseCase 실패 → 기능 실패. 어떤 외부 서비스를 쓰는지가 제품 결정이면 서비스 이름(Google, Apple, Chrome, Gemini, YouTube, 네이버 지도, Supabase, Firebase Crashlytics, Google Analytics, Google Play Integrity 등)은 쓸 수 있지만, 그 서비스의 SDK 용어는 쓰지 않는다. 원격 분석 도구나 서비스 화면에 그대로 보이는 이벤트·화면·값 이름, 사용자가 직접 설치하거나 입력하는 도구와 주소 형식, 참고 링크의 원문 제목은 그대로 쓸 수 있다.

## 영역 구분

한 스펙 문서는 다음 영역으로 나뉘며, 실질적인 내용이 있는 영역만 작성한다.

- `feature`: 사용자가 할 수 있는 행동, 진입 조건, 성공·실패·빈 상태에서 보장할 결과
- `domain`: 화면과 무관하게 유지할 정책, 상태 전이, 정렬·선택·권한 규칙
- `data`: 저장, 조회, 동기화, 외부 데이터 경계에서 지킬 계약

하나의 요구사항이 행위와 표현을 함께 포함하면 행위와 결과는 스펙에, 그 행위를 제공하고 결과를 보여 주는 구체적인 방법은 [디자인 문서](../design/README.md)에 둔다.

## 목록

같은 대상의 문서가 여러 폴더에 있으면 한 행에 함께 둔다.

| 대상 | client | common | server | 디자인 |
| --- | --- | --- | --- | --- |
| Calendar 날짜 선택 | [client](./client/calendar-select.md) | — | — | [Calendar 날짜 선택 디자인](../design/calendar-select.md) |
| Calendar 컴포넌트 | [client](./client/calendar.md) | — | — | [Calendar 디자인](../design/calendar.md) |
| CalendarBarText 컴포넌트 | [client](./client/calendar-bar-text.md) | — | — | [CalendarBarText 디자인](../design/calendar-bar-text.md) |
| CalendarHome | [client](./client/calendar-home.md) | — | — | [CalendarHome 디자인](../design/calendar-home.md) |
| CalendarMonth 컴포넌트 | [client](./client/calendar-month.md) | — | — | [CalendarMonth 디자인](../design/calendar-month.md) |
| CalendarText 컴포넌트 | [client](./client/calendar-text.md) | — | — | [CalendarText 디자인](../design/calendar-text.md) |
| CalendarWeekOfMonth 컴포넌트 | [client](./client/calendar-week-of-month.md) | — | — | [CalendarWeekOfMonth 디자인](../design/calendar-week-of-month.md) |
| ChecklistHome 화면 | [client](./client/checklist-home.md) | — | — | [ChecklistHome 디자인](../design/checklist-home.md) |
| Chrome 로그인 이어받기 | [client](./client/chrome-session-import.md) | — | — | — |
| Contact 목록·상세 배치 | [client](./client/contact-list-detail.md) | — | — | [Contact 목록·상세 배치 디자인](../design/contact-list-detail.md) |
| ContactAdd 화면 | [client](./client/contact-add.md) | — | — | [ContactAdd 디자인](../design/contact-add.md) |
| ContactDetail 메모 탭 | [client](./client/contact-detail-memo.md) | — | — | [항목 상세 메모 탭 공통 디자인](../design/entity-detail-memo.md), [ContactDetail 디자인](../design/contact-detail.md) |
| ContactDetail 화면 | [client](./client/contact-detail.md) | — | — | [ContactDetail 디자인](../design/contact-detail.md) |
| ContactHome 화면 | [client](./client/contact-home.md) | — | — | [ContactHome 디자인](../design/contact-home.md) |
| DDayHome 화면 | [client](./client/dday-home.md) | — | — | [DDayHome 디자인](../design/dday-home.md) |
| DiaryMap 컴포넌트 | [client](./client/diary-map.md) | — | — | [DiaryMap 디자인](../design/diary-map.md) |
| FileHome 화면 | [client](./client/file-home.md) | — | — | [FileHome 디자인](../design/file-home.md) |
| Gemini 모델 목록 조회 | [client](./client/gemini-model-list.md) | — | — | — |
| Google 장소 검색 | [client](./client/google-place-search.md) | — | — | — |
| HolidayHome 화면 | [client](./client/holiday-home.md) | — | — | [HolidayHome 디자인](../design/holiday-home.md) |
| Memo 목록·상세 배치 | [client](./client/memo-list-detail.md) | — | — | [Memo 목록·상세 배치 디자인](../design/memo-list-detail.md) |
| MemoAdd 화면 | [client](./client/memo-add.md) | — | — | [MemoAdd 디자인](../design/memo-add.md) |
| MemoDetail 화면 | [client](./client/memo-detail.md) | — | — | [MemoDetail 디자인](../design/memo-detail.md) |
| MemoFinishedList 화면 | [client](./client/memo-finished-list.md) | — | — | [MemoFinishedList 디자인](../design/memo-finished-list.md) |
| MemoHome 목록 | [client](./client/memo-home.md) | — | — | [MemoHome 디자인](../design/memo-home.md) |
| MoreHome 화면 | [client](./client/more-home.md) | — | — | [MoreHome 디자인](../design/more-home.md) |
| MusicAdd 화면 | [client](./client/music-add.md) | — | — | [MusicAdd 디자인](../design/music-add.md) |
| MusicDetail 화면 | [client](./client/music-detail.md) | — | — | [MusicDetail 디자인](../design/music-detail.md) |
| PlaceAdd 화면 | [client](./client/place-add.md) | — | — | [PlaceAdd 디자인](../design/place-add.md) |
| PlaceDetail 메모 탭 | [client](./client/place-detail-memo.md) | — | — | [항목 상세 메모 탭 공통 디자인](../design/entity-detail-memo.md), [PlaceDetail 디자인](../design/place-detail.md) |
| PlaceDetail 화면 | [client](./client/place-detail.md) | — | — | [PlaceDetail 디자인](../design/place-detail.md) |
| PlaceHome 화면 | [client](./client/place-home.md) | — | — | [PlaceHome 디자인](../design/place-home.md) |
| Play Integrity 판정 기록 | [client](./client/play-integrity-logging.md) | [common](./common/play-integrity-logging.md) | [server](./server/play-integrity-logging.md) | — |
| Playlist 목록·상세 배치 | [client](./client/playlist-list-detail.md) | — | — | [Playlist 목록·상세 배치 디자인](../design/playlist-list-detail.md) |
| PlaylistHome 화면 | [client](./client/playlist-home.md) | — | — | [PlaylistHome 디자인](../design/playlist-home.md) |
| ProfileImageEdit 화면 | [client](./client/profile-image-edit.md) | — | — | [ProfileImageEdit 디자인](../design/profile-image-edit.md) |
| QrAdd 화면 | [client](./client/qr-add.md) | — | — | [QrAdd 디자인](../design/qr-add.md) |
| QrHome 화면 | [client](./client/qr-home.md) | — | — | [QrHome 디자인](../design/qr-home.md) |
| QrScan 화면 | [client](./client/qr-scan.md) | — | — | [QrScan 디자인](../design/qr-scan.md) |
| Routine 목록·상세 배치 | [client](./client/routine-list-detail.md) | — | — | [Routine 목록·상세 배치 디자인](../design/routine-list-detail.md) |
| RoutineAdd 화면 | [client](./client/routine-add.md) | — | — | [RoutineAdd 디자인](../design/routine-add.md) |
| RoutineHome 화면 | [client](./client/routine-home.md) | — | — | [RoutineHome 디자인](../design/routine-home.md) |
| SearchHome 화면 | [client](./client/search-home.md) | — | — | [SearchHome 디자인](../design/search-home.md) |
| Setting 목록·상세 배치 | [client](./client/setting-list-detail.md) | — | — | [Setting 목록·상세 배치 디자인](../design/setting-list-detail.md) |
| SettingBrowser 화면 | [client](./client/setting-browser.md) | — | — | [SettingBrowser 디자인](../design/setting-browser.md) |
| SettingDownload 화면 | [client](./client/setting-download.md) | — | — | [SettingDownload 디자인](../design/setting-download.md) |
| SettingGemini 화면 | [client](./client/setting-gemini.md) | — | — | [SettingGemini 디자인](../design/setting-gemini.md) |
| SettingHoliday 화면 | [client](./client/setting-holiday.md) | — | — | [SettingHoliday 디자인](../design/setting-holiday.md) |
| SettingHome 화면 | [client](./client/setting-home.md) | — | — | [SettingHome 디자인](../design/setting-home.md) |
| SettingMap 화면 | [client](./client/setting-map.md) | — | — | [SettingMap 디자인](../design/setting-map.md) |
| SwipeToFinishAndDelete 컴포넌트 | [client](./client/swipe-to-finish-and-delete.md) | — | — | [SwipeToFinishAndDelete 디자인](../design/swipe-to-finish-and-delete.md) |
| Tag 목록·상세 배치 | [client](./client/tag-list-detail.md) | — | — | [Tag 목록·상세 배치 디자인](../design/tag-list-detail.md) |
| TagAdd 화면 | [client](./client/tag-add.md) | — | — | [TagAdd 디자인](../design/tag-add.md), [태그 이모지 입력 디자인](../design/tag-emoji-input.md) |
| TagDetail 메모 탭 | [client](./client/tag-detail-memo.md) | — | — | [TagDetail 메모 탭 디자인](../design/tag-detail-memo.md) |
| TagDetail 웹 탭 | [client](./client/tag-detail-web.md) | — | — | [TagDetail 웹 탭 디자인](../design/tag-detail-web.md) |
| TagDetail 장소 탭 | [client](./client/tag-detail-place.md) | — | — | [TagDetail 장소 탭 디자인](../design/tag-detail-place.md) |
| TagDetail 화면 | [client](./client/tag-detail.md) | — | — | [TagDetail 디자인](../design/tag-detail.md), [태그 이모지 입력 디자인](../design/tag-emoji-input.md) |
| TagFinishedList 화면 | [client](./client/tag-finished-list.md) | — | — | [TagFinishedList 디자인](../design/tag-finished-list.md) |
| TagHome 목록 | [client](./client/tag-home.md) | — | — | [TagHome 디자인](../design/tag-home.md), [필터 Bottom Sheet 디자인](../design/filter-bottom-sheet.md) |
| TagMemoFinishedList 목록·상세 배치 | [client](./client/tag-memo-finished-list-detail.md) | — | — | [TagMemoFinishedList 목록·상세 배치 디자인](../design/tag-memo-finished-list-detail.md) |
| TagMemoFinishedList 화면 | [client](./client/tag-memo-finished-list.md) | — | — | [TagMemoFinishedList 디자인](../design/tag-memo-finished-list.md) |
| TopLevelNavigation | [client](./client/top-level-navigation.md) | — | — | [TopLevelNavigation 디자인](../design/top-level-navigation.md) |
| Web 목록·상세 배치 | [client](./client/web-list-detail.md) | — | — | [Web 목록·상세 배치 디자인](../design/web-list-detail.md) |
| WebAdd 화면 | [client](./client/web-add.md) | — | — | [WebAdd 디자인](../design/web-add.md) |
| WebDetail 메모 탭 | [client](./client/web-detail-memo.md) | — | — | [항목 상세 메모 탭 공통 디자인](../design/entity-detail-memo.md), [WebDetail 디자인](../design/web-detail.md) |
| WebDetail 화면 | [client](./client/web-detail.md) | — | — | [WebDetail 디자인](../design/web-detail.md) |
| WebHome 화면 | [client](./client/web-home.md) | — | — | [WebHome 디자인](../design/web-home.md) |
| 검색어 일치 판정 | [client](./client/search-match.md) | — | — | — |
| 계정 조회 | [client](./client/account.md) | — | — | — |
| 곡 다운로드 | [client](./client/music-download.md) | — | — | [PlaylistHome 디자인](../design/playlist-home.md) |
| 곡 다운로드 프록시 | [client](./client/music-download-proxy.md) | — | — | — |
| 공휴일 국가 설정 | [client](./client/holiday-country.md) | — | — | — |
| 공휴일 노출 설정 | [client](./client/holiday-visibility.md) | — | — | — |
| 공휴일 동기화 | [client](./client/holiday-fetch.md) | — | — | — |
| 권한 요청 공통 | [client](./client/permission.md) | — | — | — |
| 기기에 저장한 공휴일 | [client](./client/holiday-database.md) | — | — | — |
| 기능 실패 로깅 | [client](./client/usecase-failure-logging.md) | — | — | — |
| 날짜·시간 입력 컴포넌트 | [client](./client/diary-date-time-input.md) | — | — | [DiaryDateTimeInput 디자인](../design/diary-date-time-input.md) |
| 네이버 장소 검색 | [client](./client/naver-place-search.md) | — | — | — |
| 더보기 준비 중 화면 공통 | [client](./client/more-menu-placeholder.md) | — | — | [더보기 준비 중 화면 공통 디자인](../design/more-menu-placeholder.md) |
| 데스크톱 앱 데이터 저장 위치 | [client](./client/jvm-database-storage.md) | — | — | — |
| 데이터 동기화 | [client](./client/data-sync.md) | [common](./common/data-sync.md) | [server](./server/data-sync.md) | — |
| 로그인 | [client](./client/login.md) | [common](./common/login.md) | [server](./server/login.md) | [Login 디자인](../design/login.md) |
| 메모 Gemini 작성 도우미 | [client](./client/memo-gemini.md) | — | — | [메모 Gemini 작성 도우미 디자인](../design/memo-gemini.md) |
| 메모 대표 태그 | [client](./client/memo-primary-tag.md) | [common](./common/memo-primary-tag.md) | — | — |
| 메모 연락처 | [client](./client/memo-contact.md) | [common](./common/memo-contact.md) | — | — |
| 메모 연락처 입력 컴포넌트 | [client](./client/memo-contact-input.md) | — | — | [메모 연락처 입력 디자인](../design/memo-contact-input.md) |
| 메모 웹 | [client](./client/memo-web.md) | [common](./common/memo-web.md) | — | — |
| 메모 웹 입력 컴포넌트 | [client](./client/memo-web-input.md) | — | — | [메모 웹 입력 디자인](../design/memo-web-input.md) |
| 메모 장소 | [client](./client/memo-place.md) | [common](./common/memo-place.md) | — | — |
| 메모 장소 카드 컴포넌트 | [client](./client/memo-place-card.md) | — | — | [메모 장소 카드 디자인](../design/memo-place-card.md) |
| 메모 태그 | [client](./client/memo-tag.md) | [common](./common/memo-tag.md) | — | — |
| 메모 태그 입력 컴포넌트 | [client](./client/memo-tag-input.md) | — | — | [메모 태그 입력 디자인](../design/memo-tag-input.md) |
| 목록 빈 상태 | [client](./client/list-empty-state.md) | — | — | [목록 빈 상태 디자인](../design/list-empty-state.md) |
| 목록 정렬 | [client](./client/list-sort.md) | — | — | [목록 정렬 디자인](../design/list-sort.md) |
| 목록 필터 반영 | [client](./client/list-filter.md) | — | — | [필터 Bottom Sheet 디자인](../design/filter-bottom-sheet.md) |
| 목록·상세 배치 공통 | [client](./client/list-detail-pane.md) | — | — | [목록·상세 배치 공통 디자인](../design/list-detail-pane.md) |
| 새로고침 | [client](./client/sync-refresh.md) | — | — | [새로고침 디자인](../design/sync-refresh.md), [PlaceHome 디자인](../design/place-home.md) |
| 설명 입력 컴포넌트 | [client](./client/description-input.md) | — | — | [설명 입력 디자인](../design/description-input.md) |
| 알림 권한 요청 | [client](./client/notification-permission.md) | — | — | — |
| 앱 로깅 | [client](./client/app-logging.md) | — | — | — |
| 앱 이름 | [client](./client/app-name.md) | — | — | — |
| 완료 목록 공통 | [client](./client/finished-list.md) | — | — | [완료 목록 공통 디자인](../design/finished-list.md) |
| 웹 태그 | [client](./client/web-tag.md) | [common](./common/web-tag.md) | — | — |
| 위치 권한 요청 | [client](./client/location-permission.md) | — | — | — |
| 음력 자료 동기화 | [client](./client/lunar-fetch.md) | — | — | — |
| 일일 메모 알림 | [client](./client/daily-memo-notification.md) | [common](./common/daily-memo-notification.md) | [server](./server/daily-memo-notification.md) | [일일 메모 알림 디자인](../design/daily-memo-notification.md) |
| 입력 정지 대기 시간 | [client](./client/input-idle-delay.md) | — | — | — |
| 장소 검색 공통 | [client](./client/place-search.md) | — | — | — |
| 장소 검색 다이얼로그 컴포넌트 | [client](./client/place-search-dialog.md) | — | — | [장소 검색 다이얼로그 디자인](../design/place-search-dialog.md) |
| 장소 보기 모드 | [client](./client/place-view-mode.md) | — | — | [장소 보기 모드 디자인](../design/place-view-mode.md) |
| 장소 태그 | [client](./client/place-tag.md) | [common](./common/place-tag.md) | — | — |
| 제목 입력 | [client](./client/title-input.md) | — | — | [제목 입력 디자인](../design/title-input.md) |
| 카메라 권한 요청 | [client](./client/camera-permission.md) | — | — | — |
| 캘린더 메모 이동 | [client](./client/calendar-memo-move.md) | — | — | [캘린더 메모 이동 디자인](../design/calendar-memo-move.md) |
| 캘린더 메모 표시 | [client](./client/calendar-memo.md) | — | — | — |
| 캘린더 연락처 생일 표시 | [client](./client/calendar-contact-birthday.md) | — | — | — |
| 컬러 입력 컴포넌트 | [client](./client/diary-color-input.md) | — | — | [DiaryColorInput 디자인](../design/diary-color-input.md) |
| 태그 선택 입력 공통 | [client](./client/tag-select-input.md) | — | — | [태그 선택 입력 공통 디자인](../design/tag-select-input.md) |
| 태그 연결 | [client](./client/tag-link.md) | [common](./common/tag-link.md) | — | — |
| 태그 연결 입력 컴포넌트 | [client](./client/tag-link-input.md) | — | — | [태그 연결 입력 디자인](../design/tag-link-input.md) |
| 태그 필터 | [client](./client/tag-filter.md) | — | — | [태그 필터 디자인](../design/tag-filter.md), [필터 Bottom Sheet 디자인](../design/filter-bottom-sheet.md) |
| 파일 보관 | [client](./client/file-storage.md) | [common](./common/file-storage.md) | [server](./server/file-storage.md) | — |
| 페이지 조회 목록의 자리 표시 | [client](./client/paged-list-placeholder.md) | — | — | [페이지 조회 목록의 자리 표시 디자인](../design/paged-list-placeholder.md) |
| 푸시 알림 수신 등록 | [client](./client/fcm-token.md) | [common](./common/fcm-token.md) | [server](./server/fcm-token.md) | — |
| 프로필 이미지 변경 | [client](./client/profile-image.md) | [common](./common/profile-image.md) | [server](./server/profile-image.md) | — |
| 항목 상세 메모 탭 공통 | [client](./client/entity-detail-memo.md) | — | — | [항목 상세 메모 탭 공통 디자인](../design/entity-detail-memo.md) |
| 항목 상세 화면 공통 | [client](./client/entity-detail.md) | — | — | — |
| 항목 연결 공통 | [client](./client/entity-link.md) | [common](./common/entity-link.md) | — | — |
| 항목 추가 화면 공통 | [client](./client/entity-add.md) | — | — | [항목 추가 화면 공통 디자인](../design/entity-add.md) |
| 항목 태그 연결 공통 | [client](./client/entity-tag.md) | [common](./common/entity-tag.md) | — | — |
| 항목 태그 입력 컴포넌트 | [client](./client/entity-tag-input.md) | — | — | [항목 태그 입력 디자인](../design/entity-tag-input.md) |
| 현재 날씨 동기화 | [client](./client/weather-fetch.md) | — | — | — |
| 현재 위치 확인 | [client](./client/current-location.md) | — | — | — |
| 화면 조회 로깅 | [client](./client/screen-view-logging.md) | — | — | — |
