@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.gemini

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.domain.setting.usecase.GetGeminiSettingUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.SetGeminiSettingUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class SettingGeminiViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-b7f4e2f9-6e46-467f-a334-663f7169f362 저장된 설정을 그대로 제공한다") {
            runTest(mainDispatcher) {
                val setting = fixtureMonkey.giveMeOne<GeminiSetting>()
                val viewModel = createViewModel(settingFlow = flowOf(Result.success(setting)))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingGeminiUiState.Loading
                    awaitItem() shouldBe SettingGeminiUiState.Content(setting = setting)
                }
            }
        }

        test("TC-8449e39b-5c2c-4c7c-9b9b-7f95beb2cd6c 저장된 설정이 없으면 비어 있는 설정을 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = createViewModel(settingFlow = flowOf(Result.success(GeminiSetting.EMPTY)))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingGeminiUiState.Loading
                    awaitItem() shouldBe SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY)
                }
            }
        }

        test("TC-8cb37358-a8e6-4600-ac68-aeb09bf93d5e 설정을 확인하지 못하면 확인 중 상태를 유지한다") {
            val settingFlowList =
                listOf(
                    emptyFlow<Result<GeminiSetting>>(),
                    flowOf(Result.failure(IllegalStateException("read error"))),
                )

            settingFlowList.forEach { settingFlow ->
                runTest(mainDispatcher) {
                    val viewModel = createViewModel(settingFlow = settingFlow)

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingGeminiUiState.Loading
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-5305f042-c198-4a29-9d9b-7b490355f139 저장하면 세 값이 함께 저장된다") {
            runTest(mainDispatcher) {
                val stored = MutableStateFlow(Result.success(GeminiSetting.EMPTY))
                val setGeminiSettingUseCase = mockk<SetGeminiSettingUseCase>()
                val target = fixtureMonkey.giveMeOne<GeminiSetting>()
                coEvery { setGeminiSettingUseCase(target) } coAnswers {
                    stored.value = Result.success(target)
                    Result.success(Unit)
                }
                val viewModel = createViewModel(settingFlow = stored, setGeminiSettingUseCase = setGeminiSettingUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingGeminiUiState.Loading
                    awaitItem() shouldBe SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY)

                    viewModel.save(setting = target)
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) { setGeminiSettingUseCase(target) }
            }
        }

        test("TC-109c63d2-da57-4c7c-9db8-31e506f23317 세 값이 비어 있어도 저장한다") {
            val settingList =
                listOf(
                    GeminiSetting.EMPTY,
                    GeminiSetting.EMPTY.copy(apiKey = "apiKey"),
                    GeminiSetting.EMPTY.copy(model = "models/gemini-flash"),
                    GeminiSetting.EMPTY.copy(systemPrompt = "prompt"),
                )

            settingList.forEach { setting ->
                runTest(mainDispatcher) {
                    val setGeminiSettingUseCase = mockk<SetGeminiSettingUseCase>()
                    coEvery { setGeminiSettingUseCase(setting) } returns Result.success(Unit)
                    val viewModel = createViewModel(setGeminiSettingUseCase = setGeminiSettingUseCase)

                    viewModel.effect.test {
                        viewModel.save(setting = setting)
                        advanceUntilIdle()

                        awaitItem() shouldBe SettingGeminiEffect.SaveSucceeded
                    }

                    coVerify(exactly = 1) { setGeminiSettingUseCase(setting) }
                }
            }
        }

        test("TC-bf990bf1-0db9-445b-8837-0b8ecd081f5a 저장할 때 값의 유효성을 확인하지 않는다") {
            runTest(mainDispatcher) {
                val setting = GeminiSetting(apiKey = "invalid", model = "models/removed", systemPrompt = "")
                val setGeminiSettingUseCase = mockk<SetGeminiSettingUseCase>()
                coEvery { setGeminiSettingUseCase(setting) } returns Result.success(Unit)
                val viewModel = createViewModel(setGeminiSettingUseCase = setGeminiSettingUseCase)

                viewModel.effect.test {
                    viewModel.save(setting = setting)
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingGeminiEffect.SaveSucceeded
                }

                coVerify(exactly = 1) { setGeminiSettingUseCase(setting) }
            }
        }

        test("TC-17c7f47a-b67b-4041-a191-1d5970ccc131 저장하는 동안 진행 상태를 제공한다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Unit>>()
                val setGeminiSettingUseCase = mockk<SetGeminiSettingUseCase>()
                coEvery { setGeminiSettingUseCase(any()) } coAnswers { completion.await() }
                val viewModel =
                    createViewModel(
                        settingFlow = MutableStateFlow(Result.success(GeminiSetting.EMPTY)),
                        setGeminiSettingUseCase = setGeminiSettingUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingGeminiUiState.Loading
                    awaitItem() shouldBe SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY)

                    viewModel.save(setting = GeminiSetting.EMPTY)
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY, isInProgress = true)

                    completion.complete(Result.success(Unit))
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY, isInProgress = false)
                }
            }
        }

        test("TC-6bddc416-393f-48aa-ae7c-2bfd712bc3f8 저장 중에는 같은 저장 요청을 처리하지 않는다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Unit>>()
                val setGeminiSettingUseCase = mockk<SetGeminiSettingUseCase>()
                coEvery { setGeminiSettingUseCase(any()) } coAnswers { completion.await() }
                val viewModel =
                    createViewModel(
                        settingFlow = MutableStateFlow(Result.success(GeminiSetting.EMPTY)),
                        setGeminiSettingUseCase = setGeminiSettingUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingGeminiUiState.Loading
                    awaitItem() shouldBe SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY)

                    viewModel.save(setting = GeminiSetting.EMPTY)
                    advanceUntilIdle()
                    viewModel.save(setting = GeminiSetting.EMPTY)
                    advanceUntilIdle()

                    completion.complete(Result.success(Unit))
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) { setGeminiSettingUseCase(any()) }
            }
        }

        test("TC-4f7ca966-2ddc-4ddc-858d-befcae34e2ee 저장에 성공하면 성공을 알린다") {
            runTest(mainDispatcher) {
                val setting = fixtureMonkey.giveMeOne<GeminiSetting>()
                val setGeminiSettingUseCase = mockk<SetGeminiSettingUseCase>()
                coEvery { setGeminiSettingUseCase(setting) } returns Result.success(Unit)
                val viewModel = createViewModel(setGeminiSettingUseCase = setGeminiSettingUseCase)

                viewModel.effect.test {
                    viewModel.save(setting = setting)
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingGeminiEffect.SaveSucceeded
                    expectNoEvents()
                }
            }
        }

        test("TC-e4611d27-bb3d-41c8-b08d-0c1028146247 저장에 실패하면 실패를 알린다") {
            runTest(mainDispatcher) {
                val setting = fixtureMonkey.giveMeOne<GeminiSetting>()
                val setGeminiSettingUseCase = mockk<SetGeminiSettingUseCase>()
                coEvery { setGeminiSettingUseCase(setting) } returns Result.failure(IllegalStateException("save error"))
                val viewModel = createViewModel(setGeminiSettingUseCase = setGeminiSettingUseCase)

                viewModel.effect.test {
                    viewModel.save(setting = setting)
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingGeminiEffect.SaveFailed
                    expectNoEvents()
                }
            }
        }

        test("저장이 끝나면 다시 저장할 수 있다") {
            runTest(mainDispatcher) {
                val setting = fixtureMonkey.giveMeOne<GeminiSetting>()
                val setGeminiSettingUseCase = mockk<SetGeminiSettingUseCase>()
                coEvery { setGeminiSettingUseCase(setting) } returns Result.failure(IllegalStateException("save error"))
                val viewModel = createViewModel(setGeminiSettingUseCase = setGeminiSettingUseCase)

                viewModel.effect.test {
                    viewModel.save(setting = setting)
                    advanceUntilIdle()
                    awaitItem() shouldBe SettingGeminiEffect.SaveFailed

                    viewModel.save(setting = setting)
                    advanceUntilIdle()
                    awaitItem() shouldBe SettingGeminiEffect.SaveFailed
                }

                coVerify(exactly = 2) { setGeminiSettingUseCase(setting) }
            }
        }
    }

    private companion object {
        private fun createViewModel(
            settingFlow: kotlinx.coroutines.flow.Flow<Result<GeminiSetting>> = flowOf(Result.success(GeminiSetting.EMPTY)),
            setGeminiSettingUseCase: SetGeminiSettingUseCase = mockk(),
        ): SettingGeminiViewModel {
            val getGeminiSettingUseCase = mockk<GetGeminiSettingUseCase>()
            every { getGeminiSettingUseCase(Unit) } returns settingFlow

            return SettingGeminiViewModel(
                getGeminiSettingUseCase = getGeminiSettingUseCase,
                setGeminiSettingUseCase = setGeminiSettingUseCase,
            )
        }
    }
}
