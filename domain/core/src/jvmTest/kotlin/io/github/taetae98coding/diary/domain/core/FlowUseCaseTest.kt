package io.github.taetae98coding.diary.domain.core

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.console.api.ConsoleLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FlowUseCaseTest :
    BehaviorSpec({
        Given("콘솔 기록 수단이 등록되어 있고 지속 관찰하는 작업이 실패하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("작업을 관찰한다") {
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-002 실패한 작업의 이름이 태그로, 실패 원인이 오류 정보로 남는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = FailingFlowUseCase(throwable = failure)

                    useCase(parameter = fixtureMonkey.giveMeOne<String>()).test {
                        awaitItem().shouldBeFailure() shouldBeSameInstanceAs failure
                        awaitComplete()
                    }

                    recording.logList.shouldHaveSize(1)
                    val log = recording.logList.single().shouldBeInstanceOf<ConsoleLog>()
                    log.tag shouldBe "FailingFlowUseCase"
                    log.throwable shouldBeSameInstanceAs failure
                }
            }
        }

        Given("콘솔 기록 수단이 등록되어 있고 지속 관찰하는 작업이 값을 보낸 뒤 실패하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("작업을 한 번 관찰한다") {
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-013 실패를 전달한 뒤 관찰이 끝나고 실패 로그는 한 번만 남는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val values = fixtureMonkey.giveMeOne<List<String>>().ifEmpty { listOf(fixtureMonkey.giveMeOne<String>()) }
                    val useCase = EmitThenFailFlowUseCase(values = values, throwable = failure)

                    useCase(parameter = fixtureMonkey.giveMeOne<String>()).test {
                        values.forEach { value -> awaitItem().shouldBeSuccess() shouldBe value }
                        awaitItem().shouldBeFailure() shouldBeSameInstanceAs failure
                        awaitComplete()
                    }

                    recording.logList.shouldHaveSize(1)
                }
            }
        }

        Given("콘솔 기록 수단이 등록되어 있고 지속 관찰하는 작업이 실패를 반복하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("같은 작업을 두 번 관찰한다") {
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-003 실패 로그가 두 번 남는다") {
                    val failure = IllegalStateException("failure-" + fixtureMonkey.giveMeOne<String>())
                    val useCase = FailingFlowUseCase(throwable = failure)

                    repeat(times = 2) {
                        useCase(parameter = fixtureMonkey.giveMeOne<String>()).test {
                            awaitItem().shouldBeFailure()
                            awaitComplete()
                        }
                    }

                    recording.logList.shouldHaveSize(2)
                }
            }
        }

        Given("콘솔 기록 수단이 등록되어 있고 지속 관찰하는 작업이 성공하도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("작업을 관찰한다") {
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-004 실패 로그가 남지 않는다") {
                    val useCase = SuccessFlowUseCase()
                    val parameter = fixtureMonkey.giveMeOne<String>()

                    useCase(parameter = parameter).test {
                        awaitItem().shouldBeSuccess() shouldBe parameter
                        awaitComplete()
                    }

                    recording.logList.shouldBeEmpty()
                }
            }
        }

        Given("콘솔 기록 수단이 등록되어 있고 지속 관찰하는 작업이 중단을 기다리도록 준비되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("관찰 중이던 작업을 중단한다") {
                Then("TC-USECASE-FAILURE-LOGGING-DOMAIN-005 실패 로그가 남지 않는다") {
                    val useCase = AwaitCancellationFlowUseCase()
                    val parameter = fixtureMonkey.giveMeOne<String>()

                    useCase(parameter = parameter).test {
                        awaitItem().shouldBeSuccess() shouldBe parameter
                        cancel()
                    }

                    recording.logList.shouldBeEmpty()
                }
            }
        }
    })

private class FailingFlowUseCase(
    private val throwable: Throwable,
) : FlowUseCase<String, String>() {
    override fun execute(parameter: String): Flow<Result<String>> = flow { throw throwable }
}

private class EmitThenFailFlowUseCase(
    private val values: List<String>,
    private val throwable: Throwable,
) : FlowUseCase<String, String>() {
    override fun execute(parameter: String): Flow<Result<String>> =
        flow {
            values.forEach { value -> emit(Result.success(value)) }
            throw throwable
        }
}

private class SuccessFlowUseCase : FlowUseCase<String, String>() {
    override fun execute(parameter: String): Flow<Result<String>> = flowOf(Result.success(parameter))
}

private class AwaitCancellationFlowUseCase : FlowUseCase<String, String>() {
    override fun execute(parameter: String): Flow<Result<String>> =
        flow {
            emit(Result.success(parameter))
            awaitCancellation()
        }
}
