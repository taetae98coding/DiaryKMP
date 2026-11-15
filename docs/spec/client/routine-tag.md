# 루틴 태그 앱 스펙

이 문서는 루틴과 태그의 연결 가운데 이 연결이 앱의 목록과 검색 결과에 주는 영향을 다룬다. 루틴과 태그의 연결이 공통으로 지키는 규칙은 [루틴 태그 스펙](../common/routine-tag.md)을, 태그 연결이 공통으로 지키는 규칙은 [항목 태그 연결 공통 스펙](../common/entity-tag.md)을, 앱이 기기에 저장된 연결로 대상 항목의 태그와 태그의 대상 항목을 조회하는 규칙은 [항목 태그 연결 앱 스펙](./entity-tag.md)을 따른다.

사용자가 태그를 고르고 해제하는 조작과 조회 결과를 보여 주는 화면은 각 입력 컴포넌트 스펙과 화면 스펙이 소유하므로 이 문서에는 `feature` 영역을 두지 않는다.

## domain

### 연결의 의미

[항목 태그 연결 공통 스펙](../common/entity-tag.md)의 `연결의 의미`는 루틴에서 다음에 적용된다.

- [RoutineHome 화면 스펙](./routine-home.md)과 [RoutineFinishedList 화면 스펙](./routine-finished-list.md)의 목록 노출 기준과 순서. 사용자가 고른 태그 필터는 예외이며, 그 판정은 같은 문서의 `태그 필터 기준`을 따른다.
- [캘린더 루틴 기록 표시 스펙](./calendar-routine.md)의 노출 기준. RoutineHome의 달력 보기가 목록 보기와 함께 쓰는 태그 필터를 루틴 기록 표시에 더하는 기준은 [RoutineHome 화면 스펙](./routine-home.md)의 `태그 필터 기준`을 따른다.
- [SearchHome 화면 스펙](./search-home.md)의 루틴 검색 결과. 연결된 태그의 제목은 루틴의 검색 일치 판정에 쓰이지 않는다.

태그 연결은 [루틴 앱 스펙](./routine.md)의 진행 상태, 주기의 합계, 달성률과 카드 문구에도, [루틴 스펙](../common/routine.md)의 `완료 여부`에도 영향을 주지 않는다.

태그로 루틴을 조회하는 [TagDetail 루틴 탭 스펙](./tag-detail-routine.md)과 [TagRoutineFinishedList 화면 스펙](./tag-routine-finished-list.md)의 목록은 이 연결을 노출 기준으로 쓴다.
