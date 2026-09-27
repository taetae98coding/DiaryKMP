# Play Integrity 판정 기록 스펙

이 문서는 Android 앱이 활성 상태가 될 때마다 Google Play Integrity 판정을 받아 그 결과를 원격 분석 로그로 남기는 정책을 다룬다. [앱 로깅](./app-logging.md)의 공통 창구와 기록 수단 체계 위에서 동작한다.

앱과 서버가 주고받는 정보와 판정 결과의 의미는 [Play Integrity 판정 기록 스펙(common)](../common/play-integrity-logging.md)이, 서버가 Google에 판정 결과를 받아 오는 처리는 [Play Integrity 판정 기록 스펙(server)](../server/play-integrity-logging.md)이 소유한다.

판정은 분석을 위해 기록만 하며, 판정 결과에 따라 사용자의 기능 사용을 막거나 안내하지 않는다. 확인은 화면 없이 앱 내부에서 진행되고 사용자가 조작하는 수단이 없으므로 `feature` 영역을 생략한다.

## domain

### 판정 확인

판정 확인은 앱이 Google Play에서 무결성 확인 정보를 받고, 서버가 그 정보를 Google에 보내 판정 결과를 돌려주는 한 번의 과정이다.

확인이 성공하면 판정 결과 하나가 원격 분석 로그로 남는다.

```mermaid
flowchart TD
    Trigger["확인 계기"] --> Platform{"Android인가"}
    Platform -- 아니오 --> Skip["확인하지 않음"]
    Platform -- 예 --> Token["Google Play에<br/>무결성 확인 정보 요청"]
    Token -- 실패 --> End["로그 없이 끝"]
    Token -- 성공 --> Server["서버에 판정 결과 요청"]
    Server -- 실패 --> End
    Server -- 성공 --> Log["판정 결과를<br/>원격 분석 로그로 남김"]
```

### 확인하는 기기

판정 확인은 Android에서만 한다. Play Integrity는 Google Play가 제공하는 Android 전용 수단이기 때문이다. iOS, 데스크톱 앱, 웹에서는 확인하지 않으며, 확인하지 않는다는 안내도 표시하지 않는다.

확인은 로그인 여부와 무관하다. 게스트도 사용자도 같은 계기에 같은 방식으로 확인한다.

Google Play에서 설치하지 않은 앱, Google Play 서비스가 없는 기기에서도 확인을 시도한다. 이런 환경에서는 무결성 확인 정보를 받지 못해 로그가 남지 않거나, 앱이나 기기를 인식하지 못했다는 판정이 그대로 남는다. 어느 쪽이든 분석 대상인 사실이다.

### 확인 계기

앱이 활성 상태가 될 때마다 확인한다. 앱이 시작되어 화면에 보이게 된 것과, 백그라운드에 있다가 다시 화면에 보이게 된 것이 모두 계기다. Android의 활성 상태 기준은 [데이터 동기화 앱 스펙](./data-sync.md)의 `동기화 계기`에 있는 플랫폼별 기준과 같다.

화면 회전처럼 화면이 재생성되어 다시 보이게 되는 것도 계기에 포함된다. 이때는 앱이 계속 보이고 있었어도 한 번 더 확인하고 로그가 남는다. 재생성은 흔하지 않고 판정 결과는 같은 기기에서 거의 달라지지 않아, 분석에 주는 영향보다 계기를 단순하게 두는 이점이 크기 때문이다. 앱 안에서 화면을 이동하는 것은 계기가 아니다.

마지막 확인 시각이나 결과를 기억하지 않는다. 계기마다 새로 확인하므로, 짧은 사이에 앱을 여러 번 열면 그 횟수만큼 확인하고 로그가 남는다.

Google Play에 확인을 요청할 준비는 처음 확인할 때 한 번 하고, 앱이 종료될 때까지 다시 쓴다. Google Play가 준비가 더 이상 유효하지 않다고 알리면 그 계기는 실패로 끝나고, 다음 계기에 다시 준비한다.

### 요청 확인 값

무결성 확인 정보를 요청할 때 담는 요청 확인 값은 확인마다 새로 만든 무작위 값이다. 사용자나 기기를 특정할 수 있는 값으로 만들지 않는다. 값의 뜻은 [Play Integrity 판정 기록 스펙(common)](../common/play-integrity-logging.md)의 `무결성 확인 정보`를 따른다.

### 계층 없는 전달

판정 결과는 항목이 여러 단계로 묶여 있지만, 로그에는 계층 없이 값 이름과 값의 한 단계 목록으로 전달한다. 원격 분석은 로그 하나의 값을 한 단계 목록으로만 집계할 수 있기 때문이다. 판정 결과의 구조는 [Play Integrity 판정 기록 스펙(common)](../common/play-integrity-logging.md)의 `판정 결과`를 따른다.

- 값 이름은 Google이 붙인 가장 안쪽 항목 이름을 소문자와 밑줄(`_`)로 이은 형태로 바꾼 것이다. 이름 안의 대문자는 앞에 밑줄을 넣고 소문자로 바꾼다. 예: Play Protect 상태 → `play_protect_verdict`
- 서로 다른 묶음에 같은 이름의 항목이 있으면, 겹치는 항목끼리 바로 위 묶음 이름을 앞에 밑줄로 붙이고 이름이 모두 달라질 때까지 반복한다.
- 여러 값을 가진 항목은 받은 순서대로 쉼표(`,`)로 이어 문자열 하나로 만든다. 예: `MEETS_BASIC_INTEGRITY,MEETS_DEVICE_INTEGRITY`
- 숫자는 숫자로, 문자열은 문자열로, 참·거짓은 `true`·`false` 문자열로 전달한다.
- 값이 비어 있는 항목(빈 목록, 아래 항목이 하나도 없는 묶음, 값이 없음으로 온 항목)은 담지 않는다. 기기 무결성 판정을 하나도 통과하지 못하면 기기 무결성 판정 목록이 비어 오므로, 이 경우 로그에 `device_recognition_verdict`가 없다.

현재 판정 결과에서 만들어지는 주요 값 이름은 다음과 같다.

| 값 이름 | 뜻 |
| --- | --- |
| `request_package_name`, `request_hash`, `timestamp_millis` | 요청한 앱, 요청 확인 값, 요청 시각 |
| `app_recognition_verdict`, `package_name`, `certificate_sha256_digest`, `version_code` | Google Play가 이 앱 바이너리를 인식하는지와 인식한 앱 정보 |
| `device_recognition_verdict` | 기기가 통과한 무결성 판정 목록 |
| `sdk_version`, `device_activity_level` | 기기의 Android 버전, 최근 한 시간 동안의 요청 수준 |
| `app_licensing_verdict` | Google Play에서 정식으로 받은 사용자인지 |
| `apps_detected`, `play_protect_verdict` | 화면을 캡처·제어하는 앱 감지 결과, Play Protect 상태 |

### 원격 분석 로그

판정 결과는 원격 분석 로그 하나로 남고, 사건 종류는 `play_integrity`다. 로그의 값은 `계층 없는 전달`로 만든 목록 전체다.

원격 분석이 한 사건에 받을 수 있는 값의 개수, 이름 길이, 값 길이에는 한도가 있다. 한도를 넘는 부분은 한도에 맞게 잘라 남기며, 자른 것 때문에 로그 전체가 남지 않는 일은 없다.

이 로그는 요청 시각, 요청 확인 값, 인증서 지문처럼 미리 정해진 고정값이 아닌 값을 담는다. 원격 분석의 고정값 제한에 대한 예외이며, 그 근거는 [앱 로깅](./app-logging.md)의 `원격 분석 기록`이 소유한다. 무결성 확인 정보 자체는 로그에 담지 않는다.

등록 정책과 플랫폼 제약은 [앱 로깅](./app-logging.md)의 `원격 분석 기록`을 따른다. 콘솔 기록 수단이 등록된 빌드에서는 같은 로그가 콘솔에도 남는다.

### 실패 처리

다음 경우에는 그 계기의 로그를 남기지 않고 끝낸다.

- Google Play 서비스가 없거나, 네트워크가 없거나, Google의 하루 요청 한도를 넘어 무결성 확인 정보를 받지 못했을 때
- 서버에 연결하지 못했거나 서버가 판정 결과를 돌려주지 못했을 때

실패해도 사용자에게 알리지 않고 앱의 다른 기능은 그대로 진행된다. 같은 계기 안에서 다시 시도하지 않으며, 다음 계기에 새로 확인한다.

실패는 오류 보고 대상이 아니다. 기기 환경 때문에 확인할 수 없는 것은 앱의 오류가 아니기 때문이다. 콘솔 기록은 [기능 실패 로깅](./usecase-failure-logging.md)을 따른다.

### 실행 경계

확인을 시작한 뒤 앱이 백그라운드로 가거나 화면이 재생성되어도 확인은 이어서 진행하고, 끝나면 로그를 남긴다. 앱 프로세스가 종료되어 확인이 중단되면 그 계기의 로그는 남지 않으며, 실패로 보지 않는다.

## data

### 판정 결과 요청

앱은 받은 무결성 확인 정보와 앱의 패키지 이름을 서버에 보내고, 서버가 돌려준 판정 결과를 `계층 없는 전달`에 따라 바꾼다. 주고받는 정보는 [Play Integrity 판정 기록 스펙(common)](../common/play-integrity-logging.md)의 `판정 결과 요청`을 따른다. 요청은 로그인 세션이 없어도 보낸다.

기기에는 무결성 확인 정보와 판정 결과를 저장하지 않는다. 판정 결과는 계정 데이터가 아니므로 [데이터 동기화 스펙](../common/data-sync.md)의 동기화 대상이 아니다.

## 참고

- [Play Integrity 개요](https://developer.android.com/google/play/integrity/overview)
- [Play Integrity 판정 결과](https://developer.android.com/google/play/integrity/verdicts)
- [Play Integrity 표준 요청](https://developer.android.com/google/play/integrity/standard)
- [Google Analytics 4 이벤트 수집 한도](https://support.google.com/analytics/answer/9267744)
