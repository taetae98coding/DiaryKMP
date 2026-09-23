@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.gemini.model

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.github.taetae98coding.diary.core.model.gemini.GeminiModel
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import io.github.taetae98coding.diary.domain.setting.usecase.FetchGeminiModelUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private const val API_KEY = "testApiKey"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class SettingGeminiModelViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-SETTING-GEMINI-FEATURE-003 만들어지는 것만으로는 모델을 받아 오지 않는다") {
            runTest(mainDispatcher) {
                val fetchGeminiModelUseCase = mockk<FetchGeminiModelUseCase>()
                val viewModel = SettingGeminiModelViewModel(fetchGeminiModelUseCase = fetchGeminiModelUseCase)

                advanceUntilIdle()

                viewModel.uiState.value shouldBe SettingGeminiModelUiState()
                coVerify(exactly = 0) { fetchGeminiModelUseCase(any()) }
            }
        }

        test("TC-SETTING-GEMINI-FEATURE-005 전달한 인증 정보로 조회한다") {
            runTest(mainDispatcher) {
                val fetchGeminiModelUseCase = mockk<FetchGeminiModelUseCase>()
                coEvery { fetchGeminiModelUseCase(any()) } returns Result.success(emptyList())
                val viewModel = SettingGeminiModelViewModel(fetchGeminiModelUseCase = fetchGeminiModelUseCase)

                viewModel.fetch(apiKey = "editingApiKey")
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchGeminiModelUseCase("editingApiKey") }
                coVerify(exactly = 0) { fetchGeminiModelUseCase(neq("editingApiKey")) }
            }
        }

        test("TC-SETTING-GEMINI-FEATURE-006 조회하는 동안 진행 상태를 제공한다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<List<GeminiModel>>>()
                val fetchGeminiModelUseCase = mockk<FetchGeminiModelUseCase>()
                coEvery { fetchGeminiModelUseCase(API_KEY) } coAnswers { completion.await() }
                val viewModel = SettingGeminiModelViewModel(fetchGeminiModelUseCase = fetchGeminiModelUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingGeminiModelUiState()

                    viewModel.fetch(apiKey = API_KEY)
                    advanceUntilIdle()

                    awaitItem().isInProgress shouldBe true

                    completion.complete(Result.success(emptyList()))
                    advanceUntilIdle()

                    awaitItem().isLoaded shouldBe true
                    awaitItem().isInProgress shouldBe false
                }
            }
        }

        test("TC-SETTING-GEMINI-FEATURE-007 조회 중에는 같은 조회 요청을 처리하지 않는다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<List<GeminiModel>>>()
                val fetchGeminiModelUseCase = mockk<FetchGeminiModelUseCase>()
                coEvery { fetchGeminiModelUseCase(API_KEY) } coAnswers { completion.await() }
                val viewModel = SettingGeminiModelViewModel(fetchGeminiModelUseCase = fetchGeminiModelUseCase)

                viewModel.fetch(apiKey = API_KEY)
                advanceUntilIdle()
                viewModel.fetch(apiKey = API_KEY)
                advanceUntilIdle()

                completion.complete(Result.success(emptyList()))
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchGeminiModelUseCase(API_KEY) }
            }
        }

        test("TC-SETTING-GEMINI-FEATURE-008 조회에 성공하면 받아 온 모델을 제공한다") {
            runTest(mainDispatcher) {
                val modelList = fixtureMonkey.giveMe<GeminiModel>(3)
                val fetchGeminiModelUseCase = mockk<FetchGeminiModelUseCase>()
                coEvery { fetchGeminiModelUseCase(API_KEY) } returns Result.success(modelList)
                val viewModel = SettingGeminiModelViewModel(fetchGeminiModelUseCase = fetchGeminiModelUseCase)

                viewModel.fetch(apiKey = API_KEY)
                advanceUntilIdle()

                val uiState = viewModel.uiState.value
                uiState.isLoaded shouldBe true
                uiState.modelList shouldBe modelList
                uiState.failure shouldBe null
            }
        }

        test("TC-SETTING-GEMINI-FEATURE-009 고를 수 있는 모델이 없으면 빈 목록을 받은 상태로 제공한다") {
            runTest(mainDispatcher) {
                val fetchGeminiModelUseCase = mockk<FetchGeminiModelUseCase>()
                coEvery { fetchGeminiModelUseCase(API_KEY) } returns Result.success(emptyList())
                val viewModel = SettingGeminiModelViewModel(fetchGeminiModelUseCase = fetchGeminiModelUseCase)

                viewModel.fetch(apiKey = API_KEY)
                advanceUntilIdle()

                val uiState = viewModel.uiState.value
                uiState.isLoaded shouldBe true
                uiState.modelList shouldBe emptyList()
                uiState.failure shouldBe null
            }
        }

        test("TC-SETTING-GEMINI-FEATURE-010 조회 실패의 원인을 구분해 제공한다") {
            val failureCases =
                listOf(
                    GeminiApiKeyInvalidException() to SettingGeminiModelFailure.INVALID_API_KEY,
                    IllegalStateException("server error") to SettingGeminiModelFailure.UNKNOWN,
                )

            failureCases.forEach { (throwable, expected) ->
                runTest(mainDispatcher) {
                    val fetchGeminiModelUseCase = mockk<FetchGeminiModelUseCase>()
                    coEvery { fetchGeminiModelUseCase(API_KEY) } returns Result.failure(throwable)
                    val viewModel = SettingGeminiModelViewModel(fetchGeminiModelUseCase = fetchGeminiModelUseCase)

                    viewModel.fetch(apiKey = API_KEY)
                    advanceUntilIdle()

                    val uiState = viewModel.uiState.value
                    uiState.failure shouldBe expected
                    uiState.isLoaded shouldBe false
                }
            }
        }

        test("TC-SETTING-GEMINI-FEATURE-011 조회에 실패해도 받아 둔 목록을 유지한다") {
            val failureCases =
                listOf(
                    GeminiApiKeyInvalidException() to SettingGeminiModelFailure.INVALID_API_KEY,
                    IllegalStateException("server error") to SettingGeminiModelFailure.UNKNOWN,
                )

            failureCases.forEach { (throwable, expected) ->
                runTest(mainDispatcher) {
                    val modelList = fixtureMonkey.giveMe<GeminiModel>(2)
                    val fetchGeminiModelUseCase = mockk<FetchGeminiModelUseCase>()
                    coEvery { fetchGeminiModelUseCase(API_KEY) } returnsMany
                        listOf(
                            Result.success(modelList),
                            Result.failure(throwable),
                        )
                    val viewModel = SettingGeminiModelViewModel(fetchGeminiModelUseCase = fetchGeminiModelUseCase)

                    viewModel.fetch(apiKey = API_KEY)
                    advanceUntilIdle()

                    viewModel.effect.test {
                        viewModel.fetch(apiKey = API_KEY)
                        advanceUntilIdle()

                        awaitItem() shouldBe expected
                    }

                    val uiState = viewModel.uiState.value
                    uiState.isLoaded shouldBe true
                    uiState.modelList shouldBe modelList
                }
            }
        }

        test("받아 둔 목록이 없는 실패는 스낵바로 알리지 않는다") {
            runTest(mainDispatcher) {
                val fetchGeminiModelUseCase = mockk<FetchGeminiModelUseCase>()
                coEvery { fetchGeminiModelUseCase(API_KEY) } returns Result.failure(IllegalStateException("server error"))
                val viewModel = SettingGeminiModelViewModel(fetchGeminiModelUseCase = fetchGeminiModelUseCase)

                viewModel.effect.test {
                    viewModel.fetch(apiKey = API_KEY)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("다시 조회하면 직전 실패 표시를 지운다") {
            runTest(mainDispatcher) {
                val modelList = fixtureMonkey.giveMe<GeminiModel>(2)
                val fetchGeminiModelUseCase = mockk<FetchGeminiModelUseCase>()
                coEvery { fetchGeminiModelUseCase(API_KEY) } returnsMany
                    listOf(
                        Result.failure(IllegalStateException("server error")),
                        Result.success(modelList),
                    )
                val viewModel = SettingGeminiModelViewModel(fetchGeminiModelUseCase = fetchGeminiModelUseCase)

                viewModel.fetch(apiKey = API_KEY)
                advanceUntilIdle()
                viewModel.uiState.value.failure shouldBe SettingGeminiModelFailure.UNKNOWN

                viewModel.fetch(apiKey = API_KEY)
                advanceUntilIdle()

                val uiState = viewModel.uiState.value
                uiState.failure shouldBe null
                uiState.modelList shouldBe modelList
            }
        }

        test("조회가 끝나면 다시 조회할 수 있다") {
            runTest(mainDispatcher) {
                val fetchGeminiModelUseCase = mockk<FetchGeminiModelUseCase>()
                coEvery { fetchGeminiModelUseCase(API_KEY) } returns Result.success(emptyList())
                val viewModel = SettingGeminiModelViewModel(fetchGeminiModelUseCase = fetchGeminiModelUseCase)

                viewModel.fetch(apiKey = API_KEY)
                advanceUntilIdle()
                viewModel.fetch(apiKey = API_KEY)
                advanceUntilIdle()

                coVerify(exactly = 2) { fetchGeminiModelUseCase(API_KEY) }
            }
        }
    }
}
