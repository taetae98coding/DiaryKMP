# Tag 목록·상세 배치 디자인

기준 스펙: [Tag 목록·상세 배치 스펙](../spec/client/tag-list-detail.md)

배치, 크기 조절, 버튼 노출과 조절 상태 표시의 공통 표현은 [목록·상세 배치 공통 디자인](./list-detail-pane.md)을 따른다.

## 두 영역에 두는 화면

목록 영역에는 태그 목록을 두고, 상세 영역에는 선택한 태그의 TagDetail 화면을 표시한다. 선택한 태그가 없으면 TagAdd 화면을 표시한다.

선택한 태그가 없을 때 상세 영역에는 [목록·상세 배치 공통 디자인](./list-detail-pane.md)의 `선택 전 상세` 안내를 두지 않고 TagAdd 화면을 그대로 표시한다. 이 TagAdd 화면의 상단 바에는 뒤로가기 버튼을 표시하지 않는다.

상세 영역에 TagAdd 화면이 놓인 동안에는 태그 목록의 추가 버튼을 감추고, TagDetail 화면이 놓인 동안에는 표시한다. 기준은 [목록·상세 배치 공통 디자인](./list-detail-pane.md)의 `버튼 노출`을 따른다.

상세 영역에서 이어 연 TagAdd 화면과 TagDetail 화면의 뒤로가기 버튼 표시는 [TagAdd 화면 디자인](./tag-add.md)과 [TagDetail 화면 디자인](./tag-detail.md)의 상단 바 기준을 따른다.
