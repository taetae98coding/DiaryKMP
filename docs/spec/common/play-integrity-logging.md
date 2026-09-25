# Play Integrity 판정 기록 스펙

이 문서는 Play Integrity 판정을 확인할 때 앱과 서버가 주고받는 정보와 판정 결과의 의미를 다룬다.

앱이 언제 확인하고 판정 결과를 원격 분석 로그로 어떻게 남기는지는 [Play Integrity 판정 기록 스펙(client)](../client/play-integrity-logging.md)이, 서버가 Google에 판정 결과를 받아 오는 처리는 [Play Integrity 판정 기록 스펙(server)](../server/play-integrity-logging.md)이 소유한다.

## domain

### 무결성 확인 정보

무결성 확인 정보는 앱이 Google Play에 요청해 받는 값으로, 이 앱과 기기의 상태에 대한 Google의 판정을 Google의 키로 암호화해 담고 있다. 앱은 이 값을 풀 수 없고, 서버를 거쳐 Google이 푼다.

앱은 요청할 때마다 새로 만든 무작위 요청 확인 값을 함께 알리고, Google은 그 값을 판정 결과에 그대로 담아 돌려준다. 요청 확인 값은 사용자나 기기를 특정할 수 있는 값으로 만들지 않는다.

### 판정 결과

판정 결과는 Google이 정한 항목을 여러 단계로 묶어 담는다. 담기는 항목은 Google이 정하며, 주요 항목은 다음과 같다. Play Console에서 켜야 제공되는 항목은 켜진 경우에만 담긴다.

| 묶음 | 담기는 항목 |
| --- | --- |
| 요청 정보 | 요청한 앱의 패키지 이름, 요청 확인 값, 요청 시각 |
| 앱 무결성 | Google Play가 이 앱 바이너리를 인식하는지와, 인식한 앱의 패키지 이름·인증서 지문·버전 |
| 기기 무결성 | 기기가 통과한 무결성 판정 목록, 기기의 Android 버전과 최근 한 시간 동안의 요청 수준 (뒤의 두 항목은 Play Console에서 켜야 제공) |
| 계정 정보 | Google Play에서 정식으로 받은 사용자인지 |
| 환경 정보 | 화면을 캡처하거나 제어하는 앱의 감지 결과, Play Protect 상태 (Play Console에서 켜야 제공) |

기기가 무결성 판정을 하나도 통과하지 못하면 Google은 기기 무결성 판정 목록을 비워서 보낸다.

## data

### 판정 결과 요청

| 방향 | 담는 정보 |
| --- | --- |
| 앱 → 서버 | 무결성 확인 정보, 앱의 패키지 이름 |
| 서버 → 앱 | Google이 돌려준 판정 결과 전체를 구조 그대로 |

요청에는 로그인 세션이 필요하지 않다. 서버가 판정 결과를 돌려주지 못하면 판정 결과 없이 실패를 돌려준다. 판정 결과를 한 단계 목록으로 바꾸는 것은 앱이 한다.

## 참고

- [Play Integrity 판정 결과](https://developer.android.com/google/play/integrity/verdicts)
- [Play Integrity 표준 요청](https://developer.android.com/google/play/integrity/standard)
