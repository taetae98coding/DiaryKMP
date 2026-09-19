# Compose Styles API 조사

이 문서는 저장소가 쓰는 Compose Multiplatform foundation에 들어 있는 Styles API(`androidx.compose.foundation.style`)로 무엇을 할 수 있고 무엇이 아직 되지 않는지를 배포된 소스로 확인해 남긴다. 어디에 Style을 쓰고 어디에 Modifier를 남기는지의 판단 기준은 [Compose UI 규칙](../../DIARY_AGENTS/rules/compose.md)의 `시각 속성은 Style, 동작과 배치는 Modifier`가 소유하고, 이 문서는 그 판단의 근거가 되는 API 사실만 담는다.

## 조사 기준

| 항목 | 값 |
| --- | --- |
| 조사 대상 | `org.jetbrains.compose.foundation:foundation`, `org.jetbrains.compose.material3:material3` |
| 기준 버전 | foundation `1.12.0`, material3 `1.12.0-alpha03` |
| 확인 방법 | 배포된 `commonMain` 소스의 공개 선언과 KDoc, `ComposeFoundationFlags` |
| 최초 조사 | 2026-09-19 |
| 최종 확인 | 2026-09-19 |

버전 값은 `gradle/libs.versions.toml`의 `jetbrainsCompose`와 `jetbrainsComposeMaterial3`가 소유한다. 이 문서의 값이 다르면 카탈로그를 기준으로 다시 조사한다. 기준 버전과 최종 확인을 적는 이유는 [참고 자료](README.md)가 소유한다.

Styles API는 `@ExperimentalFoundationStyleApi`가 붙은 실험적 API다. 별도 artifact 없이 foundation에 들어 있으므로 의존성 추가 없이 파일 상단 `@file:OptIn(ExperimentalFoundationStyleApi::class)`만으로 쓸 수 있다.

## 핵심 타입

| 타입 | 역할 |
| --- | --- |
| `Style` | `StyleScope.applyStyle()` 하나를 가진 fun interface. `Style { ... }` 람다로 만들고, 아무 속성도 없는 기본값은 `Style` companion이다 |
| `StyleScope` | 속성을 선언하는 수신자. `Density`와 `CompositionLocalAccessorScope`를 상속해 `LocalXxx.currentValue`로 CompositionLocal을 읽을 수 있다 |
| `StyleState`, `MutableStyleState` | 누름·호버·초점·선택·활성·토글 상태를 들고 있는 스냅샷 상태. `InteractionSource`를 넘기면 상호작용을 자동으로 반영하고, `isEnabled`처럼 직접 대입할 수도 있다 |
| `StyleStateKey<T>` | 재생·정지처럼 컴포넌트가 정의하는 사용자 상태의 키 |
| `Modifier.styleable(styleState, style)` | Style을 노드에 적용하는 Modifier. 여러 Style을 넘기면 오른쪽이 왼쪽의 같은 속성을 덮어쓴다 |
| `Style.then(other)`, `Style(vararg)` | Style 병합. 속성 단위로 마지막 값이 이긴다 |
| `rememberUpdatedStyleState(interactionSource) { state -> ... }` | `MutableStyleState`를 만들고 매 composition마다 블록으로 갱신하는 팩토리 |

## 쓸 수 있는 속성

`StyleScope`가 제공하는 속성은 다음 묶음이다. 상속 여부는 자식 `Text`까지 값이 내려가는지를 뜻한다.

| 묶음 | 속성 | 상속 |
| --- | --- | --- |
| 안쪽 여백 | `contentPadding(...)`, `contentPaddingStart/Top/End/Bottom` | 아니오 |
| 바깥 여백 | `externalPadding(...)`, `externalPaddingStart/Top/End/Bottom` | 아니오 |
| 크기 | `width`, `height`, `size`(Dp, DpSize, 비율), `fillWidth()`, `fillHeight()`, `fillSize()`, `minWidth`, `maxWidth` 등 | 아니오 |
| 위치 | `left`, `top`, `right`, `bottom` | 아니오 |
| 채움 | `background(Color 또는 Brush)`, `foreground(Color 또는 Brush)` | 아니오 |
| 테두리 | `border(width, color 또는 brush)`, `borderWidth`, `borderColor`, `borderBrush` | 아니오 |
| 모양 | `shape`, `clip()` | 아니오. `background`, `border`, `clip`, 그림자가 이 모양을 함께 쓴다 |
| 그림자 | `dropShadow(Shadow)`, `innerShadow(Shadow)` | 아니오 |
| 변형 | `alpha`, `scaleX/Y`, `translationX/Y`, `rotationX/Y/Z`, `transformOrigin`, `zIndex`, `colorFilter` | 아니오 |
| 글자 | `textStyle`, `fontSize`, `fontWeight`, `fontFamily`, `lineHeight`, `letterSpacing`, `textAlign`, `textDecoration` 등 | 예 |
| 글자색 | `contentColor(Color)`, `contentBrush(Brush)` | 예 |
| 상태 | `pressed { }`, `hovered { }`, `focused { }`, `selected { }`, `disabled { }`, `checked { }`, `triStateToggleOn/Off/Indeterminate { }`, 사용자 키는 `state(key, block)` | 상태 블록 안의 속성은 그 속성의 상속 규칙을 따른다 |
| 전환 | `animate { }`, `animate(spec) { }` | 감싼 속성이 상태 사이를 바뀔 때 spring 기본값으로 보간한다 |

`Style { }` 안에서 스냅샷 상태를 읽으면 그 읽기는 composition이 아니라 Style의 관찰 범위에 속한다. 값이 바뀌면 바뀐 속성이 속한 단계(배치 또는 그리기)만 다시 실행되고, 컴포저블은 다시 구성되지 않는다. `Modifier.graphicsLayer { }`나 `Modifier.drawBehind { }` 안에서 읽는 것과 같은 효과를 속성 선언으로 얻는다.

## 이 버전에서 되지 않는 것

다음은 실제 소스로 확인한 제약이다. 이 저장소의 채택 범위를 정하는 근거다.

| 제약 | 확인한 곳 | 영향 |
| --- | --- | --- |
| 글자 속성과 글자색 상속이 기본값으로 꺼져 있다 | `ComposeFoundationFlags.isInheritedTextStyleEnabled = false`. 켜도 `BasicText`의 단순 문자열 경로(`TextStringSimpleNode`)만 상속을 읽는다 | 부모의 `contentColor`, `textStyle`이 Material 3 `Text`에 전달되지 않는다. 글자색과 글자 스타일은 지금처럼 `LocalContentColor`, `LocalTextStyle`, `Text`의 파라미터로 다룬다 |
| Material 3 컴포넌트가 `style: Style` 파라미터를 받지 않는다 | material3 `1.12.0-alpha03` 공개 선언에 `androidx.compose.foundation.style` 참조가 없다 | `Button`, `Card`, `Chip`, `TextField` 같은 Material 컴포넌트의 모양은 계속 `colors`, `shape`, `border` 파라미터로 바꾼다 |
| Material 3 테마 토큰을 `StyleScope`에서 읽을 수 없다 | `LocalColorScheme`, `LocalShapes`, `LocalTypography`가 `internal`이다. `LocalContentColor`만 공개다 | `DiaryTheme.colorScheme`처럼 `@Composable` getter로만 닿는 값은 composition에서 지역 변수로 읽어 `Style { }`에 캡처한다. 테마 전역 Style 객체를 만들어도 색은 캡처로만 들어간다 |
| `dropShadow`는 `Shadow(radius, spread, offset, color)`를 받는다 | `ShadowScope` | `Modifier.shadow(elevation)`의 Material elevation 그림자와 같은 모양이 아니다. elevation 기반 그림자는 Modifier로 남긴다 |

## 채택 현황

`Modifier.styleable`로 옮긴 곳과 이유다. 컴포넌트에 `style: Style` 파라미터는 아직 열지 않았다. 두 번째 호출자가 다른 모양을 요구할 때 연다.

여러 화면이 함께 쓰는 묶음은 [공통 스타일](../design/styles.md)이 이름을 붙이고, `compose:core`의 `DiaryStyles`가 같은 이름의 `Style` 값으로 든다. 대응 규칙은 [Compose UI 규칙](../../DIARY_AGENTS/rules/compose.md)이 소유한다.

| 공통 스타일 | 코드 | 쓰는 곳 |
| --- | --- | --- |
| Bottom Sheet 제목, 내용, 영역, 선택 줄 | `DiaryStyles.bottomSheetTitle`, `bottomSheetContent`, `bottomSheetSection`, `bottomSheetRow` | 정렬 선택, 태그 필터, MemoHome 필터, TagHome 필터, WebDetail 표시 방식, TagDetail 표시 범위 Bottom Sheet |
| 카드 내용 | `DiaryStyles.cardContent` | 메모·태그·장소·웹·연락처·곡 카드, SettingHoliday 항목, SettingGemini 모델 줄 |
| 흐림 | `StyleScope.dimmed()` | 비활성 태그 필터 영역, 이동 중 캘린더 메모 조각 |
| 캘린더 아이템 모양 | `CalendarDefault.itemShape` (`Shape`) | CalendarText 배경, 이동 고스트 그림자 |

컴포넌트 하나에만 속하는 표현은 그 컴포넌트 안에서 `Modifier.styleable { }`로 선언한다.

| 컴포넌트 | 옮긴 표현 | 이유 |
| --- | --- | --- |
| `CalendarText` | 모양, 클립, 배경, 안쪽 여백 | 시각 속성 세 개가 한 노드에 모여 있어 Style 한 블록으로 읽힌다 |
| `CalendarBarText`의 색상 바 | 너비, 높이 채움, 모양, 배경 | 같은 이유 |
| `CalendarDayOfMonthText`의 원형 배경 | 모양, 배경 | 같은 이유 |
| `DiaryColorIndicator` | 크기, 모양, 배경 | 같은 이유 |
| `DiaryColorInput`, `ColorPickerPreview` | 상태가 움직이는 배경색 | `drawBehind { drawRect(state.color) }`가 하던 그리기 단계 읽기를 `background(state.color)` 선언으로 대신한다 |
| `TagFilterFlexBox` | 비활성일 때 흐림 | `MutableStyleState.isEnabled`와 `disabled { dimmed() }`로 상태 기반 표현을 선언한다 |
| CalendarHome의 이동 중 메모 조각 | 이동 중 흐림 | `Style { }` 안에서 이동 상태를 읽어 그리기 단계만 갱신한다 |

## 채택하지 않은 것

| 대상 | 넣지 않은 이유 |
| --- | --- |
| `DiaryPlaceholder`, `DiaryPickerEmptyBox`, `WebDetailPage`의 `CompositionLocalProvider(LocalContentColor provides ...)` | 글자색 상속이 Material 3 `Text`에 닿지 않는다 |
| `DiaryPlaceholder`의 아이콘 배경 | 색과 도형이 모두 Material 3 토큰이라 캡처로만 들어가고, 옮겨서 줄어드는 recomposition이 없다 |
| 누름 영역만 둥글게 자르는 `Modifier.clip(CircleShape)` 한 줄 | 속성이 하나인 노드는 Modifier 한 줄이 Style 블록보다 짧다 |
| `CalendarHomeMoveGhost`의 `Modifier.shadow(elevation)` | Material elevation 그림자를 `dropShadow(Shadow)`로 같은 모양으로 옮길 수 없다 |
| `GoogleMapPinMarker`의 라벨 배경 | Android 전용 지도 마커 비트맵 안에서 그려지는 표현이라 공용 규칙의 대상이 아니다 |
| `pressed { }`, `hovered { }` 같은 상호작용 상태 표현 | 눌림과 호버 표현은 전부 Material 컴포넌트가 맡고 있다. 자체 컴포넌트에 상호작용 표현이 생기면 그때 `InteractionSource`를 연결해 쓴다 |

## 참고

- [Styles in Compose](https://developer.android.com/develop/ui/compose/styles)
- [Fundamentals of Styles](https://developer.android.com/develop/ui/compose/styles/fundamentals)
- [State and animations in Styles](https://developer.android.com/develop/ui/compose/styles/state-animations)
- [Styles versus Modifiers](https://developer.android.com/develop/ui/compose/styles/styles-vs-modifiers)
- [Theming with Styles](https://developer.android.com/develop/ui/compose/styles/theming)
