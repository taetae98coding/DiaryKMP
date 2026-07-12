# Gradle 지침

## Version Catalog 선언 순서

`libs.versions.toml`의 선언은 다음 순서로 그룹화한다.

1. `kotlin`
2. `kotlinx`
3. `jetbrains`
4. `android`
5. `androidx`
6. `gradle-plugin`
7. 그 외 alias는 abc 순

각 그룹 안에서는 관련 alias끼리 가깝게 배치한다.
새 alias가 위 그룹 중 하나로 일반화될 만큼 반복되기 전까지는 별도 그룹을 추가하지 않는다.

## Version Catalog 네이밍

`[versions]` alias는 lowerCamelCase로 작성한다.

`[libraries]`, `[plugins]` alias는 kebab-case로 작성한다.

## Version Catalog URL 주석

`[versions]` 항목에는 같은 줄에 URL만 주석으로 남긴다.

GitHub Releases로 관리되는 항목은 특정 태그 URL이 아니라 전체 releases URL을 사용한다.

GitHub Tags로만 관리되는 항목은 특정 태그 URL이 아니라 전체 tags URL을 사용한다.

GitHub Releases 또는 Tags가 아니라 README 중심으로 관리되는 항목은 GitHub repository URL을 사용한다.

Android Developers release notes 문서는 가능한 한 버전별 URL보다 일반 release notes 진입점 URL을 사용한다.

Android Developers 문서는 영문으로 열리도록 `?hl=en`을 붙인다.

다른 버전에 맞춰 선택되는 보조 버전은 URL 주석을 남기지 않는다. 예를 들어 `jetbrainsComposeMaterial3`와 `jetbrainsNavigation3`는 `jetbrainsCompose` 릴리즈 노트에 명시된 대응 버전을 사용하고, 릴리즈 노트 URL은 기준이 되는 `jetbrainsCompose`에만 남긴다.

JetBrains Compose Multiplatform 기반 AndroidX 이식 artifact(`org.jetbrains.androidx.*`)는 JetBrains artifact 버전과 기반 AndroidX 버전이 다를 수 있다. 예를 들어 `jetbrainsCompose` `1.11.1`의 runtime/ui/foundation은 AndroidX `1.11.2` 기반이고, Navigation3는 우연히 JetBrains artifact와 AndroidX artifact가 모두 `1.1.1`이다.

두 버전 값이 같아도 의미가 다르면 Version Catalog에서 별도 alias로 선언한다. 예를 들어 공통 소스에서 `NavDisplay`가 필요하면 KMP variant를 제공하는 `org.jetbrains.androidx.navigation3:navigation3-ui`는 `jetbrainsNavigation3`를 사용하고, `androidx.navigation3:navigation3-runtime`은 `androidxNavigation3`를 사용한다.

## Version Catalog 예시

```toml
[versions]
kotlin = "2.4.0" # https://github.com/JetBrains/kotlin/releases
kotlinxBrowser = "0.5.0" # https://github.com/Kotlin/kotlinx-browser
jetbrainsCompose = "1.11.1" # https://github.com/JetBrains/compose-multiplatform/releases
jetbrainsComposeMaterial3 = "1.11.0-alpha07"
jetbrainsNavigation3 = "1.1.1"
androidGradlePlugin = "9.2.1" # https://developer.android.com/build/releases/gradle-plugin?hl=en
androidxActivity = "1.13.0" # https://developer.android.com/jetpack/androidx/releases/activity?hl=en
androidxNavigation3 = "1.1.1" # https://developer.android.com/jetpack/androidx/releases/navigation3?hl=en
spotless = "8.8.0" # https://github.com/diffplug/spotless/releases

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
