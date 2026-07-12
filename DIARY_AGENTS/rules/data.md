# Data 계층 규칙

## core와 data의 책임 경계

`core:*`는 메커니즘을 감싸는 계층이고, `data:*`는 그 메커니즘으로 무엇을 어떤 순서로 주고받을지 정하는 계층이다.

- `core:*`는 저장소·네트워크·플랫폼 기능을 호출할 수 있는 상태로 만들어 주기까지만 한다. DAO, HttpClient, 위치 제공자, 백그라운드 실행 스케줄러가 여기 속한다.
- `data:*`는 그 DataSource들을 조합해 domain이 선언한 Repository·Manager 계약을 구현한다. 어떤 종류를 어떤 순서로 올려보내고 내려받는지, 몇 개씩 나눠 보내는지, 커서를 언제 전진시키는지는 data가 소유한다.

플랫폼 실행 수단(`WorkManager`, 코루틴 스코프)과 그 수단이 실행할 작업 내용은 같은 모듈에 두지 않는다. `core`는 실행 수단만 갖고, 실행할 작업은 `core:*:api`가 선언한 포트를 `data:*`가 구현해 주입한다. 예: `core:work:api`의 `SyncWork`를 `data:sync`의 `SyncWorkImpl`이 구현하고, `core:work:impl`의 `AndroidSyncWorkManager`·`NonAndroidSyncWorkManager`는 그 포트만 실행한다.

`core:*`가 `core:database:api`와 `core:network:api`를 함께 참조하고 있으면 그 모듈은 data 책임을 들고 있는 것이므로 `data:*`로 옮긴다.

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
