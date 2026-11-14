# 루틴 태그 스펙

이 문서는 하나의 루틴이 여러 태그와 연결될 수 있다는 규칙 가운데 루틴에만 해당하는 부분을 다룬다. 태그 연결이 공통으로 지키는 규칙은 [항목 태그 연결 공통 스펙](./entity-tag.md)을, 연결의 성질과 동기화 계약은 [항목 연결 공통 스펙](./entity-link.md)을, 앱이 연결을 기기에 저장하는 규칙은 [항목 연결 앱 스펙](../client/entity-link.md)을 따른다. 이 연결이 앱의 목록과 검색 결과에 주는 영향(`연결의 의미`)은 [루틴 태그 앱 스펙](../client/routine-tag.md)이 소유한다. 연결을 동기화 때 주고받는 정보는 [데이터 동기화 스펙](./data-sync.md)을 따른다.

루틴과 루틴 기록이 갖는 정보는 [루틴 스펙](./routine.md)을, 루틴의 추가·수정·완료·삭제는 [RoutineAdd 화면 스펙](../client/routine-add.md)과 [RoutineDetail 화면 스펙](../client/routine-detail.md)을, 태그 자체의 추가·완료·삭제는 [TagAdd 화면 스펙](../client/tag-add.md)과 [TagDetail 화면 스펙](../client/tag-detail.md)을 따른다.

사용자가 태그를 고르고 해제하는 조작은 이 문서에서 다루지 않는다. 태그 선택의 행동과 정책은 [항목 태그 입력 컴포넌트 스펙](../client/entity-tag-input.md)에서, 구체적인 표현과 조작 수단은 [항목 태그 입력 컴포넌트 디자인](../../design/entity-tag-input.md)에서 다룬다. 루틴을 추가하면서 태그를 연결하는 행동은 [RoutineAdd 화면 스펙](../client/routine-add.md)에서, 이미 저장된 루틴의 태그 연결을 바꾸는 행동은 [RoutineDetail 화면 스펙](../client/routine-detail.md)에서, 태그로 루틴을 찾아보는 목록은 [TagDetail 루틴 탭 스펙](../client/tag-detail-routine.md)과 [TagRoutineFinishedList 화면 스펙](../client/tag-routine-finished-list.md)에서 다룬다. 이 문서에는 사용자 조작이 없어 `feature` 영역을 생략한다.

## domain

### 연결이 바꾸지 않는 내용

연결은 루틴과 태그 사이의 관계이며, 연결하거나 해제해도 [루틴 스펙](./routine.md)의 `루틴의 정보`와 `루틴 기록의 정보`나 태그의 이모지·제목·설명·컬러를 바꾸지 않는다.

### 루틴의 완료와 연결

루틴을 완료하거나 다시 시작해도 루틴과 태그의 연결은 해제되지 않는다. 기준은 [항목 연결 공통 스펙](./entity-link.md)의 `항목의 상태 변화와 연결`을 따른다.

### 루틴 기록과 태그

태그와 연결되는 것은 루틴이다. 루틴 기록은 태그와 연결되지 않으며, 루틴 기록을 태그로 구분해야 할 때는 그 기록이 속한 루틴과 태그의 연결을 쓴다.

루틴 기록을 넣거나 빼도 루틴과 태그의 연결은 바뀌지 않는다.

### 대표 태그를 두지 않음

루틴은 [항목 태그 연결 공통 스펙](./entity-tag.md)의 `대표 태그`에 따라 대표 태그를 갖지 않는다. 따라서 루틴과 그 루틴에 속한 기록의 표시 색은 연결한 태그와 관계없이 루틴의 컬러다.
