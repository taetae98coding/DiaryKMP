package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.datastore.impl.MusicDownloadProxySettingData
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

private const val ADDRESS = "http://192.168.0.10:27180"

class MusicDownloadProxySettingLocalDataSourceImplTest :
    FunSpec({
        test("TC-SETTING-DOWNLOAD-DATA-002 보관된 것이 없으면 비어 있는 주소를 제공한다") {
            val dataSource = MusicDownloadProxySettingLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(MusicDownloadProxySettingData())))

            dataSource.getAddress().first() shouldBe ""
        }

        test("TC-SETTING-DOWNLOAD-DATA-001 저장한 주소를 이후 조회에서 제공한다") {
            listOf(ADDRESS, "").forEach { address ->
                val dataSource = MusicDownloadProxySettingLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(MusicDownloadProxySettingData())))

                dataSource.upsertAddress(address = address)

                dataSource.getAddress().first() shouldBe address
            }
        }

        test("보관된 주소가 바뀌면 바뀐 값을 이어서 제공한다") {
            val settingFlow = MutableStateFlow(MusicDownloadProxySettingData())
            val dataSource = MusicDownloadProxySettingLocalDataSourceImpl(dataStore = mockDataStore(settingFlow))

            dataSource.getAddress().test {
                awaitItem() shouldBe ""

                dataSource.upsertAddress(address = ADDRESS)

                awaitItem() shouldBe ADDRESS
            }
        }
    })

private fun mockDataStore(settingFlow: MutableStateFlow<MusicDownloadProxySettingData>): DataStore<MusicDownloadProxySettingData> =
    mockk {
        every { data } returns settingFlow
        coEvery { updateData(any()) } coAnswers {
            val transform = firstArg<suspend (MusicDownloadProxySettingData) -> MusicDownloadProxySettingData>()

            settingFlow.value = transform(settingFlow.value)
            settingFlow.value
        }
    }
