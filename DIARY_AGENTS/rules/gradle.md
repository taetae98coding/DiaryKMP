# Gradle 규칙

## 의존성 선언

상위 artifact가 필요한 하위 artifact를 전이 의존성으로 제공하는 경우에는 상위 artifact 하나만 선언한다. 예: `compose.ui`가 `compose.runtime`을 제공하므로 `compose.runtime`은 선언하지 않는다.

⚠️ 비권장 예시:

```kotlin
dependencies {
    implementation(libs.jetbrains.compose.runtime)
    implementation(libs.jetbrains.compose.ui)
}
```

### 예외: 전이 버전이 Version Catalog 버전보다 낮은 경우

상위 artifact가 하위 artifact를 더 낮은 버전으로 제공하면 버전을 고정하는 선언이므로 유지한다. 지우면 조용히 다운그레이드된다.

중복으로 판단하기 전에 다음을 모두 확인하고, 두 조건을 모두 만족할 때만 제거한다.

1. 선언을 지워도 해당 artifact가 compile classpath에 남는가.
2. 남은 artifact의 해석 버전이 Version Catalog에 선언한 버전과 같은가.

Gradle constraint로만 연결된 artifact는 실제 전이 의존성이 아니므로 판단 근거로 삼지 않는다. `org.jetbrains.compose.*`, `org.jetbrains.androidx.*`, `androidx.*`는 낮은 버전을 전이로 제공하는 경우가 많다. 예: `compose.ui`가 전이로 주는 `lifecycle-runtime-compose`는 카탈로그보다 낮아 함께 선언한다.

## 의존성 baseline

`dependencyGuard`가 앱 모듈 runtime classpath를 `dependencies/*.txt` baseline과 비교한다. 의존성이나 버전을 의도적으로 바꿨으면 `./gradlew dependencyGuardBaseline`으로 baseline을 갱신하고, 바뀐 전이 의존성이 의도한 것인지 diff로 확인한 뒤 같은 커밋에 포함한다.
