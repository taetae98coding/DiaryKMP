# TagFinishedList 목록·상세 배치 디자인

기준 스펙: [TagFinishedList 목록·상세 배치 스펙](../spec/client/tag-finished-list-detail.md)

배치, 크기 조절, 버튼 노출, 선택 전 상세와 조절 상태 표시의 공통 표현은 [목록·상세 배치 공통 디자인](./list-detail-pane.md)을 따른다.

## 두 영역에 두는 화면

목록 영역에는 [TagFinishedList 화면 디자인](./tag-finished-list.md)의 완료된 태그 목록을, 상세 영역에는 선택한 태그의 TagDetail 화면을 표시한다. TagHome 목록은 이 배치의 어느 영역에도 표시하지 않는다.

목록 영역의 태그 카드는 영역 너비가 좁아져도 [TagFinishedList 화면 디자인](./tag-finished-list.md)의 2열 고정 그리드를 유지한다.

상세 영역의 TagDetail 화면에서 연결 입력으로 연 TagAdd 화면과 연결한 태그의 TagDetail 화면은 상세 영역에 표시하고, 뒤로가기 버튼 표시는 [TagAdd 화면 디자인](./tag-add.md)과 [TagDetail 화면 디자인](./tag-detail.md)의 상단 바 기준을 따른다. 이는 [Tag 목록·상세 배치 디자인](./tag-list-detail.md)에서 상세 영역에 TagDetail 화면이 놓였을 때와 같다.

상세 영역의 TagDetail 화면에서 메모·웹·장소 탭으로 연 화면과 TagMemoFinishedList 화면은 이 배치에 두지 않으며, 그 표시는 각 탭 디자인과 [TagMemoFinishedList 목록·상세 배치 디자인](./tag-memo-finished-list-detail.md)을 따른다.

각 상태에서 사용할 화면과 뒤로가기 결과, 공통 내비게이션 제공 여부는 기준 스펙과 각 화면 스펙을 따른다.

## 선택 전 상세

아직 태그를 선택하지 않았으면 상세 영역에 태그를 나타내는 아이콘과 태그를 선택하라는 안내를 표시한다. 아이콘은 [목록 빈 상태 디자인](./list-empty-state.md)이 TagHome 태그 목록에 쓰는 태그 아이콘과 같은 것을 쓴다.

[Tag 목록·상세 배치 디자인](./tag-list-detail.md)과 달리 선택 전 상세에 TagAdd 화면을 두지 않는다.

## 버튼 노출

목록의 상단 바에는 뒤로가기 버튼을 계속 표시하고, 누르면 TagHome 목록으로 돌아간다.

이 화면에는 떠 있는 태그 추가 버튼이 없으므로 선택 전 상세에 TagAdd 화면이 나타나지 않고, 태그 추가 단축키도 처리하지 않는다.

## 상세 영역이 닫히는 표시

목록에서 고른 TagDetail 화면에서 태그 삭제에 성공하거나 시스템 뒤로가기를 사용하면 목록을 그대로 두고 상세 영역만 선택 전 상세로 바꾼다.

상세 영역의 TagDetail 화면에서 태그를 다시 시작하면 그 카드는 목록에서 사라지지만 상세 영역은 같은 TagDetail 화면을 계속 표시한다.

## 문구와 접근성

한국어 환경과 그 외 기본 환경에서는 다음 문구를 사용한다.

| 문구 | 한국어 | 그 외 기본 |
| --- | --- | --- |
| 선택 전 상세 안내 | `태그를 선택하세요` | `Choose a tag` |
