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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Duration.Companion.seconds

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

        test("TC-f117a124-06f3-42eb-af49-da9a1bad9df5 TC-3a2b7c2b-d44c-4454-a6b0-42b409f82495 가져오는 중에는 준비 중이고 성공하든 실패하든 끝나면 가져온 횟수가 하나 늘어난다") {
            runTest(mainDispatcher) {
                val importState = MutableStateFlow(ChromeSessionImportState.IMPORTING)
                val viewModel = viewModel(importState = importState)

                viewModel.session.test {
                    awaitItem() shouldBe DiaryWebSession()
                    awaitItem() shouldBe DiaryWebSession(isPreparing = true)

                    importState.value = ChromeSessionImportState.IMPORTED
                    awaitItem() shouldBe DiaryWebSession(importCount = 1)

                    importState.value = ChromeSessionImportState.IMPORTING
                    awaitItem() shouldBe DiaryWebSession(isPreparing = true, importCount = 1)

                    importState.value = ChromeSessionImportState.FAILED
                    awaitItem() shouldBe DiaryWebSession(importCount = 2)
                }
            }
        }

        test("TC-fcdc5498-e502-467d-a4af-67e4bd7bc361 가져오는 중이 아니면 준비 중이 아니다") {
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

        test("TC-366137ee-c354-4e7a-9adc-8327e08a602a 가져오기가 실패로 끝나면 그 시점에 한 번 알린다") {
            listOf(ChromeSessionImportState.IMPORTING, ChromeSessionImportState.IDLE, ChromeSessionImportState.IMPORTED).forEach { previous ->
                runTest(mainDispatcher) {
                    val importState = MutableStateFlow(previous)
                    val viewModel = viewModel(importState = importState)
                    backgroundScope.launch { viewModel.session.collect {} }

                    viewModel.effect.test {
                        advanceUntilIdle()
                        expectNoEvents()

                        importState.value = ChromeSessionImportState.FAILED

                        awaitItem() shouldBe AppChromeSessionEffect.ImportFailed
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-37cd7760-bb69-4a46-9d5f-123779a5fbea 다음 계기에 다시 실패하면 다시 알린다") {
            runTest(mainDispatcher) {
                val importState = MutableStateFlow(ChromeSessionImportState.IMPORTING)
                val viewModel = viewModel(importState = importState)
                backgroundScope.launch { viewModel.session.collect {} }

                viewModel.effect.test {
                    advanceUntilIdle()
                    importState.value = ChromeSessionImportState.FAILED
                    awaitItem() shouldBe AppChromeSessionEffect.ImportFailed

                    importState.value = ChromeSessionImportState.IMPORTING
                    advanceUntilIdle()
                    importState.value = ChromeSessionImportState.FAILED

                    awaitItem() shouldBe AppChromeSessionEffect.ImportFailed
                }
            }
        }

        test("TC-56e3fbae-9057-4078-9803-d0e47e69c0ac 마지막 결과가 이미 실패일 때 다시 실패해도 다시 알리지 않는다") {
            runTest(mainDispatcher) {
                val importState = MutableSharedFlow<ChromeSessionImportState>(replay = 1)
                val viewModel = viewModel(importState = importState)
                backgroundScope.launch { viewModel.session.collect {} }

                viewModel.effect.test {
                    importState.emit(ChromeSessionImportState.IDLE)
                    advanceUntilIdle()
                    importState.emit(ChromeSessionImportState.FAILED)
                    awaitItem() shouldBe AppChromeSessionEffect.ImportFailed

                    importState.emit(ChromeSessionImportState.FAILED)
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-dc502268-12a5-42b9-bbc2-399e4ee17155 화면이 다시 만들어져 상태를 다시 확인해도 이미 알린 실패를 다시 알리지 않는다") {
            runTest(mainDispatcher) {
                val importState = MutableStateFlow(ChromeSessionImportState.IMPORTING)
                val viewModel = viewModel(importState = importState)
                val firstCollection = backgroundScope.launch { viewModel.session.collect {} }

                viewModel.effect.test {
                    advanceUntilIdle()
                    importState.value = ChromeSessionImportState.FAILED
                    awaitItem() shouldBe AppChromeSessionEffect.ImportFailed

                    firstCollection.cancel()
                    advanceTimeBy(SESSION_STOP_TIMEOUT)
                    backgroundScope.launch { viewModel.session.collect {} }
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-b80c0e1e-043b-4c8d-84dc-c04d396be29b 앱이 오래 보이지 않는 동안 끝난 실패는 다시 보여도 알리지 않는다") {
            runTest(mainDispatcher) {
                val importState = MutableStateFlow(ChromeSessionImportState.IMPORTING)
                val viewModel = viewModel(importState = importState)
                val firstCollection = backgroundScope.launch { viewModel.session.collect {} }

                viewModel.effect.test {
                    advanceUntilIdle()
                    firstCollection.cancel()
                    advanceTimeBy(SESSION_STOP_TIMEOUT)
                    advanceUntilIdle()

                    importState.value = ChromeSessionImportState.FAILED
                    backgroundScope.launch { viewModel.session.collect {} }
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("가져오는 중을 거치지 않은 실패는 가져온 횟수를 늘리지 않는다") {
            runTest(mainDispatcher) {
                val importState = MutableStateFlow(ChromeSessionImportState.IDLE)
                val viewModel = viewModel(importState = importState)

                viewModel.session.test {
                    awaitItem() shouldBe DiaryWebSession()
                    advanceUntilIdle()

                    importState.value = ChromeSessionImportState.FAILED
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.session.value shouldBe DiaryWebSession()
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

    private companion object {
        val SESSION_STOP_TIMEOUT = 10.seconds
    }
}
