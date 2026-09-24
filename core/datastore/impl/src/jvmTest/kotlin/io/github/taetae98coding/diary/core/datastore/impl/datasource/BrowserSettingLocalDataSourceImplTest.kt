package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.datastore.impl.BrowserSettingData
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

private const val PROFILE_DIRECTORY = "Profile 1"

class BrowserSettingLocalDataSourceImplTest :
    FunSpec({
        test("TC-SETTING-BROWSER-DATA-001 보관된 것이 없으면 고른 프로필이 없음을 제공한다") {
            val dataSource = BrowserSettingLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(BrowserSettingData())))

            dataSource.getChromeSessionProfileDirectory().first() shouldBe ""
        }

        test("TC-SETTING-BROWSER-DATA-002 저장한 선택을 이후 조회에서 제공한다") {
            listOf(PROFILE_DIRECTORY, "").forEach { directory ->
                val dataSource = BrowserSettingLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(BrowserSettingData())))

                dataSource.setChromeSessionProfileDirectory(directory = directory)

                dataSource.getChromeSessionProfileDirectory().first() shouldBe directory
            }
        }

        test("TC-SETTING-BROWSER-DATA-004 보관된 선택이 바뀌면 바뀐 값을 이어서 제공한다") {
            val settingFlow = MutableStateFlow(BrowserSettingData())
            val dataSource = BrowserSettingLocalDataSourceImpl(dataStore = mockDataStore(settingFlow))

            dataSource.getChromeSessionProfileDirectory().test {
                awaitItem() shouldBe ""

                dataSource.setChromeSessionProfileDirectory(directory = PROFILE_DIRECTORY)

                awaitItem() shouldBe PROFILE_DIRECTORY
            }
        }
    })

private fun mockDataStore(settingFlow: MutableStateFlow<BrowserSettingData>): DataStore<BrowserSettingData> =
    mockk {
        every { data } returns settingFlow
        coEvery { updateData(any()) } coAnswers {
            val transform = firstArg<suspend (BrowserSettingData) -> BrowserSettingData>()

            settingFlow.value = transform(settingFlow.value)
            settingFlow.value
        }
    }
