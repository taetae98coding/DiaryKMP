# DI 규칙

Metro 그래프 구성과, 코드가 그래프를 직접 꺼내 써도 되는 자리를 소유한다. dispatcher를 어떤 qualifier로 주입하는지는 [main-safety.md](main-safety.md)가, 화면 entry와 딥링크를 그래프에 등록하는 방법은 [navigation.md](navigation.md)가, 백그라운드 작업의 scope와 iOS 등록 콜백은 [work.md](work.md)가 소유한다.

## 그래프 조회는 진입점에서만 한다

**프레임워크나 운영체제가 대신 인스턴스를 만드는 진입점에서만 앱 그래프를 꺼내 쓰고, 그 아래로는 생성자와 인자로 내려보낸다.** 조회 지점이 흩어지면 의존이 선언에 드러나지 않아 테스트가 그래프를 띄워야 하고, DI 프레임워크를 바꿀 때 조회 지점을 모두 찾아야 한다. Metro는 그래프에 닿는 바인딩의 누락을 컴파일 때 잡지만 구조는 바꾸지 못한다.

조회가 허용되는 자리는 다음뿐이다.

| 진입점 | 조회 방법 |
| --- | --- |
| Android `Activity` | `app:android`의 `Context.appGraph`. `DiaryApplication`이 들고 있는 `AndroidAppGraph`를 돌려준다 |
| Android `AppFunctionService` | `applicationContext`를 `work:appfunction`의 `AppActionGraphOwner`로 받아 `appActionGraph`를 꺼낸다. `DiaryApplication`이 구현한다 |
| JVM·wasm `main`, iOS `DiaryStartupInitializer` | 플랫폼 소스셋의 `StartupInitializer.initialize()`가 그래프를 만들어 돌려준다 |
| Swift가 부르는 최상위 함수·`object` (`DiaryDeepLink`, 앱 동작) | `DiaryStartupInitializer.graph` (`diaryAppActionGraph()`) |
| 앱 시작 initializer, 창이 닫힌 뒤 프로세스 종료 전 처리(`saveWebLoginOnExit`) | `StartupInitializer`가 만든 그래프의 accessor |

- 운영체제 콜백(`BGTaskScheduler` 등록 콜백, 전송 세션)이 필요한 `work:*`는 그래프를 직접 꺼내지 않는다. 진입 class(`PeriodicSyncBackgroundTaskRegistrar`, `SpotlightWorkLauncher`)를 public으로 두고 `@Inject internal constructor`로 의존을 받으며, 앱 그래프가 accessor로 내주어 `StartupInitializer`가 부른다. 콜백 시점에야 필요한 의존은 `() -> T`로 받아 지연 생성한다.
- 다음 자리에서는 조회하지 않는다.
  - Composable. 그래프나 `LocalMetroViewModelFactory`의 팩토리를 직접 쓰지 않는다. 예외는 `metroViewModel`·`assistedMetroViewModel`뿐이고, 부르는 자리는 [compose.md](compose.md)의 `ViewModel을 꺼내는 자리`가 정한다.
  - `feature:*`, `compose:*`, `library:*` 모듈의 일반 class와 함수. 필요한 것은 생성자나 인자로 받는다.

## 그래프는 플랫폼 소스셋에 둔다

**`@DependencyGraph(AppScope::class)`는 `app:shared`의 플랫폼 소스셋(`AndroidAppGraph`, `IosAppGraph`, `JvmAppGraph`, `WasmAppGraph`)에 하나씩 두고, 모든 플랫폼이 내주는 accessor만 `commonMain`의 `AppGraph`에 둔다.** 그래프가 `commonMain`에 있으면 플랫폼 소스셋의 기여를 보지 못한다([Metro 멀티플랫폼](https://zacsweers.github.io/metro/latest/multiplatform/)).

- Android 그래프만 `@DependencyGraph.Factory`로 `Context`를 받는다. 그래프는 [수동 DI 가이드](https://developer.android.com/training/dependency-injection/manual)의 Application 컨테이너처럼 `app:android`의 `DiaryApplication`이 처음 쓰일 때 만들어 들고 있다. `app:android`는 Metro 플러그인을 쓰지 않으므로 `app:shared`의 `createAndroidAppGraph(context)`로 만든다. 하위 모듈의 진입점이 그래프를 받아야 하면 그 모듈이 `XxxOwner` 인터페이스를 두고 `DiaryApplication`이 구현한다. 전역 holder에 그래프를 넣지 않는다.
- 진입점이 따로 필요한 객체(`AppDeepLink`, 플랫폼 시작 작업)는 그 플랫폼 그래프의 accessor로 둔다.
- 여러 플랫폼 진입점이 같은 묶음을 쓰면 그 묶음을 소유한 모듈이 `@ContributesTo(AppScope::class)` accessor 인터페이스를 둔다(`work:appaction`의 `AppActionGraph`). 기여된 인터페이스는 플랫폼 그래프의 상위 타입이 되므로, 그 모듈은 `app:shared`가 `api`로 노출한다.

## 앱 루트는 `AppDependencies`를 받는다

**`App(dependencies: AppDependencies)`가 화면 트리의 유일한 뿌리다.** `AppDependencies`는 `app:shared`의 `@SingleIn(AppScope::class)` class로, 화면 트리가 쓰는 앱 범위 객체를 생성자로 받는다. `AppDeepLink`, `Set<DeepLinkNavigation>`, `Set<ScreenEntryInstaller>`, 플랫폼 지도 구현(`DiaryMapContentOverride`), ViewModel 팩토리(`MetroViewModelFactory`)가 여기 속한다. `App`은 루트에서 `LocalMetroViewModelFactory`를 한 번 제공한다.

- 여러 모듈이 등록하는 구현은 `@ContributesIntoSet(AppScope::class, binding = binding<T>())`로 기여하고 `Set<T>`로 받는다. 순서가 밖으로 드러나는 목록은 받는 쪽이 순서 목록을 소유해 정렬하고 목록에 없는 구현은 실패시킨다(`McpToolRegistry`의 `mcpToolSetOrderList`).
- 플랫폼마다 다른 구현은 `app:shared`의 플랫폼 소스셋 binding container(`AndroidAppModule`, `IosAppModule`, `JvmAppModule`, `WasmAppModule`)가 제공한다. `compose:*` 모듈은 Metro를 모르는 구현 class만 두고, 설정값(`BuildKonfig`)과 `Dispatchers.*`는 제공 함수에서만 참조한다.
- 플랫폼 진입점은 `AppDependencies`를 `App`에 넘기기만 하고 그 안을 열어 쓰지 않는다.

## Composable이 만드는 플랫폼 보조 class는 팩토리를 주입받는다

**플랫폼 API를 부르는 보조 class(파일·사진·폴더 선택기, 자격 증명 요청, QR 동작 실행기)는 `XxxFactory` 인터페이스의 `@Composable fun rememberXxx()`로 만든다. `feature:*:ui`의 것은 플랫폼 소스셋이 `@ContributesBinding(AppScope::class, binding = binding<XxxFactory>())` 구현을 기여하고, `compose:*`의 것은 아래처럼 `app:shared`가 제공한다.** 구현이 필요로 하는 dispatcher와 설정은 구현 class가 생성자로 받는다. `expect`/`actual` 함수로 가르면 플랫폼 구현이 그래프 의존을 받을 길이 없으므로 쓰지 않는다. 어느 플랫폼 구현도 그래프에서 받을 의존이 없는 보조 class는 대상이 아니며 `expect`/`actual` `rememberXxx()`로 둔다(`compose:permission`의 `rememberPermissionManager()`, `feature:place:ui`의 `rememberExternalMapOpener()`).

- 인터페이스와 구현은 보조 class 곁에 둔다. `feature:*:ui`의 것은 `internal`, 여러 feature가 쓰는 `compose:*`의 것은 `public`으로 두고 `app:shared`의 플랫폼 binding container가 제공한다.
- 팩토리는 그 화면의 `ScreenEntryInstaller`가 생성자로 받아 entry 함수와 Screen의 인자로 내려보낸다. `remember`는 화면이 그려지는 동안 `LocalContext`, `LocalUIViewController`, `rememberLauncherForActivityResult`를 읽어야 하므로 팩토리 안에 남긴다.
- 의존이 없는 플랫폼도 빈 구현을 기여한다. 플랫폼 그래프가 그 바인딩을 컴파일 때 확인한다.
- Compose UI 테스트는 그래프 없이 플랫폼 구현을 직접 만들어 넘긴다(`remember { AndroidFilePickerFactory() }.rememberFilePicker(...)`).
- feature 안에서만 쓰는 플랫폼 표시 요소(QR 카메라 미리보기 `QrScanCameraPreviewContent`, 웹 상세의 웹 표시 수단 `WebViewContent`)는 보조 class의 타입이 플랫폼마다 달라 `rememberXxx()`로 돌려줄 공통 타입이 없으므로, 그리는 일 자체를 `XxxContent` 인터페이스의 `@Composable fun Content(...)`로 두고 같은 방식으로 기여·주입한다. 그리는 대상이 둘이면(주소를 여는 것과 받은 본문을 그리는 것) 한 인터페이스에 `UrlContent(...)`, `PageContent(...)`처럼 대상 이름을 붙인 함수를 나란히 둔다. 플랫폼 Composable은 그 파일의 `private` 함수로 두고 구현 class가 부른다. 여러 feature가 쓰는 표시 요소(지도)는 아래 `플랫폼 표시 요소의 그래프 의존은 Override 구현이 받는다`를 따른다.

예: `feature:file:ui`의 `FilePickerFactory`(`commonMain`), `JvmFilePickerFactory`(`jvmMain`, `@FilePickerDispatcher` 주입), 팩토리를 생성자로 받는 `FileEntryInstaller`.

## 플랫폼 표시 요소의 그래프 의존은 Override 구현이 받는다

지도처럼 [compose.md](compose.md)의 `플랫폼별 표현은 Override로 바꿔 끼운다`로 그리는 컴포넌트는 feature가 아니라 `App` 루트가 `AppDependencies`로 받은 Override 구현을 `CompositionLocalProvider`로 제공한다. 플랫폼 구현(`UiKitDiaryMapContentOverride`, `JvmDiaryMapContentOverride`)이 dispatcher와 서버 같은 의존을 생성자로 받고, 의존이 필요한 플랫폼의 기본값은 제공이 빠졌음을 알리는 구현으로 둔다. 화면마다 지도 인자를 내려보내면 지도를 쓰는 모든 feature 화면의 시그니처가 지도 내부 사정을 알게 되므로 그렇게 하지 않는다.

## 바인딩 선언

- 구현 class에 `@Inject`를 붙인다. 앱 범위 단일 인스턴스는 `@SingleIn(AppScope::class)`를 더한다. 스코프가 없으면 주입할 때마다 새로 만든다.
- 계약(인터페이스)으로 받는 구현은 `@ContributesBinding(AppScope::class)`로 기여한다. 상위 타입이 둘 이상이면 `binding = binding<T>()`로 받는 계약만 고른다. `UseCase`·`FlowUseCase`처럼 아무도 그 타입으로 받지 않는 추상 상위 타입에는 바인딩하지 않는다.
- 구현은 모듈 밖에 드러내지 않으려고 `internal`로 두고, 빌드 설정의 `generateContributionProviders`로 기여한 모듈이 만든 제공 함수를 통해 그래프에 들어간다. 그래서 **`@Contributes*`를 붙인 구현은 구현 타입으로는 그래프에 없다.** 진입점이나 다른 class가 구현 타입으로도 받아야 하면(`BackgroundTaskPeriodicSyncWorkScheduler`, `AndroidFileUploadNotifier`) `@Contributes*`를 붙이지 않고, 그 모듈의 binding container에 구현을 받아 계약으로 돌려주는 `@Provides` 함수를 둔다. 스코프가 있는 구현을 기여와 구현 타입 주입으로 함께 쓰면 인스턴스가 둘로 갈린다.
- 제공 함수는 모듈마다 `@BindingContainer @ContributesTo(AppScope::class) object`에 `@Provides`로 둔다. 플랫폼 객체나 `Dispatchers.*`를 넘겨야 하는 구현(`CoreSpotlightIndex`), 외부 라이브러리 객체(Room, DataStore, `HttpClient`)를 만드는 경우에만 쓴다. 같은 모듈의 플랫폼 소스셋에 제공 함수가 필요하면 그 소스셋에 binding container를 더 둔다. 테스트 그래프가 일부 제공 함수만 바꿔 끼워야 하면(`SupabaseHttpClientEngineModule`) 그 제공 함수만 별도 binding container로 나눈다. Metro는 같은 바인딩을 덮어쓰지 못하고 `excludes`는 container 단위로만 뺀다.
- 앱 그래프가 다른 모듈의 제공 함수를 만들려면 그 시그니처의 타입이 `app:shared`의 compile classpath에 있어야 한다. 제공 함수가 받거나 돌려주는 외부 라이브러리 타입(Room, DataStore, Ktor, Supabase)은 그 모듈이 `api`로 노출한다.
- 앱이 한 플랫폼에서만 의존하는 모듈(`app:shared`의 `iosMain`만 의존하는 `work:spotlight`)도 같은 모양으로 두고, 다른 플랫폼에 빈 구현을 기여하지 않는다. Metro는 그래프에서 닿는 바인딩만 확인하므로, 그래프에 들지 않는 플랫폼의 빠진 바인딩은 오류가 아니다. 모듈을 다른 플랫폼의 앱에도 넣게 되면 그때 `work:mcp`의 `UnsupportedMcpServerManager`처럼 빈 구현을 둔다.
- Android `Worker`는 `@AssistedInject`로 `Context`·`WorkerParameters`를 받고, 모듈마다 androidx `WorkerFactory`를 `@ContributesIntoSet`으로 기여한다. `app:shared`의 `AndroidAppModule`이 `Set<WorkerFactory>`로 만든 `DelegatingWorkerFactory`를 `WorkerFactory`로 제공하고, `DiaryApplication`은 [Hilt의 WorkManager 구성](https://developer.android.com/training/dependency-injection/hilt-jetpack#workmanager)처럼 `Configuration.Provider`에서 그래프의 `workerFactory`를 넘기며, WorkManager는 처음 쓰일 때 이 구성으로 초기화된다([on-demand 초기화](https://developer.android.com/develop/background-work/background-tasks/persistent/configuration/custom-configuration)). 기본 `WorkManagerInitializer`는 매니페스트에서 뺀다.

## ViewModel

- 런타임 인자가 없는 ViewModel은 `@Inject @ViewModelKey @ContributesIntoMap(AppScope::class)`로 기여한다.
- 런타임 인자를 받는 ViewModel은 `@AssistedInject`로 두고, 안에 `@AssistedFactory @ManualViewModelAssistedFactoryKey @ContributesIntoMap(AppScope::class) fun interface Factory : ManualViewModelAssistedFactory`를 둔다. 팩토리 함수 파라미터 이름은 `@Assisted` 파라미터와 같게 한다.
- `SavedStateHandle`을 받는 ViewModel은 `@ViewModelAssistedFactoryKey`의 `ViewModelAssistedFactory`로 `create(extras)`에서 `extras.createSavedStateHandle()`을 넘기고, 호출부는 `assistedMetroViewModel<VM>()`을 쓴다. `metroViewModel()`로 부르면 컴파일은 되지만 런타임에 실패한다.
- ViewModel 맵 조회는 런타임에 일어나므로 기여를 빠뜨리면 그 화면을 열 때 실패한다. `app:shared` 테스트가 앱 그래프의 entry로 화면을 연다.

## 테스트

- 앱 전체 구성을 확인하는 `app:shared` 테스트는 앱과 같은 플랫폼 그래프(`createGraph<JvmAppGraph>()`, `createGraphFactory<AndroidAppGraph.Factory>()`)를 만든다. 가짜가 필요하면 그 테스트 소스셋에 `@DependencyGraph(AppScope::class)` 테스트 그래프를 두고 `@DependencyGraph.Factory`의 `@Provides` 파라미터로 그 바인딩만 바꾼다.
- 한 모듈의 DI 구성을 확인하는 테스트는 그 모듈 테스트 소스셋에 테스트 그래프(`IpNetworkTestGraph`)를 둔다. 바꿔 끼울 제공 함수가 든 binding container는 `excludes`로 빼고 Factory 파라미터로 넣는다.
- 화면 테스트는 그래프 없이 `core:testing`의 `TestViewModelFactory`를 `LocalMetroViewModelFactory`로 제공해 ViewModel을 바꿔 끼운다.
