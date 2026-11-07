@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.mcp

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.mcp.McpServerState
import io.github.taetae98coding.diary.domain.mcp.usecase.DisableMcpServerUseCase
import io.github.taetae98coding.diary.domain.mcp.usecase.EnableMcpServerUseCase
import io.github.taetae98coding.diary.domain.mcp.usecase.GetMcpServerEnabledUseCase
import io.github.taetae98coding.diary.domain.mcp.usecase.GetMcpServerStateUseCase
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class SettingMcpViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-30ea3b2b-ac06-4fc0-a7b3-9f87fa6aecf0 저장된 연결 사용 설정을 표시한다") {
            listOf(true, false).forEach { isEnabled ->
                runTest(mainDispatcher) {
                    val viewModel =
                        viewModel(
                            enabledFlow = flowOf(Result.success(isEnabled)),
                            stateFlow = flowOf(Result.success(McpServerState.Off)),
                        )

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingMcpUiState.Loading
                        awaitItem() shouldBe SettingMcpUiState.Content(isEnabled = isEnabled, serverState = McpServerState.Off)
                    }
                }
            }
        }

        test("TC-89b9a0f5-4c46-412e-b067-1fd08f8dd10c 꺼진 연결을 켜면 켬으로 저장한다") {
            runTest(mainDispatcher) {
                val storedEnabled = MutableStateFlow(Result.success(false))
                val enableUseCase = mockk<EnableMcpServerUseCase>()
                coEvery { enableUseCase(Unit) } coAnswers {
                    storedEnabled.value = Result.success(true)
                    Result.success(Unit)
                }
                val disableUseCase = mockk<DisableMcpServerUseCase>()
                val viewModel =
                    viewModel(
                        enabledFlow = storedEnabled,
                        stateFlow = flowOf(Result.success(McpServerState.Off)),
                        enableUseCase = enableUseCase,
                        disableUseCase = disableUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMcpUiState.Loading
                    awaitItem() shouldBe SettingMcpUiState.Content(isEnabled = false, serverState = McpServerState.Off)

                    viewModel.changeEnabled(isEnabled = true)
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingMcpUiState.Content(isEnabled = true, serverState = McpServerState.Off)
                }

                coVerify(exactly = 1) { enableUseCase(Unit) }
                coVerify(exactly = 0) { disableUseCase(Unit) }
            }
        }

        test("TC-89b9a0f5-4c46-412e-b067-1fd08f8dd10c 켜진 연결을 끄면 꺼짐으로 저장한다") {
            runTest(mainDispatcher) {
                val storedEnabled = MutableStateFlow(Result.success(true))
                val enableUseCase = mockk<EnableMcpServerUseCase>()
                val disableUseCase = mockk<DisableMcpServerUseCase>()
                coEvery { disableUseCase(Unit) } coAnswers {
                    storedEnabled.value = Result.success(false)
                    Result.success(Unit)
                }
                val viewModel =
                    viewModel(
                        enabledFlow = storedEnabled,
                        stateFlow = flowOf(Result.success(McpServerState.Off)),
                        enableUseCase = enableUseCase,
                        disableUseCase = disableUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMcpUiState.Loading
                    awaitItem() shouldBe SettingMcpUiState.Content(isEnabled = true, serverState = McpServerState.Off)

                    viewModel.changeEnabled(isEnabled = false)
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingMcpUiState.Content(isEnabled = false, serverState = McpServerState.Off)
                }

                coVerify(exactly = 1) { disableUseCase(Unit) }
                coVerify(exactly = 0) { enableUseCase(Unit) }
            }
        }

        test("TC-24a81b58-1da7-46d0-b69b-a32b19d06e94 저장에 실패하면 이전 상태를 유지한다") {
            runTest(mainDispatcher) {
                val enableUseCase = mockk<EnableMcpServerUseCase>()
                coEvery { enableUseCase(Unit) } returns Result.failure(IllegalStateException("save error"))
                val viewModel =
                    viewModel(
                        enabledFlow = MutableStateFlow(Result.success(false)),
                        stateFlow = flowOf(Result.success(McpServerState.Off)),
                        enableUseCase = enableUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMcpUiState.Loading
                    awaitItem() shouldBe SettingMcpUiState.Content(isEnabled = false, serverState = McpServerState.Off)

                    viewModel.changeEnabled(isEnabled = true)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value shouldBe SettingMcpUiState.Content(isEnabled = false, serverState = McpServerState.Off)
                }

                coVerify(exactly = 1) { enableUseCase(Unit) }
            }
        }

        test("TC-babc55c7-b2bc-4c88-a5b6-7752ff29f25d 저장된 설정을 확인하기 전에는 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        enabledFlow = emptyFlow(),
                        stateFlow = flowOf(Result.success(McpServerState.Off)),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMcpUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-babc55c7-b2bc-4c88-a5b6-7752ff29f25d 저장된 설정을 읽지 못하면 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        enabledFlow = flowOf(Result.failure(IllegalStateException("read error"))),
                        stateFlow = flowOf(Result.success(McpServerState.Off)),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMcpUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-78bc132a-5ecc-4a32-80a3-c4433b22fd2d 연결 상태가 바뀌면 조작 없이 바뀐 상태를 제공한다") {
            runTest(mainDispatcher) {
                val serverState = MutableStateFlow<Result<McpServerState>>(Result.success(McpServerState.Off))
                val running = McpServerState.Running(address = Arb.alphanumericString(prefix = "http://").next())
                val viewModel =
                    viewModel(
                        enabledFlow = MutableStateFlow(Result.success(true)),
                        stateFlow = serverState,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMcpUiState.Loading
                    awaitItem() shouldBe SettingMcpUiState.Content(isEnabled = true, serverState = McpServerState.Off)

                    serverState.value = Result.success(running)
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingMcpUiState.Content(isEnabled = true, serverState = running)
                }
            }
        }

        test("저장하는 동안 다시 켜도 한 번만 저장한다") {
            runTest(mainDispatcher) {
                val saving = CompletableDeferred<Result<Unit>>()
                val enableUseCase = mockk<EnableMcpServerUseCase>()
                coEvery { enableUseCase(Unit) } coAnswers { saving.await() }
                val viewModel =
                    viewModel(
                        enabledFlow = MutableStateFlow(Result.success(false)),
                        stateFlow = flowOf(Result.success(McpServerState.Off)),
                        enableUseCase = enableUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMcpUiState.Loading
                    awaitItem() shouldBe SettingMcpUiState.Content(isEnabled = false, serverState = McpServerState.Off)

                    viewModel.changeEnabled(isEnabled = true)
                    advanceUntilIdle()
                    viewModel.changeEnabled(isEnabled = true)
                    advanceUntilIdle()
                    saving.complete(Result.success(Unit))
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { enableUseCase(Unit) }
            }
        }

        test("현재 값과 같은 값을 고르면 저장하지 않는다") {
            listOf(true, false).forEach { isEnabled ->
                runTest(mainDispatcher) {
                    val enableUseCase = mockk<EnableMcpServerUseCase>()
                    val disableUseCase = mockk<DisableMcpServerUseCase>()
                    val viewModel =
                        viewModel(
                            enabledFlow = MutableStateFlow(Result.success(isEnabled)),
                            stateFlow = flowOf(Result.success(McpServerState.Off)),
                            enableUseCase = enableUseCase,
                            disableUseCase = disableUseCase,
                        )

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingMcpUiState.Loading
                        awaitItem() shouldBe SettingMcpUiState.Content(isEnabled = isEnabled, serverState = McpServerState.Off)

                        viewModel.changeEnabled(isEnabled = isEnabled)
                        advanceUntilIdle()

                        expectNoEvents()
                    }

                    coVerify(exactly = 0) { enableUseCase(Unit) }
                    coVerify(exactly = 0) { disableUseCase(Unit) }
                }
            }
        }

        test("확인 중에는 연결 사용을 바꿔도 저장하지 않는다") {
            runTest(mainDispatcher) {
                val enableUseCase = mockk<EnableMcpServerUseCase>()
                val viewModel =
                    viewModel(
                        enabledFlow = emptyFlow(),
                        stateFlow = flowOf(Result.success(McpServerState.Off)),
                        enableUseCase = enableUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMcpUiState.Loading

                    viewModel.changeEnabled(isEnabled = true)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 0) { enableUseCase(Unit) }
            }
        }
    }

    private fun viewModel(
        enabledFlow: Flow<Result<Boolean>>,
        stateFlow: Flow<Result<McpServerState>>,
        enableUseCase: EnableMcpServerUseCase = mockk(),
        disableUseCase: DisableMcpServerUseCase = mockk(),
    ): SettingMcpViewModel {
        val getEnabledUseCase = mockk<GetMcpServerEnabledUseCase>()
        every { getEnabledUseCase(Unit) } returns enabledFlow
        val getStateUseCase = mockk<GetMcpServerStateUseCase>()
        every { getStateUseCase(Unit) } returns stateFlow

        return SettingMcpViewModel(
            getMcpServerEnabledUseCase = getEnabledUseCase,
            getMcpServerStateUseCase = getStateUseCase,
            enableMcpServerUseCase = enableUseCase,
            disableMcpServerUseCase = disableUseCase,
        )
    }
}
