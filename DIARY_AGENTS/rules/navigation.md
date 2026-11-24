# Navigation 규칙

화면 키(`NavKey`)와 딥링크를 어느 모듈이 소유하는지와, 화면 사이에 결과를 어떻게 돌려주는지를 소유한다. 코루틴과 생명주기는 [main-safety.md](main-safety.md)와 [compose.md](compose.md)가 소유한다.

## 화면과 딥링크는 feature ui가 그래프에 기여하고 앱이 모은다

**화면 entry는 `ScreenEntryInstaller`로, 딥링크는 `DeepLinkNavigation`으로 그 화면의 `feature:*:ui`가 `@ContributesIntoSet(AppScope::class, binding = binding<...>())` 클래스로 기여하고, `app:shared`는 `AppDependencies`의 `Set<ScreenEntryInstaller>`·`Set<DeepLinkNavigation>` 생성자 인자로 모으기만 한다([di.md](di.md)).** [Navigation 3 모듈화 가이드](https://developer.android.com/guide/navigation/navigation-3/modularize)가 DI의 Set으로 entry builder와 `DeepLinkMatcher`를 모으는 구성을 Metro 멀티바인딩으로 옮긴 것이다. 새 feature는 앱을 고치지 않고 등록 클래스만 더한다.

- 백스택처럼 Compose가 소유한 앱 상태는 그래프에 넣지 않고, 앱이 구현한 `ScreenNavigator`를 `install`과 `open`의 인자로 넘긴다. 그래프 scope에 넣으면 구성 변경으로 백스택이 다시 만들어질 때 scope 수명을 따로 맞춰야 한다.
- 등록을 빠뜨리면 컴파일이 잡지 못하고 그 화면을 열 때 실패하므로, `AppEntryInstallerTest`가 앱과 같은 그래프에서 꺼낸 `AppDependencies`로 저장 가능한 모든 화면 키에 entry가 있는지 확인한다.
- 화면의 플랫폼 보조 class(파일 선택기 등)는 등록 클래스가 `XxxFactory`를 생성자로 받아 entry 함수의 인자로 내려보낸다([di.md](di.md)).
- entry 함수(`fileEntry` 등)는 internal로 두어 앱이 등록을 거치지 않고 부르지 못하게 한다. 여러 feature의 화면을 함께 배치하는 `app:shared` 테스트도 앱과 같은 그래프에서 모은 등록으로 entry를 만든다.
- **앱이 내비게이션 바 표시를 정할 때 필요한 화면의 성질은 `ScreenNavKey`의 getter 프로퍼티로 화면 키가 알린다.** 아래 화면 위에 겹쳐 열리는 화면은 `isOverlay`를, 주요 목적지 목록의 상세 영역에 놓이는 화면은 `topLevelListKey`를 override하고, `AppState`는 feature의 화면 키 타입을 분기하지 않고 이 값만 읽는다. backing field를 두면 저장 상태에 함께 직렬화되므로 `screenName`처럼 getter로만 선언한다.

### 딥링크

- 주소 문자열은 FCM·work·서버처럼 화면 모듈을 모르는 생산자가 쓰므로 `core:navigation`의 `ScreenDeepLink`에 두고 feature 모듈로 옮기지 않는다.
- 매처(`UriDeepLinkMatcher`·`DeepLinkUri`)는 Android 타깃이 있는 모듈에서만 만들므로 `feature:*:api`가 아니라 `feature:*:ui`에 둔다. `DeepLinkUri`는 Android에서 `android.net.Uri`로 typealias되어 JVM 변형과 바이트코드가 다르다. Android 타깃이 없는 `feature:*:api`·`core:navigation`은 Android 앱에 jvm 변형으로 들어가므로, 거기서 만들면 컴파일은 되고 Android 런타임에서 `NoSuchMethodError`가 난다.
- `openXxx` 정책은 등록 클래스 파일의 private 확장으로 둔다. 어느 주요 목적지 아래에 열지는 feature가 정하고, 주요 목적지까지의 백스택은 앱이 정하므로 `ScreenNavigator.topLevelBackStackOf`로 받는다.
- `List<DeepLinkNavigation>`은 순서를 정하지 않으므로 한 주소에는 하나의 `DeepLinkNavigation`만 맞아야 한다. 주소는 host를 달리하고, `AppDeepLinkNavigationTest`가 지원하는 주소마다 하나만 맞는지 확인한다. 겹치는 주소가 생기면 공식 가이드처럼 `MatchResult`의 `Comparable` 비교(`maxOrNull()`)로 바꾼다.

- 딥링크로 여는 상세는 화면 키의 `isStandalone = true`로 연다. 목록·상세 배치는 바로 아래 화면이 그 목록인지만 보고 참여를 판정하므로, 표시 없이 열면 목록을 보던 중 들어온 딥링크의 상세가 그 목록의 상세 영역에 놓인다. 화면 안에서 목록 항목을 눌러 여는 경로는 기본값(`false`)을 쓴다.

새 화면에 딥링크를 더할 때는 `ScreenDeepLink` 상수와 ui의 등록 클래스만 바뀐다. `AppState`에 화면별 분기나 `openXxx` 함수를 두지 않는다.

## 화면 간 결과 전달

**다른 화면이 만든 결과(추가한 항목의 id 등)는 Navigation 3의 `ResultEventBus`로 돌려받는다.** `AppNavigation`이 `LocalResultEventBus`로 하나를 제공한다.

- 결과 타입과 결과 키 함수는 결과를 보내는 화면의 `feature:*:api`에 둔다(`ContactAddedResult`, `contactAddedResultKey(requestKey)`). 같은 feature 안에서만 주고받는 결과는 `feature:*:ui`에 `internal`로 둔다(`QrScannedResult`).
- 다른 feature의 화면을 열어 결과를 받는 화면은 entry에서 `compose:core`의 `rememberResultRequestKey()`로 요청 키를 만들어, 여는 화면 키에 실어 보낸다(`TagAddNavKey(requestKey = addRequestKey)`). 결과 키에 요청 키가 들어가므로 같은 결과를 기다리는 화면이 백스택에 둘 이상 있어도 요청한 화면만 받는다.
- 보내는 화면은 화면 키의 `requestKey`가 `null`이 아닐 때만 `resultEventBus.sendResult(resultKey = ..., result = ...)`로 보낸다. 결과를 기다리는 화면 없이 열린 경우다.
- 받는 쪽은 `XxxResultEffect` 컴포저블에서 `ResultEffect<T>(resultKey = ...)`로 받아 콜백으로 넘긴다(`MemoContactAddedResultEffect`). `ResultEventBus`는 `resultEventBus: ResultEventBus = LocalResultEventBus.current` 파라미터로 받는다.

## 화면을 여는 콜백의 중복 이동

**사용자 조작으로 불리고 백스택에 화면 키를 더하기만 하는 `navigateToXxx` 콜백은 entry에서 `dropUnlessResumed { }`로 감싸 Screen에 넘긴다.** `NavBackStack.add`는 같은 키도 그대로 쌓고, `NavDisplay`는 전환이 끝나기 전까지 entry의 `LocalLifecycleOwner`를 `STARTED`로 묶어 두므로, 감싼 콜백은 연타나 전환 중 클릭을 버린다. 인자를 받는 콜백은 `compose:core`의 `dropUnlessResumedWithArg { id -> }`를 쓴다. Screen과 Scaffold는 감싸지 않는다.

다음은 감싸지 않는다.

- 여러 번 불려도 화면이 하나만 남는 이동. 상세 영역의 키를 걷어 내고 더하는 `core:navigation`의 `navigateToDetail` 계열(`navigateToMemoDetail` 등)과 `navigateToSettingDetail`, 필터 시트를 닫고 여는 `navigateToTagAddFromFilter`가 그렇다. 목록·상세 배치에서 전환 중에 다른 항목을 고르는 것도 받아야 한다.
- `RESUMED`가 아닐 때도 실행되어야 하는 이동. `navigateUp`, 딥링크, Effect가 부르는 이동(`navigateToCopiedMemo`, `navigateToGeminiSetting`, FileHome의 `navigateToAdd`)과 권한 요청 결과로 부르는 이동(`navigateToScan`)이 그렇다. 시스템 화면에서 돌아오는 중에는 아직 `RESUMED`가 아니다.

## 앱 시작 중 받은 딥링크

앱 밖에서 받은 주소는 `@SingleIn(AppScope::class)`인 `AppDeepLink`에 넣는다. Android `Activity`는 `appGraph.appDeepLink`로, Swift가 부르는 iOS `DiaryDeepLink`는 `DiaryStartupInitializer.graph.appDeepLink`로 같은 인스턴스를 얻고, `App`은 `AppDependencies`로 받아 `OpenDeepLinkEffect`에 넘긴다. 그래프 밖의 전역 object에 두지 않는다. 화면이 아직 없을 때 받은 주소는 `AppDeepLink`가 가장 최근 하나를 남겨 두었다가 `OpenDeepLinkEffect`가 받는다. Android는 재생성(`savedInstanceState != null`)이면 처음 받은 주소를 다시 열지 않고 `onNewIntent`로 새 주소만 받는다.
