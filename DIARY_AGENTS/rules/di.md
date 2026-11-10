# DI 규칙

Koin 모듈 구성과, 코드가 컨테이너를 직접 조회해도 되는 자리를 소유한다. dispatcher를 어떤 qualifier로 주입하는지는 [main-safety.md](main-safety.md)가, 화면 entry와 딥링크를 Koin에 등록하는 방법은 [navigation.md](navigation.md)가, 백그라운드 작업의 scope와 iOS 등록 콜백은 [work.md](work.md)가 소유한다.

## 컨테이너 조회는 진입점에서만 한다

**프레임워크나 운영체제가 대신 인스턴스를 만드는 진입점에서만 Koin을 조회하고, 그 아래로는 생성자와 인자로 내려보낸다.** 조회 지점이 흩어지면 의존이 선언에 드러나지 않아 테스트가 Koin 구성을 띄워야 하고, DI 프레임워크를 바꿀 때 조회 지점을 모두 찾아야 한다. Koin 컴파일러 플러그인이 누락은 잡아 주지만 구조는 바꾸지 못한다.

조회가 허용되는 자리는 다음뿐이다.

| 진입점 | 조회 방법 |
| --- | --- |
| Android `Activity`, `AppFunctionService` | `by inject()`, `KoinComponent` |
| JVM·wasm `main`, iOS `DiaryStartupInitializer` | `StartupInitializer.initialize()`가 `startKoin` 직후 `AppDependencies`를 꺼내 돌려준다 |
| Swift가 부르는 최상위 함수·`object` (`DiaryDeepLink`, 앱 동작) | `KoinPlatform.getKoin().get()` |
| 운영체제 콜백(`BGTaskScheduler` 등록 콜백, 전송 세션)과 앱 시작 initializer | `KoinPlatform.getKoin().get()` |

다음 자리에서는 조회하지 않는다.

- Composable. `koinInject`, `getKoin()`, `KoinPlatform`을 부르지 않는다. 예외는 [viewmodel.md](viewmodel.md)가 정하는 `koinViewModel`뿐이다.
- `feature:*`, `compose:*`, `library:*` 모듈의 일반 class와 함수. 필요한 것은 생성자나 인자로 받는다.

## 앱 루트는 `AppDependencies`를 받는다

**`App(dependencies: AppDependencies)`가 화면 트리의 유일한 뿌리다.** `AppDependencies`는 `app:shared`의 `@Single`로, 화면 트리가 쓰는 앱 범위 객체를 생성자로 받는다. `AppDeepLink`, `List<DeepLinkNavigation>`, `List<ScreenEntryInstaller>`, 플랫폼 지도 구현(`DiaryMapContentOverride`)이 여기 속한다.

- `List<T>` 생성자 인자는 Koin이 `getAll<T>()`로 채운다. 여러 모듈이 `@Single(binds = [T::class])`로 등록한 구현을 모을 때 쓴다.
- 플랫폼마다 다른 구현은 `app:shared`의 플랫폼 소스셋 Koin 모듈(`AndroidAppModule`, `IosAppModule`, `JvmAppModule`, `WasmAppModule`)이 제공한다. `compose:*` 모듈은 Koin을 모르는 구현 class만 두고, 설정값(`BuildKonfig`)과 `Dispatchers.*`는 제공 함수에서만 참조한다.
- 플랫폼 진입점은 `AppDependencies`를 `App`에 넘기기만 하고 그 안을 열어 쓰지 않는다. 진입점이 따로 필요한 객체(`AppDeepLink`)는 자기 조회로 얻는다.

✅ 권장 예시:

```kotlin
// app:shared commonMain
@Single
public class AppDependencies(
    internal val appDeepLink: AppDeepLink,
    internal val deepLinkNavigationList: List<DeepLinkNavigation>,
    internal val entryInstallerList: List<ScreenEntryInstaller>,
    internal val mapContentOverride: DiaryMapContentOverride,
)

// app:jvm
val dependencies = StartupInitializer.initialize(isDebug = true)
singleWindowApplication { App(dependencies = dependencies) }
```

## Composable이 만드는 플랫폼 보조 class는 팩토리를 주입받는다

**플랫폼 API를 부르는 보조 class(파일·사진·폴더 선택기, 자격 증명 요청, QR 동작 실행기)는 `XxxFactory` 인터페이스의 `@Composable fun rememberXxx()`로 만든다. `feature:*:ui`의 것은 플랫폼 소스셋이 `@Single(binds = [XxxFactory::class])` 구현을 등록하고, `compose:*`의 것은 아래처럼 `app:shared`가 제공한다.** 구현이 필요로 하는 dispatcher와 설정은 구현 class가 생성자로 받는다. `expect`/`actual` 함수로 가르면 플랫폼 구현이 Koin 의존을 `koinInject`로 찾게 되므로 쓰지 않는다.

- 인터페이스와 구현은 보조 class 곁에 둔다. `feature:*:ui`의 것은 `internal`, 여러 feature가 쓰는 `compose:*`의 것은 `public`으로 두고 `app:shared`의 플랫폼 모듈이 제공한다.
- 팩토리는 그 화면의 `ScreenEntryInstaller`가 생성자로 받아 entry 함수와 Screen의 인자로 내려보낸다. `remember`는 화면이 그려지는 동안 `LocalContext`, `LocalUIViewController`, `rememberLauncherForActivityResult`를 읽어야 하므로 팩토리 안에 남긴다.
- 의존이 없는 플랫폼도 빈 구현을 등록한다. Koin 컴파일러 플러그인이 플랫폼마다 바인딩이 있는지 확인한다.
- Compose UI 테스트는 Koin 없이 플랫폼 구현을 직접 만들어 넘긴다(`remember { AndroidFilePickerFactory() }.rememberFilePicker(...)`).
- feature 안에서만 쓰는 플랫폼 표시 요소(QR 카메라 미리보기)는 보조 class의 타입이 플랫폼마다 달라 `rememberXxx()`로 돌려줄 공통 타입이 없으므로, 그리는 일 자체를 `XxxContent` 인터페이스의 `@Composable fun Content(...)`로 두고 같은 방식으로 등록·주입한다. 플랫폼 Composable은 그 파일의 `private` 함수로 두고 구현 class가 부른다. 여러 feature가 쓰는 표시 요소(지도)는 아래 `플랫폼 표시 요소의 Koin 의존은 Override 구현이 받는다`를 따른다.

⚠️ 비권장 예시:

```kotlin
@Composable
internal actual fun rememberFilePicker(onPick: (FileUri) -> Unit): FilePicker {
    val dispatcher = koinInject<CoroutineDispatcher>(qualifier = named<FilePickerDispatcher>())
    ...
}
```

✅ 권장 예시:

```kotlin
// feature:file:ui commonMain
internal interface FilePickerFactory {
    @Composable
    fun rememberFilePicker(onPick: (FileUri) -> Unit): FilePicker
}

@Single(binds = [ScreenEntryInstaller::class])
internal class FileEntryInstaller(
    private val filePickerFactory: FilePickerFactory,
) : ScreenEntryInstaller { ... }

// feature:file:ui jvmMain
@Single(binds = [FilePickerFactory::class])
internal class JvmFilePickerFactory(
    @FilePickerDispatcher private val dispatcher: CoroutineDispatcher,
) : FilePickerFactory {
    @Composable
    override fun rememberFilePicker(onPick: (FileUri) -> Unit): FilePicker { ... }
}
```

## 플랫폼 표시 요소의 Koin 의존은 Override 구현이 받는다

지도처럼 [compose.md](compose.md)의 `플랫폼별 표현은 Override로 바꿔 끼운다`로 그리는 컴포넌트는 feature가 아니라 `App` 루트가 `AppDependencies`로 받은 Override 구현을 `CompositionLocalProvider`로 제공한다. 플랫폼 구현(`UiKitDiaryMapContentOverride`, `JvmDiaryMapContentOverride`)이 dispatcher와 서버 같은 의존을 생성자로 받고, 의존이 필요한 플랫폼의 기본값은 제공이 빠졌음을 알리는 구현으로 둔다. 화면마다 지도 인자를 내려보내면 지도를 쓰는 모든 feature 화면의 시그니처가 지도 내부 사정을 알게 되므로 그렇게 하지 않는다.

## 모듈 구성

- 모듈마다 `commonMain`에 `@Module @ComponentScan @Configuration` class를 하나 둔다. 같은 모듈의 플랫폼 소스셋에 둔 `@Single`·`@Factory` class도 이 scan이 모은다. 플랫폼 소스셋에 제공 함수가 필요할 때만 그 소스셋에 `@Module @Configuration` class를 더 둔다.
- 앱은 `@KoinApplication`인 `DiaryKoinApplication` 하나로 모든 `@Configuration` 모듈을 모은다. 테스트도 `koinApplication<DiaryKoinApplication>()`으로 앱과 같은 구성을 띄우고, 가짜가 필요하면 `allowOverride(true)`로 그 정의만 바꾼다.
