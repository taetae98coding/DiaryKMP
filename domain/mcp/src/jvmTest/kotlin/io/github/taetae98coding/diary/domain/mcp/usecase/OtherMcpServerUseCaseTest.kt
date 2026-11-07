package io.github.taetae98coding.diary.domain.mcp.usecase

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.mcp.McpServerState
import io.github.taetae98coding.diary.domain.mcp.manager.McpServerManager
import io.github.taetae98coding.diary.domain.mcp.repository.McpSettingRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class OtherMcpServerUseCaseTest :
    BehaviorSpec({
        Given("연결 사용 설정을 저장할 수 있다") {
            val repository = mockk<McpSettingRepository>()
            coEvery { repository.setEnabled(isEnabled = any()) } returns Unit

            When("연결을 켠다") {
                val result = EnableMcpServerUseCase(mcpSettingRepository = repository)(parameter = Unit)

                Then("켬을 저장한다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.setEnabled(isEnabled = true) }
                }
            }

            When("연결을 끈다") {
                val result = DisableMcpServerUseCase(mcpSettingRepository = repository)(parameter = Unit)

                Then("꺼짐을 저장한다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.setEnabled(isEnabled = false) }
                }
            }
        }

        Given("연결 사용 설정이 저장되어 있다") {
            val enabledFlow = MutableStateFlow(Arb.boolean().next())
            val repository = mockk<McpSettingRepository>()
            every { repository.getEnabled() } returns enabledFlow

            When("연결 사용 설정을 구독한다") {
                Then("저장된 값과 바뀐 값을 차례로 받는다") {
                    GetMcpServerEnabledUseCase(mcpSettingRepository = repository)(parameter = Unit).test {
                        val first = enabledFlow.value
                        awaitItem().shouldBeSuccess() shouldBe first

                        enabledFlow.value = !first

                        awaitItem().shouldBeSuccess() shouldBe !first
                    }
                }
            }
        }

        Given("연결 상태를 확인할 수 있다") {
            val address = Arb.string().next()
            val stateFlow = MutableStateFlow<McpServerState>(McpServerState.Off)
            val manager = mockk<McpServerManager>()
            every { manager.getState() } returns stateFlow
            every { manager.isSupported() } returns true

            When("연결 상태를 구독한다") {
                Then("상태가 바뀔 때마다 받는다") {
                    GetMcpServerStateUseCase(mcpServerManager = manager)(parameter = Unit).test {
                        awaitItem().shouldBeSuccess() shouldBe McpServerState.Off

                        stateFlow.value = McpServerState.Running(address = address)

                        awaitItem().shouldBeSuccess() shouldBe McpServerState.Running(address = address)
                    }
                }
            }

            When("연결을 제공하는지 확인한다") {
                val result = ReadMcpServerSupportUseCase(mcpServerManager = manager)(parameter = Unit)

                Then("제공 여부를 그대로 돌려준다") {
                    result.shouldBeSuccess() shouldBe true
                }
            }
        }
    })
