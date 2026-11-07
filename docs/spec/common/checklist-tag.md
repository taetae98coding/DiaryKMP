# 체크리스트 태그 스펙

이 문서는 하나의 체크리스트가 여러 태그와 연결될 수 있다는 규칙 가운데 체크리스트에만 해당하는 부분을 다룬다. 태그 연결이 공통으로 지키는 규칙은 [항목 태그 연결 공통 스펙](./entity-tag.md)을, 연결의 성질과 동기화 계약은 [항목 연결 공통 스펙](./entity-link.md)을, 앱이 연결을 기기에 저장하는 규칙은 [항목 연결 앱 스펙](../client/entity-link.md)을 따른다. 이 연결이 앱의 목록과 검색 결과에 주는 영향(`연결의 의미`)은 [체크리스트 태그 앱 스펙](../client/checklist-tag.md)이 소유한다.

체크리스트 자체의 추가·수정·삭제는 [ChecklistAdd 화면 스펙](../client/checklist-add.md)과 [ChecklistDetail 화면 스펙](../client/checklist-detail.md)을, 태그 자체의 추가·완료·삭제는 [TagAdd 화면 스펙](../client/tag-add.md)과 [TagDetail 화면 스펙](../client/tag-detail.md)을 따른다.

사용자가 태그를 고르고 해제하는 조작은 이 문서에서 다루지 않는다. 태그 선택의 행동과 정책은 [항목 태그 입력 컴포넌트 스펙](../client/entity-tag-input.md)에서, 구체적인 표현과 조작 수단은 [항목 태그 입력 컴포넌트 디자인](../../design/entity-tag-input.md)에서 다룬다. 체크리스트를 추가하면서 태그를 연결하는 행동은 [ChecklistAdd 화면 스펙](../client/checklist-add.md)에서, 이미 저장된 체크리스트의 태그 연결을 바꾸는 행동은 [ChecklistDetail 화면 스펙](../client/checklist-detail.md)에서, 태그로 체크리스트를 찾아보는 목록은 [TagDetail 체크리스트 탭 스펙](../client/tag-detail-checklist.md)에서 다룬다. 이 문서에는 사용자 조작이 없어 `feature` 영역을 생략한다.

## domain

### 연결이 바꾸지 않는 내용

연결은 체크리스트와 태그 사이의 관계이며, 연결하거나 해제해도 체크리스트의 제목·설명과 체크 항목, 체크 항목의 내용과 체크 여부나 태그의 이모지·제목·설명·컬러를 바꾸지 않는다.

### 대표 태그를 두지 않음

체크리스트는 [항목 태그 연결 공통 스펙](./entity-tag.md)의 `대표 태그`에 따라 대표 태그를 갖지 않는다.

### 체크 항목

태그는 체크리스트에만 연결된다. 체크리스트에 속한 체크 항목은 태그와 연결되지 않으며, 체크리스트의 태그 연결이 체크 항목에 이어지지도 않는다.
