# TagRoutineFinishedList 목록·상세 배치 디자인

기준 스펙: [TagRoutineFinishedList 목록·상세 배치 스펙](../spec/client/tag-routine-finished-list-detail.md)

배치, 크기 조절, 버튼 노출, 선택 전 상세와 조절 상태 표시의 공통 표현은 [목록·상세 배치 공통 디자인](./list-detail-pane.md)을 따른다.

## 두 영역에 두는 화면

목록 영역에는 [TagRoutineFinishedList 화면 디자인](./tag-routine-finished-list.md)의 완료된 루틴 목록을, 상세 영역에는 선택한 루틴의 [RoutineDetail 화면](./routine-detail.md)을 표시한다.

TagDetail 루틴 탭에서 진입한 루틴 상세와 루틴 추가는 이 배치에 참여하지 않고 단독으로 표시한다. 그 기준은 [TagDetail 루틴 탭 디자인](./tag-detail-routine.md)의 `루틴 추가`를 따른다.

각 상태에서 사용할 화면과 뒤로가기 결과, 공통 내비게이션 제공 여부는 기준 스펙과 각 화면 스펙을 따른다.

## 선택 전 상세

아직 루틴을 선택하지 않았으면 상세 영역에 루틴을 나타내는 아이콘과 루틴을 선택하라는 안내를 표시한다. 이는 [RoutineFinishedList 목록·상세 배치 디자인](./routine-finished-list-detail.md)의 선택 전 상세와 같은 표현이다.

## 버튼 노출

목록의 상단 바에는 뒤로가기 버튼을 계속 표시하고, 누르면 루틴 탭이 선택된 그 태그의 TagDetail 화면으로 돌아간다.

이 화면에는 떠 있는 루틴 추가 버튼이 없으므로 상세 영역에 RoutineAdd 화면이 나타나지 않고, 루틴 추가 단축키도 처리하지 않는다.

## 상세 영역이 닫히는 표시

상세 영역의 RoutineDetail 화면에서 루틴 삭제에 성공하거나 시스템 뒤로가기를 사용하면 목록을 그대로 두고 상세 영역만 선택 전 상세로 바꾼다.

상세 영역의 RoutineDetail 화면에서 루틴을 다시 시작했을 때와 기록이나 루틴의 정보를 바꿨을 때의 표시는 [RoutineFinishedList 목록·상세 배치 디자인](./routine-finished-list-detail.md)의 `상세 영역이 닫히는 표시`와 같다.

## 문구와 접근성

한국어 환경과 그 외 기본 환경에서는 다음 문구를 사용한다.

| 문구 | 한국어 | 그 외 기본 |
| --- | --- | --- |
| 선택 전 상세 안내 | `루틴을 선택하세요` | `Choose a routine` |
