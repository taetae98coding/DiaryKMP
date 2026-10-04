@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.app.shared.integrity

import io.github.taetae98coding.diary.domain.integrity.usecase.LogPlayIntegrityUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
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
