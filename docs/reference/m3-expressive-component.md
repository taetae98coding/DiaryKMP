# M3 Expressive 컴포넌트 조사

이 문서는 저장소가 쓰는 Compose Multiplatform Material 3에서 실제로 호출할 수 있는 컴포넌트를 모아, 어떤 것을 채택했고 어떤 것을 채택하지 않았는지 이유와 함께 남긴다. 화면별 결정은 [디자인 문서](../design/README.md)가 소유하고, 이 문서는 선택의 근거가 되는 API 목록만 담는다.

## 조사 기준

| 항목 | 값 |
| --- | --- |
| 조사 대상 | `org.jetbrains.compose.material3:material3` |
| 기준 버전 | `1.12.0-alpha03` |
| 확인 방법 | 배포된 `commonMain` 소스의 공개 선언과 KDoc |
| 최초 조사 | 2026-08-20 |
| 최종 확인 | 2026-08-24 |

버전 값은 `gradle/libs.versions.toml`의 `jetbrainsComposeMaterial3`가 소유한다. 이 문서의 값이 다르면 카탈로그를 기준으로 다시 조사한다. 기준 버전과 최종 확인을 적는 이유는 [참고 자료](README.md)가 소유한다.

최종 확인 시점에 기준 버전이 그대로였고, 아래 목록도 그대로였다. 채택 현황과 채택하지 않은 것은 저장소 코드와 다시 맞춰 확인했다.

목록은 `commonMain`에 있고 `@Deprecated`가 붙지 않은 공개 컴포저블만 담는다. Android 전용 선언과 이진 호환을 위해 남은 오버로드는 제외했다.

## 컴포넌트 목록

`M3E`는 Material 3 Expressive에서 새로 들어오거나 모양·모션이 다시 정의된 컴포넌트를 가리킨다.

### 버튼

| 컴포넌트 | M3E | 설명 |
| --- | --- | --- |
| `Button`, `ElevatedButton`, `FilledTonalButton`, `OutlinedButton`, `TextButton` | 갱신 | `ButtonDefaults`가 XSmall~XLarge 다섯 크기의 높이·여백·모양을 제공한다 |
| `ToggleButton`, `ElevatedToggleButton`, `TonalToggleButton`, `OutlinedToggleButton` | 신규 | 켜짐·꺼짐을 스스로 들고 있는 버튼. 누름과 선택에 따라 세 모양 사이를 변형한다 |
| `ButtonGroup` | 신규 | 버튼을 가로로 묶고, 누른 항목을 넓히고 이웃을 좁힌다. 폭이 부족하면 넘치는 항목을 메뉴로 내린다 |
| `SplitButtonLayout` | 신규 | 주 동작 버튼과 딸린 보조 동작 버튼을 한 덩어리로 묶는다 |
| `IconButton`, `FilledIconButton`, `FilledTonalIconButton`, `OutlinedIconButton`과 각 토글 변형 | 갱신 | `IconButtonDefaults`가 XSmall~XLarge 크기와 좁은·넓은 폭, 토글 모양 변형을 제공한다 |
| `FloatingActionButton`, `SmallFloatingActionButton`, `MediumFloatingActionButton`, `LargeFloatingActionButton` | 갱신 | Medium 크기가 새로 들어왔다 |
| `ExtendedFloatingActionButton`, `SmallExtendedFloatingActionButton`, `MediumExtendedFloatingActionButton`, `LargeExtendedFloatingActionButton` | 갱신 | 크기별 변형이 나뉘었다 |
| `FloatingActionButtonMenu`, `ToggleFloatingActionButton` | 신규 | 떠 있는 버튼을 눌러 동작 목록을 위로 펼친다 |

### 상단 바와 하단 바

| 컴포넌트 | M3E | 설명 |
| --- | --- | --- |
| `TopAppBar`, `CenterAlignedTopAppBar`, `MediumTopAppBar`, `LargeTopAppBar` | | 기존 상단 바 |
| `MediumFlexibleTopAppBar`, `LargeFlexibleTopAppBar`, `TwoRowsTopAppBar` | 신규 | 부제목을 함께 두고 제목 줄과 동작 줄을 나눠 배치한다 |
| `AppBarRow`, `AppBarColumn` | 신규 | 동작을 나열하다 폭이나 최대 개수를 넘기면 넘친 동작을 더보기 메뉴로 내린다 |
| `AppBarWithSearch` | 신규 | 제목 자리를 검색 입력이 차지하는 상단 바 |
| `BottomAppBar`, `FlexibleBottomAppBar` | 갱신 | `FlexibleBottomAppBar`는 높이와 정렬을 고를 수 있다 |
| `HorizontalFloatingToolbar`, `VerticalFloatingToolbar` | 신규 | 본문 위에 떠 있는 동작 모음. 스크롤에 따라 접히고 펼쳐진다 |

### 내비게이션

| 컴포넌트 | M3E | 설명 |
| --- | --- | --- |
| `ShortNavigationBar`, `ShortNavigationBarItem` | 신규 | 아래쪽 내비게이션. 기존 `NavigationBar`보다 낮고, 아이콘과 라벨을 가로로도 둘 수 있다 |
| `WideNavigationRail`, `WideNavigationRailItem` | 신규 | 접힘·펼침 상태를 갖는 시작 쪽 레일 |
| `ModalWideNavigationRail` | 신규 | 본문 위에 겹쳐 펼치는 레일 |
| `NavigationBar`, `NavigationRail`, `NavigationRailItem`, `NavigationBarItem` | | 이전 세대 내비게이션 |
| `ModalNavigationDrawer`, `DismissibleNavigationDrawer`, `PermanentNavigationDrawer`, `NavigationDrawerItem` | | 서랍 내비게이션 |
| `NavigationSuiteScaffold` | 갱신 | 창 환경에 맞는 내비게이션을 골라 준다. `navigationSuiteType`을 쓰는 오버로드는 `ShortNavigationBar`와 `WideNavigationRail`을, `layoutType`을 쓰는 오버로드는 이전 세대를 고른다 |

### 진행과 로딩

| 컴포넌트 | M3E | 설명 |
| --- | --- | --- |
| `LoadingIndicator`, `ContainedLoadingIndicator` | 신규 | 도형이 서로 변형되며 도는 짧은 대기 표시. 진행률을 받는 오버로드도 있다 |
| `LinearWavyProgressIndicator`, `CircularWavyProgressIndicator` | 신규 | 물결 모양 진행 표시 |
| `LinearProgressIndicator`, `CircularProgressIndicator` | | 기존 진행 표시 |
| `PullToRefreshBox`와 `PullToRefreshDefaults.LoadingIndicator` | 갱신 | 당김 새로고침의 진행 표시를 `LoadingIndicator` 모양으로 바꿀 수 있다 |

### 목록과 선택

| 컴포넌트 | M3E | 설명 |
| --- | --- | --- |
| `ListItem` | 갱신 | 표시 전용 오버로드에 더해 클릭·단일 선택·다중 선택 오버로드가 생겼다. 누를 때 모양이 변형된다 |
| `SegmentedListItem` | 신규 | 항목마다 자리에 맞는 모양을 받아 여러 항목이 하나의 묶음처럼 보이는 목록 항목 |
| `SingleChoiceSegmentedButtonRow`, `MultiChoiceSegmentedButtonRow`, `SegmentedButton` | | 분절 버튼 |
| `Checkbox`, `TriStateCheckbox`, `RadioButton`, `Switch` | | 기본 선택 컨트롤 |
| `Slider`, `RangeSlider`, `VerticalSlider`, `Label` | 갱신 | 세로 슬라이더와 값을 띄우는 라벨이 생겼다 |
| `AssistChip`, `FilterChip`, `InputChip`, `SuggestionChip`과 `Elevated` 변형 | | 칩 |

### 컨테이너와 표시

| 컴포넌트 | M3E | 설명 |
| --- | --- | --- |
| `Card`, `ElevatedCard`, `OutlinedCard`, `Surface` | | 컨테이너 |
| `ModalBottomSheet`, `BottomSheet`, `BottomSheetScaffold` | | Bottom Sheet |
| `VerticalDragHandle` | 신규 | 누름과 끌기에 따라 크기와 모양이 바뀌는 끌기 핸들 |
| `AlertDialog`, `BasicAlertDialog` | | 다이얼로그 |
| `DatePicker`, `DateRangePicker`, `TimePicker`, `TimeInput`, `TimePickerDialog` | 갱신 | `TimePickerDialog`가 컴포넌트로 들어왔다 |
| `SearchBar`, `DockedSearchBar`, `ExpandedFullScreenSearchBar`, `ExpandedDockedSearchBar`, `ExpandedDockedSearchBarWithGap`, `ExpandedFullScreenContainedSearchBar` | 갱신 | 접힘·펼침을 상태로 들고 있는 검색 바 계열. 접힌 바와 펼친 뷰를 짝지어 쓰고, 검색 입력은 `SearchBarDefaults.InputField`가 제공한다 |
| `HorizontalMultiBrowseCarousel`, `HorizontalUncontainedCarousel`, `HorizontalCenteredHeroCarousel` | 신규 | 항목이 가장자리에서 줄어드는 캐러셀 |
| `TooltipBox`와 `PlainTooltip`, `RichTooltip` | | 길게 누르거나 포인터를 올렸을 때 뜨는 설명 |
| `Badge`, `BadgedBox` | | 배지 |
| `SwipeToDismissBox` | | 스와이프 동작 |
| `Scaffold`, `TabRow` 계열, `Menu` 계열, `Text`, `TextField` 계열 | | 나머지 기본 컴포넌트 |

### 스타일 기반

| API | M3E | 설명 |
| --- | --- | --- |
| `MaterialExpressiveTheme` | 신규 | Expressive 색·모양·타이포그래피·모션 기본값을 제공하는 테마 |
| `MaterialShapes` | 신규 | `Circle`, `Cookie9Sided`, `Clover4Leaf`, `Burst`처럼 이름이 붙은 35개 도형. `RoundedPolygon.toShape()`로 `Shape`로 바꿔 쓴다 |
| `MotionScheme`, `MotionSchemeKeyTokens` | 신규 | 표준·Expressive 모션 규격. 컴포넌트가 이 값으로 애니메이션한다 |
| `Typography`의 `Emphasized` 계열 | 신규 | `titleLargeEmphasized`처럼 굵기와 자폭을 강조한 글자 스타일 |

## 채택 현황

이미 쓰고 있는 것과 이번에 넣은 것을 함께 적는다. `쓰는 곳`은 대표 위치만 남긴다.

| 컴포넌트 | 쓰는 곳 |
| --- | --- |
| `MaterialExpressiveTheme`, `Emphasized` 타이포그래피 | `DiaryTheme` |
| `ContainedLoadingIndicator` | `DiaryLoadingBox` |
| `CircularWavyProgressIndicator` | 진행 중인 아이콘 버튼, Gemini 모델 목록 |
| `FloatingActionButtonMenu`, `ToggleFloatingActionButton` | SettingHoliday 일괄 선택 |
| `SearchBarDefaults.InputField` | `DiarySearchInputField`. SearchHome과 SettingHoliday의 검색어 입력 |
| `VerticalDragHandle` | 목록·상세 배치의 영역 구분 |
| `TimePickerDialog` | `DiaryTimePickerDialog` |
| `NavigationSuiteScaffold`의 `navigationSuiteType` 오버로드 | 최상위 내비게이션 |
| `SegmentedListItem` | `DiarySegmentedListItem`. SettingHome 설정 목록, SettingMap 기본 지도 선택 |
| `TooltipBox`, `PlainTooltip` | `compose:core`의 아이콘 버튼 |
| `MaterialShapes` | 목록 빈 상태의 아이콘 배경 도형 |

## 채택하지 않은 것

목록에 있으나 넣지 않은 것 가운데, 판단이 갈렸던 것만 이유를 남긴다.

| 컴포넌트 | 넣지 않은 이유 |
| --- | --- |
| `ButtonGroup`의 연결 변형 | 단일 선택을 `ToggleButton`으로 만들면 낭독 도구에 선택이 아니라 체크 상태로 전달된다. `SegmentedButton`은 단일 선택 의미를 그대로 전달하고 이 버전에서 사용 중단되지 않았으므로 유지한다 |
| `AppBarRow`, `AppBarColumn` | 상단 바 동작이 세 개 이하이고, PlaceDetail·WebDetail은 삭제 동작을 끝 모서리에 고정하기로 이미 정해 두었다. 넘치는 동작을 메뉴로 내리면 그 자리 고정이 깨진다 |
| `MediumFlexibleTopAppBar`, `LargeFlexibleTopAppBar` | 부제목으로 둘 내용이 스펙에 없다. 제목 한 줄과 가로 이동으로 긴 제목을 다루기로 이미 정해 두었다 |
| `HorizontalFloatingToolbar`, `VerticalFloatingToolbar` | 상세 화면의 동작이 상단 바와 떠 있는 버튼으로 이미 나뉘어 있어, 세 번째 동작 모음을 두면 같은 화면에 동작 자리가 셋이 된다 |
| 접힌 바와 펼친 뷰의 짝 | 어느 화면에도 넣지 않는다. 이 계열은 펼친 뷰를 표시 영역 전체를 덮는 별도의 창으로 그리므로, 목록과 상세를 좌우로 함께 표시하는 넓은 창에서 검색과 상관없는 영역까지 덮는다. 결과도 본문과 펼친 창이 각각 그려 같은 목록을 두 번 그린다. SearchHome은 내비게이션으로 들어와 이미 펼쳐진 검색 화면이라 접힌 자리조차 없고, 넣으면 뒤로가기가 화면 이동이 아니라 접기로 소비된다. 두 화면 모두 검색 입력만 `SearchBarDefaults.InputField`로 가져다 쓰고 펼침 상태로 고정하되, 알약 컨테이너와 시작 쪽 돋보기 아이콘은 접힌 바와 같게 맞춘다. `SearchBar`와 `AppBarWithSearch`는 내부에서 `DisableSoftKeyboard`로 소프트 키보드를 막으므로 상시 활성 입력에는 컨테이너로 쓸 수 없고, `SearchBarDefaults.inputFieldShape`와 `collapsedContainedSearchBarColor`를 입력에 직접 준다 |
| `AppBarWithSearch` | 접힌 바와 펼친 뷰의 짝을 전제로 하는 상단 바다. 짝을 쓰지 않으므로 검색어 입력은 평범한 `TopAppBar`의 제목 자리에 둔다. 이때 입력 글자는 `LocalTextStyle`을 따라 제목 글자 크기로 커지므로 `MaterialTheme.typography.bodyLarge`로 고정해야 한다 |
| `SearchBarDefaults.enterAlwaysSearchBarScrollBehavior` | 결과를 내리는 동안 상단 바를 감춘다. SearchHome은 상단 바와 유형 탭 행이 자리를 옮기지 않기로 이미 정해 두었다 |
| 캐러셀 계열 | 가로로 넘겨 보는 이미지 묶음이 제품에 없다 |
| `Badge`, `BadgedBox` | 목록 개수를 배지로 알리지 않기로 이미 정해 두었다 |
| `SplitButtonLayout` | 주 동작에 딸린 보조 동작이 제품에 없다 |
| `PullToRefreshDefaults.LoadingIndicator` | `DiaryPullToRefreshBox`에 넣지 않는다. 도형이 변형되며 도는 표시는 당긴 거리를 사용자에게 되돌려 주지 못한다. 기본값인 `PullToRefreshDefaults.Indicator`는 당긴 거리만큼 원형 호가 차오르고 임계값에 이르면 화살촉이 붙어, 언제 손을 놓아야 새로고침이 시작되는지를 표시 자체가 알린다 |
| `SegmentedListItem`과 `ListItem`의 다중 선택 오버로드 | SettingHoliday 공휴일 항목에 넣지 않는다. 선택한 항목은 묶음 자리에 맞춘 모양 대신 네 모서리가 모두 둥근 선택 모양을 쓰는데, 공휴일은 설정하지 않은 항목이 선택 상태로 시작해 처음 진입하면 거의 모든 항목이 선택 모양이 된다. 여러 항목을 하나의 묶음으로 읽히게 한다는 이점이 남지 않고, 항목 간격만 좁아진다. 목록이 검색으로 늘고 줄어 묶음의 위아래 경계도 한 화면에 드러나지 않는다 |

## 참고

- [Material Design 3](https://m3.material.io/)
- [Material Design 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Compose Material 3 릴리스 노트](https://developer.android.com/jetpack/androidx/releases/compose-material3)
- [androidx.compose.material3 API 문서](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary)
- [Build adaptive navigation](https://developer.android.com/develop/adaptive-apps/guides/build-adaptive-navigation)
