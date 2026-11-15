# Kotlin 규칙

## 주석

주석은 코드만으로 알 수 없는 이유(제약, 우회, 플랫폼·라이브러리의 비직관적 동작, 되돌리면 안 되는 결정)만 남긴다. 선언 이름을 되풀이하는 KDoc은 쓰지 않는다.

다음은 이유를 담고 있어도 주석으로 남기지 않는다.

- 제품 결정. 사용자가 관찰하는 결과는 [docs/spec](../../docs/spec/README.md)이, 화면 표현은 [docs/design](../../docs/design/README.md)이 소유한다. 문서에 없으면 `spec-wave`·`design-wave`로 문서에 먼저 적고 주석은 지운다.
- 문서를 가리키기만 하는 주석(`docs/spec/xxx.md가 정한 값`). 문서의 이름과 코드의 식별자를 맞춰 찾을 수 있게 한다.
- 규칙 문서가 이미 이유를 설명하는 처리. 같은 이유를 여러 파일에 적게 되면 규칙 문서에 한 번 적는다.
- 할 일(`TODO`, `FIXME`)을 제외한, 아직 정하지 않았거나 후속 범위라는 메모. 정하지 않은 범위는 스펙이 소유한다.

✅ 남기는 주석 예시:

```kotlin
// 한 종류가 실패해도 나머지가 끝까지 진행하도록 모두 기다린 뒤 첫 실패를 전달한다.
private suspend fun List<Deferred<Unit>>.awaitAllCatching() { ... }
```

## 숫자 리터럴

**의미 있는 숫자는 이름 있는 상수로 두고, 그 값을 판단하는 책임을 가진 모듈에 둔다.** 쓰는 곳마다 따로 두면 정책이 바뀔 때 일부만 고쳐지고, 책임이 없는 모듈에 두면 그 모듈이 정책을 알게 된다.

| 값의 종류 | 예 | 두는 곳 | 문서 |
| --- | --- | --- | --- |
| 제품 정책 | 동기화 주기, 이미지 최대 변 길이·화질 | 정책을 판단하는 `:domain:*`. `data`·`core`·`work`가 그 값으로 동작해야 하면 UseCase가 Repository·Work 함수의 파라미터로 넘긴다 | spec |
| 화면 입력 규칙 | 입력 자리 수, 확대 한도, 기본 시간 단위 | 그 규칙을 적용하는 `feature`·`compose` 코드 | spec |
| 시각 표현 | 여백, 크기, 투명도, 줄 수, 비율 | 공통 값은 `DiaryTheme.dimens`·`DiaryTheme.styles`, 컴포넌트만의 값은 그 컴포넌트의 `XxxDefaults`. [compose.md](compose.md)의 `시각 속성은 Style, 동작과 배치는 Modifier`, `컴포넌트 디자인 값은 XxxDefaults` 절 | design |
| 데이터 정책 | 페이지 크기, 신선도 기준, 업로드 묶음 크기 | 그 절차를 소유하는 `:data:*`·`:work:*`. 여러 `:data:*`가 함께 쓰면 `:data:core` | 사용자가 관찰하면 spec |
| 데이터 소스 제약 | 외부 API의 최대 건수·반경, 응답 간격, 파일 포맷의 표식 값 | 그 소스를 호출하는 `:core:*`의 구현 | 사용자가 관찰하면 spec |
| 단위와 표준 | 한 주의 일수, 진법, 비트 마스크 | 쓰는 곳의 이름 있는 상수. 여러 모듈이 쓰면 `library:*` | 없음 |

- 데이터 정책을 domain으로 올리지 않는다. [domain.md](domain.md)의 `sync·fetch·refresh 어휘` 절이 막는 방향으로 계약이 뒤집힌다.
- 제품 정책과 데이터 계약의 값은 코드에만 두지 않는다. 문서에 없는 값을 발견하면 `spec-wave`로 먼저 적는다.
- 같은 값을 여러 모듈이 각자 상수로 두지 않는다. 위 표의 한 자리에 두고 가져다 쓴다. 예: 저장소마다 `PAGE_SIZE`를 두지 않고 `data:core`의 `PAGE_SIZE` 하나를 쓴다. 프로필 이미지의 최대 변 길이와 화질은 `core:file:impl`이 아니라 `domain:account`의 `ChangeProfileImageUseCase`가 상수로 두고 Repository 파라미터로 넘긴다.
- Preview와 테스트의 예시 값은 대상이 아니다.

## 상속 대신 합성

**프로젝트 클래스는 상속 계층을 만들지 않는다.** 공통 로직은 일반 클래스로 뽑아 `private` 필드로 합성하고, 플랫폼이나 종류마다 달라지는 부분은 인터페이스로 두어 생성자로 받는다(`PermissionManager`가 `PermissionPlatform`을 받는다). 추상 클래스에 공통 절차를 두고 하위 클래스가 빈 자리를 채우는 템플릿 메서드, 상수 하나를 고정하려는 하위 클래스, 모듈마다 다른 싱글턴 타입을 얻으려는 빈 하위 클래스가 모두 해당한다. ViewModel은 [viewmodel.md](viewmodel.md)의 `ViewModel 상속 대신 합성`이 같은 기준을 둔다.

상속을 그대로 두는 것은 다음뿐이다.

- 라이브러리가 요구하는 부모. androidx `ViewModel`, `RoomDatabase`, `CoroutineWorker`, `PagingSource` 등. 인터페이스가 인터페이스를 확장하는 것(`RoomDao<T>`를 확장하는 DAO 등)은 상속 계층이 아니므로 이 규칙의 대상이 아니다
- `Throwable` 계층. 예외는 상속으로만 분류할 수 있으므로 `sealed class`로 하위 종류를 닫는다. 부모 예외 자체를 범용 실패로 직접 던지면서 하위 종류도 두는 경우(`AppleCredentialsException`과 `AppleCredentialsUserCancelException`)는 `open class`로 둔다
- Swift가 Kotlin 인터페이스를 구현하기 위해 함께 상속해야 하는 빈 `open class`
- `:domain:core`의 `UseCase`·`FlowUseCase`. `Result` 래핑과 `CancellationException` 통과, 실패 로깅을 하위 클래스가 빠뜨릴 수 없게 강제하고, `UseCase<Uuid, *>`처럼 타입으로 묶어 넘길 수 있어 유지한다

Kotest에서 여러 구현이 같은 시나리오를 확인하는 테스트는 추상 스펙을 상속하지 않고 `funSpec { }`으로 만든 `TestFactory`를 각 테스트가 `include`한다. 구현마다 다른 부분은 인터페이스로 두고 테스트 파일이 `object`로 구현한다.

✅ 권장 예시 — 모듈마다 다른 싱글턴 타입이 필요하면 빈 하위 클래스(`class LunarFetchedYearSet : FetchedKeySet<Int>()`) 대신 감싼다:

```kotlin
@Single
internal class LunarFetchedYearSet {
    private val keySet = FetchedKeySet<Int>()

    suspend fun <T> fetchOnce(key: Int, cached: suspend () -> T, fetch: suspend () -> T): T = keySet.fetchOnce(key = key, cached = cached, fetch = fetch)
}

internal fun tagEntityTabLocalDataSourceTests(case: TagTabCase): TestFactory = funSpec { ... }

internal class AccountTagQrLocalDataSourceImplTest :
    FunSpec({
        include(tagEntityTabLocalDataSourceTests(case = QrTagTabCase))
    })
```

## 실험적 API Opt-in

`@OptIn(...)`은 클래스나 함수에 선언하지 않고, 해당 API를 사용하는 파일 상단에 `@file:OptIn(...)`으로 선언한다.

## 접근 지정자

explicit API 모드가 요구하지 않는 접근 지정자는 선언하지 않는다.

- data class의 주 생성자 프로퍼티에는 `public`을 붙이지 않는다. 클래스 선언 자체의 접근 지정자는 유지한다.
- `internal` 선언의 멤버(함수, 프로퍼티, companion object)에 상위 선언과 같은 `internal`을 다시 붙이지 않는다.

## Explicit backing field

내부에서만 변경하고 외부에는 읽기 전용 상위 타입으로 노출하는 프로퍼티는, 비공개 프로퍼티와 공개 프로퍼티를 쌍으로 선언하지 않고 explicit backing field로 선언한다.

```kotlin
val isLoading: StateFlow<Boolean>
    field = MutableStateFlow(false)
```

field의 타입이 프로퍼티 타입의 하위 타입이어야 하므로, `Channel`을 `receiveAsFlow()`로 바꿔 노출하는 경우처럼 변환이 필요한 쌍에는 적용하지 않고 비공개 프로퍼티를 그대로 둔다.

## nullable 문자열·컬렉션 대체

`String?`, `List<T>?`, `Set<T>?`, `Map<K, V>?`처럼 빈 값으로 대신할 수 있는 nullable 타입은 `orEmpty()`로 not-null 타입으로 바꿔 다룬다. 선언하는 프로퍼티와 파라미터의 타입도 not-null로 두고(`StateFlow<List<Tag>>`, `initialValue = emptyList()`), nullable 값을 받는 경계에서 `orEmpty()`로 변환한다.

null과 빈 값이 서로 다른 의미를 가지면 nullable 타입으로 두 의미를 겹쳐 표현하지 않고, 의미를 드러내는 별도 값으로 표현한다. 예: 목록은 not-null로 두고 조회 완료 여부를 `isTagListLoaded: Boolean` 같은 별도 프로퍼티로 둔다.

## 숫자 문자 판정

숫자로 바꿔 읽을 입력의 문자는 `Char.isDigit` 대신 `'0'..'9'` 범위로 거른다. `Char.isDigit`은 전각 숫자 같은 유니코드 숫자도 받지만 `toIntOrNull`·`toDoubleOrNull`은 0~9만 읽는다.

## enum 선언 순서

enum의 선언 순서는 계약이 아니므로 프로덕션 코드가 그 순서에 의존하지 않게 둔다. 순서에 의존하면 상수를 재배치해도 컴파일과 테스트가 통과한 채 화면의 순서만 바뀐다.

**표시 순서나 인덱스가 필요한 곳에서는 `entries` 순회와 `ordinal`을 쓰지 않고, 순서를 소유하는 `listOf`를 사용하는 모듈에 두고 순회와 인덱스를 그 목록에서 구한다.** 순서는 스펙이나 디자인 문서가 정하고 `순서 단정` 테스트가 지키므로, 목록에 순서의 출처를 주석으로 달지 않는다.

목록마다 `순서 단정`과 `entries 전체를 한 번씩만 담는지` 두 케이스를 테스트로 둔다. 뒤 케이스가 없으면 새 상수를 목록에 빠뜨려도 알려주는 것이 없다.

```kotlin
internal val searchHomeTypeList: List<SearchHomeType> =
    listOf(SearchHomeType.MEMO, SearchHomeType.TAG, SearchHomeType.PLACE, SearchHomeType.WEB)

PrimaryTabRow(selectedTabIndex = searchHomeTypeList.indexOf(state.type)) {
    searchHomeTypeList.forEach { type -> ... }
}
```

순서와 무관한 곳에서는 `entries`를 그대로 쓴다. 키로 상수를 찾거나(`entries.firstOrNull { it.key == key }`), 모든 상수를 한 번씩 훑는 테스트, 목록 완전성을 비교하는 테스트가 그렇다. `rememberSaveable`의 `Saver`처럼 저장과 복원이 같은 빌드 안에서만 짝을 이루는 곳도 `ordinal`과 `entries[index]`를 그대로 쓴다.

## enum 상수 이름

**저장하거나 전송하는 값에는 상수 이름을 쓰지 않고, 이름과 계약을 끊는 매핑을 둔다.** 이름 변경은 컴파일이 통과한 채 이미 저장된 값이나 상대가 기대하는 포맷만 어긋나게 한다.

대상은 Room 컬럼과 DataStore 값, 네트워크 요청과 응답, 딥링크처럼 앱 밖이나 다음 버전으로 값이 넘어가는 경계다. 명시 문자열 프로퍼티(`MapProviderLocalEntity.NAVER("naver")`의 `persistentValue`)나 `when` 매핑으로 옮기고, `@Serializable` enum을 그 경계로 내보낼 때는 `@SerialName`으로 직렬화 이름을 고정한다.

경계를 넘지 않는 값에는 `.name`과 `valueOf`를 그대로 쓴다. `rememberSaveable`의 `Saver`가 그렇다.
