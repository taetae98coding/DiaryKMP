package io.github.taetae98coding.diary.data.setting.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.GeminiSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GeminiSettingRepositoryImplTest :
    FunSpec({
        test("TC-SETTING-GEMINI-DATA-001 저장된 설정이 없으면 비어 있는 설정을 제공한다") {
            val repository = createRepository(MutableStateFlow(GeminiSetting.EMPTY.toLocalEntity()))

            repository.get().first() shouldBe GeminiSetting.EMPTY
        }

        test("TC-SETTING-GEMINI-DATA-002 저장한 설정을 이후 조회에서 제공한다") {
            val settingList =
                listOf(
                    fixtureMonkey.giveMeOne<GeminiSetting>(),
                    fixtureMonkey.giveMeOne<GeminiSetting>().copy(systemPrompt = "첫 줄\n둘째 줄"),
                    GeminiSetting.EMPTY,
                )

            settingList.forEach { setting ->
                val repository = createRepository(MutableStateFlow(GeminiSetting.EMPTY.toLocalEntity()))

                repository.upsert(setting = setting)

                repository.get().first() shouldBe setting
            }
        }

        test("TC-SETTING-GEMINI-DATA-003 다시 저장하면 마지막에 저장한 설정을 제공한다") {
            val first = fixtureMonkey.giveMeOne<GeminiSetting>()
            val second = fixtureMonkey.giveMeOne<GeminiSetting>()
            val last = fixtureMonkey.giveMeOne<GeminiSetting>()
            val repository = createRepository(MutableStateFlow(first.toLocalEntity()))

            repository.upsert(setting = second)
            repository.upsert(setting = last)

            repository.get().first() shouldBe last
        }

        test("TC-SETTING-GEMINI-DATA-004 값 하나만 바꿔 저장해도 세 값이 함께 기록된다") {
            val stored = fixtureMonkey.giveMeOne<GeminiSetting>()
            val repository = createRepository(MutableStateFlow(stored.toLocalEntity()))
            val changed = stored.copy(systemPrompt = stored.systemPrompt + "-changed")

            repository.upsert(setting = changed)

            val actual = repository.get().first()
            actual.systemPrompt shouldBe changed.systemPrompt
            actual.apiKey shouldBe stored.apiKey
            actual.model shouldBe stored.model
        }

        test("TC-SETTING-GEMINI-DATA-005 조회 중에 설정이 바뀌면 이어서 새 값을 제공한다") {
            val stored = fixtureMonkey.giveMeOne<GeminiSetting>()
            val next = fixtureMonkey.giveMeOne<GeminiSetting>()
            val repository = createRepository(MutableStateFlow(stored.toLocalEntity()))

            repository.get().test {
                awaitItem() shouldBe stored

                repository.upsert(setting = next)

                awaitItem() shouldBe next
            }
        }

        test("TC-SETTING-GEMINI-DATA-006 저장된 값을 읽을 수 없으면 비어 있는 설정을 제공하지 않고 실패를 그대로 알린다") {
            val failure = IllegalStateException("gemini setting read failed")
            val repository =
                GeminiSettingRepositoryImpl(
                    geminiSettingLocalDataSource =
                        mockk {
                            every { get() } returns flow { throw failure }
                        },
                )

            val thrown = shouldThrow<IllegalStateException> { repository.get().first() }

            thrown shouldBe failure
        }

        test("보관된 설정이 비워지면 비어 있는 설정을 제공한다") {
            val stored = fixtureMonkey.giveMeOne<GeminiSetting>()
            val settingFlow = MutableStateFlow(stored.toLocalEntity())
            val repository = createRepository(settingFlow)

            repository.get().test {
                awaitItem() shouldBe stored

                settingFlow.value = GeminiSetting.EMPTY.toLocalEntity()

                awaitItem() shouldBe GeminiSetting.EMPTY
            }
        }
    })

private fun GeminiSetting.toLocalEntity(): GeminiSettingLocalEntity =
    GeminiSettingLocalEntity(
        apiKey = apiKey,
        model = model,
        systemPrompt = systemPrompt,
    )

private fun createRepository(settingFlow: MutableStateFlow<GeminiSettingLocalEntity>): GeminiSettingRepositoryImpl =
    GeminiSettingRepositoryImpl(
        geminiSettingLocalDataSource =
            mockk<GeminiSettingLocalDataSource> {
                every { get() } returns settingFlow
                coEvery { upsert(any()) } coAnswers {
                    settingFlow.value = firstArg()
                }
            },
    )
