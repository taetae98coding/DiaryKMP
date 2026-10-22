@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.app.shared

import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.github.taetae98coding.diary.core.model.browser.ChromeSessionImportState
import io.github.taetae98coding.diary.domain.browser.usecase.GetChromeSessionImportStateUseCase
import io.github.taetae98coding.diary.domain.browser.usecase.RequestChromeSessionImportUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class AppChromeSessionViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-4fc567a4-7f80-4f00-b206-87105a1264a2 앱이 보이게 되면 로그인 정보 가져오기를 한 번 요청한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<RequestChromeSessionImportUseCase>()
                coEvery { useCase(Unit) } returns Result.success(Unit)
                val viewModel = viewModel(importState = MutableStateFlow(ChromeSessionImportState.IDLE), requestUseCase = useCase)

                viewModel.requestImport()
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(Unit) }
            }
        }

        test("TC-f117a124-06f3-42eb-af49-da9a1bad9df5 TC-3a2b7c2b-d44c-4454-a6b0-42b409f82495 가져오는 중에는 준비 중이고 끝나면 가져온 횟수가 하나 늘어난다") {
            runTest(mainDispatcher) {
                val importState = MutableStateFlow(ChromeSessionImportState.IMPORTING)
                val viewModel = viewModel(importState = importState)

                viewModel.session.test {
                    awaitItem() shouldBe DiaryWebSession()
                    awaitItem() shouldBe DiaryWebSession(isPreparing = true)

                    importState.value = ChromeSessionImportState.IMPORTED

                    awaitItem() shouldBe DiaryWebSession(importCount = 1)
                }
            }
        }

        test("TC-fcdc5498-e502-467d-a4af-67e4bd7bc361 가져오는 중이 아니면 준비 중이 아니고 실패 번호가 없다") {
            listOf(ChromeSessionImportState.IDLE, ChromeSessionImportState.IMPORTED).forEach { state ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(importState = MutableStateFlow(state))

                    viewModel.session.test {
                        awaitItem() shouldBe DiaryWebSession()
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-26869b48-2548-44ea-9c03-6ce75c277fc3 TC-72308d3d-3936-49f3-80b1-651b641342f4 실패마다 새 실패 번호를 제공하고 성공하면 실패 번호를 지운다") {
            runTest(mainDispatcher) {
                val importState = MutableStateFlow(ChromeSessionImportState.FAILED)
                val viewModel = viewModel(importState = importState)

                viewModel.session.test {
                    awaitItem() shouldBe DiaryWebSession()
                    awaitItem() shouldBe DiaryWebSession(failureId = 1)

                    importState.value = ChromeSessionImportState.IMPORTING
                    awaitItem() shouldBe DiaryWebSession(isPreparing = true)

                    importState.value = ChromeSessionImportState.FAILED
                    awaitItem() shouldBe DiaryWebSession(importCount = 1, failureId = 2)

                    importState.value = ChromeSessionImportState.IMPORTING
                    awaitItem() shouldBe DiaryWebSession(isPreparing = true, importCount = 1)

                    importState.value = ChromeSessionImportState.IMPORTED
                    awaitItem() shouldBe DiaryWebSession(importCount = 2)
                }
            }
        }

        test("TC-56e3fbae-9057-4078-9803-d0e47e69c0ac 마지막 결과가 이미 실패일 때 다시 실패해도 새 실패 번호를 제공하지 않는다") {
            runTest(mainDispatcher) {
                val importState = MutableSharedFlow<ChromeSessionImportState>(replay = 1)
                val viewModel = viewModel(importState = importState)

                viewModel.session.test {
                    awaitItem() shouldBe DiaryWebSession()

                    importState.emit(ChromeSessionImportState.FAILED)
                    awaitItem() shouldBe DiaryWebSession(failureId = 1)

                    importState.emit(ChromeSessionImportState.FAILED)
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("가져오는 중을 거치지 않은 실패는 가져온 횟수를 늘리지 않고 실패 번호만 제공한다") {
            runTest(mainDispatcher) {
                val importState = MutableStateFlow(ChromeSessionImportState.IDLE)
                val viewModel = viewModel(importState = importState)

                viewModel.session.test {
                    awaitItem() shouldBe DiaryWebSession()
                    advanceUntilIdle()

                    importState.value = ChromeSessionImportState.FAILED

                    awaitItem() shouldBe DiaryWebSession(failureId = 1)
                }
            }
        }

        test("가져오기 요청이 반복되어도 진행 중에는 요청 UseCase를 한 번만 실행하고 끝나면 다시 요청할 수 있다") {
            runTest(mainDispatcher) {
                val useCase = mockk<RequestChromeSessionImportUseCase>()
                coEvery { useCase(Unit) } returns Result.success(Unit)
                val viewModel = viewModel(importState = MutableStateFlow(ChromeSessionImportState.IDLE), requestUseCase = useCase)

                viewModel.requestImport()
                viewModel.requestImport()
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(Unit) }

                viewModel.requestImport()
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(Unit) }
            }
        }
    }

    private fun viewModel(
        importState: Flow<ChromeSessionImportState>,
        requestUseCase: RequestChromeSessionImportUseCase = mockk(),
    ): AppChromeSessionViewModel {
        val getUseCase = mockk<GetChromeSessionImportStateUseCase>()
        every { getUseCase(Unit) } returns importState.map { state -> Result.success(state) }

        return AppChromeSessionViewModel(getChromeSessionImportStateUseCase = getUseCase, requestChromeSessionImportUseCase = requestUseCase)
    }
}
