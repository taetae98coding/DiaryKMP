@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.app.shared.integrity

import io.github.taetae98coding.diary.core.model.integrity.PlayIntegrityVerdict
import io.github.taetae98coding.diary.domain.integrity.repository.PlayIntegrityRepository
import io.github.taetae98coding.diary.domain.integrity.usecase.LogPlayIntegrityUseCase
import io.github.taetae98coding.diary.logger.analytics.api.AnalyticsEventLog
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class AppPlayIntegrityViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("확인을 요청하면 서버 응답을 기다려 끝까지 진행한다") {
            runTest(mainDispatcher) {
                val serverResponse = CompletableDeferred<Unit>()
                var finishedCount = 0
                val useCase = mockk<LogPlayIntegrityUseCase>()
                coEvery { useCase(Unit) } coAnswers {
                    serverResponse.await()
                    finishedCount += 1
                    Result.success(Unit)
                }
                val viewModel = AppPlayIntegrityViewModel(logPlayIntegrityUseCase = useCase)

                viewModel.log()
                advanceUntilIdle()
                serverResponse.complete(Unit)
                advanceUntilIdle()

                finishedCount shouldBe 1
            }
        }

        test("TC-0af135da-aa35-4414-8edb-2e9ad8af5115 확인이 진행 중일 때 겹쳐 생긴 계기는 건너뛴다") {
            runTest(mainDispatcher) {
                val serverResponse = CompletableDeferred<PlayIntegrityVerdict?>()
                val verdict = PlayIntegrityVerdict(fieldMap = emptyMap())
                var fetchCount = 0
                val repository = mockk<PlayIntegrityRepository>()
                coEvery { repository.fetch() } coAnswers {
                    fetchCount += 1
                    if (fetchCount == 1) serverResponse.await() else verdict
                }
                val logList = mutableListOf<DiaryLog>()
                val delegate = mockk<DiaryLoggerDelegate>()
                every { delegate.log(log = any()) } answers { logList += firstArg<DiaryLog>() }
                DiaryLogger.add(delegate = delegate)
                val viewModel = AppPlayIntegrityViewModel(logPlayIntegrityUseCase = logPlayIntegrityUseCase(repository = repository))

                viewModel.log()
                advanceUntilIdle()
                viewModel.log()
                advanceUntilIdle()

                fetchCount shouldBe 1
                logList.count { log -> log is AnalyticsEventLog && log.name == "play_integrity" } shouldBe 0

                serverResponse.complete(verdict)
                advanceUntilIdle()

                fetchCount shouldBe 1
                logList.count { log -> log is AnalyticsEventLog && log.name == "play_integrity" } shouldBe 1

                viewModel.log()
                advanceUntilIdle()

                fetchCount shouldBe 2
                logList.count { log -> log is AnalyticsEventLog && log.name == "play_integrity" } shouldBe 2
            }
        }

        test("확인 요청이 반복되어도 진행 중에는 UseCase를 한 번만 실행하고 끝나면 다시 요청할 수 있다") {
            runTest(mainDispatcher) {
                val useCase = mockk<LogPlayIntegrityUseCase>()
                coEvery { useCase(Unit) } returns Result.success(Unit)
                val viewModel = AppPlayIntegrityViewModel(logPlayIntegrityUseCase = useCase)

                viewModel.log()
                viewModel.log()
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(Unit) }

                viewModel.log()
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(Unit) }
            }
        }
    }
}

// 원격 분석 로그는 실제 확인 과정이 남기므로 실제 확인 과정을 쓰고 서버 응답만 제어한다.
// 확인 과정의 생성자는 다른 모듈에서 부를 수 없게 막혀 있어 Java 리플렉션으로 만든다.
private fun logPlayIntegrityUseCase(repository: PlayIntegrityRepository): LogPlayIntegrityUseCase =
    LogPlayIntegrityUseCase::class.java
        .getConstructor(PlayIntegrityRepository::class.java)
        .newInstance(repository)
