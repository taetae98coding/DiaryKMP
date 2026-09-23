# Kotlin 규칙

## 주석

주석은 코드에서 유추하기 어려운 것만 남긴다. 이름과 흐름으로 알 수 있는 내용을 다시 적으면 코드가 바뀔 때 주석이 먼저 낡고, 읽는 사람이 코드와 주석을 대조하는 비용만 늘어난다.

다음 경우에만 주석을 쓴다.

- 코드가 왜 그렇게 되어 있는지 코드만으로는 알 수 없는 경우(제약, 우회, 트레이드오프, 그 자리에 있어야 하는 이유)
- 플랫폼이나 라이브러리의 비직관적 동작에 맞춘 처리
- 되돌리면 안 되는 결정이라 이력을 남겨야 하는 경우

무엇인지 설명하는 주석이 필요하다고 느껴지면 먼저 이름을 고친다. 이름이 계약을 드러내면 그 주석은 필요 없어진다.

다음은 이유를 담고 있어도 주석으로 남기지 않는다.

- 제품 결정. 무엇을 보여 주는지, 실패를 어떻게 다루는지, 어떤 값을 쓰는지처럼 사용자가 관찰하는 결과는 [docs/spec](../../docs/spec/README.md)이, 화면 표현은 [docs/design](../../docs/design/README.md)이 소유한다. 문서에 없으면 `spec-wave`·`design-wave`로 문서에 먼저 적고 주석은 지운다. 주석에 둔 결정은 테스트 케이스와 디자인의 근거가 되지 못하고, 결정이 바뀔 때 문서와 어긋난다.
- 문서를 가리키기만 하는 주석(`docs/spec/xxx.md가 정한 값`). 문서의 이름과 코드의 식별자를 맞춰 찾을 수 있게 한다.
- 규칙 문서가 이미 이유를 설명하는 처리. 예: enum 선언 순서 대신 목록이 순서를 소유하는 이유는 아래 `enum 선언 순서` 절이 설명한다. 같은 이유를 여러 파일에 적게 되면 규칙 문서에 한 번 적는다.
- 할 일(`TODO`, `FIXME`)을 제외한, 아직 정하지 않았거나 후속 범위라는 메모. 정하지 않은 범위는 스펙이 소유한다.

⚠️ 비권장 예시:

```kotlin
public interface SyncManager {
    /** 진행을 보고하도록 요청된 동기화가 실행 중인지. */
    public val isProgressReported: Flow<Boolean>

    /** 동기화를 요청한다. [reportsProgress]가 true이면 실행되는 동안 [isProgressReported]가 true가 된다. */
    public fun requestSync(
        accountId: Uuid,
        reportsProgress: Boolean,
    )
}
```

✅ 권장 예시:

```kotlin
public interface SyncManager {
    public val isProgressReported: Flow<Boolean>

    public fun requestSync(
        accountId: Uuid,
        reportsProgress: Boolean,
    )
}
```

✅ 남기는 주석 예시:

```kotlin
// 한 종류가 실패해도 나머지가 끝까지 진행하도록 모두 기다린 뒤 첫 실패를 전달한다.
private suspend fun List<Deferred<Unit>>.awaitAllCatching() {
    map { deferred -> deferred.awaitCatching() }
        .forEach { result -> result.getOrThrow() }
}
```

## 숫자 리터럴

**의미 있는 숫자는 이름 있는 상수로 두고, 그 값을 판단하는 책임을 가진 모듈에 둔다.** 값을 쓰는 곳마다 상수를 따로 두면 정책이 바뀔 때 일부만 고쳐지고, 책임이 없는 모듈에 두면 그 모듈이 정책을 알게 된다.

| 값의 종류 | 예 | 두는 곳 | 문서 |
| --- | --- | --- | --- |
| 제품 정책 | 동기화 주기, 알림 시각, 이미지 최대 변 길이·화질 | 정책을 판단하는 `:domain:*`. `data`·`core`·`work`가 그 값으로 동작해야 하면 UseCase가 Repository·Work 함수의 파라미터로 넘긴다 | spec |
| 화면 입력 규칙 | 입력 자리 수, 확대 한도, 기본 시간 단위 | 그 규칙을 적용하는 `feature`·`compose` 코드 | spec |
| 시각 표현 | 여백, 크기, 투명도, 줄 수, 비율 | 공통 값은 `DiaryTheme.dimens`·`DiaryTheme.styles`, 컴포넌트만의 값은 그 컴포넌트의 `XxxDefaults`. [compose.md](compose.md)의 `시각 속성은 Style, 동작과 배치는 Modifier`, `컴포넌트 디자인 값은 XxxDefaults` 절 | design |
| 데이터 정책 | 페이지 크기, 신선도 기준, 업로드 묶음 크기 | 그 절차를 소유하는 `:data:*`·`:work:*`. 여러 `:data:*`가 함께 쓰면 `:data:core` | 사용자가 관찰하면 spec |
| 데이터 소스 제약 | 외부 API의 최대 건수·반경, 응답 간격, 파일 포맷의 표식 값 | 그 소스를 호출하는 `:core:*`의 구현 | 사용자가 관찰하면 spec |
| 단위와 표준 | 한 주의 일수, 진법, 비트 마스크 | 쓰는 곳의 이름 있는 상수. 여러 모듈이 쓰면 `library:*` | 없음 |

- 데이터 정책을 domain으로 올리지 않는다. 신선도 기준과 묶음 크기를 domain이 알면 [domain.md](domain.md)의 `원격 호출 연산 어휘` 절이 막는 방향으로 계약이 뒤집힌다.
- 제품 정책과 데이터 계약의 값은 코드에만 두지 않는다. 문서에 없는 값을 발견하면 `spec-wave`로 먼저 적는다.
- 같은 값을 여러 모듈이 각자 상수로 두지 않는다. 위 표의 한 자리에 두고 가져다 쓴다.
- Preview와 테스트의 예시 값은 대상이 아니다.

⚠️ 비권장 예시:

```kotlin
// data:contact, data:memo, data:tag … 저장소마다
private companion object {
    const val PAGE_SIZE: Int = 20
}
```

✅ 권장 예시:

```kotlin
// data:core
public const val PAGE_SIZE: Int = 20
```

⚠️ 비권장 예시:

```kotlin
// core:image:impl
internal const val JPEG_QUALITY_PERCENT: Int = 90
```

✅ 권장 예시:

```kotlin
// domain:account
private const val JPEG_QUALITY_PERCENT = 90

userDataRepository.updateProfileImage(
    uri = parameter.uri,
    cropRegion = parameter.cropRegion,
    maxSideLength = MAX_SIDE_LENGTH_PX,
    jpegQuality = JPEG_QUALITY_PERCENT,
)
```

## 실험적 API Opt-in

실험적 API 사용을 위해 `@OptIn(...)`을 선언할 때는 클래스나 함수에 선언하지 않고, 해당 API를 사용하는 Kotlin 파일 상단에 파일 어노테이션으로 선언한다.

⚠️ 비권장 예시:

```kotlin
@OptIn(ExperimentalForeignApi::class)
class Example

@OptIn(ExperimentalForeignApi::class)
fun example() = Unit
```

✅ 권장 예시:

```kotlin
@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary
```

## data class 프로퍼티 접근 지정자

explicit API 모드에서도 data class의 주 생성자 프로퍼티에는 접근 지정자가 요구되지 않으므로 선언하지 않는다. 클래스 선언 자체의 접근 지정자는 그대로 유지한다.

⚠️ 비권장 예시:

```kotlin
public data class SupabaseUser(
    public val email: String,
    public val profileImage: String?,
)
```

✅ 권장 예시:

```kotlin
public data class SupabaseUser(
    val email: String,
    val profileImage: String?,
)
```

## internal 선언 멤버 접근 지정자

explicit API 모드에서도 `internal` 선언의 멤버는 외부에 노출되지 않아 명시적 접근 지정자가 요구되지 않으므로, 상위 선언과 동일한 `internal`을 멤버에 중복 선언하지 않는다.

⚠️ 비권장 예시:

```kotlin
internal abstract class DiaryDatabase : RoomDatabase() {
    internal abstract fun memoDao(): MemoDao

    internal companion object {
        const val NAME: String = "diary.db"
    }
}
```

✅ 권장 예시:

```kotlin
internal abstract class DiaryDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao

    companion object {
        const val NAME: String = "diary.db"
    }
}
```

## Explicit backing field

내부에서만 변경하고 외부에는 읽기 전용 상위 타입으로 노출하는 프로퍼티는, 비공개 프로퍼티와 공개 프로퍼티를 쌍으로 선언하지 않고 explicit backing field(`field = ...`)로 선언한다. 이름이 하나로 줄어 `mutableXxx`·`_xxx` 같은 접두사 규칙이 필요 없고, 노출 타입과 실제 타입이 한 선언에 함께 드러난다.

클래스 내부에서 프로퍼티를 참조하면 field의 타입으로 해석되므로 변경 함수를 그대로 호출할 수 있고, 외부에서는 선언한 읽기 전용 타입만 보인다.

⚠️ 비권장 예시:

```kotlin
internal class CalendarHomeWeatherViewModel : ViewModel() {
    private val mutableIsLoading = MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()
}
```

✅ 권장 예시:

```kotlin
internal class CalendarHomeWeatherViewModel : ViewModel() {
    val isLoading: StateFlow<Boolean>
        field = MutableStateFlow(false)
}
```

field의 타입이 프로퍼티 타입의 하위 타입이어야 하므로, `Channel`을 `receiveAsFlow()`로 바꿔 노출하는 경우처럼 변환이 필요한 쌍에는 적용하지 않고 비공개 프로퍼티를 그대로 둔다.

```kotlin
private val _effect = Channel<TagAddEffect>(Channel.BUFFERED)
val effect: Flow<TagAddEffect> = _effect.receiveAsFlow()
```

## nullable 문자열·컬렉션 대체

`String?`, `List<T>?`, `Set<T>?`, `Map<K, V>?`처럼 빈 값으로 대신할 수 있는 nullable 타입은 `orEmpty()`로 not-null 타입으로 바꿔 다룬다. 이렇게 하면 호출자가 null 검사와 빈 값 검사를 두 번 하지 않고, 빈 값 처리 분기가 한곳에 모인다.

선언하는 프로퍼티와 파라미터의 타입도 not-null로 두고, nullable 값을 받는 경계에서 `orEmpty()`로 변환한다.

⚠️ 비권장 예시:

```kotlin
internal data class MemoAddUiState(
    val tagList: List<Tag>? = null,
)

private val tagList: StateFlow<List<Tag>?> =
    getTagUseCase(parameter = Unit)
        .map { result -> result.getOrNull() }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = null)
```

✅ 권장 예시:

```kotlin
internal data class MemoAddUiState(
    val tagList: List<Tag> = emptyList(),
)

private val tagList: StateFlow<List<Tag>> =
    getTagUseCase(parameter = Unit)
        .map { result -> result.getOrNull().orEmpty() }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = emptyList())
```

null과 빈 값이 서로 다른 의미를 가지는 경우에는 nullable 타입으로 두 의미를 겹쳐 표현하지 않고, 의미를 드러내는 별도 값으로 표현한다. 예를 들어 아직 조회되지 않은 상태와 조회 결과가 비어 있는 상태를 구분해야 하면 목록은 not-null로 두고 조회 완료 여부를 별도 프로퍼티로 표현한다.

✅ 권장 예시:

```kotlin
internal data class MemoAddUiState(
    val isTagListLoaded: Boolean = false,
    val tagList: List<Tag> = emptyList(),
)
```

## enum 선언 순서

enum의 선언 순서는 계약이 아니라 우연이므로, 프로덕션 코드가 그 순서에 의존하지 않게 둔다. 순서에 의존하면 상수를 재배치하거나 새 상수를 중간에 끼워 넣을 때 컴파일과 기존 테스트가 모두 통과한 채로 화면에 놓이는 순서만 바뀐다.

**표시 순서나 인덱스가 필요한 곳에서는 `entries` 순회와 `ordinal`을 쓰지 않고, 순서를 소유하는 `listOf`를 사용하는 모듈에 두고 순회와 인덱스를 그 목록에서 구한다.** 순서는 스펙이나 디자인 문서가 정하고 아래 `순서 단정` 테스트가 그 순서를 지키므로, 목록에 순서의 출처를 주석으로 달지 않는다.

목록마다 `순서 단정`과 `entries 전체를 한 번씩만 담는지` 두 케이스를 테스트로 둔다. 뒤 케이스가 없으면 새 상수를 목록에 넣는 것을 빠뜨렸을 때 `indexOf`가 `-1`을 돌려주거나 그 상수가 화면에서 사라지는 것을 아무도 알려주지 않는다.

⚠️ 비권장 예시:

```kotlin
PrimaryTabRow(selectedTabIndex = state.type.ordinal) {
    SearchHomeType.entries.forEach { type ->
        Tab(selected = type == state.type, onClick = { state.select(type) })
    }
}
```

✅ 권장 예시:

```kotlin
internal val searchHomeTypeList: List<SearchHomeType> =
    listOf(
        SearchHomeType.MEMO,
        SearchHomeType.TAG,
        SearchHomeType.PLACE,
        SearchHomeType.WEB,
    )

PrimaryTabRow(selectedTabIndex = searchHomeTypeList.indexOf(state.type)) {
    searchHomeTypeList.forEach { type ->
        Tab(selected = type == state.type, onClick = { state.select(type) })
    }
}
```

순서와 무관한 곳에서는 `entries`를 그대로 쓴다. 키로 상수를 찾거나(`entries.firstOrNull { it.key == key }`), 모든 상수를 한 번씩 훑는 테스트, 목록 완전성을 비교하는 테스트가 그렇다. `rememberSaveable`의 `Saver`처럼 저장과 복원이 같은 빌드 안에서만 짝을 이루는 곳도 `ordinal`과 `entries[index]`를 그대로 쓴다. 순서를 바꾸면 양쪽이 함께 바뀐다.

## enum 상수 이름

**저장하거나 전송하는 값에는 상수 이름을 쓰지 않고, 이름과 계약을 끊는 매핑을 둔다.** 이름 변경은 IDE 리팩터링으로 모든 참조가 함께 바뀌어 컴파일이 통과하므로, 이미 저장된 값이나 상대가 기대하는 포맷만 어긋난다. 컴파일러가 잡아 주지 못하는 종류의 변경이다.

대상은 Room 컬럼과 DataStore 값, 네트워크 요청과 응답, 딥링크처럼 앱 밖이나 다음 버전으로 값이 넘어가는 경계다. 명시 문자열 프로퍼티나 `when` 매핑으로 옮기고, `@Serializable` enum을 그 경계로 내보낼 때는 `@SerialName`으로 직렬화 이름을 고정한다. `when` 매핑은 상수를 추가하면 컴파일 오류가 나므로 빠뜨릴 수 없다.

⚠️ 비권장 예시:

```kotlin
public enum class MapProviderLocalEntity {
    NAVER,
    GOOGLE,
}

dataStore.updateData { setting -> setting.copy(mapDefaultProvider = provider.name) }
```

✅ 권장 예시:

```kotlin
public enum class MapProviderLocalEntity(
    public val persistentValue: String,
) {
    NAVER("naver"),
    GOOGLE("google"),
}

dataStore.updateData { setting -> setting.copy(mapDefaultProvider = provider.persistentValue) }
```

경계를 넘지 않는 값에는 `.name`과 `valueOf`를 그대로 쓴다. `rememberSaveable`의 `Saver`가 그렇다. 저장과 복원이 같은 빌드 안에서만 짝을 이루므로 이름을 바꿔도 양쪽이 함께 바뀐다.
