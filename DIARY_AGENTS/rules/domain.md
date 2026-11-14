# Domain 계층 규칙

## UseCase·Repository 네이밍

UseCase는 사용자 행위를 드러내는 행위 기반 이름을 사용한다. 예: `SelectMemoFilterTagUseCase`, `FinishMemoUseCase`.

Repository는 행위 해석을 담지 않고 데이터 조작을 그대로 가리키는 이름을 사용한다. 행위를 데이터 조작으로 해석하는 책임은 UseCase가 가진다. 예: `AccountMemoFilterRepository`는 `selectTag`/`unselectTag`가 아니라 `upsert`/`delete`를 선언하고, `SelectMemoFilterTagUseCase`가 선택을 `upsert`로 해석한다.

이름은 **저장소에 실제로 무슨 일이 일어나는지**로 고른다. `update*`를 기계적으로 쓰지 않는다. 저장소가 기기 안인지 서버인지는 기준이 아니다.

| 이름 | 쓰는 경우 |
| --- | --- |
| `get` | 목록·집합·설정 값처럼 값이 바뀌는 것을 관찰하는 조회. `Flow`를 반환한다 |
| `find` | 식별자나 조건으로 하나를 찾아 관찰하는 조회. `Flow`를 반환하고, 없을 수 있으면 `Flow<T?>`로 둔다 |
| `page` | 페이징 조회. `Flow<PagingData<T>>`를 반환한다 |
| `read` | 관찰하지 않고 한 번 읽고 끝나는 조회. `suspend`로 값을 반환한다. 무엇을 읽는지 접미사로 드러낸다(`readTagIdSet`, `readPendingList`) |
| `stream` | 한 번 실행하고 끝나는 원격 연산이 결과를 만들어지는 대로 나누어 받는다. `Flow`를 반환하며, 중간 진행을 내보내다가 마지막 결과를 내보낸 뒤 끝난다. 값이 바뀌는 것을 계속 지켜보는 `get`·`find`와 달리 끝이 있다. 예: `MemoDraftRepository.stream` |
| `create` | 넘긴 값이 그대로 저장되지 않고 다른 자원으로 교환되는 생성. 예: `SessionRepository.create(credential)`은 credential을 세션으로 바꾼다 |
| `upsert` | 행이 없으면 INSERT, 있으면 UPDATE |
| `update*` | 항상 기존 행을 갱신한다. 무엇을 갱신하는지 접미사로 드러낸다(`updateFinished`, `updateDetail`) |
| `delete` | 삭제 |
| `addXxx`, `removeXxx` | 다수 관계인 집합에서 항목 하나를 넣고 뺀다. 예: `addHiddenKey`, `removeHiddenKey` |
| `setXxx`, `unsetXxx` | 최대 하나인 단일 값을 지정하고 지운다. 예: `setDefaultProvider` |
| `submitXxx` | 집합 전체를 한 번에 교체한다. 항목별 조작으로 표현하면 중간 상태가 관찰되는 일괄 변경에 쓴다. 예: `submitHiddenKeySet` |
| `requestXxx`, `scheduleXxx`, `cancelXxx`, `startXxx`/`stopXxx` | Manager가 작업을 실행 수단에 넣거나 예약·취소하고, 관찰 구간을 열고 닫는다. 예: `SyncManager.requestSync`, `schedulePeriodicSync`, `cancelPeriodicSync` |

이 표는 `domain:*`의 Repository·Manager 이름을 정한다. `core:*` DataSource·Transaction의 이름은 [data.md](data.md)의 `DataSource 연산 이름`을 따른다.

집합을 다루는 Repository에 `add`/`remove`와 `submit`을 함께 두는 것은 중복이 아니다.

Boolean 플래그는 Repository 시그니처에서 시작한다. UseCase는 플래그로 분기하지 않고 행위별로 나눈다. 예: `FinishMemoUseCase`/`RestartMemoUseCase`가 `AccountMemoRepository.updateFinished(isFinished)` 하나를 함께 쓴다.

## 입력 없는 UseCase 호출

입력이 필요 없는 UseCase는 `UseCase<Unit, *>`·`FlowUseCase<Unit, *>`로 두고, 호출부는 `Unit`을 넘기지 않고 `:domain:core`의 `invoke()` 확장으로 부른다(`getAccountUseCase()`). MockK의 `every`·`coEvery`·`verify`도 같은 형태로 쓴다. 테스트에서 인자 없이 부를 수 있는 mock 헬퍼 함수가 `Unit` 입력 UseCase의 mock 변수·파라미터와 한 범위에 있으면 헬퍼를 다른 이름(`stubSignOutUseCase()`)으로 둔다. 같은 이름이면 `signOutUseCase()`가 변수의 `invoke()`가 아니라 헬퍼 호출로 해석되거나, 반대로 `x: X = x()` 기본값이 헬퍼가 아니라 파라미터 자신의 `invoke()`로 해석된다.

## sync·fetch·refresh 어휘

로컬과 서버의 데이터를 맞추거나 서버의 현재 데이터를 얻는 연산에는 `sync`, `fetch`, `refresh` 세 이름만 쓰고, 각 이름을 **성공 후 보장하는 상태**로 정의한다. 서버를 호출하는지, 몇 번 호출하는지는 기준이 아니다.

| 이름 | 성공 후 보장 | 아무 일도 하지 않을 수 있는가 |
| --- | --- | --- |
| `sync` | 로컬의 보류 중 변경이 서버에 반영되고, 그 시점의 서버 변경이 로컬에 반영된다 | 예. 주고받을 변경이 없으면 아무 일도 하지 않는다 |
| `fetch` | 호출 시점의 서버 데이터를 얻는다. 로컬에 보관하는 데이터라면 신선도 기준을 만족하게 반영한다 | 예. 이미 기준을 만족하면 아무 일도 하지 않는다 |
| `refresh` | 로컬 데이터가 호출 시점의 서버 상태를 반영한다 | 아니오. 신선도와 무관하게 항상 다시 반영한다 |

정의에 캐시, 만료 시간, 변경 플래그, 동기화 커서를 쓰지 않는다. 이것들은 data 계층 구현이다. 신선도 기준값은 data 계층이 소유하고 domain은 기준을 만족하는지까지만 계약한다.

- `sync`와 `refresh`는 데이터를 반환하지 않고 로컬에 반영만 한다. `fetch`는 얻은 데이터를 반환할 수 있고, 호출자가 값을 쓰지 않으면 반환하지 않아도 된다.
- 값이 바뀌는 것을 관찰해야 하는 조회는 `get`, `find`, `page`로 두고 `FlowUseCase`로 노출한다. 저장소의 값을 한 번 읽고 끝나는 조회는 `read`로, 호출 시점의 서버 데이터를 한 번 얻고 끝나는 조회는 `fetch`로 두고 `UseCase`로 노출한다.
- 서버 데이터를 만들어지는 대로 나누어 받는 Repository 연산은 `fetch`가 아니라 `stream`으로 둔다. 이를 감싼 UseCase가 중간 진행을 파라미터로 받은 콜백에 넘기고 마지막 결과만 반환하며 끝나면, 한 번 실행하고 끝나는 기능이므로 `UseCase`로 노출하고 `Fetch` 이름을 쓴다. 예: `FetchMemoDraftUseCase`가 `MemoDraftRepository.stream`을 감싼다.
- 세 이름을 한 Repository에 모두 선언하지 않는다. 호출자가 있는 연산만 선언한다.
- 이 세 이름은 위 표의 의미에만 쓴다. 예: 세션 인증 유효 여부는 `isRefreshed`가 아니라 `isSessionValid`. 외부 라이브러리의 이름을 옮기는 경우(`refreshToken`, `SessionStatus.RefreshFailure`)는 원본 어휘를 유지한다.

UseCase도 Repository 연산 이름을 그대로 쓴다(`FetchHolidayUseCase`). 호출 계기(화면 진입, 당겨서 새로고침, 권한 허용)를 이름에 넣지 않는다.

작업을 큐에 넣고 즉시 반환하는 UseCase에는 `Request` 접두사를 붙이고(`RequestSyncUseCase`), 접두사가 없으면 연산이 끝날 때까지 suspend한다.

이 어휘는 domain·data 계층 계약에만 적용한다. UI의 당겨서 새로고침은 Material3 어휘(`isRefreshing`, `Refresh` 이벤트)를 그대로 쓴다.

## 정책의 결론을 이름에 넣지 않는다

domain 정책이 계기로부터 결과를 도출한다면, UseCase는 **계기**를 받고 결과는 domain 안에서 정한다. 결과를 이름에 박으면 정책이 호출부로 샌다. 예: `RequestSyncWithProgressUseCase`가 아니라 `RequestSyncUseCase(SyncTrigger)`.

`RequestSyncWithProgressUseCase`처럼 진행 표시 여부를 이름에 넣으면, 계정 확인 계기는 표시하지 않기로 정책이 바뀔 때 호출부 이름부터 바꿔야 한다. `RequestSyncUseCase`는 `SyncTrigger`를 받아 `reportsProgress`를 안에서 도출하고 `SyncManager.requestSync(reportsProgress = ...)`로 넘긴다.

정책의 결론은 Repository나 Manager 같은 port로 넘긴다. port는 정책을 판단하지 않는 메커니즘 경계이므로 `reportsProgress`처럼 결론을 파라미터로 받아도 된다.

## 다른 domain 모듈 의존

UseCase가 다른 domain 모듈에서 무엇을 주입받을지는 [data.md](data.md)의 `work의 UseCase 주입` 절과 같은 기준으로 가른다. 같은 모듈 안에서도 같은 기준을 쓴다.

- 정책 판단이 필요하면 UseCase를 주입한다. 예: 계정 판정은 `SessionRepository`·`UserDataRepository`가 아니라 `GetAccountUseCase`를, 변경 뒤 동기화는 `SyncManager`가 아니라 `RequestSyncUseCase`를 쓴다.
- 값을 읽고 쓰기만 하면 Repository를 주입한다. 예: `FetchMemoDraftUseCase`는 `GeminiSettingRepository`를 직접 쓴다. `GetGeminiSettingUseCase`가 Repository 값을 그대로 돌려줄 뿐이면 감쌀 이유가 없다.

Repository를 직접 쓰던 조회에 나중에 정책이 붙어 UseCase가 정책을 갖게 되면, 그 Repository를 주입받던 다른 모듈의 UseCase를 새 UseCase로 바꾼다.

domain 모듈 사이 의존에는 순환이 없어야 한다. 두 모듈이 서로의 UseCase나 Repository를 주입해야 한다면 모듈을 합치거나, 공유하는 정책을 둘이 함께 의존하는 아래쪽 모듈로 내린다.
