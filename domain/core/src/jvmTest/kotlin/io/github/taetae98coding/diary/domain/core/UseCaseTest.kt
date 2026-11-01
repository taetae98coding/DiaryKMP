package io.github.taetae98coding.diary.domain.core

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.console.api.ConsoleLog
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class UseCaseTest :
    BehaviorSpec({
        Given("콘솔 기록 수단이 등록되어 있고 한 번 실행하고 끝나는 작업이 실패하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("작업을 실행한다") {
                Then("TC-4fc2499b-f700-42cd-a3b9-493ab186dc7d 실패한 작업의 이름이 태그로, 실패 원인이 오류 정보로 남는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = FailingUseCase(throwable = failure)

                    useCase(parameter = fixtureMonkey.giveMeOne<String>())

                    recording.logList.shouldHaveSize(1)
                    val log = recording.logList.single().shouldBeInstanceOf<ConsoleLog>()
                    log.tag shouldBe "FailingUseCase"
                    log.throwable shouldBeSameInstanceAs failure
                }

                Then("TC-0f78fa8d-126c-4ac3-b98c-f6dbdd373790 호출한 쪽은 실패 원인을 담은 실패 결과를 그대로 받는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = FailingUseCase(throwable = failure)

                    val result = useCase(parameter = fixtureMonkey.giveMeOne<String>())

                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                }
            }
        }

        Given("콘솔 기록 수단이 등록되어 있고 한 번 실행하는 작업이 다른 작업의 실패를 이어받아 실패하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("바깥 작업을 실행한다") {
                Then("TC-75ca3733-21b8-4f5c-9416-eb6754875509 안쪽 작업과 바깥 작업의 이름으로 실패 로그가 하나씩 남는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = RethrowingUseCase(inner = FailingUseCase(throwable = failure))

                    useCase(parameter = fixtureMonkey.giveMeOne<String>())

                    recording.logList.map { log -> log.shouldBeInstanceOf<ConsoleLog>().tag } shouldBe listOf("FailingUseCase", "RethrowingUseCase")
                }
            }
        }

        Given("콘솔 기록 수단이 등록되어 있고 같은 작업이 실패를 반복하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("같은 작업을 두 번 실행한다") {
                Then("TC-ffab2916-0d70-4877-b321-ec59083a40e9 실패 로그가 두 번 남는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = FailingUseCase(throwable = failure)

                    useCase(parameter = fixtureMonkey.giveMeOne<String>())
                    useCase(parameter = fixtureMonkey.giveMeOne<String>())

                    recording.logList.shouldHaveSize(2)
                }
            }
        }

        Given("콘솔 기록 수단이 등록되어 있고 작업이 성공하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("작업을 실행한다") {
                Then("TC-ab12ade8-95b3-443e-ba83-0b1fda65f82a 실패 로그가 남지 않는다") {
                    val useCase = SuccessUseCase()
                    val parameter = fixtureMonkey.giveMeOne<String>()

                    val result = useCase(parameter = parameter)

                    result.shouldBeSuccess() shouldBe parameter
                    recording.logList.shouldBeEmpty()
                }
            }
        }

        Given("콘솔 기록 수단이 등록되어 있고 작업이 중단을 기다리도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("진행 중이던 작업을 중단한다") {
                Then("TC-9f2cf9fb-cb64-462f-a7e3-efc210cd71ce 실패 로그가 남지 않는다") {
                    val onStart = CompletableDeferred<Unit>()
                    val useCase = AwaitCancellationUseCase(onStart = onStart)

                    coroutineScope {
                        val job = launch { useCase(parameter = Unit) }

                        onStart.await()
                        job.cancelAndJoin()
                    }

                    recording.logList.shouldBeEmpty()
                }
            }
        }

        Given("콘솔 기록 수단이 등록되어 있고 특정 문자열을 입력 값으로 받은 작업이 실패하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("작업을 실행한다") {
                Then("TC-7d961071-3576-4a0b-8b28-21d4cf998249 실패 로그에 입력 값이 포함되지 않는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = FailingUseCase(throwable = failure)
                    val parameter = "parameter-" + fixtureMonkey.giveMeOne<String>()

                    useCase(parameter = parameter)

                    recording.logList.single().toString() shouldNotContain parameter
                }
            }
        }

        Given("기록 수단이 하나도 등록되지 않은 새 실행 환경에서 작업이 실패하도록 준비되어 있다") {
            When("작업을 실행한다") {
                Then("TC-b976a725-ee2a-41b6-b7e8-8df3f47c0c0d 실패 로그는 어디에도 남지 않고 실패 결과는 그대로 전달된다") {
                    // 공통 창구는 등록을 해제할 수 없어 앞선 테스트가 등록한 수단이 남으므로, 아무것도 등록하지 않은 새 프로세스에서 실행한다.
                    val failureMessage = "failure${fixtureMonkey.giveMeOne<Int>()}"
                    val parameter = "parameter${fixtureMonkey.giveMeOne<Int>()}"

                    val result = runNoDelegateFailure(failureMessage = failureMessage, parameter = parameter)

                    result.exitCode shouldBe 0
                    result.output shouldBe "IllegalStateException:$failureMessage"
                    result.errorOutput shouldBe ""
                }
            }
        }

        Given("기록 수단이 등록되어 있고 오류 보고 대상 작업이 실패하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("작업을 실행한다") {
                Then("TC-117c1b06-4930-4a5e-acd3-d7895901b0b7 콘솔 로그와 오류 보고가 함께 남는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = ReportingFailingUseCase(throwable = failure)

                    useCase(parameter = fixtureMonkey.giveMeOne<String>())

                    recording.logList.shouldHaveSize(2)

                    val consoleLog = recording.logList.filterIsInstance<ConsoleLog>().single()
                    consoleLog.tag shouldBe "ReportingFailingUseCase"
                    consoleLog.throwable shouldBeSameInstanceAs failure

                    val reportLog = recording.logList.filterIsInstance<TestReportLog>().single()
                    reportLog.message shouldContain "ReportingFailingUseCase"
                    reportLog.throwable shouldBeSameInstanceAs failure
                }

                Then("TC-a020b1f1-8af2-4d01-a99d-691411e6575f 호출한 쪽은 실패 원인을 담은 실패 결과를 그대로 받는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = ReportingFailingUseCase(throwable = failure)

                    val result = useCase(parameter = fixtureMonkey.giveMeOne<String>())

                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                }
            }
        }

        Given("기록 수단이 등록되어 있고 오류 보고 대상이 아닌 작업이 실패하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("작업을 실행한다") {
                Then("TC-318e8f13-7bf1-403a-986d-9bc10d750dcc 콘솔 로그만 남고 오류 보고는 남지 않는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = FailingUseCase(throwable = failure)

                    useCase(parameter = fixtureMonkey.giveMeOne<String>())

                    recording.logList.shouldHaveSize(1)
                    recording.logList.single().shouldBeInstanceOf<ConsoleLog>()
                }
            }
        }

        Given("기록 수단이 등록되어 있고 오류 보고 대상 작업이 중단을 기다리도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("진행 중이던 작업을 중단한다") {
                Then("TC-c31898f9-0147-4546-a29e-c9f84d5cc9c2 콘솔 로그와 오류 보고가 모두 남지 않는다") {
                    val onStart = CompletableDeferred<Unit>()
                    val useCase = ReportingAwaitCancellationUseCase(onStart = onStart)

                    coroutineScope {
                        val job = launch { useCase(parameter = Unit) }

                        onStart.await()
                        job.cancelAndJoin()
                    }

                    recording.logList.shouldBeEmpty()
                }
            }
        }

        Given("기록 수단이 등록되어 있고 오류 보고 대상 작업이 성공하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("작업을 실행한다") {
                Then("성공한 오류 보고 대상 작업은 콘솔 로그와 오류 보고를 남기지 않는다") {
                    val useCase = ReportingSuccessUseCase()
                    val parameter = fixtureMonkey.giveMeOne<String>()

                    val result = useCase(parameter = parameter)

                    result.shouldBeSuccess() shouldBe parameter
                    recording.logList.shouldBeEmpty()
                }
            }
        }
    })

private fun runNoDelegateFailure(
    failureMessage: String,
    parameter: String,
): NoDelegateFailureResult {
    val java =
        ProcessHandle
            .current()
            .info()
            .command()
            .orElseThrow()
    val errorFile = createTempFile(prefix = "no-delegate-failure", suffix = ".err")
    val process =
        ProcessBuilder(
            java,
            "-cp",
            System.getProperty("java.class.path"),
            "io.github.taetae98coding.diary.domain.core.NoDelegateFailureMainKt",
            failureMessage,
            parameter,
        ).redirectError(errorFile.toFile())
            .start()
    val output = process.inputStream.bufferedReader().use { reader -> reader.readText() }

    check(process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)) { "process timed out" }
    return NoDelegateFailureResult(
        exitCode = process.exitValue(),
        output = output,
        errorOutput = errorFile.readText().also { errorFile.deleteIfExists() },
    )
}

private data class NoDelegateFailureResult(
    val exitCode: Int,
    val output: String,
    val errorOutput: String,
)

private const val PROCESS_TIMEOUT_SECONDS = 30L

private class FailingUseCase(
    private val throwable: Throwable,
) : UseCase<String, String>() {
    override suspend fun execute(parameter: String): String = throw throwable
}

private class RethrowingUseCase(
    private val inner: UseCase<String, String>,
) : UseCase<String, String>() {
    override suspend fun execute(parameter: String): String = inner(parameter = parameter).getOrThrow()
}

private class SuccessUseCase : UseCase<String, String>() {
    override suspend fun execute(parameter: String): String = parameter
}

private class AwaitCancellationUseCase(
    private val onStart: CompletableDeferred<Unit>,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        onStart.complete(Unit)
        awaitCancellation()
    }
}

private data class TestReportLog(
    val message: String,
    val throwable: Throwable,
) : DiaryLog

private class ReportingFailingUseCase(
    private val throwable: Throwable,
) : UseCase<String, String>() {
    override suspend fun execute(parameter: String): String = throw throwable

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.log(log = TestReportLog(message = "${this::class.simpleName.orEmpty()} 실패", throwable = throwable))
    }
}

private class ReportingSuccessUseCase : UseCase<String, String>() {
    override suspend fun execute(parameter: String): String = parameter

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.log(log = TestReportLog(message = "${this::class.simpleName.orEmpty()} 실패", throwable = throwable))
    }
}

private class ReportingAwaitCancellationUseCase(
    private val onStart: CompletableDeferred<Unit>,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        onStart.complete(Unit)
        awaitCancellation()
    }

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.log(log = TestReportLog(message = "${this::class.simpleName.orEmpty()} 실패", throwable = throwable))
    }
}
