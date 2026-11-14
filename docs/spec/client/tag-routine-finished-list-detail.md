# TagRoutineFinishedList 목록·상세 배치 스펙

이 문서는 TagRoutineFinishedList 화면에서 완료된 루틴 목록과 상세를 함께 사용할 때 이 배치에만 해당하는 뒤로가기와 초기화 경계를 다룬다. 함께 사용과 단독 사용, 상세 선택과 교체, 선택 전 상태, 뒤로가기, 표시 비율 조절, 배치에 참여하는 화면과 유지·초기화의 공통 기준은 [목록·상세 배치 공통 스펙](./list-detail-pane.md)을, 완료 목록 배치의 공통 기준은 같은 문서의 `완료 목록의 배치`를 따른다. 상세 영역의 구성과 상세 선택 가운데 완료된 루틴 목록에 공통인 규칙은 [RoutineFinishedList 목록·상세 배치 스펙](./routine-finished-list-detail.md)의 `상세 영역의 구성`과 `상세 선택`을 따른다. 목록 화면 자체의 내용과 행동은 [TagRoutineFinishedList 화면 스펙](./tag-routine-finished-list.md)을, 상세 영역에 표시되는 화면의 내용과 행동은 [RoutineDetail 화면 스펙](./routine-detail.md)을 따른다.

디자인: [TagRoutineFinishedList 목록·상세 배치 디자인](../../design/tag-routine-finished-list-detail.md)

선택 상태와 표시 비율은 화면 표시 상태일 뿐 사용자 데이터를 만들거나 저장하지 않으므로 `data` 영역은 생략한다.

## feature

### 뒤로가기

뒤로가기의 결과는 [목록·상세 배치 공통 스펙](./list-detail-pane.md)의 `뒤로가기`와 `완료 목록의 배치`를 따르며, 이 배치를 떠나면 루틴 탭이 선택된 같은 태그의 TagDetail 화면으로 돌아간다.

## domain

### 배치에 참여하는 화면

이 배치에는 TagRoutineFinishedList 화면과 그 화면에서 진입한 루틴 상세만 참여한다. TagDetail 루틴 탭, RoutineHome, RoutineFinishedList, SearchHome처럼 다른 곳에서 진입한 루틴 상세와 루틴 추가는 이 배치에 참여하지 않는다. TagDetail 루틴 탭에서 진입한 루틴 상세와 루틴 추가는 단독으로 표시하며, 그 기준은 [TagDetail 루틴 탭 스펙](./tag-detail-routine.md)의 `루틴 상세 확인`과 `루틴 추가`를 따른다. RoutineFinishedList에서 진입한 경우의 기준은 [RoutineFinishedList 목록·상세 배치 스펙](./routine-finished-list-detail.md)을 따른다.

### 유지와 초기화 기준

사용자가 목록 화면을 떠나는 것을 이 배치를 떠난 것으로 보며, 그 뒤의 초기화는 [목록·상세 배치 공통 스펙](./list-detail-pane.md)의 `유지와 초기화 기준`을 따른다.
