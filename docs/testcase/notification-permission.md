# 알림 권한 요청 테스트 케이스

기준 스펙: [알림 권한 요청 스펙](../spec/notification-permission.md)

## feature

### TC-NOTIFICATION-PERMISSION-FEATURE-001: 알림 권한이 결정되어 있지 않으면 앱 시작 시 시스템 알림 권한 요청이 시작된다

- 근거: `feature > 앱 시작 시 알림 권한 요청`
- Given: 알림 권한 요청을 제공하는 환경에서 알림 권한이 아직 결정되어 있지 않고, 앱이 시작될 준비가 되어 있다.
- When: 앱이 시작된다.
- Then: 시스템 알림 권한 요청이 한 번 시작된다.

### TC-NOTIFICATION-PERMISSION-FEATURE-002: 알림 권한이 이미 허용되어 있으면 요청이 시작되지 않는다

- 근거: `domain > 요청 기준`
- Given: 알림 권한이 이미 허용되어 있고, 앱이 시작될 준비가 되어 있다.
- When: 앱이 시작된다.
- Then: 시스템 알림 권한 요청이 시작되지 않는다.

### TC-NOTIFICATION-PERMISSION-FEATURE-003: 요청에 어떻게 응답해도 앱 화면이 유지되고 별도 안내가 표시되지 않는다

- 근거: `feature > 앱 시작 시 알림 권한 요청`
- Given: 알림 권한이 결정되어 있지 않아 앱 시작으로 시스템 알림 권한 요청이 시작되었고, 요청 응답이 정해져 있다.
- When: 사용자가 알림 권한 요청에 응답한다.
- Then: 앱 화면이 그대로 유지되고 사용자에게 별도 안내가 표시되지 않는다.
- 테스트 데이터:

  | 요청 응답 |
  | --- |
  | 허용 |
  | 거부 |

## domain

스펙 `domain > 요청 기준`이 정의한 경로와 각 경로를 덮는 케이스는 다음과 같다.

```mermaid
flowchart TD
    Start["앱 시작"] --> Support{"알림 권한 요청을<br/>제공하는 환경인가"}
    Support -- 아니오 --> Denied["거부로 처리<br/>TC-NOTIFICATION-PERMISSION-DOMAIN-001"]
    Support -- 예 --> Granted{"이미 허용되어 있는가"}
    Granted -- 예 --> Skip["요청하지 않고 허용으로 처리<br/>TC-NOTIFICATION-PERMISSION-FEATURE-002"]
    Granted -- 아니오 --> Request{"시스템이 요청을 표시하는가"}
    Request -- 아니오 --> NotShown["거부로 처리<br/>TC-NOTIFICATION-PERMISSION-DOMAIN-005"]
    Request -- 예 --> Answer["사용자 응답에 따라 처리<br/>TC-NOTIFICATION-PERMISSION-FEATURE-001<br/>TC-NOTIFICATION-PERMISSION-FEATURE-003"]
```

### TC-NOTIFICATION-PERMISSION-DOMAIN-001: 알림 권한 요청을 제공하지 않는 환경에서는 요청 없이 거부로 처리한다

- 근거: `domain > 요청 제공 범위`
- Given: 알림 권한 요청을 제공하지 않는 환경이다.
- When: 앱이 알림 권한을 요청한다.
- Then: 시스템 알림 권한 요청이 시작되지 않고 거부로 처리된다.

### TC-NOTIFICATION-PERMISSION-DOMAIN-002: 앱이 실행되는 동안에는 요청 조건을 다시 확인하지 않는다

- 근거: `domain > 요청 시점`
- Given: 알림 권한이 결정되어 있지 않아 앱 시작으로 시스템 알림 권한 요청이 한 번 시작되었다.
- When: 앱을 다시 시작하지 않은 채 실행 상태가 바뀐다.
- Then: 시스템 알림 권한 요청이 다시 시작되지 않는다.
- 테스트 데이터:

  | 실행 상태 변화 |
  | --- |
  | 화면 재구성 |
  | 백그라운드로 갔다가 다시 앞으로 돌아옴 |

### TC-NOTIFICATION-PERMISSION-DOMAIN-003: 앱 화면이 처음부터 다시 시작되면 요청 조건을 다시 확인한다

- 근거: `domain > 요청 시점`
- Given: 알림 권한이 아직 결정되어 있지 않고, 앱 화면이 시작되어 시스템 알림 권한 요청이 한 번 시작되었다.
- When: 앱 화면이 재생성되어 처음부터 다시 시작된다.
- Then: 시스템 알림 권한 요청이 다시 한 번 시작된다.

### TC-NOTIFICATION-PERMISSION-DOMAIN-004: 웹에서 브라우저가 알림 기능을 제공하지 않으면 요청 없이 거부로 처리한다

- 근거: `domain > 요청 제공 범위`
- Given: 브라우저가 알림 기능을 제공하지 않는 웹 환경이다.
- When: 앱이 알림 권한을 요청한다.
- Then: 시스템 알림 권한 요청이 시작되지 않고 거부로 처리된다.
- 작성하지 않는 이유: 브라우저가 알림 기능을 제공하지 않는 상황은 실제 브라우저에서만 재현되고, 현재 테스트 환경에는 웹 실행 환경이 없다. 브라우저의 알림 기능 제공 여부를 제어할 수 있는 테스트 환경이 제공되면 자동화한다.

### TC-NOTIFICATION-PERMISSION-DOMAIN-005: 시스템이 더 이상 요청을 표시하지 않으면 거부와 같이 처리한다

- 근거: `domain > 요청 기준`
- Given: 사용자가 이전에 요청을 거부해 시스템이 더 이상 알림 권한 요청을 표시하지 않는 상태다.
- When: 앱이 알림 권한을 요청한다.
- Then: 사용자에게 요청이 표시되지 않고 거부로 처리된다.
- 작성하지 않는 이유: 요청을 더 이상 표시하지 않는 판단은 각 플랫폼의 권한 정책이 소유하며, 현재 테스트 환경은 실제 플랫폼의 권한 상태를 그 상태로 만들 수 없다. 플랫폼의 권한 표시 여부를 제어할 수 있는 테스트 환경이 제공되면 자동화한다.

### TC-NOTIFICATION-PERMISSION-DOMAIN-006: 알림을 화면에 표시하는 권한만 요청한다

- 근거: `domain > 요청 범위`
- Given: 알림 권한이 결정되어 있지 않은 환경이다.
- When: 앱이 알림 권한을 요청한다.
- Then: 알림을 화면에 표시하는 권한만 요청하고 소리, 배지 권한은 요청하지 않는다.
- 작성하지 않는 이유: 어떤 알림 방식을 요청했는지는 플랫폼 권한 요청 화면에서만 관찰할 수 있고, 현재 테스트 환경은 실제 플랫폼의 권한 요청을 실행하지 않는다. 요청한 알림 방식을 확인할 수 있는 테스트 환경이 제공되면 자동화한다.
