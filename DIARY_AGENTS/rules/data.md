# Data 계층 규칙

## core와 data, work의 책임 경계

`core:*`와 `notification`은 메커니즘을 감싸는 계층이고, `data:*`와 `work:*`는 그 메커니즘으로 무엇을 어떤 순서로 주고받을지 정하는 계층이다.

- `core:*`는 저장소·네트워크·플랫폼 기능을 호출할 수 있는 상태로 만들어 주기까지만 한다. DAO, HttpClient, 위치 제공자가 여기 속한다. `notification`은 알림을 게시하는 수단만 갖는 같은 성격의 모듈이다.
- `data:*`는 그 DataSource들을 조합해 domain이 선언한 Repository·Manager 계약을 구현한다. 어떤 종류를 어떤 순서로 올려보내고 내려받는지, 몇 개씩 나눠 보내는지, 커서를 언제 전진시키는지는 data가 소유한다.
- `work:*`는 백그라운드에서 실행되는 작업 하나를 기능 단위로 소유한다. domain 계약 구현, 작업 내용, 그 작업을 플랫폼이 깨우는 수단을 한 모듈에 둔다. `work:sync`는 동기화, `work:daily-memo`는 일일 메모 알림이다.

`work:*` 안에서는 플랫폼 실행 수단(`WorkManager`, `BGTaskScheduler`, 코루틴 타이머)을 플랫폼 소스셋에, 작업 내용과 domain 계약 구현을 `commonMain`에 둔다. `androidMain`은 `commonMain`을 볼 수 있지만 반대는 컴파일되지 않으므로, 실행 수단이 작업 내용에 의존하고 작업 내용은 실행 수단을 모르는 방향을 소스셋이 강제한다. 예: `work:sync`의 `SyncWorkImpl`은 `commonMain`에 있고, `androidMain`의 `SyncWorker`·`AndroidSyncWorkScheduler`는 그 작업을 실행하고 예약만 한다.

`WorkManager`를 감싸는 범용 실행 모듈은 두지 않는다. 작업마다 예약 정책, 제약, 상태 관찰 요구가 달라 공통 계약이 `WorkManager`의 표면을 다시 쓰는 것이 되고, 실제로 공유되는 코드는 `CoroutineWorker` 상속 정도다. 세 번째 작업이 같은 보일러플레이트를 반복하게 되면 그때 Android 전용 위임 Worker를 분리한다.

iOS는 정해진 시각에 앱 코드를 깨우는 대신 알림 자체를 미리 등록하므로, 전달 시각 예약은 `notification`이 소유하고 `work:*`의 `iosMain`은 그 수단을 쓴다.

`core:*`가 `core:database:api`와 `core:network:api`를 함께 참조하고 있으면 그 모듈은 data 책임을 들고 있는 것이므로 `data:*`나 `work:*`로 옮긴다.

### work의 UseCase 주입

`work:*`는 Repository·DataSource뿐 아니라 `domain:*`의 UseCase도 주입받을 수 있다. 백그라운드 작업이 실행 시점에 판단해야 하는 것이 domain 정책이면, 그 정책을 `work:*`에서 다시 조합하지 않고 UseCase를 그대로 호출한다. 예: `work:sync`는 동기화 대상 계정을 실행 시점에 확인할 때 `GetAccountUseCase`를 쓴다.

어느 쪽을 주입할지는 필요한 것이 정책인지 데이터 조작인지로 가른다.

- 정책 판단이 필요하면 UseCase를 주입한다. 계정 상태 판정, 노출 조건, 기본값 결정이 여기 속한다.
- 저장소나 원격에 값을 읽고 쓰기만 하면 Repository나 DataSource를 주입한다. 업로드 대기 항목 조회, 커서 갱신이 여기 속한다.

정책 판단에 Repository를 직접 주입하면 UseCase가 하던 조합이 `work:*`에 복제되어 같은 정책이 두 곳에 생기고, 정책이 바뀔 때 UseCase만 고쳐서는 백그라운드 동작이 따라오지 않는다.

의존 방향은 아래 `계층 의존 방향`을 그대로 따른다. `work:* → domain:*`이므로 UseCase 주입이 방향을 바꾸지 않는다.

### 매퍼의 소유

엔티티와 모델 사이의 변환은 그 엔티티를 읽고 쓰는 `data:*` 또는 `work:*`가 소유한다. 매퍼를 별도 `core:*` 모듈에 모으면 그 모듈이 모든 저장소·네트워크 API를 참조하는 허브가 되어 위 경계를 어기고, 엔티티 하나를 바꿀 때 무관한 data 모듈까지 다시 컴파일된다.

| 변환 | 소유 모듈 | 예 |
| --- | --- | --- |
| 로컬 엔티티 ↔ 모델 | 그 Repository를 구현하는 `data:*` | `data:tag`의 `TagLocalEntity.toDomain()` |
| 로컬 엔티티 ↔ 원격 엔티티 | 그 교환을 수행하는 `work:sync` | `work:sync`의 `TagLocalEntity.toRemote()` |
| 외부 API 응답 → 모델 | 그 API를 호출하는 `data:*` | `data:place`의 `NaverPlaceRemoteEntity.toDomain()` |
| 여러 `data:*`가 함께 쓰는 변환 | `data:core` | `ListSort.toLocal()` |

다른 `data:*`가 쓰는 매퍼만 `public`으로 두고, 그 모듈 안에서만 쓰는 매퍼는 `internal`로 둔다.

## 계층 의존 방향

Repository·Manager 계약은 `domain:*`이 선언하고 `data:*`와 `work:*`가 구현한다. 의존은 `data:* → domain:*`, `work:* → domain:*` 방향이고, `domain:*`은 어떤 `data:*`나 `work:*`도 참조하지 않는다.

Android 공식 아키텍처 가이드와 Now in Android는 반대 방향(`domain → data`, Repository 인터페이스를 data 계층에 둠)을 권장한다. 이 저장소는 domain을 구현 세부에서 독립시키고 data를 교체 가능한 플러그인으로 두기 위해 Clean Architecture의 의존성 역전을 택했으므로, 이 항목에서는 `참고 우선순위`보다 이 문서를 우선한다.

## data 계층 에러 처리

data 계층은 발생한 에러를 삼키지 않고 호출자에게 그대로 전파한다. 에러를 어떻게 다룰지는 제품 정책이므로 UseCase에서 판단한다.

에러를 대신할 기본값으로 바꿔 정상 결과처럼 내보내면, 조회 실패와 데이터가 없는 상태를 UseCase가 구분할 수 없게 되고 실패가 화면까지 조용히 감춰진다.

data 계층에서도 다음처럼 에러를 다룰 이유가 분명한 경우에는 처리할 수 있다. 이때는 그 이유를 코드에 드러낸다.

- 라이브러리나 플랫폼이 요구하는 계약에 맞춰 에러 타입을 바꿔 다시 던지는 경우
- 여러 작업의 결과를 모으기 위해 실패 원인을 유지한 채 `Result.failure`로 감싸는 경우
- 플랫폼 API가 성공과 실패만 표현하고 원인을 담을 자리가 없어 실패 결과로 바꾸는 경우
- 함께 수행하는 여러 조회 중 부가 정보의 실패를 스펙이 부재 값으로 다루도록 정한 경우

금지하는 것은 이유 없이 조용히 삼키는 경우다. 위 예외에 해당하지 않으면서 실패를 빈 목록, 빈 집합, `null`, 기본값으로 바꿔 성공처럼 내보내는 것을 금지한다.

### 부가 정보의 실패를 부재 값으로 다루는 예외

마지막 예외는 다음을 모두 만족할 때만 쓴다. 하나라도 어긋나면 실패를 그대로 전파하고 판단을 UseCase에 맡긴다.

- 스펙이 그 실패를 전체 실패로 다루지 않는다고 명시했다.
- 부재 값이 스펙에 정의되어 있고, 그 값이 호출자에게 "실패"가 아니라 "없음"으로 읽혀도 되는 의미다.
- 부가 정보가 없어도 같은 연산의 나머지 결과가 그대로 성립한다.
- 여러 조회를 한 스코프에서 함께 수행해, 그 자리에서 잡지 않으면 나머지 조회까지 취소된다.

`CancellationException`은 이 예외에서도 삼키지 않고 그대로 다시 던진다. 코루틴 취소를 부재 값으로 바꾸면 상위 스코프가 취소를 관찰할 수 없다.

✅ 권장 예시:

```kotlin
val locationName =
    async {
        try {
            weatherRemoteDataSource.getLocationName(latitude = ..., longitude = ...).toLocationName()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
            ""
        }
    }
```

⚠️ 비권장 예시:

```kotlin
@Factory
internal class HolidaySettingRepositoryImpl(
    private val holidaySettingLocalDataSource: HolidaySettingLocalDataSource,
) : HolidaySettingRepository {
    override fun getHiddenKeySet(): Flow<Set<String>> =
        holidaySettingLocalDataSource
            .getHiddenKeySet()
            .catch { emit(emptySet()) }
}
```

✅ 권장 예시:

```kotlin
@Factory
internal class HolidaySettingRepositoryImpl(
    private val holidaySettingLocalDataSource: HolidaySettingLocalDataSource,
) : HolidaySettingRepository {
    override fun getHiddenKeySet(): Flow<Set<String>> = holidaySettingLocalDataSource.getHiddenKeySet()
}
```

`FlowUseCase`는 `execute`에서 전파된 예외를 `Result.failure`로 감싸 호출자에게 전달한다. 그래서 data 계층이 에러를 그대로 올려보내면 UseCase가 따로 처리하지 않아도 실패가 실패로 전달된다. UseCase에서 실패를 다른 결과로 바꿀 때는 그 이유를 코드에 남긴다.

✅ 권장 예시:

```kotlin
// 한 년도의 조회 실패는 다른 대상 년도의 공휴일 사용을 막지 않는다.
private fun holidayListFlow(year: Int): Flow<List<Holiday>> =
    combine(
        year.goldenHolidaySourceYearList().map { targetYear ->
            getHolidayUseCase(parameter = targetYear)
                .map { result -> result.getOrDefault(emptyList()) }
        },
    ) { holidayListArray -> holidayListArray.flatMap { holidayList -> holidayList } }
```
