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

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class UseCaseTest :
    BehaviorSpec({
        Given("콘솔 기록 수단이 등록되어 있고 한 번 실행하고 끝나는 작업이 실패하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("작업을 실행한다") {
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-001 실패한 작업의 이름이 태그로, 실패 원인이 오류 정보로 남는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = FailingUseCase(throwable = failure)

                    useCase(parameter = fixtureMonkey.giveMeOne<String>())

                    recording.logList.shouldHaveSize(1)
                    val log = recording.logList.single().shouldBeInstanceOf<ConsoleLog>()
                    log.tag shouldBe "FailingUseCase"
                    log.throwable shouldBeSameInstanceAs failure
                }

                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-007 호출한 쪽은 실패 원인을 담은 실패 결과를 그대로 받는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = FailingUseCase(throwable = failure)

                    val result = useCase(parameter = fixtureMonkey.giveMeOne<String>())

                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                }
            }
        }

        Given("콘솔 기록 수단이 등록되어 있고 같은 작업이 실패를 반복하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("같은 작업을 두 번 실행한다") {
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-003 실패 로그가 두 번 남는다") {
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
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-004 실패 로그가 남지 않는다") {
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
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-005 실패 로그가 남지 않는다") {
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
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-006 실패 로그에 입력 값이 포함되지 않는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = FailingUseCase(throwable = failure)
                    val parameter = "parameter-" + fixtureMonkey.giveMeOne<String>()

                    useCase(parameter = parameter)

                    recording.logList.single().toString() shouldNotContain parameter
                }
            }
        }

        Given("기록 수단이 등록되어 있지 않고 작업이 실패하도록 준비되어 있다") {
            val recording = recordingDelegate()

            When("작업을 실행한다") {
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-008 그 수단에 로그가 남지 않고 실패 결과는 그대로 전달된다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = FailingUseCase(throwable = failure)

                    val result = useCase(parameter = fixtureMonkey.giveMeOne<String>())

                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    recording.logList.shouldBeEmpty()
                }
            }
        }

        Given("기록 수단이 등록되어 있고 오류 보고 대상 작업이 실패하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("작업을 실행한다") {
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-009 콘솔 로그와 오류 보고가 함께 남는다") {
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

                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-012 호출한 쪽은 실패 원인을 담은 실패 결과를 그대로 받는다") {
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
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-010 콘솔 로그만 남고 오류 보고는 남지 않는다") {
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
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-011 콘솔 로그와 오류 보고가 모두 남지 않는다") {
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

private class FailingUseCase(
    private val throwable: Throwable,
) : UseCase<String, String>() {
    override suspend fun execute(parameter: String): String = throw throwable
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
