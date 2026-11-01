# 항목 완료 메모 목록·상세 배치 스펙

이 문서는 TagMemoFinishedList, ContactMemoFinishedList, PlaceMemoFinishedList, WebMemoFinishedList 화면에서 완료된 메모 목록과 상세를 함께 사용할 때 이 배치에만 해당하는 상세 선택 상태, 뒤로가기와 초기화 경계를 다룬다. 함께 사용과 단독 사용, 상세 선택과 교체, 선택 전 상태, 뒤로가기, 표시 비율 조절, 배치에 참여하는 화면과 유지·초기화의 공통 기준은 [목록·상세 배치 공통 스펙](./list-detail-pane.md)을, 완료 목록 배치의 공통 기준은 같은 문서의 `완료 목록의 배치`를 따른다. 목록 화면 자체의 내용과 행동은 [항목 완료 메모 목록 화면 스펙](./entity-memo-finished-list.md)을, 상세 영역에 표시되는 화면의 내용과 행동은 [MemoDetail 화면 스펙](./memo-detail.md)을 따른다.

상세 영역의 구성, 뒤로가기와 상세 선택 가운데 완료된 메모 목록에 공통인 규칙은 [MemoFinishedList 목록·상세 배치 스펙](./memo-finished-list-detail.md)의 `상세 영역의 구성`, `뒤로가기`, `상세 선택`을 따른다. TagMemoFinishedList에만 해당하는 차이는 [TagMemoFinishedList 목록·상세 배치 스펙](./tag-memo-finished-list-detail.md)이 정한다.

디자인: [항목 완료 메모 목록·상세 배치 디자인](../../design/entity-memo-finished-list-detail.md)

선택 상태와 표시 비율은 화면 표시 상태일 뿐 사용자 데이터를 만들거나 저장하지 않으므로 `data` 영역은 생략한다.

## feature

### 뒤로가기

이 배치를 떠나면 메모 탭이 선택된 같은 항목의 상세 화면으로 돌아간다.

## domain

### 배치에 참여하는 화면

이 배치에는 완료 메모 목록 화면과 그 화면에서 진입한 메모 상세만 참여한다. TagDetail·ContactDetail·PlaceDetail·WebDetail 메모 탭, 캘린더 홈, MemoFinishedList처럼 다른 곳에서 진입한 메모 상세와 메모 추가는 이 배치에 참여하지 않는다. 메모 탭에서 진입한 경우의 기준은 [항목 상세 메모 탭 공통 스펙](./entity-detail-memo.md)과 [TagDetail 메모 탭 스펙](./tag-detail-memo.md)의 `메모 상세 확인`과 `메모 추가`를, MemoFinishedList에서 진입한 경우의 기준은 [MemoFinishedList 목록·상세 배치 스펙](./memo-finished-list-detail.md)을 따른다.

### 유지와 초기화 기준

사용자가 목록 화면을 떠나는 것을 이 배치를 떠난 것으로 보며, 그 뒤의 초기화는 [목록·상세 배치 공통 스펙](./list-detail-pane.md)의 `유지와 초기화 기준`을 따른다.
