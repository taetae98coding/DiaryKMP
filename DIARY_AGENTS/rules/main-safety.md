# Main-safety 규칙

블로킹 작업을 어디서 메인 스레드 밖으로 옮기는지(`withContext`)와, 옮길 `CoroutineDispatcher`·`CoroutineScope`를 어떻게 받는지를 소유한다.

근거는 Android의 [코루틴 권장사항](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)과 [도메인 레이어의 Threading](https://developer.android.com/topic/architecture/domain-layer#threading)이다. `suspend` 함수는 메인 스레드에서 불러도 안전해야 하고(main-safe), 블로킹 작업을 하는 클래스가 그 작업을 직접 옮긴다. 그래야 부르는 쪽이 어떤 dispatcher를 써야 하는지 신경 쓰지 않는다. 변경 가능한 상태를 노출하지 않는 일은 [viewmodel.md](viewmodel.md)가, `CancellationException`을 삼키지 않는 일은 [work.md](work.md)·[data.md](data.md)가 소유한다.

## 경계는 dispatcher를 주입받는 컴포넌트가 긋는다

**`withContext`는 다음 두 조건을 모두 만족하는 곳에만 둔다.**

1. dispatcher를 생성자로 주입받는 컴포넌트다. Koin이 관리하는 DataSource, Repository, Manager, Provider, Transport, 변환기가 여기 속한다. UseCase는 아래 `UseCase는 다시 옮기지 않는다`의 예외일 때만 속한다. 그 컴포넌트가 주입받은 dispatcher를 생성자로 넘겨 만드는 보조 class(플랫폼 구현을 나눠 맡는 `JavaFileLocalDataSource` 등)와, Composable이 `remember`로 만들고 `koinInject`로 받은 dispatcher를 생성자로 넘기는 보조 class(파일·사진 선택기, 자격 증명 요청, 카메라 등)도 포함한다.
2. 그 컴포넌트가 직접 하거나, 주입받거나 만든 객체로 하는 작업이 호출한 스레드를 막을 수 있다. 파일·`ContentResolver` I/O, 프로세스 실행과 대기, 이미지 디코딩·인코딩, 행마다 하는 복호화나 여러 해의 날짜를 훑는 계산처럼 큰 CPU 작업, 응답을 기다리는 동기 시스템 호출이 그렇다.

컴포넌트는 블로킹 작업을 감싼 `suspend` 함수를 내보내 main-safe한 경계가 된다. Repository, UseCase, ViewModel, Compose effect, `coroutineScope`는 그 함수를 부르기만 하고 다시 옮기지 않는다. 블로킹이 일어나는 가장 낮은 컴포넌트에서 한 번 옮기면 위의 모든 호출 경로가 main-safe해진다.

### UseCase는 다시 옮기지 않는다

UseCase는 기본적으로 `withContext`를 두지 않는다. UseCase가 부르는 Repository·Manager·Provider가 이미 main-safe하므로 UseCase도 main-safe하다.

예외로, 다른 계층에 둘 수 없는 domain 정책 계산이 무거우면 UseCase가 주입받은 dispatcher로 그 계산만 옮긴다. 정책의 결론은 domain이 소유하므로([domain.md](domain.md)) 계산을 data로 내리지 않는다. 다만 Android 가이드가 권하듯, 결과를 여러 화면에서 다시 쓰려고 저장하는 계산이면 data 계층에 두는 것을 먼저 검토한다. 예: 여러 해의 날짜와 연차 조합을 모두 비교하는 `GetGoldenHolidayUseCase`.

다음에도 `withContext`를 두지 않는다.

- Composable, `LaunchedEffect`·`produceState` 같은 effect, ViewModel. 블로킹 작업은 위의 보조 class나 컴포넌트로 옮기고 그 `suspend` 함수를 부른다.
- 컴포넌트가 아닌 내부 객체. 라이브러리가 대신 부르는 객체(Ktor의 `OutgoingContent.writeTo` 등)는 그 라이브러리가 정한 스레드에서 실행된다. dispatcher를 인자로 넘기지 않는다.
- 블로킹 수준이 아닌 작업. 이미 있는 디렉터리 확인, 한 번 읽어 기억하는 작은 표, 브라우저에 넘기기만 하는 호출처럼 몇 ms면 끝나는 작업은 옮기지 않는다. 옮기려고 구조나 프레임워크 기본값을 바꾸지 않는다.

⚠️ 비권장 예시 — effect가 직접 옮긴다:

```kotlin
LaunchedEffect(session, dispatcher) {
    withContext(dispatcher) { session.startRunning() }
}
```

✅ 권장 예시 — 보조 class가 경계를 긋고 effect는 부르기만 한다:

```kotlin
// 입력을 붙이며 카메라 장치를 열 때와 startRunning·stopRunning이 끝날 때까지 호출한 스레드가 막히므로 메인 스레드 밖에서 한다.
internal class QrScanCamera(
    private val dispatcher: CoroutineDispatcher,
) {
    suspend fun start(session: AVCaptureSession) {
        withContext(dispatcher) { session.startRunning() }
    }
}

LaunchedEffect(session, camera) {
    camera.start(session = session)
}
```

⚠️ 비권장 예시 — Koin이 관리하지 않는 내부 객체에 dispatcher를 넘긴다. `writeTo`는 Ktor 엔진이 이미 IO dispatcher에서 부른다:

```kotlin
internal class RawSourceContent(
    private val openContent: suspend () -> RawSource,
    private val dispatcher: CoroutineDispatcher,
) : OutgoingContent.WriteChannelContent() {
    override suspend fun writeTo(channel: ByteWriteChannel) {
        withContext(dispatcher) { /* ... */ }
    }
}
```

## 긴 반복은 취소를 확인한다

코루틴 취소는 협력적이라, `withContext` 안의 블로킹 코드는 중단 지점이나 취소 확인을 만나기 전까지 계속 실행된다. 항목 수만큼 블로킹 작업을 반복하는 루프(파일 목록을 읽거나 DB 행마다 복호화하는 등)는 반복마다 `ensureActive()`로 취소를 확인한다. 화면을 떠나 취소되어도 남은 항목을 끝까지 처리하지 않게 하기 위해서다.

✅ 권장 예시:

```kotlin
withContext(dispatcher) {
    rowList.mapNotNull { row ->
        ensureActive()
        row.toEntity(key = key)
    }
}
```

## 호출한 쪽보다 오래 가는 작업은 주입받은 scope에서

호출한 쪽의 수명에 묶인 작업은 `suspend` 함수 안에서 `coroutineScope`·`supervisorScope`로 만든다. 화면을 떠나도 끝까지 이어야 하는 작업이나 플랫폼 콜백에서 시작하는 작업은 Koin 제공 함수가 만든 외부 `CoroutineScope`를 생성자로 주입받아 실행한다. `GlobalScope`를 쓰거나 클래스 안에서 `CoroutineScope(...)`를 직접 만들지 않는다. 그러면 테스트가 scope를 바꿔 넣을 수 없고, 예외 처리와 dispatcher를 한곳에서 정할 수 없다.

scope를 만드는 제공 함수는 `workCoroutineScope(dispatcher = ...)`처럼 dispatcher를 명시하고 qualifier(`SyncScope`, `FileUploadScope`)로 구분한다. 블로킹 작업은 scope에서 시작하더라도 컴포넌트가 주입받은 dispatcher의 `withContext`로 감싼다.

⚠️ 비권장 예시:

```kotlin
internal class BackgroundSessionFileUploadTransport(
    private val dispatcher: CoroutineDispatcher,
) : FileUploadTransport {
    private val fileScope = CoroutineScope(SupervisorJob() + dispatcher)
}
```

✅ 권장 예시:

```kotlin
internal class BackgroundSessionFileUploadTransport(
    private val dispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope,
) : FileUploadTransport {
    private fun onComplete(path: String) {
        scope.launch { withContext(dispatcher) { SystemFileSystem.delete(Path(path), mustExist = false) } }
    }
}
```

## Dispatcher 주입

**`Dispatchers.IO`·`Dispatchers.Default`를 직접 참조하지 않고, `CoroutineDispatcher`를 생성자로 주입받아 `withContext(dispatcher)`로 쓴다.** Composable이 만드는 보조 class는 qualifier를 public으로 두고 앱 모듈의 플랫폼 Koin 모듈이 공급하며, 그 class를 만드는 `rememberXxx()` 팩토리가 `koinInject`로 받아 생성자에 넘긴다. 컴포넌트가 만드는 보조 class는 그 컴포넌트가 주입받은 dispatcher를 그대로 넘긴다.

직접 참조하면 테스트가 dispatcher를 바꿔 넣을 수 없고, wasm처럼 `IO`가 없는 플랫폼에 다른 dispatcher를 줄 수 없다. 이 선택은 플랫폼 소스셋의 Koin 모듈이 소유한다.

1. 모듈의 `impl/di`(domain은 `di`)에 `internal` `@Qualifier` 어노테이션을 하나 둔다(`FileDispatcher`, `BrowserCookieDispatcher`, `DiarySettingDispatcher`, `HolidayDispatcher`). 하나의 모듈 안에서는 dispatcher 하나를 공유한다. Composable이 만드는 보조 class의 qualifier는 위에 적은 대로 public으로 보조 class 곁에 둔다.
2. Koin 모듈의 제공 함수가 그 qualifier로 `CoroutineDispatcher`를 제공한다. `Dispatchers.IO`·`Dispatchers.Default`는 제공 함수에서만 참조하고, 기본 인자로도 두지 않는다. `IO`는 wasm에 없으므로 플랫폼 소스셋의 모듈에서, 모든 플랫폼에 있는 `Default`는 `commonMain` 모듈에서 제공해도 된다. 작업 전용 `CoroutineScope`를 만드는 제공 함수도 dispatcher를 명시해 넘긴다.
3. 컴포넌트는 같은 qualifier로 `CoroutineDispatcher`를 주입받는다.
4. 테스트는 `runTest`의 `testScheduler`를 공유하는 `TestDispatcher`(`StandardTestDispatcher(testScheduler)`, `UnconfinedTestDispatcher(testScheduler)`)를 생성자에 넘긴다. 모든 코루틴이 테스트 스레드 하나에서 실행되어 결과가 결정적이다. scope를 받는 컴포넌트에는 `runTest`의 `backgroundScope`를 넘긴다. 실패를 삼키는 `CoroutineExceptionHandler`처럼 제공 함수의 scope와 같은 구성이 필요하면 `CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler) + ...)`로 직접 만들어도 된다. 실제 서버처럼 다른 스레드의 작업을 기다릴 때는 `withTimeout` 대신 `runTest(timeout = ...)`로 한도를 준다. `runTest` 안의 `withTimeout`은 가상 시간으로 흘러 곧바로 시간 초과가 난다.

⚠️ 비권장 예시:

```kotlin
@Factory
internal class JvmFileLocalDataSource : FileLocalDataSource {
    override suspend fun size(uri: FileUri): Long = withContext(Dispatchers.IO) { uri.toFile().length() }
}
```

✅ 권장 예시:

```kotlin
// core:file:impl commonMain di/FileDispatcher.kt
@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class FileDispatcher

// core:file:impl nonWasmMain
@Module
@Configuration
public class NonWasmFileModule {
    @Factory
    @FileDispatcher
    internal fun providesFileDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

// core:file:impl jvmMain
@Factory
internal class JvmFileLocalDataSource(
    @FileDispatcher private val dispatcher: CoroutineDispatcher,
) : FileLocalDataSource {
    override suspend fun size(uri: FileUri): Long = withContext(dispatcher) { uri.toFile().length() }
}
```

## 대상이 아닌 것

- 플랫폼이 특정 스레드를 요구해 `Dispatchers.Main`으로 옮기는 경우. 메인 큐에서만 읽고 쓰는 상태를 다루는 `NSURLSession` 대리 객체처럼 메인 스레드에서만 부를 수 있는 API가 그렇다.
- 콜백을 `suspendCancellableCoroutine`으로 기다리기만 하는 코드. 블로킹이 없어 옮길 것이 없다. 다만 CoreLocation 대리 객체처럼 콜백이 만든 스레드의 런 루프로 오는 API는 런 루프가 있는 호출한 스레드에서 만든다.
- `NonCancellable`만 더하는 `withContext`. 스레드를 옮기지 않는다.
