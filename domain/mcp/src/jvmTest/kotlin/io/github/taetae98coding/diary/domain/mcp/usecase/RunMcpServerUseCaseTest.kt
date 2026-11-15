package io.github.taetae98coding.diary.domain.mcp.usecase

import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.mcp.manager.McpServerManager
import io.github.taetae98coding.diary.domain.mcp.repository.McpSettingRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class RunMcpServerUseCaseTest :
    BehaviorSpec({
        Given("연결 사용 설정이 켬으로 저장되어 있다") {
            When("앱이 시작된다") {
                Then("TC-af4cbda8-1a65-4f20-bc57-160e76364072 연결을 시작한다") {
                    runTest {
                        val manager = serverManager()
                        val useCase = RunMcpServerUseCase(mcpSettingRepository = settingRepository(MutableStateFlow(true)), mcpServerManager = manager)

                        backgroundScope.launch { useCase() }
                        runCurrent()

                        coVerify(exactly = 1) { manager.start() }
                        coVerify(exactly = 0) { manager.stop() }
                    }
                }
            }
        }

        Given("연결 사용 설정이 꺼짐으로 저장되어 있다") {
            When("앱이 시작된다") {
                Then("TC-af4cbda8-1a65-4f20-bc57-160e76364072 연결을 시작하지 않고 꺼짐으로 둔다") {
                    runTest {
                        val manager = serverManager()
                        val useCase = RunMcpServerUseCase(mcpSettingRepository = settingRepository(MutableStateFlow(false)), mcpServerManager = manager)

                        backgroundScope.launch { useCase() }
                        runCurrent()

                        coVerify(exactly = 0) { manager.start() }
                        coVerify(exactly = 1) { manager.stop() }
                    }
                }
            }
        }

        Given("앱이 실행 중이고 연결이 꺼져 있다") {
            When("연결 사용 설정이 켬으로 바뀐다") {
                Then("TC-ed294eaa-40c8-48a5-b9d8-e5b2c70ccbdc 연결을 바로 시작한다") {
                    runTest {
                        val enabledFlow = MutableStateFlow(false)
                        val manager = serverManager()
                        val useCase = RunMcpServerUseCase(mcpSettingRepository = settingRepository(enabledFlow), mcpServerManager = manager)
                        backgroundScope.launch { useCase() }
                        runCurrent()
                        clearMocks(manager, answers = false)

                        enabledFlow.value = true
                        runCurrent()

                        coVerify(exactly = 1) { manager.start() }
                        coVerify(exactly = 0) { manager.stop() }
                    }
                }
            }
        }

        Given("앱이 실행 중이고 연결이 동작 중이거나 시작에 실패했다") {
            When("연결 사용 설정이 꺼짐으로 바뀐다") {
                Then("TC-ed294eaa-40c8-48a5-b9d8-e5b2c70ccbdc 연결을 바로 끝낸다") {
                    runTest {
                        val enabledFlow = MutableStateFlow(true)
                        val manager = serverManager()
                        val useCase = RunMcpServerUseCase(mcpSettingRepository = settingRepository(enabledFlow), mcpServerManager = manager)
                        backgroundScope.launch { useCase() }
                        runCurrent()

                        enabledFlow.value = false
                        runCurrent()

                        coVerifyOrder {
                            manager.start()
                            manager.stop()
                        }
                        coVerify(exactly = 1) { manager.start() }
                        coVerify(exactly = 1) { manager.stop() }
                    }
                }
            }
        }

        Given("앱이 실행 중이고 연결 사용 설정이 켬이다") {
            When("같은 설정 값이 다시 전달된다") {
                Then("연결을 다시 시작하지 않는다") {
                    runTest {
                        val manager = serverManager()
                        val useCase = RunMcpServerUseCase(mcpSettingRepository = settingRepository(flowOf(true, true, true)), mcpServerManager = manager)

                        backgroundScope.launch { useCase() }
                        runCurrent()

                        coVerify(exactly = 1) { manager.start() }
                    }
                }
            }

            When("연결을 시작하는 동안 설정이 꺼짐으로 바뀐다") {
                Then("시작을 기다리지 않고 연결을 끝낸다") {
                    runTest {
                        val enabledFlow = MutableStateFlow(true)
                        val manager = serverManager()
                        coEvery { manager.start() } coAnswers { awaitCancellation() }
                        val useCase = RunMcpServerUseCase(mcpSettingRepository = settingRepository(enabledFlow), mcpServerManager = manager)
                        backgroundScope.launch { useCase() }
                        runCurrent()

                        enabledFlow.value = false
                        runCurrent()

                        coVerify(exactly = 1) { manager.stop() }
                    }
                }
            }
        }

        Given("연결 사용 설정을 아직 확인하지 못했다") {
            When("앱이 시작된다") {
                Then("TC-641ab193-7241-40bc-8dbe-b972d7f8fb28 연결을 시작하지 않는다") {
                    runTest {
                        val manager = serverManager()
                        val useCase = RunMcpServerUseCase(mcpSettingRepository = settingRepository(flow { awaitCancellation() }), mcpServerManager = manager)

                        val result = async { useCase() }
                        runCurrent()

                        result.isCompleted shouldBe false
                        coVerify(exactly = 0) { manager.start() }
                        result.cancel()
                    }
                }
            }
        }

        Given("연결 사용 설정을 읽지 못한다") {
            val failure = IllegalStateException(Arb.string().next())

            When("앱이 시작된다") {
                Then("TC-641ab193-7241-40bc-8dbe-b972d7f8fb28 연결을 시작하지 않고 실패로 끝난다") {
                    runTest {
                        val manager = serverManager()
                        val useCase = RunMcpServerUseCase(mcpSettingRepository = settingRepository(flow { throw failure }), mcpServerManager = manager)

                        val result = useCase()

                        result.shouldBeFailure<IllegalStateException>().message shouldBe failure.message
                        coVerify(exactly = 0) { manager.start() }
                    }
                }
            }

            When("설정 값이 전달되지 않은 채 끝난다") {
                Then("TC-641ab193-7241-40bc-8dbe-b972d7f8fb28 연결을 시작하지 않는다") {
                    runTest {
                        val manager = serverManager()
                        val useCase = RunMcpServerUseCase(mcpSettingRepository = settingRepository(emptyFlow()), mcpServerManager = manager)

                        useCase()

                        coVerify(exactly = 0) { manager.start() }
                    }
                }
            }
        }
    })

private fun settingRepository(enabledFlow: Flow<Boolean>): McpSettingRepository =
    mockk<McpSettingRepository>().also { repository ->
        every { repository.getEnabled() } returns enabledFlow
    }

private fun serverManager(): McpServerManager =
    mockk<McpServerManager>().also { manager ->
        coEvery { manager.start() } returns Unit
        coEvery { manager.stop() } returns Unit
    }
