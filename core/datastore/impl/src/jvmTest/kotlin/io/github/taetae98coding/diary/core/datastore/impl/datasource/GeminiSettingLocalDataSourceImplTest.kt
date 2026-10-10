package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.GeminiSettingData
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GeminiSettingLocalDataSourceImplTest :
    FunSpec({
        test("보관된 것이 없으면 비어 있는 설정을 제공한다") {
            val dataSource = GeminiSettingLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(GeminiSettingData())))

            dataSource.get().first() shouldBe GeminiSettingLocalEntity(apiKey = "", model = "", systemPrompt = "")
        }

        test("보관된 설정을 그대로 제공한다") {
            val stored = fixtureMonkey.giveMeOne<GeminiSettingData>()
            val dataSource = GeminiSettingLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(stored)))

            dataSource.get().first() shouldBe
                GeminiSettingLocalEntity(
                    apiKey = stored.apiKey,
                    model = stored.model,
                    systemPrompt = stored.systemPrompt,
                )
        }

        test("보관된 설정이 바뀌면 바뀐 설정을 이어서 제공한다") {
            val settingFlow = MutableStateFlow(GeminiSettingData())
            val dataSource = GeminiSettingLocalDataSourceImpl(dataStore = mockDataStore(settingFlow))
            val entity = fixtureMonkey.giveMeOne<GeminiSettingLocalEntity>().copy(apiKey = fixtureMonkey.nonBlankString())

            dataSource.get().test {
                awaitItem() shouldBe GeminiSettingLocalEntity(apiKey = "", model = "", systemPrompt = "")

                dataSource.upsert(setting = entity)

                awaitItem() shouldBe GeminiSettingLocalEntity(apiKey = entity.apiKey, model = entity.model, systemPrompt = entity.systemPrompt)
            }
        }

        test("세 값을 DataStore의 한 번의 갱신으로 저장한다") {
            val settingFlow = MutableStateFlow(fixtureMonkey.giveMeOne<GeminiSettingData>())
            val dataStore = mockDataStore(settingFlow)
            val dataSource = GeminiSettingLocalDataSourceImpl(dataStore = dataStore)
            val entity = fixtureMonkey.giveMeOne<GeminiSettingLocalEntity>()

            dataSource.upsert(setting = entity)

            coVerify(exactly = 1) { dataStore.updateData(any()) }
            settingFlow.value shouldBe
                GeminiSettingData(
                    apiKey = entity.apiKey,
                    model = entity.model,
                    systemPrompt = entity.systemPrompt,
                )
        }

        test("세 값이 모두 비어 있어도 저장한다") {
            val settingFlow = MutableStateFlow(fixtureMonkey.giveMeOne<GeminiSettingData>())
            val dataSource = GeminiSettingLocalDataSourceImpl(dataStore = mockDataStore(settingFlow))

            dataSource.upsert(setting = GeminiSettingLocalEntity(apiKey = "", model = "", systemPrompt = ""))

            settingFlow.value shouldBe GeminiSettingData()
        }

        test("저장에 실패하면 실패를 그대로 알린다") {
            val failure = IllegalStateException("write failed")
            val dataStore =
                mockk<DataStore<GeminiSettingData>> {
                    coEvery { updateData(any()) } throws failure
                }
            val dataSource = GeminiSettingLocalDataSourceImpl(dataStore = dataStore)

            val actual =
                shouldThrowExactly<IllegalStateException> {
                    dataSource.upsert(setting = fixtureMonkey.giveMeOne<GeminiSettingLocalEntity>())
                }

            actual shouldBeSameInstanceAs failure
        }
    })

private fun mockDataStore(settingFlow: MutableStateFlow<GeminiSettingData>): DataStore<GeminiSettingData> =
    mockk<DataStore<GeminiSettingData>>().also { dataStore ->
        every { dataStore.data } returns settingFlow
        coEvery { dataStore.updateData(any()) } coAnswers {
            val updated = firstArg<suspend (GeminiSettingData) -> GeminiSettingData>().invoke(settingFlow.value)

            settingFlow.value = updated
            updated
        }
    }
