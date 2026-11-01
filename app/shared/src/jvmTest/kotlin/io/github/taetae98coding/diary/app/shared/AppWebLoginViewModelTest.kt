@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.app.shared

import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.github.taetae98coding.diary.core.model.web.WebLoginRestoreState
import io.github.taetae98coding.diary.domain.web.usecase.RestoreWebLoginUseCase
import io.github.taetae98coding.diary.domain.web.usecase.SaveWebLoginUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class AppWebLoginViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-6054d52b-e3a5-4e4f-beb3-856bf404688b 되돌리기가 끝나기 전에는 되돌리는 중이고 성공하든 실패하든 끝나면 되돌리는 중이 아니다") {
            listOf(
                Result.success(WebLoginRestoreState.DONE),
                Result.failure(IllegalStateException()),
            ).forEach { ending ->
                runTest(mainDispatcher) {
                    val restoreFlow = MutableSharedFlow<Result<WebLoginRestoreState>>()
                    val viewModel = viewModel(restoreFlow = restoreFlow)

                    viewModel.session.test {
                        awaitItem() shouldBe DiaryWebSession(isRestoring = true)
                        advanceUntilIdle()

                        restoreFlow.emit(Result.success(WebLoginRestoreState.RESTORING))
                        advanceUntilIdle()
                        expectNoEvents()

                        restoreFlow.emit(ending)
                        awaitItem() shouldBe DiaryWebSession(isRestoring = false)
                    }
                }
            }
        }

        test("TC-44b89df3-9791-4f8f-8de6-048c4714a94a 되돌릴 세션 쿠키가 없으면 곧바로 되돌리는 중이 아니게 된다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(restoreFlow = flowOf(Result.success(WebLoginRestoreState.DONE)))
                advanceUntilIdle()

                viewModel.session.value shouldBe DiaryWebSession(isRestoring = false)
            }
        }

        test("TC-9ffe581d-39f9-4181-9ad3-b1e6aca53a53 보관한 세션 쿠키를 읽지 못해도 되돌리는 중이 끝난다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(restoreFlow = flowOf(Result.failure(IllegalStateException())))
                advanceUntilIdle()

                viewModel.session.value shouldBe DiaryWebSession(isRestoring = false)
            }
        }

        test("TC-353204d8-2c5e-4805-93a5-d7dd0289d27b 보관을 기다리는 동안 다시 벗어나도 보관은 하나만 진행한다") {
            runTest(mainDispatcher) {
                val restoreEnd = CompletableDeferred<Unit>()
                val saveUseCase = mockk<SaveWebLoginUseCase>()
                // 되돌리기가 끝날 때까지 기다리는 것은 보관 기능이 맡으므로, 그동안 끝나지 않는 보관으로 둔다.
                coEvery { saveUseCase(Unit) } coAnswers {
                    restoreEnd.await()
                    Result.success(Unit)
                }
                val viewModel = viewModel(restoreFlow = MutableSharedFlow(), saveUseCase = saveUseCase)
                advanceUntilIdle()

                viewModel.save()
                viewModel.save()
                advanceUntilIdle()
                restoreEnd.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { saveUseCase(Unit) }
            }
        }

        test("TC-e6141c76-3fcc-4ccb-a23b-84439d5f9f35 보관이 끝난 뒤 다시 벗어나면 다시 보관한다") {
            runTest(mainDispatcher) {
                val saveUseCase = saveUseCase()
                val viewModel = viewModel(restoreFlow = flowOf(Result.success(WebLoginRestoreState.DONE)), saveUseCase = saveUseCase)
                advanceUntilIdle()

                viewModel.save()
                advanceUntilIdle()
                viewModel.save()
                advanceUntilIdle()

                coVerify(exactly = 2) { saveUseCase(Unit) }
            }
        }

        test("되돌리기를 여러 번 요청해도 한 번만 되돌린다") {
            runTest(mainDispatcher) {
                val restoreUseCase = mockk<RestoreWebLoginUseCase>()
                every { restoreUseCase(Unit) } returns flowOf(Result.success(WebLoginRestoreState.DONE))
                val viewModel = AppWebLoginViewModel(restoreWebLoginUseCase = restoreUseCase, saveWebLoginUseCase = saveUseCase())

                viewModel.restore()
                viewModel.restore()
                advanceUntilIdle()

                verify(exactly = 1) { restoreUseCase(Unit) }
            }
        }

        test("되돌리기를 요청하기 전에는 되돌리는 중으로 둔다") {
            runTest(mainDispatcher) {
                val viewModel = AppWebLoginViewModel(restoreWebLoginUseCase = mockk(), saveWebLoginUseCase = saveUseCase())
                advanceUntilIdle()

                viewModel.session.value shouldBe DiaryWebSession(isRestoring = true)
            }
        }
    }

    private fun saveUseCase(): SaveWebLoginUseCase {
        val saveUseCase = mockk<SaveWebLoginUseCase>()
        coEvery { saveUseCase(Unit) } returns Result.success(Unit)

        return saveUseCase
    }

    private fun viewModel(
        restoreFlow: Flow<Result<WebLoginRestoreState>>,
        saveUseCase: SaveWebLoginUseCase = saveUseCase(),
    ): AppWebLoginViewModel {
        val restoreUseCase = mockk<RestoreWebLoginUseCase>()
        every { restoreUseCase(Unit) } returns restoreFlow

        return AppWebLoginViewModel(restoreWebLoginUseCase = restoreUseCase, saveWebLoginUseCase = saveUseCase).apply { restore() }
    }
}
