# 목록 추가 단축키 디자인

기준 스펙: [목록 추가 단축키 스펙](../spec/client/list-add-shortcut.md)

이 문서는 목록 화면에서 항목 추가를 실행하는 키보드 단축키의 키 조합, 처리 조건과 피드백을 소유한다. 추가 버튼의 자리와 모양, 버튼을 눌렀을 때 여는 화면은 각 화면 디자인이 소유한다.

적용 대상은 [목록 추가 단축키 스펙](../spec/client/list-add-shortcut.md)의 `적용 대상`을 따르며, 이 문서를 참조하는 디자인 문서는 다음과 같다.

- [MemoHome 목록 디자인](./memo-home.md)
- [TagHome 목록 디자인](./tag-home.md)
- [RoutineHome 화면 디자인](./routine-home.md)
- [ContactHome 화면 디자인](./contact-home.md)
- [FileHome 화면 디자인](./file-home.md)
- [PlaceHome 화면 디자인](./place-home.md)
- [PlaylistHome 화면 디자인](./playlist-home.md)
- [QrHome 화면 디자인](./qr-home.md)
- [WebHome 화면 디자인](./web-home.md)

## 키 조합

물리 키보드 환경에서 `Command + A`를 누르면 그 화면의 추가 버튼을 누른 것과 같은 동작을 실행한다. macOS와 iOS에서는 `Cmd`, 그 밖의 환경에서는 `Meta`를 `A`와 함께 누른다. 모든 적용 대상이 같은 조합을 써서 사용자가 화면마다 다른 조합을 외우지 않게 한다.

## 처리 조건

추가 버튼을 표시하지 않는 동안에는 단축키도 처리하지 않는다. 목록과 상세를 함께 표시하는 동안 버튼을 감추는 기준은 [목록·상세 배치 공통 디자인](./list-detail-pane.md)의 `버튼 노출`을 따른다.

추가 버튼을 표시하더라도 눌러도 아무 일이 일어나지 않는 동안에는 단축키도 처리하지 않는다. FileHome에서 올리는 중인 파일이 있어 버튼에 진행 표시를 보이는 동안이 그렇다.

목록과 상세를 함께 표시하는 동안 상세 영역 화면의 탭이 같은 `Command + A`를 그 탭의 추가에 쓰면, 그 단축키는 상세 영역의 추가만 실행한다. TagHome과 함께 표시하는 [TagDetail 화면 디자인](./tag-detail.md), ContactHome과 함께 표시하는 [ContactDetail 화면 디자인](./contact-detail.md)의 `키보드 단축키`가 탭별 단축키를 소유한다.

## 피드백

단축키만의 별도 피드백은 두지 않는다. 버튼을 눌렀을 때와 같은 화면 전환이 일어나고, 추가 버튼에는 누름 표현을 보이지 않는다.
