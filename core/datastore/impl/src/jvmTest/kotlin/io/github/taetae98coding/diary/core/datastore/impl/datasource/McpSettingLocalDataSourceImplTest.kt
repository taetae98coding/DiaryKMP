package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.datastore.impl.McpSettingData
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
import kotlinx.coroutines.flow.first

class McpSettingLocalDataSourceImplTest :
    FunSpec({
        test("TC-af4cbda8-1a65-4f20-bc57-160e76364072 저장한 적이 없으면 저장된 값이 없다") {
            val dataSource = McpSettingLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(McpSettingData())))

            dataSource.getEnabled().first() shouldBe null
        }

        test("저장한 값을 이어서 제공한다") {
            val settingFlow = MutableStateFlow(McpSettingData())
            val dataStore = mockDataStore(settingFlow)
            val dataSource = McpSettingLocalDataSourceImpl(dataStore = dataStore)
            val isEnabled = Arb.boolean().next()

            dataSource.getEnabled().test {
                awaitItem() shouldBe null

                dataSource.upsertEnabled(isEnabled = isEnabled)
                awaitItem() shouldBe isEnabled

                dataSource.upsertEnabled(isEnabled = !isEnabled)
                awaitItem() shouldBe !isEnabled
            }
            coVerify(exactly = 2) { dataStore.updateData(any()) }
        }
    })

private fun mockDataStore(settingFlow: MutableStateFlow<McpSettingData>): DataStore<McpSettingData> =
    mockk<DataStore<McpSettingData>>().also { dataStore ->
        every { dataStore.data } returns settingFlow
        coEvery { dataStore.updateData(any()) } coAnswers {
            val updated = firstArg<suspend (McpSettingData) -> McpSettingData>().invoke(settingFlow.value)

            settingFlow.value = updated
            updated
        }
    }
