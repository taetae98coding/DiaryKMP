# Navigation 규칙

화면 키(`NavKey`)와 딥링크를 어느 모듈이 소유하는지를 소유한다. 코루틴과 생명주기는 [main-safety.md](main-safety.md)와 [compose.md](compose.md)가 소유한다.

## 화면과 딥링크는 feature ui가 Koin에 등록하고 앱이 모은다

**화면 entry는 `ScreenEntryInstaller`로, 딥링크는 `DeepLinkNavigation`으로 그 화면의 `feature:*:ui`가 `@Single(binds = [...])` 클래스로 등록하고, `app:shared`는 `getAll()`로 모으기만 한다.** [Navigation 3 모듈화 가이드](https://developer.android.com/guide/navigation/navigation-3/modularize)가 DI의 Set으로 entry builder와 `DeepLinkMatcher`를 모으는 구성을 Koin으로 옮긴 것이다. 새 feature는 앱을 고치지 않고 등록 클래스만 더한다.

- 백스택처럼 Compose가 소유한 앱 상태는 Koin에 넣지 않고, 앱이 구현한 `ScreenNavigator`를 `install`과 `open`의 인자로 넘긴다. Koin scope에 넣으면 구성 변경으로 백스택이 다시 만들어질 때 scope 수명을 따로 맞춰야 한다.
- `koin-compose-navigation3`의 `navigation<T>`·`koinEntryProvider`는 쓰지 않는다. entry 메타데이터가 고정 `Map`뿐이라 백스택에 따라 바뀌는 목록·상세 배치와 `clazzContentKey`를 표현하지 못하고, DSL 전용이라 어노테이션 구성과 섞이며, `EntryProviderScope<Any>`로 받아 화면 키 타입을 잃는다.
- 등록을 빠뜨리면 컴파일이 잡지 못하고 그 화면을 열 때 실패하므로, `AppEntryInstallerTest`가 앱과 같은 Koin 구성으로 저장 가능한 모든 화면 키에 entry가 있는지 확인한다.
- entry 함수(`fileEntry` 등)는 internal로 두어 앱이 등록을 거치지 않고 부르지 못하게 한다. 여러 feature의 화면을 함께 배치하는 `app:shared` 테스트도 앱과 같은 Koin 구성에서 모은 등록으로 entry를 만든다.

### 딥링크

- 주소 문자열은 FCM·work·서버처럼 화면 모듈을 모르는 생산자가 쓰므로 `core:navigation`의 `ScreenDeepLink`에 두고 feature 모듈로 옮기지 않는다.
- 매처(`UriDeepLinkMatcher`·`DeepLinkUri`)는 Android 타깃이 있는 모듈에서만 만들므로 `feature:*:api`가 아니라 `feature:*:ui`에 둔다. `DeepLinkUri`는 Android에서 `android.net.Uri`로 typealias되어 JVM 변형과 바이트코드가 다르다. Android 타깃이 없는 `feature:*:api`·`core:navigation`은 Android 앱에 jvm 변형으로 들어가므로, 거기서 만들면 컴파일은 되고 Android 런타임에서 `NoSuchMethodError`가 난다.
- `openXxx` 정책은 등록 클래스 파일의 private 확장으로 둔다. 어느 주요 목적지 아래에 열지는 feature가 정하고, 주요 목적지까지의 백스택은 앱이 정하므로 `ScreenNavigator.topLevelBackStackOf`로 받는다.
- `getAll()`은 순서를 정하지 않으므로 한 주소에는 하나의 `DeepLinkNavigation`만 맞아야 한다. 주소는 host를 달리하고, `AppDeepLinkNavigationTest`가 지원하는 주소마다 하나만 맞는지 확인한다. 겹치는 주소가 생기면 `MatchResult`의 `Comparable` 비교로 바꾼다.

새 화면에 딥링크를 더할 때는 `ScreenDeepLink` 상수와 ui의 등록 클래스만 바뀐다. `AppState`에 화면별 분기나 `openXxx` 함수를 두지 않는다.

## 앱 시작 중 받은 딥링크

앱 밖에서 받은 주소는 Koin `@Single`인 `AppDeepLink`에 넣는다. Android `Activity`는 `by inject()`로, Swift가 부르는 iOS `DiaryDeepLink`는 `KoinPlatform.getKoin().get()`으로 같은 인스턴스를 얻고, `App`은 `koinInject`로 받아 `OpenDeepLinkEffect`에 넘긴다. Koin 밖의 전역 object에 두지 않는다. 화면이 아직 없을 때 받은 주소는 `AppDeepLink`가 가장 최근 하나를 남겨 두었다가 `OpenDeepLinkEffect`가 받는다. Android는 재생성(`savedInstanceState != null`)이면 처음 받은 주소를 다시 열지 않고 `onNewIntent`로 새 주소만 받는다.
