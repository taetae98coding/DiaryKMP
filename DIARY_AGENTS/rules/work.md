# Work 규칙

`work:*`가 어느 계층에 속하고 무엇을 주입받는지, 실행 수단을 어느 소스셋에 두는지는 [data.md](data.md)가 소유한다. 이 문서는 플랫폼 실행 수단(`WorkManager`, `BGTaskScheduler`, 코루틴 예약기)을 **어떻게 쓰는지**만 소유한다.

## 작업 내용은 던지고 실행 수단이 받는다

`commonMain`의 작업 내용(`SyncWork.doWork()`)은 실패를 예외로 끝낸다. 실패를 어디에 보고할지도 작업 내용이 정한다. 실패 보고는 스펙이 플랫폼과 무관하게 한 번으로 정한 제품 동작이므로, 실행 수단마다 따로 보고하면 플랫폼 수만큼 갈라진다.

플랫폼 실행 수단은 그 예외를 잡아 **그 플랫폼이 이해하는 결과**로 바꾼다. 실패가 어디로 가는지가 실행 수단 코드에 드러나야 하고, 실행 수단의 기본 동작에 기대지 않는다. 기본 동작이 실행 수단마다 달라서(`WorkManager`는 예외를 실패로 바꾸지만, `BGTask`는 완료를 알리지 않으면 만료까지 매달리고, 코루틴은 스코프를 죽인다) 한 플랫폼에서만 성립하는 생략을 두면 다른 플랫폼 어댑터를 쓸 때 빠뜨린다.

| 실행 수단 | 실패의 결과 |
| --- | --- |
| `CoroutineWorker` | `Result.failure()` |
| `BGTask` | `setTaskCompletedWithSuccess(false)` |
| 즉시 실행 코루틴 예약기 | 상태를 `NONE`으로 정리하고, 예외는 전용 스코프의 `CoroutineExceptionHandler`가 받는다 |
| 주기 코루틴 예약기 | 한 주기의 실패를 삼키고 다음 주기를 기다린다 |

`CancellationException`은 어디서든 다시 던진다. 취소는 실패가 아니고, 삼키면 취소 후 재시작이 이전 작업을 끝내지 못한다.

`Result.retry()`와 `BackoffPolicy`는 쓰지 않는다. 스펙([데이터 동기화](../../docs/spec/data-sync.md)의 `동기화 계기`)은 같은 계기 안에서 다시 시도하지 않고 다음 계기에 맡기도록 정했다. 재시도를 플랫폼 실행 수단에 두면 이 규칙이 플랫폼마다 달라진다.

⚠️ 비권장 예시:

```kotlin
// 예외를 WorkManager가 실패로 바꿔 주는 데 기댄다. 같은 작업을 BGTask나 코루틴으로 옮기면 실패가 어디로 가는지 정해지지 않는다.
override suspend fun doWork(): Result {
    work.doWork()

    return Result.success()
}
```

✅ 권장 예시:

```kotlin
override suspend fun doWork(): Result =
    try {
        work.doWork()
        Result.success()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Throwable) {
        Result.failure()
    }
```

## 예약은 하나로 유지한다

같은 작업의 예약은 어느 플랫폼에서든 하나만 남는다. 앱 시작, 계정 확인, 설정 변경 같은 계기마다 다시 예약해도 예약이 늘거나 실행 시각이 앞당겨지지 않아야 한다. ViewModel이 예약 요청을 삼키지 않아도 되는 근거가 이 규칙이므로([viewmodel.md](viewmodel.md)의 `ViewModel 시작 트리거`), 실행 수단이 이를 보장한다.

- `WorkRequest`는 모두 고유 이름으로 넣는다. 이름은 그 모듈의 상수로 두고, 주기 작업과 즉시 작업은 서로 다른 이름을 쓴다. 스펙이 주기 동기화와 다른 계기의 동기화가 서로 취소하지 않는다고 정했기 때문이다.
- 즉시 실행은 `ExistingWorkPolicy.REPLACE`로 넣는다. 진행 중인 작업을 취소하고 새로 시작하는 스펙의 취소 후 재시작 규칙이다.
- 주기 예약은 `ExistingPeriodicWorkPolicy.KEEP`으로 넣는다. 이미 예약이 있으면 그대로 두어야 앱을 다시 시작해도 예약이 하나로 유지되고 발생 시각이 앞당겨지지 않는다.
- 다른 실행 수단도 같은 의미를 구현한다. iOS는 대기 중인 `BGTaskRequest`가 있으면 다시 제출하지 않고(다시 제출하면 `earliestBeginDate`가 뒤로 밀린다), 코루틴 예약기는 활성 `Job`이 있으면 요청을 무시한다. 즉시 실행 코루틴 예약기는 이전 `Job`을 취소하고 새로 시작해 `REPLACE`와 같은 의미를 갖는다.

## 제약은 스펙의 플랫폼 표를 그대로 옮긴다

`Constraints`와 `BGTaskRequest`의 조건에는 스펙이 플랫폼별 실행 조건으로 정한 것만 둔다. 동기화는 Android `NetworkType.CONNECTED`, iOS `requiresNetworkConnectivity`이고, 알림 예약은 제약이 없다. 충전 중, 유휴 상태처럼 스펙에 없는 제약을 편의로 붙이지 않는다. 실행 조건은 제품 정책이라 스펙이 먼저 바뀌어야 한다.

즉시 동기화는 `setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)`로 넣는다. 스펙의 `즉시 실행 요청`이 즉시 실행이 허용되지 않아 요청이 사라지는 경우는 없다고 정했으므로 `DROP_WORK_REQUEST`는 쓰지 않는다. 주기 작업에는 즉시 실행을 요청하지 않는다.

## iOS `BGTaskScheduler`

- 작업 식별자는 앱 시작 시 한 번 등록한다. 시스템은 앱이 실행을 마치기 전에 등록된 식별자만 깨울 수 있다. 등록 콜백 안에서 실행 수단을 Koin으로 지연 조회한다. 시스템이 깨운 시점에야 동기화 수단이 필요하기 때문이다.
- 한 요청은 한 번만 실행되므로, 작업을 실행하기 **전에** 다음 주기를 다시 제출한다. 이번 실행의 성패나 만료와 무관하게 예약이 이어져야 한다.
- 다시 제출할 간격은 기기에 남긴다(`NSUserDefaults`). 시스템이 앱을 깨운 시점에는 예약을 요청한 코드가 살아 있지 않다.
- `expirationHandler`에서 실행 중인 `Job`을 취소하고 실패로 완료를 알린다. 만료까지 완료를 알리지 않으면 시스템이 앱을 종료할 수 있다.
- 제출이 거절되는 환경(시뮬레이터)은 실패로 다루지 않고 다음 계기에서 다시 예약한다.

## JVM 데스크톱·웹 코루틴 예약기

- 작업마다 전용 `CoroutineScope`를 Koin `@Single`로 둔다. `SupervisorJob`과 빈 `CoroutineExceptionHandler`를 붙여 한 작업의 실패가 다른 작업이나 앱을 죽이지 않게 한다. 화면 수명과 무관해야 하므로 ViewModel의 스코프를 쓰지 않는다.
- 주기 예약기는 한 주기의 실패를 잡고 다음 주기를 계속 기다린다. 스펙이 주기 동기화가 실패해도 예약은 해제되지 않는다고 정했다.
- 즉시 실행 예약기는 취소된 이전 작업의 정리 코드가 새 작업의 상태를 덮지 않도록, 상태를 정리하기 전에 자기 작업이 최신인지 확인한다.
