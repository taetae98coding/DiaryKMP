package io.github.taetae98coding.diary.work.mcp.repository

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.McpSettingLocalDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class McpSettingRepositoryImplTest :
    FunSpec({
        test("TC-af4cbda8-1a65-4f20-bc57-160e76364072 한 번도 바꾸지 않은 연결 사용 설정은 꺼짐이다") {
            val dataSource = mockk<McpSettingLocalDataSource>()
            every { dataSource.getEnabled() } returns MutableStateFlow(null)
            val repository = McpSettingRepositoryImpl(mcpSettingLocalDataSource = dataSource)

            repository.getEnabled().test {
                awaitItem() shouldBe false
            }
        }

        test("TC-af4cbda8-1a65-4f20-bc57-160e76364072 저장된 연결 사용 설정을 그대로 제공한다") {
            val enabledFlow = MutableStateFlow<Boolean?>(Arb.boolean().next())
            val dataSource = mockk<McpSettingLocalDataSource>()
            every { dataSource.getEnabled() } returns enabledFlow
            val repository = McpSettingRepositoryImpl(mcpSettingLocalDataSource = dataSource)

            repository.getEnabled().test {
                val first = enabledFlow.value!!
                awaitItem() shouldBe first

                enabledFlow.value = !first

                awaitItem() shouldBe !first
            }
        }

        test("TC-30ea3b2b-ac06-4fc0-a7b3-9f87fa6aecf0 바꾼 적 없으면 꺼짐, 저장된 값은 그대로다") {
            listOf<Pair<Boolean?, Boolean>>(null to false, false to false, true to true).forEach { (stored, expected) ->
                val dataSource = mockk<McpSettingLocalDataSource>()
                every { dataSource.getEnabled() } returns MutableStateFlow(stored)
                val repository = McpSettingRepositoryImpl(mcpSettingLocalDataSource = dataSource)

                repository.getEnabled().test {
                    awaitItem() shouldBe expected
                }
            }
        }

        test("연결 사용 설정을 저장한다") {
            val isEnabled = Arb.boolean().next()
            val dataSource = mockk<McpSettingLocalDataSource>()
            coEvery { dataSource.upsertEnabled(isEnabled = any()) } returns Unit
            val repository = McpSettingRepositoryImpl(mcpSettingLocalDataSource = dataSource)

            repository.setEnabled(isEnabled = isEnabled)

            coVerify(exactly = 1) { dataSource.upsertEnabled(isEnabled = isEnabled) }
        }
    })
