# 앱 동작 디자인

기준 스펙: [앱 동작 제공 스펙](../spec/client/app-action.md), [AI 도구 제공 공통 스펙](../spec/client/ai-tool.md)

이 문서는 Android AI 도우미와 iOS Siri·단축어가 앱의 동작을 보여 줄 때 쓰는 이름, 설명, 입력값 이름, 결과 문구와 항목 표시, Siri 바로 실행 문구를 소유한다. 각 동작이 무엇을 받고 무엇을 돌려주며 언제 실패하는지는 기준 스펙과 그 스펙이 가리키는 데스크톱 앱의 연결을 따르며, 이 문서는 그것을 다시 정하지 않는다.

앱 동작에는 앱 화면이 없다. 동작 목록, 입력값을 고르는 화면, 결과 카드의 배치와 크기, 색, 글자 크기는 운영체제가 그리며, 이 문서는 운영체제에 넘기는 글과 아이콘만 정한다.

## 동작 목록과 이름 규칙

앱 동작은 데스크톱 앱 연결의 도구와 하나씩 짝을 이루며 모두 137개다. 동작마다 데스크톱 도구 이름을 기준 열쇠로 삼아 Android 함수 이름과 iOS 이름을 정한다.

| 환경 | 보이는 이름 | 규칙 |
| --- | --- | --- |
| Android | 함수 이름 | 데스크톱 도구 이름의 밑줄을 없애고 두 번째 단어부터 첫 글자를 대문자로 쓴다. `memo_finished_list` → `memoFinishedList` |
| iOS | 동작 제목 | 아래 `기능 영역별 동작` 표의 한국어·영어 제목. 같은 종류의 동작은 다음 표의 형태를 따른다 |

iOS 동작 제목의 형태는 다음과 같다. `{대상}`은 `항목 종류 이름`의 이름이다. 영어 제목은 Apple의 동작 제목 관례대로 단어마다 첫 글자를 대문자로 쓴다.

| 동작 종류 | 한국어 | 영어 |
| --- | --- | --- |
| 목록 | `{목록 이름} 보기` | `Get {List Name}` |
| 하나 조회 | `{대상} 상세 보기` | `Get {Entity} Details` |
| 검색 | `{대상} 검색` | `Search {Entities}` |
| 추가 | `{대상} 추가` | `Add {Entity}` |
| 수정 | `{대상} 수정` | `Edit {Entity}` |
| 완료 | `{대상} 완료` | `Finish {Entity}` |
| 다시 시작 | `{대상} 다시 시작` | `Restart {Entity}` |
| 삭제 | `{대상} 삭제` | `Delete {Entity}` |
| 삭제 되돌리기 | `삭제한 {대상} 되돌리기` | `Restore Deleted {Entity}` |
| 연결 | `{대상}에 {연결 대상} 연결` | `Link {Linked} to {Entity}` |
| 연결 해제 | `{대상}에서 {연결 대상} 연결 해제` | `Unlink {Linked} from {Entity}` |

목록 이름과 동사는 앱 화면의 문구를 그대로 쓴다. 완료를 되돌리는 동작은 앱 화면과 같이 `다시 시작`, 곡의 아티스트는 앱 화면과 같이 `가수`라고 부른다.

## 설명의 언어

| 환경 | 언어 | 설명 |
| --- | --- | --- |
| Android | 영어 | 함수와 입력값의 설명은 데스크톱 도구의 설명을 그대로 쓴다. AI 도우미에 전달되는 설명은 기기 언어별로 바꿀 수 없으므로 하나의 언어로 두고, AI 도우미가 사용자의 언어로 옮겨 전한다 |
| iOS | 한국어, 영어 | 기기 언어가 한국어이면 한국어를, 그 밖의 언어이면 영어를 보여 준다. 앱 화면의 언어 기준과 같다 |

Android 입력값 이름은 데스크톱 도구의 입력값 이름을 그대로 쓴다. Android 결과의 각 값에 붙이는 설명은 데스크톱 결과의 값 이름을 풀어 쓴 영어 한 문장으로, 예를 들어 `title`은 `Title of the memo.`처럼 쓴다. 다음 페이지 위치에는 `Pass this value as cursor to read the next page. Absent on the last page.`를 쓴다.

iOS 동작 설명은 단축어 앱에서 동작을 펼쳤을 때 보이는 한두 문장이다. 아래 표의 설명을 그대로 쓴다.

## 항목 종류 이름

iOS에서 결과로 돌려주거나 입력값으로 고르는 항목의 종류 이름이다. 단축어 앱이 변수 종류와 선택 목록 제목에 쓴다. 아이콘은 단축어의 항목 표시와 [Spotlight 디자인](./spotlight.md)이 함께 쓰는 SF Symbol이다.

| 항목 | 한국어 | 영어 단수 / 복수 | 아이콘 |
| --- | --- | --- | --- |
| 메모 | `메모` | `Memo` / `Memos` | `note.text` |
| 태그 | `태그` | `Tag` / `Tags` | `tag` |
| 장소 | `장소` | `Place` / `Places` | `mappin.and.ellipse` |
| 웹 | `웹` | `Web` / `Webs` | `globe` |
| 연락처 | `연락처` | `Contact` / `Contacts` | `person.crop.circle` |
| 체크리스트 | `체크리스트` | `Checklist` / `Checklists` | `checklist` |
| 체크리스트 항목 | `체크리스트 항목` | `Checklist Item` / `Checklist Items` | 체크됨 `checkmark.square`, 체크 안 됨 `square` |
| QR | `QR` | `QR Code` / `QR Codes` | `qrcode` |
| 곡 | `곡` | `Song` / `Songs` | `music.note` |
| 디데이 | `디데이` | `D-Day` / `D-Days` | `hourglass` |
| 루틴 | `루틴` | `Routine` / `Routines` | `repeat` |
| 루틴 기록 | `루틴 기록` | `Routine Record` / `Routine Records` | `checkmark.circle` |
| 캘린더 메모 | `캘린더 메모` | `Calendar Memo` / `Calendar Memos` | `calendar` |
| 캘린더 디데이 | `캘린더 디데이` | `Calendar D-Day` / `Calendar D-Days` | `calendar` |
| 생일 | `생일` | `Birthday` / `Birthdays` | `gift` |
| 공휴일 | `공휴일` | `Holiday` / `Holidays` | `flag` |
| 음력 날짜 | `음력 날짜` | `Lunar Date` / `Lunar Dates` | `moon` |

메모, 태그, 장소, 디데이, 루틴, 캘린더 메모의 아이콘은 그 항목의 색으로, 캘린더 디데이의 아이콘은 그 디데이의 색으로 칠하고, 나머지는 앱의 주 색으로 칠한다.

## 기능 영역별 동작

각 칸의 위 줄은 한국어, 아래 줄은 영어다.

### 메모

| 도구 | Android 함수 | iOS 제목 | iOS 설명 |
| --- | --- | --- | --- |
| `memo_list` | `memoList` | 진행 중 메모 목록 보기<br/>Get In-Progress Memos | 메모 목록에 보이는, 완료하지 않은 메모를 가져옵니다. 앱에서 고른 필터가 적용됩니다.<br/>Gets the memos shown in the memo list that aren't finished. The filters chosen in the app apply. |
| `memo_finished_list` | `memoFinishedList` | 완료된 메모 목록 보기<br/>Get Finished Memos | 완료된 메모를 가져옵니다.<br/>Gets finished memos. |
| `memo_get` | `memoGet` | 메모 상세 보기<br/>Get Memo Details | 메모 하나를 연결된 태그, 장소, 웹, 연락처와 함께 가져옵니다.<br/>Gets a memo with its linked tags, places, webs and contacts. |
| `memo_add` | `memoAdd` | 메모 추가<br/>Add Memo | 새 메모를 추가합니다. 색을 고르지 않으면 무작위 색을 씁니다.<br/>Adds a new memo. A random color is used if you don't choose one. |
| `memo_update` | `memoUpdate` | 메모 수정<br/>Edit Memo | 메모의 제목, 설명, 색, 기간을 바꿉니다. 비워 둔 값은 그대로 둡니다.<br/>Changes the title, description, color or period of a memo. Values left empty stay the same. |
| `memo_copy` | `memoCopy` | 메모 복사<br/>Copy Memo | 메모를 연결과 함께 진행 중인 새 메모로 복사합니다.<br/>Copies a memo with its links into a new memo that isn't finished. |
| `memo_finish` | `memoFinish` | 메모 완료<br/>Finish Memo | 메모를 완료합니다.<br/>Marks a memo as finished. |
| `memo_restart` | `memoRestart` | 메모 다시 시작<br/>Restart Memo | 완료된 메모를 다시 시작합니다.<br/>Marks a finished memo as not finished. |
| `memo_delete` | `memoDelete` | 메모 삭제<br/>Delete Memo | 메모를 삭제합니다. 같은 메모로 ‘삭제한 메모 되돌리기’를 실행하면 되돌릴 수 있습니다.<br/>Deletes a memo. Run Restore Deleted Memo with the same memo to undo. |
| `memo_restore` | `memoRestore` | 삭제한 메모 되돌리기<br/>Restore Deleted Memo | 삭제한 메모를 되돌립니다.<br/>Restores a deleted memo. |
| `memo_tag_add` | `memoTagAdd` | 메모에 태그 연결<br/>Link Tag to Memo | 메모에 태그를 연결합니다.<br/>Links a tag to a memo. |
| `memo_tag_remove` | `memoTagRemove` | 메모에서 태그 연결 해제<br/>Unlink Tag from Memo | 메모에서 태그 연결을 해제합니다. 대표 태그였으면 대표 태그도 해제됩니다.<br/>Unlinks a tag from a memo. If it was the primary tag, the primary tag is cleared too. |
| `memo_primary_tag_set` | `memoPrimaryTagSet` | 메모 대표 태그 지정<br/>Set Memo Primary Tag | 메모의 대표 태그를 정합니다. 연결되지 않은 태그이면 함께 연결합니다.<br/>Sets the primary tag of a memo. The tag is linked too if it isn't linked yet. |
| `memo_primary_tag_unset` | `memoPrimaryTagUnset` | 메모 대표 태그 해제<br/>Clear Memo Primary Tag | 메모의 대표 태그를 해제합니다. 태그 연결은 그대로 둡니다.<br/>Clears the primary tag of a memo. The tag stays linked. |
| `memo_place_add` | `memoPlaceAdd` | 메모에 장소 연결<br/>Link Place to Memo | 메모에 장소를 연결합니다.<br/>Links a place to a memo. |
| `memo_place_remove` | `memoPlaceRemove` | 메모에서 장소 연결 해제<br/>Unlink Place from Memo | 메모에서 장소 연결을 해제합니다.<br/>Unlinks a place from a memo. |
| `memo_web_add` | `memoWebAdd` | 메모에 웹 연결<br/>Link Web to Memo | 메모에 웹을 연결합니다.<br/>Links a web to a memo. |
| `memo_web_remove` | `memoWebRemove` | 메모에서 웹 연결 해제<br/>Unlink Web from Memo | 메모에서 웹 연결을 해제합니다.<br/>Unlinks a web from a memo. |
| `memo_contact_add` | `memoContactAdd` | 메모에 연락처 연결<br/>Link Contact to Memo | 메모에 연락처를 연결합니다.<br/>Links a contact to a memo. |
| `memo_contact_remove` | `memoContactRemove` | 메모에서 연락처 연결 해제<br/>Unlink Contact from Memo | 메모에서 연락처 연결을 해제합니다.<br/>Unlinks a contact from a memo. |

### 태그

| 도구 | Android 함수 | iOS 제목 | iOS 설명 |
| --- | --- | --- | --- |
| `tag_list` | `tagList` | 진행 중 태그 목록 보기<br/>Get In-Progress Tags | 태그 목록에 보이는, 완료하지 않은 태그를 가져옵니다. 앱에서 고른 필터가 적용됩니다.<br/>Gets the tags shown in the tag list that aren't finished. The filters chosen in the app apply. |
| `tag_finished_list` | `tagFinishedList` | 완료된 태그 목록 보기<br/>Get Finished Tags | 완료된 태그를 가져옵니다.<br/>Gets finished tags. |
| `tag_get` | `tagGet` | 태그 상세 보기<br/>Get Tag Details | 태그 하나를 그 태그가 연결한 하위 태그와 함께 가져옵니다.<br/>Gets a tag with the child tags it links to. |
| `tag_add` | `tagAdd` | 태그 추가<br/>Add Tag | 새 태그를 추가합니다. 색을 고르지 않으면 무작위 색을 씁니다.<br/>Adds a new tag. A random color is used if you don't choose one. |
| `tag_update` | `tagUpdate` | 태그 수정<br/>Edit Tag | 태그의 이모지, 제목, 설명, 색을 바꿉니다. 비워 둔 값은 그대로 둡니다.<br/>Changes the emoji, title, description or color of a tag. Values left empty stay the same. |
| `tag_finish` | `tagFinish` | 태그 완료<br/>Finish Tag | 태그를 완료합니다.<br/>Marks a tag as finished. |
| `tag_restart` | `tagRestart` | 태그 다시 시작<br/>Restart Tag | 완료된 태그를 다시 시작합니다.<br/>Marks a finished tag as not finished. |
| `tag_delete` | `tagDelete` | 태그 삭제<br/>Delete Tag | 태그를 삭제합니다. 같은 태그로 ‘삭제한 태그 되돌리기’를 실행하면 되돌릴 수 있습니다.<br/>Deletes a tag. Run Restore Deleted Tag with the same tag to undo. |
| `tag_restore` | `tagRestore` | 삭제한 태그 되돌리기<br/>Restore Deleted Tag | 삭제한 태그를 되돌립니다.<br/>Restores a deleted tag. |
| `tag_memo_list` | `tagMemoList` | 태그의 진행 중 메모 보기<br/>Get Tag's In-Progress Memos | 태그에 연결된, 완료하지 않은 메모를 가져옵니다.<br/>Gets memos linked to a tag that aren't finished. |
| `tag_memo_finished_list` | `tagMemoFinishedList` | 태그의 완료된 메모 보기<br/>Get Tag's Finished Memos | 태그에 연결된 완료된 메모를 가져옵니다.<br/>Gets finished memos linked to a tag. |
| `tag_web_list` | `tagWebList` | 태그의 웹 보기<br/>Get Tag's Webs | 태그에 연결된 웹을 가져옵니다.<br/>Gets webs linked to a tag. |
| `tag_place_list` | `tagPlaceList` | 태그의 장소 보기<br/>Get Tag's Places | 태그에 연결된 장소를 가져옵니다.<br/>Gets places linked to a tag. |
| `tag_contact_list` | `tagContactList` | 태그의 연락처 보기<br/>Get Tag's Contacts | 태그에 연결된 연락처를 가져옵니다.<br/>Gets contacts linked to a tag. |
| `tag_checklist_list` | `tagChecklistList` | 태그의 체크리스트 보기<br/>Get Tag's Checklists | 태그에 연결된 체크리스트를 가져옵니다.<br/>Gets checklists linked to a tag. |
| `tag_music_list` | `tagMusicList` | 태그의 곡 보기<br/>Get Tag's Songs | 태그에 연결된 곡을 가져옵니다.<br/>Gets songs linked to a tag. |
| `tag_qr_list` | `tagQrList` | 태그의 QR 보기<br/>Get Tag's QR Codes | 태그에 연결된 QR을 가져옵니다.<br/>Gets QR codes linked to a tag. |
| `tag_dday_list` | `tagDdayList` | 태그의 디데이 보기<br/>Get Tag's D-Days | 태그에 연결된 디데이를 가져옵니다.<br/>Gets D-Days linked to a tag. |
| `tag_routine_list` | `tagRoutineList` | 태그의 루틴 보기<br/>Get Tag's Routines | 태그에 연결된, 완료하지 않은 루틴을 진행 중, 시작 전 순으로 가져옵니다.<br/>Gets routines linked to a tag that aren't finished, in progress first, then not started. |
| `tag_routine_finished_list` | `tagRoutineFinishedList` | 태그의 완료된 루틴 보기<br/>Get Tag's Finished Routines | 태그에 연결된 완료된 루틴을 가져옵니다.<br/>Gets finished routines linked to a tag. |
| `tag_link_add` | `tagLinkAdd` | 태그에 하위 태그 연결<br/>Link Child Tag to Tag | 태그에 다른 태그를 하위 태그로 연결합니다. 메모, 웹, 장소의 태그는 바뀌지 않습니다.<br/>Links another tag to a tag as its child. Tags of memos, webs and places don't change. |
| `tag_link_remove` | `tagLinkRemove` | 태그에서 하위 태그 연결 해제<br/>Unlink Child Tag from Tag | 태그에서 하위 태그 연결을 해제합니다. 반대 방향의 연결은 그대로 둡니다.<br/>Unlinks a child tag from a tag. A link in the opposite direction stays. |

### 장소

| 도구 | Android 함수 | iOS 제목 | iOS 설명 |
| --- | --- | --- | --- |
| `place_list` | `placeList` | 장소 목록 보기<br/>Get Places | 저장한 장소를 장소 목록과 같은 순서로 가져옵니다.<br/>Gets saved places in the order of the place list. |
| `place_list_in_area` | `placeListInArea` | 지역 안 장소 보기<br/>Get Places in Area | 위도와 경도로 정한 지역 안의 장소를 한 번에 모두 가져옵니다.<br/>Gets all places inside an area set by latitude and longitude at once. |
| `place_get` | `placeGet` | 장소 상세 보기<br/>Get Place Details | 장소 하나를 연결된 태그와 함께 가져옵니다.<br/>Gets a place with its linked tags. |
| `place_memo_list` | `placeMemoList` | 장소의 진행 중 메모 보기<br/>Get Place's In-Progress Memos | 장소에 연결된, 완료하지 않은 메모를 가져옵니다.<br/>Gets memos linked to a place that aren't finished. |
| `place_memo_finished_list` | `placeMemoFinishedList` | 장소의 완료된 메모 보기<br/>Get Place's Finished Memos | 장소에 연결된 완료된 메모를 가져옵니다.<br/>Gets finished memos linked to a place. |
| `place_add` | `placeAdd` | 장소 추가<br/>Add Place | 새 장소를 추가합니다. 주소와 좌표는 찾아 주지 않으므로 알고 있는 값을 넣습니다.<br/>Adds a new place. The address and coordinates aren't looked up, so enter values you know. |
| `place_update` | `placeUpdate` | 장소 수정<br/>Edit Place | 장소의 제목, 설명, 색, 주소, 좌표를 바꿉니다. 비워 둔 값은 그대로 둡니다.<br/>Changes the title, description, color, address or coordinates of a place. Values left empty stay the same. |
| `place_delete` | `placeDelete` | 장소 삭제<br/>Delete Place | 장소를 삭제합니다. 메모와의 연결은 남습니다. 같은 장소로 ‘삭제한 장소 되돌리기’를 실행하면 되돌릴 수 있습니다.<br/>Deletes a place. Links to memos stay. Run Restore Deleted Place with the same place to undo. |
| `place_restore` | `placeRestore` | 삭제한 장소 되돌리기<br/>Restore Deleted Place | 삭제한 장소를 되돌립니다.<br/>Restores a deleted place. |
| `place_tag_add` | `placeTagAdd` | 장소에 태그 연결<br/>Link Tag to Place | 장소에 태그를 연결합니다.<br/>Links a tag to a place. |
| `place_tag_remove` | `placeTagRemove` | 장소에서 태그 연결 해제<br/>Unlink Tag from Place | 장소에서 태그 연결을 해제합니다.<br/>Unlinks a tag from a place. |

### 웹

| 도구 | Android 함수 | iOS 제목 | iOS 설명 |
| --- | --- | --- | --- |
| `web_list` | `webList` | 웹 목록 보기<br/>Get Webs | 저장한 웹을 웹 목록과 같은 순서로 가져옵니다.<br/>Gets saved webs in the order of the web list. |
| `web_get` | `webGet` | 웹 상세 보기<br/>Get Web Details | 웹 하나를 요청 헤더, 연결된 태그와 함께 가져옵니다.<br/>Gets a web with its request headers and linked tags. |
| `web_memo_list` | `webMemoList` | 웹의 진행 중 메모 보기<br/>Get Web's In-Progress Memos | 웹에 연결된, 완료하지 않은 메모를 가져옵니다.<br/>Gets memos linked to a web that aren't finished. |
| `web_memo_finished_list` | `webMemoFinishedList` | 웹의 완료된 메모 보기<br/>Get Web's Finished Memos | 웹에 연결된 완료된 메모를 가져옵니다.<br/>Gets finished memos linked to a web. |
| `web_add` | `webAdd` | 웹 추가<br/>Add Web | 새 웹을 추가합니다. 페이지 정보는 가져오지 않으므로 제목을 직접 넣습니다.<br/>Adds a new web. Page information isn't fetched, so enter the title yourself. |
| `web_update` | `webUpdate` | 웹 수정<br/>Edit Web | 웹의 제목, 설명, URL, 요청 헤더를 바꿉니다. 비워 둔 값은 그대로 둡니다.<br/>Changes the title, description, URL or request headers of a web. Values left empty stay the same. |
| `web_delete` | `webDelete` | 웹 삭제<br/>Delete Web | 웹을 삭제합니다. 메모와의 연결은 남습니다. 같은 웹으로 ‘삭제한 웹 되돌리기’를 실행하면 되돌릴 수 있습니다.<br/>Deletes a web. Links to memos stay. Run Restore Deleted Web with the same web to undo. |
| `web_restore` | `webRestore` | 삭제한 웹 되돌리기<br/>Restore Deleted Web | 삭제한 웹을 되돌립니다.<br/>Restores a deleted web. |
| `web_tag_add` | `webTagAdd` | 웹에 태그 연결<br/>Link Tag to Web | 웹에 태그를 연결합니다.<br/>Links a tag to a web. |
| `web_tag_remove` | `webTagRemove` | 웹에서 태그 연결 해제<br/>Unlink Tag from Web | 웹에서 태그 연결을 해제합니다.<br/>Unlinks a tag from a web. |

### 연락처

| 도구 | Android 함수 | iOS 제목 | iOS 설명 |
| --- | --- | --- | --- |
| `contact_list` | `contactList` | 연락처 목록 보기<br/>Get Contacts | 연락처를 가져옵니다. 즐겨찾기한 연락처가 항상 먼저 옵니다.<br/>Gets contacts. Favorite contacts always come first. |
| `contact_get` | `contactGet` | 연락처 상세 보기<br/>Get Contact Details | 연락처 하나의 모든 정보를 연결된 태그와 함께 가져옵니다.<br/>Gets all details of a contact with its linked tags. |
| `contact_memo_list` | `contactMemoList` | 연락처의 진행 중 메모 보기<br/>Get Contact's In-Progress Memos | 연락처에 연결된, 완료하지 않은 메모를 가져옵니다.<br/>Gets memos linked to a contact that aren't finished. |
| `contact_memo_finished_list` | `contactMemoFinishedList` | 연락처의 완료된 메모 보기<br/>Get Contact's Finished Memos | 연락처에 연결된 완료된 메모를 가져옵니다.<br/>Gets finished memos linked to a contact. |
| `contact_add` | `contactAdd` | 연락처 추가<br/>Add Contact | 새 연락처를 추가합니다. 새 연락처는 즐겨찾기가 아닙니다.<br/>Adds a new contact. The new contact isn't a favorite. |
| `contact_update` | `contactUpdate` | 연락처 수정<br/>Edit Contact | 연락처의 정보를 바꿉니다. 비워 둔 값은 그대로 두고, 지우기를 켠 값은 지웁니다.<br/>Changes the details of a contact. Values left empty stay the same, and values with Remove turned on are removed. |
| `contact_favorite` | `contactFavorite` | 연락처 즐겨찾기<br/>Favorite Contact | 연락처를 즐겨찾기합니다.<br/>Marks a contact as a favorite. |
| `contact_unfavorite` | `contactUnfavorite` | 연락처 즐겨찾기 해제<br/>Unfavorite Contact | 연락처의 즐겨찾기를 해제합니다.<br/>Removes the favorite mark from a contact. |
| `contact_delete` | `contactDelete` | 연락처 삭제<br/>Delete Contact | 연락처를 삭제합니다. 같은 연락처로 ‘삭제한 연락처 되돌리기’를 실행하면 되돌릴 수 있습니다.<br/>Deletes a contact. Run Restore Deleted Contact with the same contact to undo. |
| `contact_restore` | `contactRestore` | 삭제한 연락처 되돌리기<br/>Restore Deleted Contact | 삭제한 연락처를 되돌립니다.<br/>Restores a deleted contact. |
| `contact_tag_add` | `contactTagAdd` | 연락처에 태그 연결<br/>Link Tag to Contact | 연락처에 태그를 연결합니다.<br/>Links a tag to a contact. |
| `contact_tag_remove` | `contactTagRemove` | 연락처에서 태그 연결 해제<br/>Unlink Tag from Contact | 연락처에서 태그 연결을 해제합니다.<br/>Unlinks a tag from a contact. |

### 체크리스트

| 도구 | Android 함수 | iOS 제목 | iOS 설명 |
| --- | --- | --- | --- |
| `checklist_list` | `checklistList` | 체크리스트 목록 보기<br/>Get Checklists | 체크리스트를 제목순으로, 체크한 항목 수와 함께 가져옵니다.<br/>Gets checklists by title, with how many items are checked. |
| `checklist_get` | `checklistGet` | 체크리스트 상세 보기<br/>Get Checklist Details | 체크리스트 하나를 모든 항목, 연결된 태그와 함께 가져옵니다.<br/>Gets a checklist with all its items and linked tags. |
| `checklist_add` | `checklistAdd` | 체크리스트 추가<br/>Add Checklist | 새 체크리스트를 추가합니다. 항목을 함께 넣을 수 있고, 빈 항목은 건너뜁니다.<br/>Adds a new checklist. You can add items too; empty items are skipped. |
| `checklist_update` | `checklistUpdate` | 체크리스트 수정<br/>Edit Checklist | 체크리스트의 제목과 설명을 바꿉니다. 비워 둔 값은 그대로 둡니다.<br/>Changes the title or description of a checklist. Values left empty stay the same. |
| `checklist_reset` | `checklistReset` | 체크리스트 리셋<br/>Reset Checklist | 체크리스트의 모든 체크를 한 번에 해제합니다. 항목은 그대로 남습니다.<br/>Unchecks all items of a checklist at once. The items stay. |
| `checklist_delete` | `checklistDelete` | 체크리스트 삭제<br/>Delete Checklist | 체크리스트를 삭제합니다. 같은 체크리스트로 ‘삭제한 체크리스트 되돌리기’를 실행하면 되돌릴 수 있습니다.<br/>Deletes a checklist. Run Restore Deleted Checklist with the same checklist to undo. |
| `checklist_restore` | `checklistRestore` | 삭제한 체크리스트 되돌리기<br/>Restore Deleted Checklist | 삭제한 체크리스트를 되돌립니다.<br/>Restores a deleted checklist. |
| `checklist_tag_add` | `checklistTagAdd` | 체크리스트에 태그 연결<br/>Link Tag to Checklist | 체크리스트에 태그를 연결합니다.<br/>Links a tag to a checklist. |
| `checklist_tag_remove` | `checklistTagRemove` | 체크리스트에서 태그 연결 해제<br/>Unlink Tag from Checklist | 체크리스트에서 태그 연결을 해제합니다.<br/>Unlinks a tag from a checklist. |
| `checklist_item_add` | `checklistItemAdd` | 체크리스트 항목 추가<br/>Add Checklist Item | 체크리스트 끝에 체크 안 된 항목을 추가합니다.<br/>Adds an unchecked item to the end of a checklist. |
| `checklist_item_check` | `checklistItemCheck` | 체크리스트 항목 체크<br/>Check Checklist Item | 체크리스트 항목을 체크합니다.<br/>Checks a checklist item. |
| `checklist_item_uncheck` | `checklistItemUncheck` | 체크리스트 항목 체크 해제<br/>Uncheck Checklist Item | 체크리스트 항목의 체크를 해제합니다.<br/>Unchecks a checklist item. |
| `checklist_item_delete` | `checklistItemDelete` | 체크리스트 항목 삭제<br/>Delete Checklist Item | 체크리스트에서 항목을 지웁니다. 되돌릴 수 없으므로 필요하면 같은 내용을 다시 추가합니다.<br/>Removes an item from its checklist. This can't be undone; add the content again if needed. |

### QR

| 도구 | Android 함수 | iOS 제목 | iOS 설명 |
| --- | --- | --- | --- |
| `qr_list` | `qrList` | QR 목록 보기<br/>Get QR Codes | 저장한 QR을 제목순으로 가져옵니다.<br/>Gets saved QR codes by title. |
| `qr_get` | `qrGet` | QR 상세 보기<br/>Get QR Code Details | 저장한 QR 하나를 연결된 태그와 함께 가져옵니다.<br/>Gets a saved QR code with its linked tags. |
| `qr_add` | `qrAdd` | QR 추가<br/>Add QR Code | 새 QR을 추가합니다. 내용에는 QR에 담을 글을 그대로 넣습니다.<br/>Adds a new QR code. Enter the exact text to encode as the content. |
| `qr_update` | `qrUpdate` | QR 수정<br/>Edit QR Code | QR의 제목, 설명, 내용을 바꿉니다. 비워 둔 값은 그대로 둡니다.<br/>Changes the title, description or content of a QR code. Values left empty stay the same. |
| `qr_delete` | `qrDelete` | QR 삭제<br/>Delete QR Code | QR을 삭제합니다. 같은 QR로 ‘삭제한 QR 되돌리기’를 실행하면 되돌릴 수 있습니다.<br/>Deletes a QR code. Run Restore Deleted QR Code with the same QR code to undo. |
| `qr_restore` | `qrRestore` | 삭제한 QR 되돌리기<br/>Restore Deleted QR Code | 삭제한 QR을 되돌립니다.<br/>Restores a deleted QR code. |
| `qr_tag_add` | `qrTagAdd` | QR에 태그 연결<br/>Link Tag to QR Code | QR에 태그를 연결합니다.<br/>Links a tag to a QR code. |
| `qr_tag_remove` | `qrTagRemove` | QR에서 태그 연결 해제<br/>Unlink Tag from QR Code | QR에서 태그 연결을 해제합니다.<br/>Unlinks a tag from a QR code. |

### 곡

| 도구 | Android 함수 | iOS 제목 | iOS 설명 |
| --- | --- | --- | --- |
| `music_list` | `musicList` | 플레이리스트 보기<br/>Get Playlist | 플레이리스트의 곡을 가져옵니다.<br/>Gets the songs in the playlist. |
| `music_get` | `musicGet` | 곡 상세 보기<br/>Get Song Details | 플레이리스트의 곡 하나를 연결된 태그와 함께 가져옵니다.<br/>Gets a song in the playlist with its linked tags. |
| `music_add` | `musicAdd` | 곡 추가<br/>Add Song | 플레이리스트에 곡을 추가합니다. 링크로 곡 정보를 불러오지 않으므로 제목과 가수를 직접 넣습니다.<br/>Adds a song to the playlist. Song information isn't loaded from the link, so enter the title and artist yourself. |
| `music_update` | `musicUpdate` | 곡 수정<br/>Edit Song | 곡의 제목, 가수, 링크를 바꿉니다. 비워 둔 값은 그대로 둡니다.<br/>Changes the title, artist or link of a song. Values left empty stay the same. |
| `music_delete` | `musicDelete` | 곡 삭제<br/>Delete Song | 플레이리스트에서 곡을 삭제합니다. 같은 곡으로 ‘삭제한 곡 되돌리기’를 실행하면 되돌릴 수 있습니다.<br/>Deletes a song from the playlist. Run Restore Deleted Song with the same song to undo. |
| `music_restore` | `musicRestore` | 삭제한 곡 되돌리기<br/>Restore Deleted Song | 삭제한 곡을 되돌립니다.<br/>Restores a deleted song. |
| `music_tag_add` | `musicTagAdd` | 곡에 태그 연결<br/>Link Tag to Song | 곡에 태그를 연결합니다.<br/>Links a tag to a song. |
| `music_tag_remove` | `musicTagRemove` | 곡에서 태그 연결 해제<br/>Unlink Tag from Song | 곡에서 태그 연결을 해제합니다.<br/>Unlinks a tag from a song. |

### 디데이

| 도구 | Android 함수 | iOS 제목 | iOS 설명 |
| --- | --- | --- | --- |
| `dday_list` | `ddayList` | 디데이 목록 보기<br/>Get D-Days | 디데이 목록에 보이는 디데이를 제목순으로, 남은 날이나 지난 날과 함께 가져옵니다. 앱에서 고른 필터가 적용됩니다.<br/>Gets the D-Days shown in the D-Day list by title, with the days left or passed. The filters chosen in the app apply. |
| `dday_get` | `ddayGet` | 디데이 상세 보기<br/>Get D-Day Details | 디데이 하나를 연결된 태그와 함께 가져옵니다.<br/>Gets a D-Day with its linked tags. |
| `dday_add` | `ddayAdd` | 디데이 추가<br/>Add D-Day | 새 디데이를 추가합니다. 기준일을 고르지 않으면 오늘을, 색을 고르지 않으면 무작위 색을 씁니다.<br/>Adds a new D-Day. Today is used if you don't choose a start date, and a random color if you don't choose one. |
| `dday_update` | `ddayUpdate` | 디데이 수정<br/>Edit D-Day | 디데이의 제목, 설명, 색, 기준일, 반복 간격, 종료일 같은 정보를 바꿉니다. 비워 둔 값은 그대로 둡니다.<br/>Changes the title, description, color, start date, repeat intervals, end date or other details of a D-Day. Values left empty stay the same. |
| `dday_delete` | `ddayDelete` | 디데이 삭제<br/>Delete D-Day | 디데이를 삭제합니다. 같은 디데이로 ‘삭제한 디데이 되돌리기’를 실행하면 되돌릴 수 있습니다.<br/>Deletes a D-Day. Run Restore Deleted D-Day with the same D-Day to undo. |
| `dday_restore` | `ddayRestore` | 삭제한 디데이 되돌리기<br/>Restore Deleted D-Day | 삭제한 디데이를 되돌립니다.<br/>Restores a deleted D-Day. |
| `dday_tag_add` | `ddayTagAdd` | 디데이에 태그 연결<br/>Link Tag to D-Day | 디데이에 태그를 연결합니다.<br/>Links a tag to a D-Day. |
| `dday_tag_remove` | `ddayTagRemove` | 디데이에서 태그 연결 해제<br/>Unlink Tag from D-Day | 디데이에서 태그 연결을 해제합니다.<br/>Unlinks a tag from a D-Day. |

### 루틴

| 도구 | Android 함수 | iOS 제목 | iOS 설명 |
| --- | --- | --- | --- |
| `routine_list` | `routineList` | 루틴 목록 보기<br/>Get Routines | 완료하지 않은 루틴을 진행 중, 시작 전 순으로, 이번 주기의 합계와 달성률과 함께 가져옵니다. 앱에서 고른 태그 필터가 적용되고, 앱에서 고른 보기 범위와 관계없이 오늘 할 루틴이 아닌 루틴도 가져옵니다.<br/>Gets the routines that aren't finished, in progress first, then not started, with this period's total and progress. The tag filter chosen in the app applies, and routines that aren't for today are included whatever the list is set to show. |
| `routine_finished_list` | `routineFinishedList` | 완료된 루틴 목록 보기<br/>Get Finished Routines | 완료된 루틴을 가져옵니다.<br/>Gets finished routines. |
| `routine_get` | `routineGet` | 루틴 상세 보기<br/>Get Routine Details | 루틴 하나를 연결된 태그와 함께 가져옵니다.<br/>Gets a routine with its linked tags. |
| `routine_add` | `routineAdd` | 루틴 추가<br/>Add Routine | 새 루틴을 추가합니다. 시작 날짜를 고르지 않으면 오늘을 쓰고, 색은 무작위로 정합니다.<br/>Adds a new routine. Today is used if you don't choose a start date, and the color is chosen at random. |
| `routine_update` | `routineUpdate` | 루틴 수정<br/>Edit Routine | 루틴의 제목, 설명, 색, 시작 날짜, 주기, 단위, 목표량, 한 번에 기록할 양을 바꿉니다. 비워 둔 값은 그대로 둡니다.<br/>Changes the title, description, color, start date, repeat, unit, goal or amount per record of a routine. Values left empty stay the same. |
| `routine_finish` | `routineFinish` | 루틴 완료<br/>Finish Routine | 루틴을 완료합니다. 기록은 그대로 남습니다.<br/>Marks a routine as finished. Its records stay. |
| `routine_restart` | `routineRestart` | 루틴 다시 시작<br/>Restart Routine | 완료된 루틴을 다시 시작합니다.<br/>Marks a finished routine as not finished. |
| `routine_delete` | `routineDelete` | 루틴 삭제<br/>Delete Routine | 루틴을 삭제합니다. 기록은 지워지지 않습니다. 같은 루틴으로 ‘삭제한 루틴 되돌리기’를 실행하면 기록과 함께 되돌릴 수 있습니다.<br/>Deletes a routine. Its records aren't removed. Run Restore Deleted Routine with the same routine to bring it back with its records. |
| `routine_restore` | `routineRestore` | 삭제한 루틴 되돌리기<br/>Restore Deleted Routine | 삭제한 루틴을 되돌립니다.<br/>Restores a deleted routine. |
| `routine_tag_add` | `routineTagAdd` | 루틴에 태그 연결<br/>Link Tag to Routine | 루틴에 태그를 연결합니다.<br/>Links a tag to a routine. |
| `routine_tag_remove` | `routineTagRemove` | 루틴에서 태그 연결 해제<br/>Unlink Tag from Routine | 루틴에서 태그 연결을 해제합니다.<br/>Unlinks a tag from a routine. |
| `routine_record_list` | `routineRecordList` | 루틴 기록 보기<br/>Get Routine Records | 루틴의 기록을 날짜가 늦은 순으로 가져옵니다.<br/>Gets the records of a routine, latest date first. |
| `routine_record_add` | `routineRecordAdd` | 루틴 기록 넣기<br/>Add Routine Record | 루틴에 기록을 넣습니다. 날짜를 고르지 않으면 오늘을, 양을 넣지 않으면 그 루틴의 한 번에 기록할 양을 씁니다.<br/>Adds a record to a routine. Today is used if you don't choose a date, and the routine's amount per record if you don't enter an amount. |
| `routine_record_remove` | `routineRecordRemove` | 루틴 기록 빼기<br/>Remove Routine Record | 루틴에서 기록 하나를 뺍니다. 되돌릴 수 없으므로 필요하면 같은 날짜와 양으로 다시 넣습니다.<br/>Removes a record from its routine. This can't be undone; add it again with the same date and amount if needed. |

루틴 기록의 넣기와 빼기는 [RoutineDetail 화면 디자인](./routine-detail.md)의 넣기 버튼, 빼기 버튼과 같은 동사를 쓴다.

### 검색

| 도구 | Android 함수 | iOS 제목 | iOS 설명 |
| --- | --- | --- | --- |
| `search_memo` | `searchMemo` | 메모 검색<br/>Search Memos | 검색 화면의 메모 결과와 같이, 완료된 메모를 포함해 메모를 검색합니다.<br/>Searches memos, including finished ones, like the memo results of the search screen. |
| `search_tag` | `searchTag` | 태그 검색<br/>Search Tags | 검색 화면의 태그 결과와 같이 태그를 검색합니다.<br/>Searches tags like the tag results of the search screen. |
| `search_place` | `searchPlace` | 저장한 장소 검색<br/>Search Saved Places | 앱에 저장한 장소를 검색합니다. 앱 밖의 장소는 찾지 않습니다.<br/>Searches places saved in the app. Places outside the app aren't searched. |
| `search_web` | `searchWeb` | 웹 검색<br/>Search Webs | 앱에 저장한 웹을 검색합니다.<br/>Searches webs saved in the app. |
| `search_dday` | `searchDday` | 디데이 검색<br/>Search D-Days | 앱에 저장한 디데이를 남은 날이나 지난 날과 함께 검색합니다.<br/>Searches D-Days saved in the app, with the days left or passed. |
| `search_routine` | `searchRoutine` | 루틴 검색<br/>Search Routines | 완료된 루틴을 포함해 앱에 저장한 루틴을 이번 주기의 합계와 달성률과 함께 검색합니다.<br/>Searches routines saved in the app, including finished ones, with this period's total and progress. |

### 캘린더

| 도구 | Android 함수 | iOS 제목 | iOS 설명 |
| --- | --- | --- | --- |
| `calendar_memo_list` | `calendarMemoList` | 캘린더 메모 보기<br/>Get Calendar Memos | 기간이 고른 날짜 범위와 겹치는, 캘린더에 보이는 메모를 가져옵니다. 완료된 메모도 포함하고 앱에서 고른 캘린더 태그 필터가 적용됩니다.<br/>Gets the memos shown on the calendar whose period overlaps the date range. Finished memos are included, and the calendar tag filter chosen in the app applies. |
| — (iOS 전용) | — | 날짜별 캘린더 메모 보기<br/>Get Calendar Memos by Date | 고른 날짜 하루에 캘린더에 보이는 메모를 가져옵니다. 완료된 메모도 포함하고 앱에서 고른 캘린더 태그 필터가 적용됩니다.<br/>Gets the memos shown on the calendar on the chosen date. Finished memos are included, and the calendar tag filter chosen in the app applies. |
| `calendar_birthday_list` | `calendarBirthdayList` | 캘린더 생일 보기<br/>Get Calendar Birthdays | 날짜 범위 안의 연락처 생일을 양력 날짜로 가져옵니다. 음력 생일은 음력 자료를 받아 둔 연도에만 나타납니다.<br/>Gets contact birthdays in the date range as solar dates. Lunar birthdays appear only for years whose lunar data is saved. |
| `calendar_dday_list` | `calendarDdayList` | 캘린더 디데이 보기<br/>Get Calendar D-Days | 날짜 범위 안에서 캘린더에 보이는 디데이의 기준일 당일과 기념일을 가져옵니다. 앱에서 고른 캘린더 태그 필터가 적용됩니다.<br/>Gets the start dates and anniversaries of D-Days shown on the calendar in the date range. The calendar tag filter chosen in the app applies. |
| `holiday_list` | `holidayList` | 공휴일 보기<br/>Get Holidays | 한 해의 공휴일을 캘린더와 같이 가져옵니다. 이 기기에 받아 둔 연도만 나타납니다.<br/>Gets a year's holidays as shown on the calendar. Only years saved on this device appear. |
| `lunar_list` | `lunarList` | 음력 날짜 보기<br/>Get Lunar Dates | 날짜 범위 안 양력 날짜의 음력 날짜를 가져옵니다. 음력 자료를 받지 않은 날짜는 빠집니다.<br/>Gets the lunar dates of solar dates in the range. Dates without downloaded lunar data are missing. |

## 입력값

### Android

입력값 이름과 설명은 데스크톱 도구와 같다.

### iOS 입력값 이름과 설명

입력값 이름은 단축어 앱의 입력 칸 제목과 Siri가 값을 물을 때 쓰는 이름이다. 설명은 입력 칸을 눌렀을 때 보이는 안내다. 같은 데스크톱 입력값은 어느 동작에서나 같은 이름을 쓴다. 각 칸의 위 줄은 한국어, 아래 줄은 영어다.

| 데스크톱 입력값 | 이름 | 설명 |
| --- | --- | --- |
| `title` | 제목<br/>Title | 수정에서는 비워 두면 지금 제목을 그대로 둡니다.<br/>When editing, leave it empty to keep the current title. |
| `name` (연락처) | 이름<br/>Name | 수정에서는 비워 두면 지금 이름을 그대로 둡니다.<br/>When editing, leave it empty to keep the current name. |
| `description` | 설명<br/>Description | — |
| `color` | 색<br/>Color | `#4285F4`처럼 16진수 색으로 넣습니다.<br/>Enter a hex color like #4285F4. |
| `emoji` | 이모지<br/>Emoji | 제목 앞에 보이는 이모지 하나. 수정에서 지우려면 빈 글을 넣습니다.<br/>One emoji shown before the title. To remove it when editing, enter empty text. |
| `dateTime` | 하루 종일, 시작, 끝<br/>All Day, Start, End | 아래 `기간 입력` |
| `clearDateTime` | 기간 지우기<br/>Remove Period | 켜면 기간을 지웁니다.<br/>Turn on to remove the period. |
| `address` | 주소<br/>Address | — |
| `latitude` | 위도<br/>Latitude | -90부터 90 사이의 소수. 예: 37.566500<br/>A decimal from -90 to 90, like 37.566500. |
| `longitude` | 경도<br/>Longitude | -180부터 180 사이의 소수. 예: 126.978000<br/>A decimal from -180 to 180, like 126.978000. |
| `south`, `north` | 남쪽 위도, 북쪽 위도<br/>South Latitude, North Latitude | 지역의 남쪽, 북쪽 끝 위도.<br/>Latitude of the southern or northern edge of the area. |
| `west`, `east` | 서쪽 경도, 동쪽 경도<br/>West Longitude, East Longitude | 지역의 서쪽, 동쪽 끝 경도. 180도 경선을 넘는 지역이면 서쪽이 동쪽보다 클 수 있습니다.<br/>Longitude of the western or eastern edge. West may be greater than east when the area crosses the 180th meridian. |
| `url` | URL<br/>URL | 수정에서는 비워 두면 지금 URL을 그대로 둡니다.<br/>When editing, leave it empty to keep the current URL. |
| `headers` | 요청 헤더<br/>Request Headers | 앱에서 페이지를 열 때 보내는 헤더. 한 줄에 `이름: 값` 하나씩. 수정에서는 넣은 헤더로 모두 바뀝니다.<br/>Headers sent when the page opens in the app, one `Name: value` per entry. When editing, they replace all current headers. |
| `value` (QR) | 내용<br/>Content | QR에 담을 글. URL, 글, 와이파이 정보처럼 담을 글을 그대로 넣습니다.<br/>The exact text to encode, such as a URL, text or Wi-Fi information. |
| `artist` | 가수<br/>Artist | — |
| `link` | YouTube 링크<br/>YouTube Link | YouTube 동영상 링크. 빈 글은 링크 없음입니다.<br/>A YouTube video link. Empty text means no link. |
| `items` | 항목<br/>Items | 넣은 순서대로 추가합니다.<br/>Added in the order given. |
| `content` | 내용<br/>Content | 항목의 내용 한 줄.<br/>One line of item content. |
| `heightCm` | 키 (cm)<br/>Height (cm) | 소수 첫째 자리까지. 예: 172.5<br/>Up to one decimal place, like 172.5. |
| `footSizeMm` | 신발 사이즈 (mm)<br/>Shoe Size (mm) | 0부터 999 사이. 예: 270<br/>From 0 to 999, like 270. |
| `birthday` | 생일<br/>Birthday | 음력 생일은 음력 연·월·일 그대로 고릅니다. 태어난 연도도 이 연도로 정해집니다.<br/>For a lunar birthday, choose the lunar year, month and day as is. The birth year is set to its year. |
| `birthdayCalendar` | 생일 달력<br/>Birthday Calendar | 선택지: 양력, 음력 / Solar, Lunar |
| `birthYear` | 태어난 연도<br/>Birth Year | 생일 없이 연도만 알 때 씁니다.<br/>Use when only the year is known. |
| `hometown` | 고향<br/>Hometown | — |
| `howWeMet` | 알게 된 경로<br/>How We Met | — |
| `phoneNumbers` | 전화번호<br/>Phone Numbers | 수정에서는 넣은 번호로 모두 바뀝니다. 빈 목록이면 모두 지웁니다.<br/>When editing, they replace all current numbers. An empty list removes them all. |
| `clearHeight`, `clearFootSize`, `clearBirthday`, `clearBirthYear` | 키 지우기, 신발 사이즈 지우기, 생일 지우기, 태어난 연도 지우기<br/>Remove Height, Remove Shoe Size, Remove Birthday, Remove Birth Year | 켜면 그 값을 지웁니다.<br/>Turn on to remove the value. |
| `baseDate` | 기준일<br/>Start Date | 추가에서 비워 두면 오늘입니다. 수정에서는 비워 두면 지금 기준일을 그대로 둡니다.<br/>When adding, leave it empty to use today. When editing, leave it empty to keep the current start date. |
| `countsBaseDay` | 기준일을 1일로 세기<br/>Count Start Date as Day 1 | 켜면 기준일 당일을 1일째로 셉니다. 추가에서 고르지 않으면 켜집니다.<br/>On counts the start date as day 1. When adding, it's on if not chosen. |
| `repeats` | 반복 간격<br/>Repeat Intervals | 간격 하나를 `P100D`(100일), `P3M`(3개월), `P1Y`(1년)처럼 넣습니다. 한 간격에 일과 개월을 함께 넣을 수 없습니다. 수정에서는 넣은 간격으로 모두 바뀌고, 빈 목록이면 모두 지웁니다.<br/>Enter each interval like P100D (100 days), P3M (3 months) or P1Y (1 year). One interval can't mix days with months. When editing, they replace all current intervals; an empty list removes them all. |
| `endDate` (디데이) | 종료일<br/>End Date | 기념일을 이 날까지만 알립니다.<br/>Anniversaries are shown only up to this date. |
| `clearEndDate` | 종료일 지우기<br/>Remove End Date | 켜면 종료일을 지웁니다.<br/>Turn on to remove the end date. |
| `overflowPolicy` | 달력에 없는 날짜<br/>Missing Dates | 개월 간격의 기념일이 그 달에 없는 날짜에 걸릴 때의 처리. 선택지: 말일로 당김, 표시하지 않음, 다음 달 1일로 미룸 / Use last day of month, Skip, Use 1st of next month. 추가에서 고르지 않으면 말일로 당김입니다.<br/>What to do when a monthly anniversary falls on a date the month doesn't have. If not chosen when adding, Use last day of month. |
| `startDate` (루틴) | 시작 날짜<br/>Start Date | 추가에서 비워 두면 오늘입니다. 수정에서는 비워 두면 지금 시작 날짜를 그대로 둡니다.<br/>When adding, leave it empty to use today. When editing, leave it empty to keep the current start date. |
| `cycle` | 주기<br/>Repeat | 선택지: 매일, 평일만, 주말만, 매주, 매월 / Daily, Weekdays, Weekends, Weekly, Monthly. 추가에서 고르지 않으면 매일입니다.<br/>If not chosen when adding, Daily. |
| `unit` | 단위<br/>Unit | 양 뒤에 붙여 읽을 글자. 10자까지. 예: 회, ml, 분. 추가에서 비워 두면 단위 없이 숫자만 보입니다. 수정에서 지우려면 빈 글을 넣습니다.<br/>Text shown right after amounts, up to 10 characters, like times, ml or min. Leave it empty when adding to show numbers only. To remove it when editing, enter empty text. |
| `goalAmount` | 목표량<br/>Goal | 한 주기에 채우려는 양. 1부터 9999999 사이의 정수. 비워 두면 추가에서는 목표 없는 루틴이 됩니다.<br/>The amount to reach in each period, a whole number from 1 to 9999999. Leave it empty to add a routine without a goal. |
| `clearGoalAmount` | 목표량 지우기<br/>Remove Goal | 켜면 목표량을 지웁니다.<br/>Turn on to remove the goal. |
| `amountPerRecord` | 한 번에 기록할 양<br/>Amount per Record | 1부터 9999999 사이의 정수. 추가에서 넣지 않으면 1입니다.<br/>A whole number from 1 to 9999999. If not entered when adding, 1. |
| `date` (루틴 기록) | 날짜<br/>Date | 루틴의 시작 날짜와 같거나 그 뒤여야 합니다. 비워 두면 오늘입니다.<br/>Must be on or after the routine's start date. Leave it empty to use today. |
| `amount` (루틴 기록) | 양<br/>Amount | 1부터 9999999 사이의 정수. 비워 두면 그 루틴의 한 번에 기록할 양입니다.<br/>A whole number from 1 to 9999999. Leave it empty to use the routine's amount per record. |
| `query` | 검색어<br/>Search Term | — |
| `startDate`, `endDate` | 시작일, 마지막 날<br/>Start Date, End Date | 마지막 날도 범위에 포함합니다.<br/>The end date is included. |
| `year` | 연도<br/>Year | — |
| `sort` | 정렬<br/>Sort | 선택지는 그 목록의 [목록 정렬](./list-sort.md) 문구: 기본순, 제목순, 이름순, 최근 수정순 / Default, Title, Name, Recently updated. 고르지 않으면 그 목록의 처음 정렬입니다.<br/>If not chosen, the list's initial order is used. |
| `itemSort` | 항목 정렬<br/>Item Sort | 선택지: 추가순, 체크 안 된 항목 우선 / Added order, Unchecked first |
| `scope` | 포함할 태그<br/>Tags to Include | 선택지: 이 태그만, 바로 연결한 태그까지, 연결된 모든 태그까지 / This tag only, Directly linked tags, All linked tags. 고르지 않으면 이 태그만입니다.<br/>If not chosen, this tag only. |
| `cursor` | 페이지<br/>Page | 1부터 시작하는 페이지 번호. 비워 두면 첫 페이지입니다.<br/>Page number starting at 1. Leave empty for the first page. |

항목을 가리키는 입력값(`memoId`, `tagId`, `placeId`, `webId`, `contactId`, `checklistId`, `itemId`, `qrId`, `musicId`, `ddayId`, `routineId`, `recordId`, `primaryTagId`, `linkedTagId`와 그 목록)은 iOS에서 id 글 대신 항목을 고르는 입력으로 보여 준다. 이름은 다음과 같다.

| 쓰임 | 한국어 | 영어 |
| --- | --- | --- |
| 동작의 대상 하나 | `{대상}` | `{Entity}` |
| 연결·해제할 항목 | `연결할 {대상}`, `연결을 해제할 {대상}` | `{Entity} to Link`, `{Entity} to Unlink` |
| 추가할 때 함께 연결하는 항목 목록 | `연결할 {대상}` | `{Entities} to Link` |
| 대표 태그 | `대표 태그` | `Primary Tag` |
| 하위 태그 | `하위 태그` | `Child Tag` |
| 삭제 되돌리기의 대상 | `되돌릴 {대상}` | `{Entity} to Restore` |

항목 선택 목록은 `결과 항목 표시`의 제목과 부제로 항목을 보여 주고, 맨 위에 검색 칸을 둔다. 삭제한 항목은 선택 목록에 나타나지 않으므로, 삭제 되돌리기에는 삭제 동작에 넘긴 항목을 변수로 그대로 넘긴다. 그 변수를 삭제 되돌리기가 아닌 동작에 넘기면 `결과 표시`의 `삭제된 항목을 대상으로 넘김` 문구로 끝난다.

### 기간 입력

메모의 기간은 iOS에서 세 입력으로 나눈다. 셋 다 비워 두면 기간을 정하지 않는다.

| 입력 | 한국어 | 영어 | 설명 |
| --- | --- | --- | --- |
| 하루 종일 | `하루 종일` | `All Day` | 켜면 날짜만, 끄면 날짜와 시간을 씁니다. / On uses dates only; off uses date and time. |
| 시작 | `시작` | `Start` | — |
| 끝 | `끝` | `End` | 끝도 기간에 포함합니다. / The end is included. |

### 입력 칸 순서와 요약 문장

단축어 앱에서 입력 칸은 데스크톱 도구의 입력값 순서대로 둔다. 동작 요약 문장에는 반드시 넣어야 하는 입력값만 그 순서대로 넣고, 나머지는 운영체제의 `더 보기` 안에 둔다.

| 동작 종류 | 한국어 요약 | 영어 요약 |
| --- | --- | --- |
| 추가 | `{대상} 추가: {필수 입력값…}` | `Add {entity} {필수 입력값…}` |
| 대상 하나를 받는 동작 | `{대상 입력} {동작 제목에서 대상을 뺀 말}`. 예: `{메모} 완료` | `{동사} {대상 입력}`. 예: `Finish {Memo}` |
| 연결·해제 | `{대상 입력}에 {연결할 항목} 연결` | `Link {연결할 항목} to {대상 입력}` |
| 검색 | `{대상} 검색: {검색어}` | `Search {entities} for {검색어}` |
| 날짜 범위 | `{시작일}부터 {마지막 날}까지의 {목록 이름}` | `{목록 이름} from {시작일} to {마지막 날}` |
| 입력값이 없는 목록 | 동작 제목 그대로 | 동작 제목 그대로 |

한국어 요약은 받침에 따라 바뀌는 조사를 피하려고 `{대상} 추가: {값}`처럼 쌍점 뒤에 값을 둔다.

## Siri 바로 실행 문구

기준 스펙의 10개 동작을 Siri 바로 실행으로 제공한다. 단축어 앱의 앱 페이지와 Spotlight의 앱 바로 가기에는 이 순서로 표시한다.

### 앱 이름

모든 문구에는 앱 이름을 넣는다. 앱 이름은 문구에 글자로 적지 않고 운영체제가 채우는 앱 이름 자리를 쓰며, 아래 표에서는 `{앱}`으로 적는다. 앱 이름은 홈 화면의 이름 `Diary`이고, 한국어로 말하는 사용자를 위해 Siri가 함께 알아듣는 다른 이름으로 `다이어리`를 둔다.

### 문구와 표시

| 동작 | 짧은 이름 | 아이콘 | 한국어 문구 | 영어 문구 |
| --- | --- | --- | --- | --- |
| 메모 추가 (`memo_add`) | 메모 추가<br/>Add Memo | `square.and.pencil` | `{앱}에 메모 추가`<br/>`{앱}에서 메모 추가해 줘`<br/>`{앱} 메모 추가` | `Add a memo in {앱}`<br/>`Create a memo in {앱}`<br/>`New {앱} memo` |
| 메모 검색 (`search_memo`) | 메모 검색<br/>Search Memos | `magnifyingglass` | `{앱}에서 메모 검색`<br/>`{앱}에서 메모 찾아 줘`<br/>`{앱} 메모 검색` | `Search memos in {앱}`<br/>`Find a memo in {앱}`<br/>`Search {앱} memos` |
| 날짜별 캘린더 메모 보기 (iOS 전용) | 날짜별 메모<br/>Memos by Date | `calendar` | `{앱}에서 날짜별 메모 보기`<br/>`{앱} 캘린더 메모 보여 줘`<br/>`{앱}에서 그날 메모 보기` | `Show memos for a date in {앱}`<br/>`Show {앱} calendar memos`<br/>`What's on my {앱} calendar` |
| 진행 중 메모 목록 보기 (`memo_list`) | 진행 중 메모<br/>In-Progress Memos | `list.bullet` | `{앱}에서 진행 중 메모 보기`<br/>`{앱} 메모 목록 보여 줘`<br/>`{앱}에서 할 일 보기` | `Show in-progress memos in {앱}`<br/>`Show my {앱} memos`<br/>`List memos in {앱}` |
| 태그 추가 (`tag_add`) | 태그 추가<br/>Add Tag | `tag` | `{앱}에 태그 추가`<br/>`{앱}에서 태그 만들어 줘` | `Add a tag in {앱}`<br/>`Create a tag in {앱}` |
| 체크리스트 추가 (`checklist_add`) | 체크리스트 추가<br/>Add Checklist | `checklist` | `{앱}에 체크리스트 추가`<br/>`{앱}에서 체크리스트 만들어 줘` | `Add a checklist in {앱}`<br/>`Create a checklist in {앱}` |
| 웹 추가 (`web_add`) | 웹 추가<br/>Add Web | `globe` | `{앱}에 웹 추가`<br/>`{앱}에 웹 페이지 저장` | `Add a web in {앱}`<br/>`Save a web page in {앱}` |
| 저장한 장소 검색 (`search_place`) | 장소 검색<br/>Search Places | `mappin.and.ellipse` | `{앱}에서 저장한 장소 검색`<br/>`{앱}에서 장소 찾아 줘` | `Search saved places in {앱}`<br/>`Find a place in {앱}` |
| 곡 추가 (`music_add`) | 곡 추가<br/>Add Song | `music.note` | `{앱}에 곡 추가`<br/>`{앱} 플레이리스트에 곡 추가` | `Add a song in {앱}`<br/>`Add a song to my {앱} playlist` |
| QR 추가 (`qr_add`) | QR 추가<br/>Add QR Code | `qrcode` | `{앱}에 QR 추가`<br/>`{앱}에 QR 코드 저장` | `Add a QR code in {앱}`<br/>`Save a QR code in {앱}` |

짧은 이름과 아이콘은 단축어 앱의 앱 페이지 타일과 Spotlight의 바로 가기에 보인다. 타일 색은 운영체제 기본값을 쓴다. 문구에는 입력값을 넣지 않으며, 꼭 필요한 값은 실행할 때 묻는다.

날짜별 캘린더 메모 보기는 날짜 하나만 받는 iOS 전용 동작이다. 단축어 앱의 동작 목록에는 두 날짜를 받는 `캘린더 메모 보기`와 함께 보이며, 바로 실행 타일을 누르거나 문구로 실행하면 날짜 하나만 묻는다. 목록과 검색의 바로 실행은 정렬과 페이지를 묻지 않고 처음 정렬의 첫 페이지를 보여 준다. 다음 페이지가 있으면 `결과 표시`의 대화 문구로 알린다.

### 값을 물을 때의 문구

바로 실행으로 실행할 때 꼭 필요한 값이 없으면 Siri가 다음 문구로 묻는다. 단축어 앱에서 입력 칸을 `매번 묻기`로 둔 경우에도 같은 문구를 쓴다.

| 동작 | 값 | 한국어 | 영어 |
| --- | --- | --- | --- |
| 메모 추가 | 제목 | `메모 제목이 뭔가요?` | `What's the memo title?` |
| 메모 검색 | 검색어 | `어떤 메모를 찾을까요?` | `What memo are you looking for?` |
| 날짜별 캘린더 메모 보기 | 날짜 | `어느 날짜의 메모를 볼까요?` | `Which date?` |
| 태그 추가 | 제목 | `태그 제목이 뭔가요?` | `What's the tag title?` |
| 체크리스트 추가 | 제목 | `체크리스트 제목이 뭔가요?` | `What's the checklist title?` |
| 웹 추가 | 제목 | `웹 제목이 뭔가요?` | `What's the web title?` |
| 웹 추가 | URL | `URL이 뭔가요?` | `What's the URL?` |
| 저장한 장소 검색 | 검색어 | `어떤 장소를 찾을까요?` | `What place are you looking for?` |
| 곡 추가 | 제목 | `곡 제목이 뭔가요?` | `What's the song title?` |
| QR 추가 | 제목 | `QR 제목이 뭔가요?` | `What's the QR code title?` |
| QR 추가 | 내용 | `QR에 담을 내용이 뭔가요?` | `What should the QR code contain?` |

바로 실행이 아닌 동작에서 꼭 필요한 값이 빠지면 앱이 문구를 따로 두지 않고, 운영체제의 기본 질문에 `iOS 입력값 이름과 설명`의 입력값 이름을 넣어 묻는다.

## 결과 표시

Android에서는 결과를 AI 도우미에 값으로 넘기고, AI 도우미가 사용자에게 어떻게 전할지 정한다. 앱이 따로 문구를 만들지 않는다.

iOS에서는 결과 항목과 함께 Siri가 말하고 단축어가 보여 주는 대화 문구를 넘긴다. 단축어 안에서 다른 동작으로 이어질 때는 운영체제가 대화 문구를 표시하지 않는다.

### 대화 문구

`{대상을}`은 아래 `조사가 붙은 대상 이름`의 목적격, `N`은 개수, `P`는 다음 페이지 번호다. 영어에서 `N`이 1이면 단수형을 쓴다.

| 결과 | 한국어 | 영어 |
| --- | --- | --- |
| 목록·검색에 항목이 있음 | `{대상} N개를 찾았어요.` | `Found N {entities}.` |
| 목록·검색에 항목이 있고 다음 페이지가 있음 | `{대상} N개를 찾았어요. 다음 페이지 P가 있어요.` | `Found N {entities}. There's more on page P.` |
| 목록·검색에 항목이 없음 | `결과가 없어요.` | `No results.` |
| 하나 조회에서 항목이 있음 | 문구 없이 항목만 보여 준다 | 문구 없이 항목만 보여 준다 |
| 하나 조회에서 항목이 없음 | `{대상을} 찾을 수 없어요.` | `Couldn't find the {entity}.` |
| 삭제된 항목을 대상으로 넘김(삭제 되돌리기 제외) | `{대상을} 찾을 수 없어요.` | `Couldn't find the {entity}.` |
| 추가 | `{대상을} 추가했어요.` | `Added the {entity}.` |
| 복사 | `메모를 복사했어요.` | `Copied the memo.` |
| 바뀐 개수를 돌려주는 동작에서 1개 이상 | 아래 `바꾸기 결과 문구` | 아래 `바꾸기 결과 문구` |
| 바뀐 개수가 0 | `바뀐 내용이 없어요.` | `Nothing changed.` |
| 실패 | `실행하지 못했어요. {실패 사유}` | `Couldn't complete the action. {실패 사유}` |

`{실패 사유}`는 데스크톱 앱의 연결이 알리는 실패 내용을 그대로 붙인다. 계정을 확인할 수 없는 실패도 같은 형식을 쓴다.

바꾸기 결과 문구는 동작 종류별로 다음과 같다.

| 동작 종류 | 한국어 | 영어 |
| --- | --- | --- |
| 수정 | `{대상을} 수정했어요.` | `Updated the {entity}.` |
| 완료 | `{대상을} 완료했어요.` | `Finished the {entity}.` |
| 다시 시작 | `{대상을} 다시 시작했어요.` | `Restarted the {entity}.` |
| 삭제 | `{대상을} 삭제했어요.` | `Deleted the {entity}.` |
| 삭제 되돌리기 | `{대상을} 되돌렸어요.` | `Restored the {entity}.` |
| 연결 | `연결했어요.` | `Linked.` |
| 연결 해제 | `연결을 해제했어요.` | `Unlinked.` |
| 대표 태그 지정 | `대표 태그를 지정했어요.` | `Set the primary tag.` |
| 대표 태그 해제 | `대표 태그를 해제했어요.` | `Cleared the primary tag.` |
| 즐겨찾기 / 해제 | `즐겨찾기했어요.` / `즐겨찾기를 해제했어요.` | `Added to favorites.` / `Removed from favorites.` |
| 체크 / 체크 해제 | `체크했어요.` / `체크를 해제했어요.` | `Checked.` / `Unchecked.` |
| 체크리스트 리셋 | `체크 N개를 해제했어요.` | `Unchecked N items.` |
| 루틴 기록 넣기 / 빼기 | `기록을 넣었어요.` / `기록을 뺐어요.` | `Added the record.` / `Removed the record.` |

조사가 붙은 대상 이름은 다음과 같다.

| 항목 | 목적격 |
| --- | --- |
| 메모, 태그, 장소, 연락처, 체크리스트, 체크리스트 항목, 디데이 | `메모를`, `태그를`, `장소를`, `연락처를`, `체크리스트를`, `체크리스트 항목을`, `디데이를` |
| 웹, QR, 곡, 루틴, 루틴 기록 | `웹을`, `QR을`, `곡을`, `루틴을`, `루틴 기록을` |

### 결과 항목 표시

결과 항목은 단축어와 Siri의 결과 카드, 항목 선택 목록에 제목, 부제, 아이콘으로 보인다. 아이콘은 `항목 종류 이름`을 따른다. 값이 비어 있는 부제는 두지 않는다.

| 항목 | 제목 | 부제 |
| --- | --- | --- |
| 메모 | 제목 | 기간이 있으면 기간, 없으면 설명 |
| 캘린더 메모 | 제목 | 기간 |
| 태그 | 이모지가 있으면 `{이모지} {제목}`, 없으면 제목 | 설명 |
| 장소 | 제목 | 주소가 있으면 주소, 없으면 설명 |
| 웹 | 제목 | URL |
| 연락처 | [연락처 표시 이름](./contact-name.md)의 형식 | 설명 |
| 체크리스트 | 제목 | 목록 결과이면 `{체크한 수}/{전체 수}`, 상세 결과이면 설명 |
| 체크리스트 항목 | 내용 | — |
| QR | 제목 | 설명 |
| 곡 | 제목 | 가수 |
| 디데이 | 제목 | 카드 문구 |
| 캘린더 디데이 | `🎉 {제목} {날짜 문구}` | 날짜 |
| 루틴 | 제목 | 카드 문구. 달성률이 있으면 `{카드 문구} · {달성률}` |
| 루틴 기록 | 양 문구 | 날짜 |
| 생일 | `🎂 {연락처 표시 이름}` | 날짜 |
| 공휴일 | 이름 | 하루이면 날짜, 여러 날이면 `{시작일} – {마지막 날}` |
| 음력 날짜 | 한국어 `음력 {월}월 {일}일`, 윤달이면 `음력 윤{월}월 {일}일`<br/>영어 `Lunar {월}/{일}`, 윤달이면 `Lunar leap {월}/{일}` | 양력 날짜 |

QR의 내용은 남에게 보이면 안 되는 값일 수 있으므로 제목과 부제에 넣지 않는다. 단축어에서는 항목의 값으로 꺼내 쓸 수 있다.

디데이의 카드 문구는 동작을 실행한 시점의 오늘로 계산한 값을 [DDayHome 화면 디자인](./dday-home.md)의 `카드 문구 표기`대로 기기 언어에 맞게 쓴다. 캘린더 디데이의 `{날짜 문구}`는 [CalendarHome 디자인](./calendar-home.md)의 디데이 아이템과 같은 표기를 쓴다.

루틴의 카드 문구와 달성률은 동작을 실행한 시점의 오늘로 계산한 값을 [RoutineHome 화면 디자인](./routine-home.md)의 `카드 문구 표기`대로 기기 언어에 맞게 쓴다. 예를 들어 `오늘 1500 / 2000ml · 75%`, `이번 달 12000m`, `시작 전`, `완료`다. 루틴 기록의 양 문구도 같은 표기를 쓰고 그 루틴의 단위로 읽으며, 단위가 비어 있으면 숫자만 쓴다.

날짜와 시간은 기기의 지역 설정에 맞는 운영체제 기본 형식으로 보여 준다. 하루 종일 기간은 날짜만, 시간이 있는 기간은 날짜와 시간을 보여 주며, 시작과 끝이 같은 날이면 날짜를 한 번만 쓴다.

결과 항목에서 단축어가 꺼내 쓸 수 있는 값의 이름은 `iOS 입력값 이름과 설명`의 같은 값 이름을 쓴다. 입력값에 없는 값은 다음 이름을 쓴다.

| 값 | 한국어 | 영어 |
| --- | --- | --- |
| 완료 여부 | `완료됨` | `Finished` |
| 즐겨찾기 여부 | `즐겨찾기` | `Favorite` |
| 체크 여부 | `체크됨` | `Checked` |
| 체크한 수, 전체 수 | `체크한 수`, `전체 수` | `Checked Count`, `Total Count` |
| 만든 시각, 수정한 시각 | `만든 시각`, `수정한 시각` | `Created`, `Updated` |
| 연결된 태그·장소·웹·연락처 | `태그`, `장소`, `웹`, `연락처` | `Tags`, `Places`, `Webs`, `Contacts` |
| 하위 태그 | `하위 태그` | `Child Tags` |
| 체크리스트의 항목 | `항목` | `Items` |
| 공휴일 쉬는 날 여부 | `쉬는 날` | `Day Off` |
| 음력 연·월·일, 윤달 여부, 양력 날짜 | `음력 연도`, `음력 월`, `음력 일`, `윤달`, `양력 날짜` | `Lunar Year`, `Lunar Month`, `Lunar Day`, `Leap Month`, `Solar Date` |
| 디데이의 상태 | `상태`. 값은 `아직 오지 않음`, `당일`, `지남` | `Status`. Values are `Upcoming`, `Today`, `Passed` |
| 남은 날 수, 지난 날 수 | `남은 날 수`, `지난 날 수` | `Days Left`, `Days Passed` |
| 채운 년, 채운 개월 | `채운 년`, `채운 개월` | `Full Years`, `Full Months` |
| 캘린더 디데이의 날짜와 날짜 문구 | `날짜`, `날짜 문구` | `Date`, `Date Label` |
| 캘린더 디데이의 디데이 | `디데이` | `D-Day` |
| 루틴의 진행 상태 | `진행 상태`. 값은 `시작 전`, `진행 중` | `Status`. Values are `Not started`, `In progress` |
| 루틴의 이번 주기 합계, 달성률 | `이번 주기 합계`, `달성률` | `Period Total`, `Progress` |
| 루틴 기록의 루틴 | `루틴` | `Routine` |

## 참고

- [AppFunctions | Android Developers](https://developer.android.com/ai/appfunctions)
- [App Intents | Apple Developer Documentation](https://developer.apple.com/documentation/appintents)
- [App Shortcuts | Apple Developer Documentation](https://developer.apple.com/documentation/appintents/app-shortcuts)
