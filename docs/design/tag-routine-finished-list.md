# TagRoutineFinishedList 화면 디자인

기준 스펙: [TagRoutineFinishedList 화면 스펙](../spec/client/tag-routine-finished-list.md)

화면 구조, 목록과 빈 상태·새로고침의 공통 표현은 [완료 목록 공통 디자인](./finished-list.md)을 따른다. 같은 문서의 `정렬 줄`은 따르지 않는다.

## 화면 구조와 이동

TagRoutineFinishedList 화면은 창 너비와 관계없이 TagHome이나 TagDetail과 함께 배치하지 않는다. 완료된 루틴 목록과 루틴 상세를 함께 표시하는 배치는 [TagRoutineFinishedList 목록·상세 배치 디자인](./tag-routine-finished-list-detail.md)을 따른다.

상단 바에는 두 줄 제목을 둔다. 첫 줄에는 대상 태그의 저장 이모지와 제목을, 그 아래 보조 줄에는 `완료된 루틴`을 표시한다. 두 줄 제목의 표시 형태, 첫 줄의 한 줄 유지와 가로 이동, 보조 줄을 가로 이동하지 않는 것, 대상 태그를 아직 조회하지 못한 동안 첫 줄을 비우고 보조 줄만 표시하는 것은 [TagMemoFinishedList 화면 디자인](./tag-memo-finished-list.md)의 `화면 구조와 이동`과 같다.

상단 바 아래 본문은 완료된 루틴 목록 하나로 채운다. 정렬 줄을 두지 않는 것은 [RoutineFinishedList 화면 디자인](./routine-finished-list.md)의 `화면 구조와 이동`과 같다. TagDetail 화면의 표시 범위 버튼도 이 화면에 두지 않는다.

루틴을 선택하면 RoutineDetail 화면을 표시한다. 완료된 루틴 목록과 함께 표시하는지 단독으로 표시하는지는 [TagRoutineFinishedList 목록·상세 배치 디자인](./tag-routine-finished-list-detail.md)을 따른다.

## 목록

루틴 카드와 1열 배치, 카드 문구 `완료`, 빠른 기록 버튼을 두지 않는 것, 자리 표시 카드는 [RoutineFinishedList 화면 디자인](./routine-finished-list.md)의 `목록`을 따른다.

카드에는 그 루틴이 연결한 태그를 표시하지 않는다.

## 빈 상태

빈 상태의 아이콘과 문구는 [목록 빈 상태 디자인](./list-empty-state.md)이 `TagRoutineFinishedList 태그별 완료된 루틴 목록`으로 정한 값을 따른다.

대상 태그를 아직 조회하지 못해 상단 바 첫 줄이 비어 있는 동안에도 목록과 빈 상태는 같은 기준으로 표시한다.

## 다시 시작·삭제 제스처와 피드백

[SwipeToFinishAndDelete 컴포넌트 디자인](./swipe-to-finish-and-delete.md)에 따라 카드를 왼쪽에서 오른쪽으로 스와이프하면 다시 시작하고, 오른쪽에서 왼쪽으로 스와이프하면 삭제한다.

시작 방향의 상태 아이콘에는 완료 아이콘 대신 화살표가 원을 그리는 다시 시작 아이콘을 표시한다. 끝 방향의 삭제 아이콘은 TagDetail 루틴 탭의 루틴 목록과 같다.

다시 시작 또는 삭제 뒤의 피드백과 실행 취소는 스낵바로 표시한다.

## 문구와 접근성

상단 바 첫 줄은 언어 환경과 관계없이 대상 태그에 저장된 이모지와 제목을 그대로 표시한다.

[완료 목록 공통 디자인](./finished-list.md)의 `문구와 접근성`에 더해 한국어 환경과 그 외 기본 환경에서는 다음 문구를 사용한다.

| 문구 | 한국어 | 그 외 기본 |
| --- | --- | --- |
| 상단 바 보조 제목 | `완료된 루틴` | `Finished Routines` |
| 다시 시작 안내 | `루틴을 다시 시작했습니다.` | `Routine restarted.` |
| 삭제 안내 | `루틴이 삭제되었습니다.` | `Routine deleted.` |
| 실행 취소 동작 | `실행 취소` | `Undo` |
| 다시 시작 스와이프 아이콘 접근성 이름 | `루틴 다시 시작` | `Restart routine` |
| 삭제 스와이프 아이콘 접근성 이름 | `루틴 삭제` | `Delete routine` |
