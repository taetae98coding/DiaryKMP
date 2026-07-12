# Gradle 규칙

## 의존성 선언

상위 artifact가 필요한 하위 artifact를 전이 의존성으로 제공하는 경우에는 상위 artifact 하나만 선언한다.

⚠️ 비권장 예시:

```kotlin
dependencies {
    implementation(libs.jetbrains.compose.runtime)
    implementation(libs.jetbrains.compose.ui)
}
```

✅ 권장 예시:

```kotlin
dependencies {
    implementation(libs.jetbrains.compose.ui)
}
```

### 예외: 전이 버전이 Version Catalog 버전보다 낮은 경우

상위 artifact가 하위 artifact를 더 낮은 버전으로 제공하면 중복 선언이 아니라 버전을 고정하는 선언이므로 유지한다. 이때 선언을 지우면 조용히 다운그레이드되고, 그 버전에 없는 API를 쓰고 있으면 컴파일이 깨진다.

중복으로 판단하기 전에 다음을 모두 확인한다.

1. 선언을 지워도 해당 artifact가 compile classpath에 남는가.
2. 남은 artifact의 해석 버전이 Version Catalog에 선언한 버전과 같은가.

두 조건을 모두 만족할 때만 제거한다. Gradle constraint로만 연결된 artifact는 실제 전이 의존성이 아니므로 판단 근거로 삼지 않는다.

`org.jetbrains.compose.*`, `org.jetbrains.androidx.*`, `androidx.*`는 상위 artifact가 낮은 버전을 전이로 제공하는 경우가 많아 특히 주의한다.

✅ 유지 예시 — `compose.ui`가 `lifecycle-runtime-compose`를 전이로 제공하지만 Version Catalog보다 낮은 버전이라 함께 선언한다:

```kotlin
dependencies {
    implementation(libs.jetbrains.compose.ui)
    implementation(libs.jetbrains.lifecycle.runtime.compose)
}
```
