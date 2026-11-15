# RoutineFinishedList 목록·상세 배치 스펙

이 문서는 RoutineFinishedList 화면에서 완료된 루틴 목록과 상세를 함께 사용할 때 이 배치에만 해당하는 상세 선택 상태, 뒤로가기와 초기화 경계를 다룬다. 함께 사용과 단독 사용, 상세 선택과 교체, 선택 전 상태, 뒤로가기, 표시 비율 조절, 배치에 참여하는 화면과 유지·초기화의 공통 기준은 [목록·상세 배치 공통 스펙](./list-detail-pane.md)을, 완료 목록 배치의 공통 기준은 같은 문서의 `완료 목록의 배치`를 따른다. 목록 화면 자체의 내용과 행동은 [RoutineFinishedList 화면 스펙](./routine-finished-list.md)을, 상세 영역에 표시되는 화면의 내용과 행동은 [RoutineDetail 화면 스펙](./routine-detail.md)을 따른다.

RoutineHome에서 사용하는 목록·상세 배치는 [Routine 목록·상세 배치 스펙](./routine-list-detail.md)에서 다룬다. 이 화면은 RoutineHome의 루틴 목록과 함께 표시하지 않으며, RoutineHome의 배치 대신 이 배치를 사용한다. 완료된 루틴 목록의 다른 배치도 이 문서의 루틴 상세 규칙을 따르며, 그 목록은 끝의 `이 문서를 참조하는 문서`에 모았다.

디자인: [RoutineFinishedList 목록·상세 배치 디자인](../../design/routine-finished-list-detail.md)

선택 상태와 표시 비율은 화면 표시 상태일 뿐 사용자 데이터를 만들거나 저장하지 않으므로 `data` 영역은 생략한다.

## feature

### 상세 영역의 구성

아직 루틴을 선택하지 않았으면 상세는 선택 전 상태이며, 목록에서 루틴을 선택하라는 안내만 제공한다.

상세 영역의 루틴 상세에서 루틴을 다시 시작하면 그 루틴은 목록에서 사라지지만 상세 영역의 상세는 그대로 남고, 그 상세에서 다시 완료하면 목록에 다시 나타난다. 기준은 [목록·상세 배치 공통 스펙](./list-detail-pane.md)의 `완료 목록의 배치`와 [RoutineDetail 화면 스펙](./routine-detail.md)의 `완료와 다시 시작`을 따른다.

상세 영역의 루틴 상세에서 [RoutineDetail 화면 스펙](./routine-detail.md)의 `기록 넣기`와 `기록 빼기`로 기록을 바꾸거나 루틴을 수정해도 목록의 카드 문구는 `완료` 그대로다. 제목이나 컬러, 시작 날짜를 고치면 목록에 놓인 그 루틴의 제목, 컬러와 자리가 같은 화면에서 바뀐다.

상세 영역의 루틴 상세에서 태그의 상세나 추가로 이동한 화면은 이 배치에 참여하지 않고 단독으로 표시한다. 그 화면에서 돌아온 결과는 [목록·상세 배치 공통 스펙](./list-detail-pane.md)의 `배치에 참여하는 화면`을 따른다.

### 뒤로가기

뒤로가기의 결과는 [목록·상세 배치 공통 스펙](./list-detail-pane.md)의 `뒤로가기`와 `완료 목록의 배치`를 따르며, 이 배치를 떠나면 RoutineHome의 목록 보기로 돌아간다.

## domain

### 배치에 참여하는 화면

이 배치에는 RoutineFinishedList 화면과 그 화면에서 진입한 루틴 상세만 참여한다. RoutineHome의 루틴 목록과 달력 보기, TagDetail 루틴 탭, TagRoutineFinishedList, SearchHome처럼 다른 곳에서 진입한 루틴 상세와 루틴 추가는 이 배치에 참여하지 않고 각 진입 화면의 기준을 따른다.

### 상세 선택

상세 영역이 가질 수 있는 상태는 선택 전 상태와 목록에서 선택한 루틴의 상세뿐이다.

### 유지와 초기화 기준

사용자가 목록 화면을 떠나 RoutineHome으로 돌아가는 것을 이 배치를 떠난 것으로 본다. 이 화면에서는 공통 내비게이션을 제공하지 않지만, 파일 올리기 알림을 선택해 `더보기` 위에 FileHome 화면이 열리는 것처럼 앱이 다른 주요 목적지를 여는 것도 이 배치를 떠난 것으로 본다. 그 이동의 결과는 [파일 올리기 알림 스펙](./file-upload-notification.md)의 `알림에서 앱 열기`와 [TopLevelNavigation 스펙](./top-level-navigation.md)의 `목적지 이동과 화면 상태`를 따르며, 그 뒤 루틴으로 돌아오면 RoutineHome의 목록 보기가 보인다. 그 뒤의 초기화는 [목록·상세 배치 공통 스펙](./list-detail-pane.md)의 `유지와 초기화 기준`을 따른다.

## 이 문서를 참조하는 문서

- [TagRoutineFinishedList 목록·상세 배치 스펙](./tag-routine-finished-list-detail.md)
