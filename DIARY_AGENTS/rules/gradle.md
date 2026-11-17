# Gradle 규칙

## 의존성 선언

상위 artifact가 필요한 하위 artifact를 전이 의존성으로 제공하는 경우에는 상위 artifact 하나만 선언한다. 예: `compose.ui`가 `compose.runtime`을 제공하므로 `compose.runtime`은 선언하지 않는다.

### 예외: 전이 버전이 Version Catalog 버전보다 낮은 경우

상위 artifact가 하위 artifact를 더 낮은 버전으로 제공하면 버전을 고정하는 선언이므로 유지한다. 지우면 조용히 다운그레이드된다.

중복으로 판단하기 전에 다음을 모두 확인하고, 두 조건을 모두 만족할 때만 제거한다.

1. 선언을 지워도 해당 artifact가 compile classpath에 남는가.
2. 남은 artifact의 해석 버전이 Version Catalog에 선언한 버전과 같은가.

Gradle constraint로만 연결된 artifact는 실제 전이 의존성이 아니므로 판단 근거로 삼지 않는다. `org.jetbrains.compose.*`, `org.jetbrains.androidx.*`, `androidx.*`는 낮은 버전을 전이로 제공하는 경우가 많다. 예: `compose.ui`가 전이로 주는 `lifecycle-runtime-compose`는 카탈로그보다 낮아 함께 선언한다.

## Gradle과 AGP는 Kotlin에 맞춘다

**Gradle과 Android Gradle Plugin은 쓰고 있는 Kotlin 버전이 [호환 표](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html#version-compatibility)에서 완전히 지원한다고 적은 범위 안에서 고른다.** 각 도구의 최신 안정 버전이 그 범위를 넘으면 올리지 않고, Kotlin을 올릴 때 표의 새 범위 안에서 함께 올린다. 범위를 넘겨 쓰면 Kotlin Gradle 플러그인이 아직 맞추지 않은 Gradle·AGP 동작에서 deprecation 경고가 나거나 일부 기능이 동작하지 않을 수 있다. 표의 상한과 major·minor가 같은 패치 버전은 회귀 수정만 담으므로 범위 안으로 보고 그 minor의 최신 패치를 쓴다. 예: Kotlin 2.4.21의 표 상한은 Gradle 9.7.0, AGP 9.3.1이므로 Gradle 9.7.x, AGP 9.3.x의 최신 패치까지 쓰고 Gradle 9.8, AGP 9.4로는 올리지 않는다.

## Compose Multiplatform이 짝지은 버전

**Compose Multiplatform과 함께 쓰는 라이브러리는 각 저장소의 최신 버전이 아니라, 쓰고 있는 `jetbrainsCompose` 버전의 [GitHub 릴리즈](https://github.com/JetBrains/compose-multiplatform/releases) `Libraries` 표가 짝지은 버전을 쓴다.** 최신 버전은 다음 Compose Multiplatform과 짝일 수 있다. 예: `navigation3` 1.2.0은 Compose Multiplatform 1.13.0-beta와 짝이고, 1.12.2와 짝인 버전은 1.2.0-rc01이다.

- `Coordinates` 열: JetBrains가 멀티플랫폼으로 다시 배포한 라이브러리 버전이다. 대상은 `jetbrainsComposeMaterial3`, `jetbrainsComposeMaterial3Adaptive`, `jetbrainsLifecycle`, `jetbrainsNavigation3`이다.
- `Based on Jetpack` 열: 같은 계열의 androidx alias 버전이다. 대상은 `androidxComposeUi`, `androidxLifecycle`, `androidxNavigation3`이다. 예: 1.12.2의 UI는 `Coordinates`가 1.12.2, `Based on Jetpack`이 1.12.1이다.

표에 계열이 없는 androidx 아티팩트를 JetBrains 아티팩트와 함께 선언하면, 그 JetBrains 아티팩트의 Maven `.module` 파일이 요구하는 버전에 맞춘다.

두 열의 값이 같아도 의미가 다르므로 별도 alias로 선언한다. 예: `jetbrainsLifecycle`과 `androidxLifecycle`은 둘 다 2.11.0이지만 따로 둔다. 공통 소스에서 `NavDisplay`가 필요하면 KMP variant를 제공하는 `org.jetbrains.androidx.navigation3:navigation3-ui`는 `jetbrainsNavigation3`를, `androidx.navigation3:navigation3-runtime`은 `androidxNavigation3`를 쓴다.

## 의존성 baseline

`dependencyGuard`가 `:app:android`와 `:app:wasm`의 runtime classpath를 그 모듈의 `dependencies/*.txt` baseline과 비교한다. `:app:jvm`은 `compose.desktop.currentOs`가 빌드하는 OS의 네이티브 런타임을 골라 baseline이 OS마다 달라지므로 검사하지 않는다. 의존성이나 버전을 의도적으로 바꿨으면 `./gradlew dependencyGuardBaseline`으로 baseline을 갱신하고, 바뀐 전이 의존성이 의도한 것인지 diff로 확인한 뒤 같은 커밋에 포함한다.

## Version Catalog 형식

### 선언 순서

`libs.versions.toml`의 선언은 다음 순서로 그룹화한다.

1. `kotlin`
2. `kotlinx`
3. `jetbrains`
4. `android`
5. `androidx`
6. `gradle-plugin`
7. 그 외 alias는 abc 순

각 그룹 안에서는 관련 alias끼리 가깝게 배치한다. 새 alias가 위 그룹 중 하나로 일반화될 만큼 반복되기 전까지는 별도 그룹을 추가하지 않는다.

### 네이밍

`[versions]` alias는 lowerCamelCase로, `[libraries]`·`[plugins]` alias는 kebab-case로 작성한다.

### URL 주석

`[versions]` 항목에는 같은 줄에 URL만 주석으로 남긴다.

- GitHub Releases로 관리되는 항목은 특정 태그 URL이 아니라 전체 releases URL을 사용한다.
- GitHub Tags로만 관리되는 항목은 특정 태그 URL이 아니라 전체 tags URL을 사용한다.
- GitHub Releases 또는 Tags가 아니라 README 중심으로 관리되는 항목은 GitHub repository URL을 사용한다.
- Android Developers release notes 문서는 가능한 한 버전별 URL보다 일반 release notes 진입점 URL을 사용하고, 영문으로 열리도록 `?hl=en`을 붙인다.

다른 버전에 맞춰 선택되는 보조 버전은 URL 주석을 남기지 않는다. 예: `Coordinates` 열로 고르는 `jetbrainsComposeMaterial3`·`jetbrainsNavigation3` 등은 기준인 `jetbrainsCompose`에만 URL을 남긴다. `Based on Jetpack` 열로 고르는 androidx alias는 자기 artifact의 release notes URL을 남긴다.

### 예시

```toml
[versions]
kotlin = "2.4.21" # https://github.com/JetBrains/kotlin/releases
kotlinxBrowser = "0.5.0" # https://github.com/Kotlin/kotlinx-browser
jetbrainsCompose = "1.12.2" # https://github.com/JetBrains/compose-multiplatform/releases
jetbrainsComposeMaterial3 = "1.12.0-alpha03"
jetbrainsNavigation3 = "1.2.0-rc01"
androidGradlePlugin = "9.3.3" # https://developer.android.com/build/releases/gradle-plugin?hl=en
androidxActivity = "1.13.0" # https://developer.android.com/jetpack/androidx/releases/activity?hl=en
androidxNavigation3 = "1.2.0" # https://developer.android.com/jetpack/androidx/releases/navigation3?hl=en
spotless = "8.11.0" # https://github.com/diffplug/spotless/releases

[libraries]
kotlinx-browser = { group = "org.jetbrains.kotlinx", name = "kotlinx-browser", version.ref = "kotlinxBrowser" }
jetbrains-compose-ui = { group = "org.jetbrains.compose.ui", name = "ui", version.ref = "jetbrainsCompose" }
jetbrains-navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "jetbrainsNavigation3" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "androidxActivity" }
androidx-navigation3-runtime = { module = "androidx.navigation3:navigation3-runtime", version.ref = "androidxNavigation3" }
gradle-plugin-jetbrains-compose = { module = "org.jetbrains.compose:org.jetbrains.compose.gradle.plugin", version.ref = "jetbrainsCompose" }

[plugins]
jetbrains-compose = { id = "org.jetbrains.compose", version.ref = "jetbrainsCompose" }
android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
spotless = { id = "com.diffplug.spotless", version.ref = "spotless" }
```
