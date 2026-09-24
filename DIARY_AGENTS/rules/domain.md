# Domain 계층 규칙

## UseCase·Repository 네이밍

UseCase는 사용자 행위를 드러내는 행위 기반 이름을 사용한다. 예: `SelectMemoFilterTagUseCase`, `FinishMemoUseCase`.

Repository는 행위 해석을 담지 않고 데이터 조작을 그대로 가리키는 이름을 사용한다. 행위를 데이터 조작으로 해석하는 책임은 UseCase가 가지며, Repository에 행위 이름이 내려오면 같은 데이터 조작이 행위마다 중복 선언된다.

이름은 **저장소에 실제로 무슨 일이 일어나는지**로 고른다. `update*`를 기계적으로 쓰지 않는다. 그 저장소가 기기 안인지 서버인지는 기준이 아니다. domain은 Repository 뒤의 위치를 알지 못하므로, 서버에 행을 넣고 지우는 연산도 `upsert`·`delete`처럼 일어나는 일로 이름을 붙인다.

| 이름 | 쓰는 경우 |
| --- | --- |
| `get`, `find`, `page` | 조회. 값이 바뀌는 것을 관찰하는 조회는 `Flow`로, 한 번 읽고 끝나는 단건 조회는 `find`, 페이징 조회는 `page` |
| `create` | 넘긴 값이 그대로 저장되지 않고 다른 자원으로 교환되는 생성. 예: `SessionRepository.create(credential)`은 credential을 저장하지 않고 세션으로 바꾼다 |
| `upsert` | 행이 없으면 INSERT, 있으면 UPDATE. 첫 저장과 갱신을 한 연산이 모두 담당한다 |
| `update*` | 항상 기존 행을 갱신한다. 무엇을 갱신하는지 접미사로 드러낸다(`updateFinished`, `updateDetail`) |
| `delete` | 삭제 |
| `addXxx`, `removeXxx` | 다수 관계인 집합에서 항목 하나를 넣고 뺀다. 예: `addHiddenKey`, `removeHiddenKey` |
| `setXxx`, `unsetXxx` | 최대 하나인 단일 값을 지정하고 지운다. 예: `setDefaultProvider` |
| `submitXxx` | 집합 전체를 한 번에 교체한다. 항목별 조작(`add`/`remove`)으로 표현하면 중간 상태가 관찰되는 일괄 변경에 쓴다. 예: `submitHiddenKeySet` |

집합을 다루는 Repository에 `add`/`remove`와 `submit`을 함께 두는 것은 중복이 아니다. 항목 하나를 바꾸는 행위(공휴일 하나 숨기기)와 집합 전체를 한 번에 정하는 행위(전체 선택, 공휴일만 선택)는 저장소에서 서로 다른 연산이고, 후자를 `add`/`remove` 반복으로 표현하면 중간 집합이 관찰된다.

Boolean 플래그는 Repository 시그니처에서 시작한다. UseCase는 플래그로 분기하지 않고 행위별로 나눈다. 예: `FinishMemoUseCase`/`RestartMemoUseCase`가 `AccountMemoRepository.updateFinished(isFinished)` 하나를 함께 쓴다.

⚠️ 비권장 예시:

```kotlin
public interface AccountMemoFilterRepository {
    public suspend fun selectTag(
        account: Account,
        tagId: Uuid,
    )

    public suspend fun unselectTag(
        account: Account,
        tagId: Uuid,
    )
}
```

✅ 권장 예시:

```kotlin
public interface AccountMemoFilterRepository {
    public suspend fun upsert(
        account: Account,
        tagId: Uuid,
    )

    public suspend fun delete(
        account: Account,
        tagId: Uuid,
    )
}

@Factory
public class SelectMemoFilterTagUseCase internal constructor(
    private val accountMemoFilterRepository: AccountMemoFilterRepository,
) : UseCase<Uuid, Unit>() {
    override suspend fun execute(parameter: Uuid) {
        // 행위(선택)를 데이터 조작(upsert)으로 해석하는 책임은 UseCase가 가진다.
        accountMemoFilterRepository.upsert(...)
    }
}
```

## sync·fetch·refresh 어휘

로컬과 서버의 데이터를 맞추거나 서버의 현재 데이터를 얻는 연산은 CRUD 이름으로 표현되지 않는다. 이 연산에는 `sync`, `fetch`, `refresh` 세 이름만 쓰고, 각 이름을 **성공 후 보장하는 상태**로 정의한다. 연산이 서버를 호출하는지, 몇 번 호출하는지는 이름을 가르는 기준이 아니다. domain은 Repository가 그 상태를 어떻게 만드는지 알지 못하고, 보장하는 상태로만 계약한다.

| 이름 | 성공 후 보장 | 아무 일도 하지 않을 수 있는가 |
| --- | --- | --- |
| `sync` | 로컬의 보류 중 변경이 서버에 반영되고, 그 시점의 서버 변경이 로컬에 반영된다 | 예. 주고받을 변경이 없으면 아무 일도 하지 않는다 |
| `fetch` | 호출 시점의 서버 데이터를 얻는다. 로컬에 보관하는 데이터라면 신선도 기준을 만족하게 반영한다 | 예. 이미 기준을 만족하면 아무 일도 하지 않는다 |
| `refresh` | 로컬 데이터가 호출 시점의 서버 상태를 반영한다 | 아니오. 신선도와 무관하게 항상 다시 반영한다 |

정의에 캐시, 만료 시간, 변경 플래그, 동기화 커서를 쓰지 않는다. 이것들은 data 계층 구현이고 domain은 알 수도 검증할 수도 없다. domain 계약이 data 구현을 규정하면 의존 방향이 뒤집히고, 구현을 바꿀 때마다 domain 계약이 따라 바뀐다. 신선도 기준값은 data 계층이 소유하고 domain은 기준을 만족하는지까지만 계약한다.

- `sync`와 `refresh`는 데이터를 반환하지 않고 로컬에 반영만 한다. `fetch`는 로컬에 보관하지 않는 데이터도 다루므로 얻은 데이터를 반환할 수 있고, 호출자가 값을 쓰지 않으면 반환하지 않아도 된다.
- 조회의 이름과 타입은 관찰이 필요한지로 갈린다. 로컬 저장소나 설정처럼 값이 바뀌는 것을 관찰해야 하는 조회는 `get`, `find`, `page`로 두고 `FlowUseCase`로 노출한다. 호출 시점의 서버 데이터를 한 번 얻고 끝나는 조회는 `fetch`로 두고 `UseCase`로 노출한다. 일회성 조회를 `get`으로 두면 같은 이름이 관찰 가능한 조회와 섞여 호출자가 타입을 보고 무엇을 기대할지 알 수 없다.
- 세 이름을 한 Repository에 모두 선언하지 않는다. 호출자가 있는 연산만 선언한다.
- 이 세 이름은 위 표의 의미에만 쓴다. 다른 의미에 같은 단어를 쓰면 어휘가 무의미해진다. 예를 들어 세션 인증이 유효한지는 `isRefreshed`가 아니라 `isSessionValid`로 표현한다. 외부 라이브러리의 이름을 그대로 옮기는 경우(토큰 재발급을 뜻하는 `refreshToken`, `SessionStatus.RefreshFailure` 등)는 원본 어휘를 유지한다.

UseCase도 Repository 연산 이름을 그대로 쓴다(`FetchHolidayUseCase`). 이 연산의 호출 계기(화면 진입, 당겨서 새로고침, 권한 허용)는 여러 개이고 한 UseCase가 그 계기를 모두 받으므로, 계기를 이름에 넣으면 같은 연산이 계기마다 중복 선언된다. 행위 기반 네이밍이 막으려는 중복과 같은 종류의 중복이다.

작업을 큐에 넣고 즉시 반환하는 UseCase에는 `Request` 접두사를 붙이고(`RequestSyncUseCase`), 접두사가 없으면 연산이 끝날 때까지 suspend한다. 호출자가 진행 상태를 직접 들고 있어야 하는지가 여기서 갈리므로 이름으로 구분한다.

이 어휘는 domain·data 계층 계약에만 적용한다. UI의 당겨서 새로고침은 Material3 어휘(`isRefreshing`, `Refresh` 이벤트)를 그대로 쓴다.

## 정책의 결론을 이름에 넣지 않는다

domain 정책이 계기로부터 결과를 도출한다면, UseCase는 **계기**를 받고 결과는 domain 안에서 정한다. 결과를 이름에 박으면 호출자가 정책의 결론을 미리 알고 있어야 하고, 정책이 바뀔 때 domain 안에서 끝날 변경이 호출부 전체를 고치는 변경이 된다. domain이 소유해야 할 정책이 호출부로 새는 것이다.

⚠️ 비권장 예시:

```kotlin
// 진행 표시 여부는 계기로부터 도출되는 결과인데, UseCase 이름이 그 결과를 먼저 말한다.
// 계정 확인 계기는 표시하지 않기로 정책이 바뀌면 호출부 이름부터 바꿔야 한다.
public class RequestSyncWithProgressUseCase internal constructor(...)
```

✅ 권장 예시:

```kotlin
public enum class SyncTrigger { PULL_TO_REFRESH, ACCOUNT_CONFIRMED, DATA_CHANGED }

public class RequestSyncUseCase internal constructor(
    private val syncManager: SyncManager,
) : UseCase<SyncTrigger, Unit>() {
    override suspend fun execute(parameter: SyncTrigger) {
        syncManager.requestSync(
            accountId = ...,
            reportsProgress = parameter.reportsProgress(),
        )
    }

    // 어떤 계기의 동기화를 사용자에게 알릴지는 이 정책 한곳에서만 정한다.
    private fun SyncTrigger.reportsProgress(): Boolean = ...
}
```

정책의 결론은 Repository나 Manager 같은 port로 넘긴다. port는 정책을 판단하지 않는 메커니즘 경계이므로 `reportsProgress`처럼 결론을 파라미터로 받아도 된다.

## 다른 domain 모듈 의존

UseCase가 다른 domain 모듈에서 무엇을 주입받을지는 [data.md](data.md)의 `work의 UseCase 주입` 절과 같은 기준으로 가른다. 필요한 것이 **정책 판단이면 그 모듈의 UseCase**, **저장소에 값을 읽고 쓰기만 하면 그 모듈의 Repository**를 주입한다. 같은 모듈 안에서도 같은 기준을 쓴다.

- 정책 판단이 필요하면 UseCase를 주입한다. 예: 계정은 세션과 사용자 정보를 합쳐 `Account`로 판정하는 정책이므로 `SessionRepository`·`UserDataRepository`가 아니라 `GetAccountUseCase`를 쓴다. 변경 뒤 동기화는 계기별 진행 보고 여부와 오래된 데이터 초기화를 정하는 정책이므로 `SyncManager`가 아니라 `RequestSyncUseCase`를 쓴다.
- 값을 읽고 쓰기만 하면 Repository를 주입한다. 예: `FetchMemoDraftUseCase`는 Gemini 설정을 그대로 읽기만 하므로 `domain:setting`의 `GeminiSettingRepository`를 직접 쓴다. `GetGeminiSettingUseCase`가 있어도 그 UseCase가 Repository 값을 그대로 돌려줄 뿐이면 감쌀 이유가 없다.

정책이 있는 UseCase를 두고 그 아래 Repository를 직접 주입하면 정책이 호출부마다 복제되고, 정책이 바뀔 때 UseCase만 고쳐서는 다른 모듈의 동작이 따라오지 않는다. 반대로 단순 조회를 UseCase로 감싸면 `Result`와 `Flow` 포장을 벗기는 코드만 늘고 의존이 한 겹 깊어진다.

⚠️ 비권장 예시:

```kotlin
// GetAccountUseCase가 가진 Account 판정 정책을 memo에서 다시 조합한다.
public class GetDailyMemoUseCase internal constructor(
    private val sessionRepository: SessionRepository,
    private val userDataRepository: UserDataRepository,
    private val accountDailyMemoRepository: AccountDailyMemoRepository,
)
```

✅ 권장 예시:

```kotlin
public class GetDailyMemoUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountDailyMemoRepository: AccountDailyMemoRepository,
)
```

Repository를 직접 쓰던 조회에 나중에 정책이 붙어 UseCase가 정책을 갖게 되면, 그 Repository를 주입받던 다른 모듈의 UseCase를 새 UseCase로 바꾼다. 정책이 생긴 시점에 호출부를 옮기지 않으면 같은 데이터가 두 규칙으로 읽힌다.

domain 모듈 사이 의존에는 순환이 없어야 한다. 두 모듈이 서로의 UseCase나 Repository를 주입해야 한다면 한 컨텍스트라는 뜻이므로 모듈을 합치거나, 공유하는 정책을 둘이 함께 의존하는 아래쪽 모듈로 내린다.
