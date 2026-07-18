# UseCase 실패 로깅 테스트 케이스

기준 스펙: [UseCase 실패 로깅 스펙](../spec/usecase-failure-logging.md)

## domain

### TC-USECASE-FAILURE-LOGGING-DOMAIN-001: 한 번 실행하는 작업이 실패하면 콘솔에 실패 로그가 남는다

- 근거: `domain > 콘솔 기록`
- Given: 콘솔 기록 수단이 등록되어 있다.
- When: 한 번 실행하고 끝나는 도메인 작업이 실패한다.
- Then: 실패한 작업의 이름이 태그로, 실패 원인이 오류 정보로 개발 콘솔에 남는다.

### TC-USECASE-FAILURE-LOGGING-DOMAIN-002: 지속 관찰하는 작업이 실패하면 콘솔에 실패 로그가 남는다

- 근거: `domain > 콘솔 기록`
- Given: 콘솔 기록 수단이 등록되어 있다.
- When: 지속 관찰하는 도메인 작업이 실패한다.
- Then: 실패한 작업의 이름이 태그로, 실패 원인이 오류 정보로 개발 콘솔에 남는다.

### TC-USECASE-FAILURE-LOGGING-DOMAIN-003: 같은 작업이 여러 번 실패하면 실패할 때마다 로그가 남는다

- 근거: `domain > 도메인 작업 실패`
- Given: 콘솔 기록 수단이 등록되어 있다.
- When: 같은 도메인 작업이 두 번 실패한다.
- Then: 개발 콘솔에 실패 로그가 두 번 남는다.

### TC-USECASE-FAILURE-LOGGING-DOMAIN-004: 성공한 작업은 실패 로그를 남기지 않는다

- 근거: `domain > 도메인 작업 실패`
- Given: 콘솔 기록 수단이 등록되어 있다.
- When: 도메인 작업이 성공한다.
- Then: 개발 콘솔에 실패 로그가 남지 않는다.

### TC-USECASE-FAILURE-LOGGING-DOMAIN-005: 중단된 작업은 실패 로그를 남기지 않는다

- 근거: `domain > 도메인 작업 실패`
- Given: 콘솔 기록 수단이 등록되어 있다.
- When: 진행 중이던 도메인 작업이 중단(취소)된다.
- Then: 개발 콘솔에 실패 로그가 남지 않는다.

### TC-USECASE-FAILURE-LOGGING-DOMAIN-006: 실패 로그에 작업 입력 값이 담기지 않는다

- 근거: `domain > 실패 로그 내용`
- Given: 콘솔 기록 수단이 등록되어 있다.
- When: 특정 문자열을 입력 값으로 받은 도메인 작업이 실패한다.
- Then: 개발 콘솔에 남은 실패 로그에 그 입력 값이 포함되지 않는다.

### TC-USECASE-FAILURE-LOGGING-DOMAIN-007: 실패 로그를 남겨도 실패는 기존과 동일하게 전달된다

- 근거: `domain > 도메인 작업 실패`
- Given: 콘솔 기록 수단이 등록되어 있다.
- When: 도메인 작업이 특정 원인으로 실패한다.
- Then: 호출한 쪽은 그 원인을 담은 실패 결과를 그대로 받는다.

### TC-USECASE-FAILURE-LOGGING-DOMAIN-008: 기록 수단이 없어도 실패는 기존과 동일하게 전달된다

- 근거: `domain > 콘솔 기록`
- Given: 어떤 기록 수단도 등록되어 있지 않다.
- When: 도메인 작업이 특정 원인으로 실패한다.
- Then: 실패 로그는 어디에도 남지 않고, 호출한 쪽은 그 원인을 담은 실패 결과를 그대로 받는다.

### TC-USECASE-FAILURE-LOGGING-DOMAIN-009: 오류 보고 대상 작업이 실패하면 콘솔 로그와 오류 보고가 함께 남는다

- 근거: `domain > 오류 보고`
- Given: 콘솔 기록 수단과 오류 보고 기록 수단이 등록되어 있다.
- When: 오류 보고 대상인 도메인 작업이 특정 원인으로 실패한다.
- Then: 실패한 작업의 이름과 그 원인이 담긴 콘솔 로그가 남고, 같은 원인을 담은 오류 보고도 함께 남는다.

### TC-USECASE-FAILURE-LOGGING-DOMAIN-010: 오류 보고 대상이 아닌 작업의 실패는 콘솔 로그로만 남는다

- 근거: `domain > 오류 보고`
- Given: 콘솔 기록 수단과 오류 보고 기록 수단이 등록되어 있다.
- When: 오류 보고 대상이 아닌 도메인 작업이 실패한다.
- Then: 콘솔 로그만 남고 오류 보고는 남지 않는다.

### TC-USECASE-FAILURE-LOGGING-DOMAIN-011: 오류 보고 대상 작업이 중단되면 아무 로그도 남지 않는다

- 근거: `domain > 오류 보고`, `domain > 도메인 작업 실패`
- Given: 콘솔 기록 수단과 오류 보고 기록 수단이 등록되어 있다.
- When: 진행 중이던 오류 보고 대상 작업이 중단(취소)된다.
- Then: 콘솔 로그와 오류 보고가 모두 남지 않는다.

### TC-USECASE-FAILURE-LOGGING-DOMAIN-012: 오류 보고를 남겨도 실패는 기존과 동일하게 전달된다

- 근거: `domain > 오류 보고`, `domain > 도메인 작업 실패`
- Given: 콘솔 기록 수단과 오류 보고 기록 수단이 등록되어 있다.
- When: 오류 보고 대상 작업이 특정 원인으로 실패한다.
- Then: 호출한 쪽은 그 원인을 담은 실패 결과를 그대로 받는다.
