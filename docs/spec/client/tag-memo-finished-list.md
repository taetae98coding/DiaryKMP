# TagMemoFinishedList 화면 스펙

이 문서는 사용자가 TagDetail 화면의 메모 탭에서 그 태그의 완료된 메모를 모아 확인하는 TagMemoFinishedList 화면 가운데 다른 항목의 완료 메모 목록과 다른 부분만 다룬다. 진입과 이동, 목록 표시, 빈 상태, 메모 상세 확인, 다시 시작과 삭제, 노출 기준, 실행 경계와 조회·저장은 [항목 완료 메모 목록 화면 스펙](./entity-memo-finished-list.md)을 따르며, 이 화면의 상세 대상 항목은 태그이고 짝이 되는 목록은 같은 태그의 [TagDetail 메모 탭](./tag-detail-memo.md)이다. 완료 목록의 공통 규칙은 [완료 목록 공통 스펙](./finished-list.md)을 따른다.

목록과 메모 상세를 함께 사용할 때의 선택 상태와 뒤로가기는 [TagMemoFinishedList 목록·상세 배치 스펙](./tag-memo-finished-list-detail.md)에서 다룬다.

디자인: [TagMemoFinishedList 화면 디자인](../../design/tag-memo-finished-list.md)

## feature

### 진입과 이동

대상 태그의 완료 여부, 삭제 여부와 조회 결과는 이동을 막지 않는다. 그 기준은 [TagDetail 메모 탭 스펙](./tag-detail-memo.md)의 `완료된 메모 확인`을 따른다.

### 화면 제목

화면 제목에는 대상 태그의 최신 저장 이모지와 제목을 표시하고, 완료된 메모를 모아 보여 주는 목록임을 함께 알린다. 이모지 표시 규칙은 [TagHome 목록 스펙](./tag-home.md)의 `태그 이모지 표시`를 따르며, 화면이 열려 있는 동안 대상 태그의 저장 이모지나 제목이 다른 경로에서 바뀌면 화면 제목을 최신 저장 내용으로 갱신한다.

대상 태그를 아직 조회하지 못했거나 조회할 수 없는 동안의 화면 제목과 한 줄 표시는 [항목 완료 메모 목록 화면 스펙](./entity-memo-finished-list.md)의 `화면 제목`과 같으며, 이모지와 제목을 함께 이름으로 본다.

## domain

### 태그 연결과 노출 기준

[항목 완료 메모 목록 화면 스펙](./entity-memo-finished-list.md)의 `항목 연결과 노출 기준`에서 상세 대상 항목은 대상 태그 하나다. 짝이 되는 메모 탭과 달리 TagDetail 화면의 표시 범위는 이 목록에 적용하지 않는다. 그 기준은 [TagDetail 메모 탭 스펙](./tag-detail-memo.md)의 `완료된 메모 확인`을 따른다.
