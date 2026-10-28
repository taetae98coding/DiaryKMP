# TagMemoFinishedList 목록·상세 배치 스펙

이 문서는 TagMemoFinishedList 화면에서 완료된 메모 목록과 상세를 함께 사용할 때 다른 항목의 완료 메모 목록 배치와 다른 부분만 다룬다. 상세 선택 상태, 뒤로가기와 초기화 경계는 [항목 완료 메모 목록·상세 배치 스펙](./entity-memo-finished-list-detail.md)을 따르며, 이 배치를 떠나면 메모 탭이 선택된 같은 태그의 TagDetail 화면으로 돌아간다. 목록 화면 자체의 내용과 행동은 [TagMemoFinishedList 화면 스펙](./tag-memo-finished-list.md)을, 상세 영역에 표시되는 화면의 내용과 행동은 [MemoDetail 화면 스펙](./memo-detail.md)을 따른다.

디자인: [TagMemoFinishedList 목록·상세 배치 디자인](../../design/tag-memo-finished-list-detail.md)

선택 상태와 표시 비율은 화면 표시 상태일 뿐 사용자 데이터를 만들거나 저장하지 않으므로 `data` 영역은 생략한다.

## domain

### 배치에 참여하지 않는 화면

TagDetail 메모 탭에서 진입한 메모 상세와 메모 추가는 이 배치에 참여하지 않고 단독으로 표시한다. 그 기준은 [TagDetail 메모 탭 스펙](./tag-detail-memo.md)의 `메모 상세 확인`과 `메모 추가`를 따른다. MemoHome에서 사용하는 목록·상세 배치는 [Memo 목록·상세 배치 스펙](./memo-list-detail.md)에서 다룬다.
