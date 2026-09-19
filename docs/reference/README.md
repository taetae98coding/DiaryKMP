# 참고 자료

이 폴더는 [제품 문서](../README.md)가 아닌 참고 자료를 모은다. API 사용법 학습, 라이브러리 동작 검증 결과처럼 특정 화면이나 기능에 묶이지 않는 자료가 대상이다.

Markdown 외 형식도 둘 수 있다. 파일을 추가하면 아래 목록에 한 줄로 등록한다.

## 목록

| 자료 | 형식 | 기준 | 최종 확인 | 내용 |
| --- | --- | --- | --- | --- |
| [FlexBox 정렬 3형제](flexbox-alignment.html) | HTML | Compose Multiplatform `1.12.0` | 2026-08-27 | `justifyContent`, `alignItems`, `alignContent`의 차이를 값을 바꿔 가며 확인하는 인터랙티브 문서 |
| [M3 Expressive 컴포넌트 조사](m3-expressive-component.md) | Markdown | `org.jetbrains.compose.material3:material3` `1.12.0-alpha03` | 2026-08-24 | 저장소가 쓰는 Material 3 버전에서 호출할 수 있는 컴포넌트 목록과 채택·미채택 이유 |
| [Compose Styles API 조사](compose-styles.md) | Markdown | `org.jetbrains.compose.foundation:foundation` `1.12.0` · `material3` `1.12.0-alpha03` | 2026-09-19 | Styles API로 할 수 있는 것과 이 버전에서 되지 않는 것, 저장소의 채택·미채택 이유 |
| [DiaryMap 구조](diary-map-architecture.html) | HTML | `compose:map` 모듈 · 커밋 `2796dc33` | 2026-08-24 | `DiaryMapState`의 상태 관리와 네이버·Google 지도가 Android·iOS·JVM·wasmJs에 붙는 방식 |

## 최신 상태 유지

이 폴더의 자료는 특정 시점의 라이브러리나 저장소 코드를 보고 적은 것이라, 그 대상이 바뀌면 내용이 사실이 아니게 될 수 있다. 자료마다 다음 두 값을 자료 안에 적고, 위 목록에도 같은 값을 옮긴다.

- **기준**: 내용을 확인한 대상. 외부 라이브러리를 다루는 자료는 라이브러리와 버전을 적고, 버전 값은 `gradle/libs.versions.toml`이 소유한다. 저장소 코드를 다루는 자료는 확인한 커밋을 적는다.
- **최종 확인**: 자료의 내용이 그 기준에서 그대로임을 마지막으로 확인한 날짜.

카탈로그의 버전이나 대상 코드가 자료의 기준과 달라지면 그 자료는 검토 대상이다. 다시 확인해 내용이 같으면 기준과 최종 확인만 갱신하고, 달라지면 내용까지 고친다.

날짜 하나만으로는 자료가 낡았는지 판정할 수 없다. 낡음을 만드는 것은 버전이고 날짜는 언제 판정했는지만 알려 주므로 두 값을 함께 적는다. git 로그도 최종 확인을 대신하지 못한다. 로그는 파일을 고친 시점만 알려 주고, 고칠 필요가 없다고 확인한 시점은 남기지 않는다.

## FlexBox 정렬 3형제

브라우저에서 `docs/reference/flexbox-alignment.html`을 열면 된다. 별도 실행 환경 없이 파일 하나로 동작한다.

정렬 속성이 3개인 이유를 축(main·cross)과 대상(아이템·줄)의 조합으로 설명하고, 각 속성이 실제로 효과를 내는 조건을 배치 결과에서 역산해 표시한다. 현재 설정에 해당하는 `FlexBox(config = { ... })` 코드도 함께 보여준다.

`FlowRow 기본`, `DiaryFlexBox(현재)`, `alignContent Center 버그`, `Column 방향` 프리셋으로 이 저장소의 실제 사용처 설정과 쓰지 않기로 한 설정을 재현할 수 있다.

`alignContent`가 cross 축 min 제약이 없을 때 줄 묶음을 위로 밀어내는 동작도 함께 다룬다. `1.12.0`에서도 같으며, 이 저장소가 `alignContent(Start)`로 고정한 근거다.

## DiaryMap 구조

브라우저에서 `docs/reference/diary-map-architecture.html`을 열면 된다. 별도 실행 환경 없이 파일 하나로 동작한다.

`compose:map`을 세 층(배치·전환, 상태·계약, SDK 연동)으로 나눠 설명하고, `DiaryMapState`의 상태별 공개 범위와 지도·앱 사이의 두 방향 흐름, 저장·복원 항목을 정리한다. 플랫폼 탭으로 Android·iOS·JVM·wasmJs의 네이버·Google 구현을 나란히 보여주고, 같은 동작을 두 SDK에서 어떻게 얻는지 대조표로 둔다.

제품 관점의 동작과 정책은 [스펙](../spec/diary-map.md)이, 화면 표현은 [디자인](../design/diary-map.md)이 소유한다. 이 자료는 구현 구조만 다루고, 스펙과 어긋나면 이 자료를 고친다.
