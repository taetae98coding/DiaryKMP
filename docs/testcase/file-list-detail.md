# File 목록·상세 배치 테스트 케이스

기준 스펙: [File 목록·상세 배치 스펙](../spec/client/file-list-detail.md)

이 문서에서 `domain > 배치에 참여하는 화면`, `feature > 추가 진입 수단`, `feature > 함께 사용과 단독 사용`, `feature > 함께 사용할 때의 행동` 절은 [목록·상세 배치 공통 스펙](../spec/client/list-detail-pane.md)이 소유한다. 나머지 케이스의 절은 기준 스펙이 소유한다.

표시 비율 조절의 공통 행동과 유지 기준은 [목록·상세 배치 공통 스펙](../spec/client/list-detail-pane.md)에 위임되어 있다. 한 화면만 사용하는 환경에서 파일 목록에서 FileAdd나 FileDetail로 이동하는 케이스는 [FileHome 테스트 케이스](./file-home.md)에서, 상세 영역에서 파일을 올리는 케이스는 [FileAdd 테스트 케이스](./file-add.md)에서, 상세 영역에서 파일 정보를 고치고 내려받는 케이스는 [FileDetail 테스트 케이스](./file-detail.md)에서, 올리기가 끝났을 때 어느 화면을 보고 있는 것으로 판정하는지는 [파일 올리기 알림 테스트 케이스](./file-upload-notification.md)에서 다룬다.

상세 영역에 FileAdd가 놓인 동안 끝난 올리기의 결과를 FileAdd가 알리는 것은 [FileAdd 테스트 케이스](./file-add.md)의 올리기 결과 케이스가, 두 파일 화면을 함께 표시할 때 FileAdd를 보고 있는 것으로 판정하는 것은 [파일 올리기 알림 테스트 케이스](./file-upload-notification.md)의 TC-FILE-UPLOAD-NOTIFICATION-DOMAIN-006이 다룬다.

## feature

### TC-FILE-LIST-DETAIL-FEATURE-001: 목록과 상세를 함께 쓰는 환경에서는 두 영역을 함께 표시한다

- 근거: `feature > 함께 사용과 단독 사용`
- Given: 목록과 상세를 함께 사용할 수 있는 환경이고 로그인한 상태다.
- When: 사용자가 FileHome 화면에 진입한다.
- Then: 파일 목록을 유지한 채 상세 영역에 FileAdd가 함께 표시된다.

### TC-FILE-LIST-DETAIL-FEATURE-002: 한 화면만 제공하는 환경에서는 현재 화면만 단독으로 표시한다

- 근거: `feature > 함께 사용과 단독 사용`
- Given: 목록과 상세를 함께 사용할 수 없는 환경이고 로그인한 상태다.
- When: 사용자가 파일 목록 또는 FileAdd를 사용한다.
- Then: 현재 사용하는 화면만 단독으로 표시된다.

### TC-FILE-LIST-DETAIL-FEATURE-003: 상세 영역에 FileAdd가 놓이면 목록에서 추가 수단을 제공하지 않는다

- 근거: `feature > 추가 진입 수단`
- Given: 로그인한 상태이고 상세 영역에 FileAdd가 놓여 있다.
- When: 사용자가 파일 목록에서 실행할 수 있는 동작을 확인한다.
- Then: 파일 추가 동작이 제공되지 않는다. 추가 단축키를 눌러도 추가가 실행되지 않는다.

### TC-FILE-LIST-DETAIL-FEATURE-004: 상세 영역에 FileAdd가 놓여 있지 않으면 목록에서 추가 수단을 제공한다

- 근거: `feature > 추가 진입 수단`
- Given: 로그인한 상태이고 상세 영역이 테스트 데이터의 상태다.
- When: 사용자가 파일 목록에서 실행할 수 있는 동작을 확인한다.
- Then: 파일 추가 동작이 제공된다.
- 테스트 데이터:

| 상세 영역 |
| --- |
| 상세 영역을 함께 표시하지 않음 |
| FileDetail이 놓여 있음 |

### TC-FILE-LIST-DETAIL-FEATURE-005: 목록과 함께 표시되면 상세 영역의 화면에서 뒤로가기 수단을 제공하지 않는다

- 근거: `feature > 함께 사용할 때의 행동`
- Given: 파일 목록과 테스트 데이터의 화면이 함께 표시되어 있다.
- When: 사용자가 상세 영역의 화면에서 실행할 수 있는 동작을 확인한다.
- Then: 뒤로가기 동작이 제공되지 않는다.
- 테스트 데이터:

| 상세 영역의 화면 |
| --- |
| FileAdd |
| FileDetail |

### TC-FILE-LIST-DETAIL-FEATURE-006: 단독으로 표시되면 그 화면에서 뒤로가기 수단을 제공한다

- 근거: `feature > 함께 사용과 단독 사용`
- Given: 테스트 데이터의 화면이 단독으로 표시되어 있다.
- When: 사용자가 그 화면에서 실행할 수 있는 동작을 확인한다.
- Then: 뒤로가기 동작이 제공된다.
- 테스트 데이터:

| 화면 |
| --- |
| FileAdd |
| FileDetail |

### TC-FILE-LIST-DETAIL-FEATURE-007: 목록에서 파일을 선택하면 상세 영역이 그 파일의 FileDetail로 바뀐다

- 근거: `feature > 상세 영역의 구성`
- Given: 파일 목록과 FileAdd가 함께 표시되어 있다.
- When: 사용자가 목록에서 파일 하나를 선택한다.
- Then: 목록은 그대로 유지되고 상세 영역이 그 파일의 FileDetail로 바뀐다.

### TC-FILE-LIST-DETAIL-FEATURE-008: 상세 영역에서 뒤로가면 FileAdd로 되돌아간다

- 근거: `feature > 뒤로가기`
- Given: 파일 목록과 선택한 파일의 FileDetail이 함께 표시되어 있다.
- When: 사용자가 상세 영역에서 시스템 뒤로가기를 실행한다.
- Then: 목록은 그대로 유지되고 상세 영역이 FileAdd로 되돌아간다.

### TC-FILE-LIST-DETAIL-FEATURE-009: FileDetail이 놓인 동안 파일 추가를 실행하면 상세 영역이 FileAdd로 바뀐다

- 근거: `feature > 상세 영역의 구성`
- Given: 파일 목록과 선택한 파일의 FileDetail이 함께 표시되어 있고 올리는 중인 파일이 없다.
- When: 사용자가 목록에서 파일 추가를 실행한다.
- Then: 목록은 그대로 유지되고 상세 영역이 FileAdd로 바뀌며 선택한 파일이 없는 상태가 된다.

### TC-FILE-LIST-DETAIL-FEATURE-010: FileDetail 위에서 연 FileAdd에서 뒤로가면 이전 FileDetail로 돌아간다

- 근거: `feature > 상세 영역의 구성`
- Given: 파일 목록과 선택한 파일의 FileDetail이 함께 표시된 상태에서 사용자가 목록의 파일 추가를 실행해 상세 영역에 FileAdd가 놓여 있다.
- When: 사용자가 상세 영역에서 시스템 뒤로가기를 실행한다.
- Then: 목록은 그대로 유지되고 상세 영역이 FileAdd를 열기 전에 보던 파일의 FileDetail로 돌아간다.

### TC-FILE-LIST-DETAIL-FEATURE-011: FileDetail 위에서 연 FileAdd는 빈 입력으로 시작한다

- 근거: `feature > 상세 영역의 구성`
- Given: 파일 목록과 상세 영역이 함께 표시되고, 선택한 파일이 없을 때의 FileAdd에 제목과 설명을 적고 파일을 고른 뒤 목록에서 파일을 골라 상세 영역에 FileDetail이 놓여 있다.
- When: 사용자가 목록에서 파일 추가를 실행한다.
- Then: 목록은 그대로 유지되고, 상세 영역에 놓인 FileAdd의 제목과 설명이 비어 있으며 고른 파일 없음으로 표시된다.

### TC-FILE-LIST-DETAIL-FEATURE-012: FileDetail이 놓인 동안 다른 파일을 고르면 앞 파일의 FileDetail을 쌓지 않고 교체한다

- 근거: `feature > 상세 영역의 구성`
- Given: 파일 목록과 FileAdd가 함께 표시되어 있고 목록에 파일 A와 파일 B가 있다.
- When: 사용자가 목록에서 파일 A를 고른 뒤 파일 B를 고르고, 시스템 뒤로가기를 사용한다.
- Then: 파일 B를 고르면 상세 영역이 파일 B의 FileDetail로 바뀌고, 뒤로가기를 사용하면 파일 A의 FileDetail이 아니라 FileAdd로 되돌아가며 목록은 그대로 유지된다.

### TC-FILE-LIST-DETAIL-FEATURE-013: 목록에서 연 FileAdd가 놓인 동안 다른 파일을 고르면 FileAdd를 걷어내고 그 FileDetail로 바꾼다

- 근거: `feature > 상세 영역의 구성`
- Given: 목록과 상세를 함께 사용할 수 있는 환경이고, 테스트 데이터의 상태에서 목록의 파일 추가로 연 FileAdd가 상세 영역에 놓여 있으며 그 FileAdd에 제목을 적어 두었다.
- When: 사용자가 목록에서 파일 B를 고른 뒤 시스템 뒤로가기를 실행한다.
- Then: 상세 영역이 파일 B의 FileDetail로 바뀌고, 뒤로가기를 실행하면 그 전에 보던 FileDetail이나 걷어낸 FileAdd가 아니라 선택한 파일이 없을 때의 FileAdd로 되돌아가며, 걷어낸 FileAdd에 적어 둔 제목은 보이지 않는다.
- 테스트 데이터:

| FileAdd를 열기 전의 상태 |
| --- |
| 상세 영역에 파일 A의 FileDetail이 놓여 있었음 |
| 한 영역만 표시되던 동안 목록에서 FileAdd를 열었고, 그 뒤 창이 넓어짐 |

### TC-FILE-LIST-DETAIL-FEATURE-014: 파일을 고르지 않은 상태에서 뒤로가면 `더보기`로 돌아간다

- 근거: `feature > 뒤로가기`
- Given: 파일 목록과 테스트 데이터의 상세 영역이 함께 표시되어 있다.
- When: 사용자가 시스템 뒤로가기를 사용한다.
- Then: 두 영역이 함께 사라지고 `더보기` 화면으로 돌아간다.
- 테스트 데이터:

| 상세 영역 |
| --- |
| 선택한 파일이 없을 때의 FileAdd |
| 게스트 상태라 비어 있음 |

### TC-FILE-LIST-DETAIL-FEATURE-015: 목록의 뒤로가기 동작은 상세 영역의 화면과 관계없이 `더보기`로 돌아간다

- 근거: `feature > 뒤로가기`
- Given: 파일 목록과 테스트 데이터의 화면이 함께 표시되어 있다.
- When: 사용자가 파일 목록의 뒤로가기 동작을 선택한다.
- Then: 두 영역이 함께 사라지고 `더보기` 화면으로 돌아간다.
- 테스트 데이터:

| 상세 영역의 화면 |
| --- |
| 선택한 파일이 없을 때의 FileAdd |
| 목록에서 연 FileAdd |
| FileDetail |

### TC-FILE-LIST-DETAIL-FEATURE-016: 상세 영역에 FileDetail이 놓인 동안 끝난 올리기의 실패는 목록에서 알린다

- 근거: `feature > 올리기 결과를 알리는 화면`
- Given: 사용자 언어가 한국어이고, 파일 목록과 선택한 파일의 FileDetail이 함께 표시된 동안 올리는 중이다.
- When: 올리기가 그 밖의 이유로 실패한다.
- Then: 목록 영역에 `파일을 올리지 못했습니다` 안내가 표시되고 상세 영역에는 FileDetail이 그대로 남는다.

### TC-FILE-LIST-DETAIL-FEATURE-017: 상세 영역의 화면이 계정 변경으로 닫히면 뒤로가기를 실행한 것과 같이 되돌아간다

- 근거: `feature > 상세 영역의 구성`
- Given: 파일 목록과 테스트 데이터의 상세 영역 화면이 함께 표시되어 있다.
- When: 계정이 테스트 데이터의 상태로 바뀐다.
- Then: 목록은 그대로 유지되고 상세 영역이 테스트 데이터의 결과가 된다.
- 테스트 데이터:

| 상세 영역의 화면 | 바뀐 계정 상태 | 결과 |
| --- | --- | --- |
| FileDetail | 다른 사용자 | 선택한 파일이 없을 때의 FileAdd로 되돌아간다 |
| FileDetail을 보다가 목록에서 연 FileAdd | 게스트 | FileAdd와 그 아래의 FileDetail이 모두 닫히고 상세 영역이 비어 있다 |

### TC-FILE-LIST-DETAIL-FEATURE-018: FileDetail 위에서 연 FileAdd가 놓인 동안 다른 사용자로 바뀌면 FileAdd는 남고 뒤로가면 FileDetail이 닫힌다

- 근거: `feature > 상세 영역의 구성`
- Given: 파일 목록과 선택한 파일의 FileDetail이 함께 표시된 상태에서 목록의 파일 추가로 연 FileAdd가 상세 영역에 놓여 있고, 그 FileAdd에 제목을 적어 두었다.
- When: 계정이 다른 사용자로 바뀐 뒤 사용자가 상세 영역에서 시스템 뒤로가기를 실행한다.
- Then: 계정이 바뀐 뒤에도 상세 영역에는 FileAdd와 적어 둔 제목이 그대로 남고, 뒤로가기를 실행하면 FileDetail이 다시 보이지 않고 상세 영역이 선택한 파일이 없을 때의 FileAdd로 돌아간다.

### TC-FILE-LIST-DETAIL-FEATURE-019: 선택 전 FileAdd에서 올리기를 시작해도 상세 영역은 FileAdd로 남는다

- 근거: `feature > 상세 영역의 구성`
- Given: 파일 목록과 선택한 파일이 없을 때의 FileAdd가 함께 표시되고, FileAdd에 제목을 적고 파일을 골랐다. 올리기 요청이 받아들여지도록 설정되어 있다.
- When: 사용자가 FileAdd에서 올리기를 실행한다.
- Then: 목록과 상세 영역의 FileAdd가 그대로 표시되고, FileAdd의 제목이 비고 고른 파일 없음으로 표시된다.

### TC-FILE-LIST-DETAIL-FEATURE-020: 선택 전 FileAdd에서 올린 파일은 성공하면 같은 화면의 목록에 나타난다

- 근거: `feature > 상세 영역의 구성`
- Given: 사용자 언어가 한국어이고, 파일 목록과 선택한 파일이 없을 때의 FileAdd가 함께 표시된 동안 올리는 중이다. 목록을 다시 불러오면 서버가 방금 올린 파일을 맨 앞에 담아 돌려주도록 설정되어 있다.
- When: 올리기가 성공으로 끝난다.
- Then: 상세 영역의 FileAdd에 `파일을 올렸습니다.` 안내가 표시되고 FileAdd가 그대로 남으며, 목록에 방금 올린 파일이 나타난다.

### TC-FILE-LIST-DETAIL-FEATURE-021: 올리는 중에도 목록에서 파일을 선택해 상세 영역에 FileDetail을 연다

- 근거: `feature > 상세 영역의 구성`
- Given: 파일 목록과 상세 영역이 함께 표시된 동안 올리는 중이다.
- When: 사용자가 목록에서 파일 하나를 선택한다.
- Then: 목록은 그대로 유지되고 상세 영역이 그 파일의 FileDetail로 바뀐다.

### TC-FILE-LIST-DETAIL-FEATURE-022: 목록에서 연 FileAdd를 닫아도 선택 전 FileAdd의 작성 내용이 남는다

- 근거: `feature > 상세 영역의 구성`, `domain > 유지와 초기화 기준`
- Given: 파일 목록과 상세 영역이 함께 표시되고, 선택한 파일이 없을 때의 FileAdd에 제목과 설명을 적고 파일을 고른 뒤, 목록에서 파일 A를 골라 연 FileDetail 위에 목록의 파일 추가로 FileAdd를 열어 두었다.
- When: 사용자가 테스트 데이터의 방법으로 목록에서 연 FileAdd를 닫고, 상세 영역이 선택한 파일이 없을 때의 FileAdd로 돌아올 때까지 시스템 뒤로가기를 실행한다.
- Then: 선택한 파일이 없을 때의 FileAdd에 적어 두던 제목과 설명, 고른 파일이 그대로 남아 있다.
- 테스트 데이터:

| 닫는 방법 |
| --- |
| 시스템 뒤로가기 |
| 목록에서 파일 B를 골라 FileAdd를 걷어냄 |

## domain

### TC-FILE-LIST-DETAIL-DOMAIN-001: 파일 목록에서 진입하지 않은 FileAdd와 FileDetail은 목록과 함께 표시하지 않는다

- 근거: `domain > 배치에 참여하는 화면`
- Given: 목록과 상세를 함께 사용할 수 있는 환경이다.
- When: 사용자가 파일 목록이 아닌 화면에서 테스트 데이터의 화면으로 이동한다.
- Then: 그 화면만 단독으로 표시되고 파일 목록은 함께 표시되지 않는다.
- 테스트 데이터:

| 화면 |
| --- |
| FileAdd |
| FileDetail |

### TC-FILE-LIST-DETAIL-DOMAIN-002: 파일 목록에서 이어 진입한 FileAdd와 FileDetail은 목록과 함께 표시한다

- 근거: `domain > 배치에 참여하는 화면`
- Given: 목록과 상세를 함께 사용할 수 있는 환경이다.
- When: 사용자가 파일 목록에서 테스트 데이터의 화면으로 이동한다.
- Then: 파일 목록이 그대로 표시된 채 그 화면이 상세 영역에 함께 표시된다.
- 테스트 데이터:

| 화면 |
| --- |
| FileAdd |
| FileDetail |
| FileDetail을 본 뒤 목록에서 연 FileAdd |

### TC-FILE-LIST-DETAIL-DOMAIN-003: `더보기`로 돌아갔다가 다시 진입하면 상세 선택이 초기화된다

- 근거: `domain > 유지와 초기화 기준`
- Given: 목록과 상세 영역이 함께 표시되고 상세 영역에 선택한 파일의 FileDetail이 놓여 있다.
- When: 사용자가 파일 목록의 뒤로가기 동작으로 `더보기` 화면으로 돌아갔다가 파일 화면에 다시 진입한다.
- Then: 상세 영역에는 FileDetail이 아니라 선택한 파일이 없을 때의 FileAdd가 놓인다.

### TC-FILE-LIST-DETAIL-DOMAIN-004: `더보기`로 돌아갔다가 다시 진입하면 상세 영역의 FileAdd는 빈 상태로 시작한다

- 근거: `domain > 유지와 초기화 기준`
- Given: 목록과 상세 영역이 함께 표시되고 선택한 파일이 없어 상세 영역에 FileAdd가 놓여 있다. 사용자가 FileAdd에 제목과 설명을 적고 파일을 골랐다.
- When: 사용자가 `더보기` 화면으로 돌아갔다가 파일 화면에 다시 진입한다.
- Then: 상세 영역의 FileAdd는 제목과 설명이 비어 있고 고른 파일이 없다.

### TC-FILE-LIST-DETAIL-DOMAIN-005: 배치를 떠나기 전에는 상세 영역에서 FileDetail을 보고 돌아와도 FileAdd의 작성 내용이 남는다

- 근거: `domain > 유지와 초기화 기준`
- Given: 목록과 상세 영역이 함께 표시되고 선택한 파일이 없어 상세 영역에 FileAdd가 놓여 있다. 사용자가 FileAdd에 제목과 설명을 적고 파일을 골랐다.
- When: 사용자가 목록에서 파일을 골라 상세 영역에서 FileDetail을 본 뒤 뒤로가기를 사용해 FileAdd로 돌아온다.
- Then: FileAdd에 적어 두던 제목과 설명, 고른 파일이 그대로 남아 있다.

### TC-FILE-LIST-DETAIL-DOMAIN-006: 게스트가 되면 선택한 파일이 없을 때의 상세 영역을 비우고 작성 내용을 남기지 않는다

- 근거: `domain > 계정 상태에 따른 상세 영역`
- Given: 목록과 상세 영역이 함께 표시되고 선택한 파일이 없어 상세 영역에 FileAdd가 놓여 있다. 사용자가 FileAdd에 제목을 적고 파일을 골랐다.
- When: 계정이 게스트가 되고, 그 뒤 다시 사용자가 된다.
- Then: 게스트인 동안 상세 영역에는 FileAdd가 표시되지 않고, 다시 사용자가 되면 상세 영역의 FileAdd는 제목이 비어 있고 고른 파일이 없다.

### TC-FILE-LIST-DETAIL-DOMAIN-007: 계정 상태를 확정하지 않은 동안에는 상세 영역의 FileAdd와 작성 내용을 그대로 둔다

- 근거: `domain > 계정 상태에 따른 상세 영역`
- Given: 목록과 상세 영역이 함께 표시되고 선택한 파일이 없어 상세 영역에 FileAdd가 놓여 있다. 사용자가 FileAdd에 제목을 적었다.
- When: 계정 상태를 게스트·사용자로 확정하지 않은 상태가 된다.
- Then: 상세 영역에 FileAdd와 적어 둔 제목이 그대로 남는다.

### TC-FILE-LIST-DETAIL-DOMAIN-008: 화면이 재생성되어도 상세 선택을 유지한다

- 근거: `domain > 유지와 초기화 기준`
- Given: 목록과 상세 영역이 함께 표시되고 목록에서 고른 파일의 FileDetail이 상세 영역에 놓여 있다.
- When: 화면이 재생성된다.
- Then: 목록과 함께 상세 영역에 같은 파일의 FileDetail이 놓여 있고, 상세 영역에서 뒤로가면 FileAdd로 되돌아간다.

### TC-FILE-LIST-DETAIL-DOMAIN-009: 시스템이 앱을 정리했다가 다시 만들어도 상세 선택과 뒤로가기 기록을 유지한다

- 근거: `domain > 유지와 초기화 기준`
- Given: 목록과 상세 영역이 함께 표시되고 목록에서 고른 파일의 FileDetail이 상세 영역에 놓여 있다.
- When: 앱이 백그라운드에 있는 동안 시스템이 앱을 정리했다가 다시 만든다.
- Then: 목록과 함께 상세 영역에 같은 파일의 FileDetail이 놓여 있고, 상세 영역에서 뒤로가면 FileAdd로 되돌아간다.

### TC-FILE-LIST-DETAIL-DOMAIN-010: 앱이 백그라운드에 다녀와도 상세 선택을 유지한다

- 근거: `domain > 유지와 초기화 기준`
- Given: 목록과 상세 영역이 함께 표시되고 목록에서 고른 파일의 FileDetail이 상세 영역에 놓여 있다.
- When: 앱이 백그라운드에 갔다가 같은 화면으로 돌아온다.
- Then: 목록과 함께 상세 영역에 같은 파일의 FileDetail이 놓여 있고, 상세 영역에서 뒤로가면 FileAdd로 되돌아간다.

### TC-FILE-LIST-DETAIL-DOMAIN-011: 상세 영역의 화면이 바뀌어도 목록은 보던 자리를 유지하고 다시 불러오지 않는다

- 근거: `domain > 유지와 초기화 기준`
- Given: 목록과 상세 영역이 함께 표시되고, 사용자가 목록을 아래로 이동해 앞부분의 파일이 보이지 않는다.
- When: 사용자가 목록에서 보이는 파일을 골라 상세 영역에 FileDetail을 연 뒤 시스템 뒤로가기로 FileAdd로 돌아온다.
- Then: 목록은 처음부터 다시 불러오지 않고, 이동해 둔 자리가 그대로 보인다.

## 작성하지 않는 이유

`더보기`로 돌아갔다가 다시 진입했을 때 상세 선택이 초기화되는 결과는 TC-FILE-LIST-DETAIL-DOMAIN-003에서 다룬다. 앱을 다시 실행했을 때의 상세 선택 초기화는 앱 프로세스를 끝내고 다시 띄워야 관찰할 수 있어 자동화하지 않으며, 프로세스 재시작을 제어할 수 있는 테스트 환경이 갖춰지면 자동화한다.

두 영역의 표시 비율 조절과 조절 중 즉시 반영, 화면 재생성·백그라운드 복귀 후 비율 유지, `더보기`로 돌아갔다가 다시 진입하거나 앱을 다시 실행한 뒤의 기본 비율 초기화는 실제 화면 크기와 실행 경계를 함께 판정해야 한다. 같은 경계에서 상세 선택이 유지되는지는 TC-FILE-LIST-DETAIL-DOMAIN-008, TC-FILE-LIST-DETAIL-DOMAIN-009, TC-FILE-LIST-DETAIL-DOMAIN-010이 따로 검증한다. 현재 테스트 환경에서는 결정적으로 자동화할 수 없으며, 화면 크기와 실행 경계를 제어·관찰할 수 있는 통합 UI 테스트 환경이 갖춰지면 자동화한다.
