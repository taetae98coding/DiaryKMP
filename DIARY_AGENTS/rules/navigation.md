# Navigation 규칙

화면 키(`NavKey`)와 딥링크를 어느 모듈이 소유하는지를 소유한다. 코루틴과 생명주기는 [main-safety.md](main-safety.md)와 [compose.md](compose.md)가 소유한다.

## 딥링크는 feature ui가 알아보고 앱이 모은다

**주소 문자열은 `core:navigation`의 `ScreenDeepLink`에, 주소를 알아보는 매처와 알아본 결과를 백스택에 반영하는 정책은 그 화면의 `feature:*:ui`에 두고, `app:shared`의 `appDeepLinkNavigationList`는 feature가 만든 `DeepLinkNavigation`을 모으기만 한다.** [Navigation 3 모듈화 가이드](https://developer.android.com/guide/navigation/navigation-3/modularize)처럼 화면을 소유한 feature가 그 화면의 딥링크도 소유한다.

- 주소 문자열은 FCM·work·서버처럼 화면 모듈을 모르는 생산자가 쓰므로 feature 모듈로 옮기지 않는다.
- 매처(`UriDeepLinkMatcher`·`DeepLinkUri`)는 Android 타깃이 있는 모듈에서만 만들므로 `feature:*:api`가 아니라 `feature:*:ui`에 둔다. `DeepLinkUri`는 Android에서 `android.net.Uri`로 typealias되어 JVM 변형과 바이트코드가 다르다. Android 타깃이 없는 `feature:*:api`·`core:navigation`은 Android 앱에 jvm 변형으로 들어가므로, 거기서 만들면 컴파일은 되고 Android 런타임에서 `NoSuchMethodError`가 난다.
- feature ui는 `fileHomeDeepLinkNavigation`처럼 `DeepLinkNavigation`을 돌려주는 public 함수 하나만 내보내고, `openXxx` 정책은 그 파일의 private 확장으로 둔다. 어느 주요 목적지 아래에 둘지처럼 앱 전체 구조에 달린 값은 파라미터로 받는다.
- feature가 Koin으로 등록하고 앱이 `getAll()`로 모으지 않는다. 순서가 정해지지 않아 첫 매치 비교가 흔들리고, 앱 구조에 달린 파라미터를 넘길 수 없다. entry처럼 `appDeepLinkNavigationList`에서 직접 호출한다.

새 화면에 딥링크를 더할 때는 `ScreenDeepLink` 상수, ui의 `xxxDeepLinkNavigation`, `appDeepLinkNavigationList`의 항목 하나만 바뀐다. `AppState`에 화면별 분기나 `openXxx` 함수를 두지 않는다.

주소는 서로 겹치지 않게 host를 달리하고, 매처 목록은 첫 매치에서 멈춘다. 겹치는 주소가 생기면 `MatchResult`의 `Comparable` 비교로 바꾼다.

## 앱 시작 중 받은 딥링크

앱 밖에서 받은 주소는 Koin `@Single`인 `AppDeepLink`에 넣는다. Android `Activity`는 `by inject()`로, Swift가 부르는 iOS `DiaryDeepLink`는 `KoinPlatform.getKoin().get()`으로 같은 인스턴스를 얻고, `App`은 `koinInject`로 받아 `OpenDeepLinkEffect`에 넘긴다. Koin 밖의 전역 object에 두지 않는다. 화면이 아직 없을 때 받은 주소는 `AppDeepLink`가 가장 최근 하나를 남겨 두었다가 `OpenDeepLinkEffect`가 받는다. Android는 재생성(`savedInstanceState != null`)이면 처음 받은 주소를 다시 열지 않고 `onNewIntent`로 새 주소만 받는다.
