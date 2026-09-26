# 장소 태그 스펙

이 문서는 하나의 장소가 여러 태그와 연결될 수 있다는 규칙 가운데 장소에만 해당하는 부분을 다룬다. 태그 연결이 공통으로 지키는 규칙은 [항목 태그 연결 공통 스펙](./entity-tag.md)을, 연결의 성질과 동기화 계약은 [항목 연결 공통 스펙](./entity-link.md)을, 앱이 연결을 기기에 저장하는 규칙은 [항목 연결 앱 스펙](../client/entity-link.md)을 따른다. 이 연결이 앱의 목록과 검색 결과에 주는 영향(`연결의 의미`)과 장소 컬러의 표시(`장소 컬러`)는 [장소 태그 앱 스펙](../client/place-tag.md)이 소유한다.

장소 자체의 추가·수정·삭제는 [PlaceAdd 화면 스펙](../client/place-add.md)과 [PlaceDetail 화면 스펙](../client/place-detail.md)을, 태그 자체의 추가·완료·삭제는 [TagAdd 화면 스펙](../client/tag-add.md)과 [TagDetail 화면 스펙](../client/tag-detail.md)을 따른다.

사용자가 태그를 고르고 해제하는 조작은 이 문서에서 다루지 않는다. 태그 선택의 행동과 정책은 [항목 태그 입력 컴포넌트 스펙](../client/entity-tag-input.md)에서, 구체적인 표현과 조작 수단은 [항목 태그 입력 컴포넌트 디자인](../../design/entity-tag-input.md)에서 다룬다. 장소를 추가하면서 태그를 연결하는 행동은 [PlaceAdd 화면 스펙](../client/place-add.md)에서, 이미 저장된 장소의 태그 연결을 바꾸는 행동은 [PlaceDetail 화면 스펙](../client/place-detail.md)에서, 태그로 장소를 찾아보는 목록은 [TagDetail 장소 탭 스펙](../client/tag-detail-place.md)에서 다룬다. 이 문서에는 사용자 조작이 없어 `feature` 영역을 생략한다.

메모와 장소의 연결은 이 문서가 다루는 연결과 별개이며 [메모 장소 스펙](./memo-place.md)이 소유한다.

## domain

### 연결이 바꾸지 않는 내용

연결은 장소와 태그 사이의 관계이며, 연결하거나 해제해도 장소의 제목·설명·컬러·좌표·주소나 태그의 이모지·제목·설명·컬러를 바꾸지 않는다.

### 대표 태그

장소는 [항목 태그 연결 공통 스펙](./entity-tag.md)의 `대표 태그`에 따라 대표 태그를 갖지 않는다.
