@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.gemini

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.domain.memo.usecase.FetchMemoDraftUseCase
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import io.github.taetae98coding.diary.domain.setting.usecase.GetGeminiSettingUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

private val COMPLETE_SETTING =
    GeminiSetting(
        apiKey = "testApiKey",
        model = "models/gemini-flash",
        systemPrompt = "testSystemPrompt",
    )

private val DRAFT =
    MemoDraft(
        title = "주간 회고 정리",
        description = "## 이번 주",
        dateTime = MemoDateTime.AllDay(dateRange = LocalDate(2026, 9, 21)..LocalDate(2026, 9, 22)),
    )

private val PARAMETER =
    FetchMemoDraftUseCase.Parameter(
        prompt = "회고를 써 줘",
        title = "",
        description = "",
        dateTime = null,
    )

class MemoGeminiViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MEMO-GEMINI-FEATURE-001 설정이 갖춰져 있으면 프롬프트 입력으로 시작한다") {
            runTest(mainDispatcher) {
                val viewModel = createViewModel()
                advanceUntilIdle()

                viewModel.open()
                advanceUntilIdle()

                viewModel.uiState.test {
                    val uiState = awaitItem()

                    uiState.step shouldBe MemoGeminiStep.PROMPT
                    uiState.draft shouldBe MemoDraft.EMPTY
                    uiState.failure shouldBe null
                }
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-002 설정이 갖춰지지 않으면 열지 않고 설정 필요를 알린다") {
            listOf(
                COMPLETE_SETTING.copy(apiKey = ""),
                COMPLETE_SETTING.copy(model = ""),
                GeminiSetting.EMPTY,
            ).forEach { setting ->
                runTest(mainDispatcher) {
                    val fetchMemoDraftUseCase = mockk<FetchMemoDraftUseCase>()
                    val viewModel = createViewModel(setting = setting, fetchMemoDraftUseCase = fetchMemoDraftUseCase)
                    advanceUntilIdle()

                    viewModel.effect.test {
                        viewModel.open()
                        advanceUntilIdle()

                        awaitItem() shouldBe MemoGeminiEffect.SettingRequired
                        expectNoEvents()
                    }

                    viewModel.uiState.value.step shouldBe MemoGeminiStep.CLOSED
                    coVerify(exactly = 0) { fetchMemoDraftUseCase(any()) }
                }
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-003 설정을 확인하지 못하면 시작 동작을 제공하지 않는다") {
            runTest(mainDispatcher) {
                val viewModel = createViewModel(settingResult = Result.failure(IllegalStateException("read failed")))
                advanceUntilIdle()

                viewModel.uiState.test {
                    awaitItem().isButtonVisible shouldBe false

                    viewModel.open()
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-MEMO-GEMINI-DOMAIN-001 인증 정보와 모델이 모두 있어야 시작할 수 있다") {
            listOf(
                COMPLETE_SETTING to MemoGeminiStep.PROMPT,
                COMPLETE_SETTING.copy(systemPrompt = "") to MemoGeminiStep.PROMPT,
                COMPLETE_SETTING.copy(apiKey = "") to MemoGeminiStep.CLOSED,
                COMPLETE_SETTING.copy(model = "") to MemoGeminiStep.CLOSED,
                GeminiSetting.EMPTY to MemoGeminiStep.CLOSED,
            ).forEach { (setting, expected) ->
                runTest(mainDispatcher) {
                    val viewModel = createViewModel(setting = setting)
                    advanceUntilIdle()

                    viewModel.open()
                    advanceUntilIdle()

                    viewModel.uiState.value.step shouldBe expected
                    viewModel.uiState.value.isButtonVisible shouldBe true
                }
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-004 프롬프트가 비어 있어도 생성을 실행한다") {
            runTest(mainDispatcher) {
                val parameter = PARAMETER.copy(prompt = "")
                val fetchMemoDraftUseCase = createFetchUseCase()
                val viewModel = createViewModel(fetchMemoDraftUseCase = fetchMemoDraftUseCase)
                advanceUntilIdle()

                viewModel.open()
                viewModel.generate(parameter = parameter)
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchMemoDraftUseCase(parameter) }
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-005 생성하는 동안 진행 상태를 표시한다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<MemoDraft>>()
                val viewModel =
                    createViewModel(
                        fetchMemoDraftUseCase =
                            mockk {
                                coEvery { this@mockk(any()) } coAnswers { completion.await() }
                            },
                    )
                advanceUntilIdle()
                viewModel.open()
                advanceUntilIdle()

                viewModel.uiState.test {
                    awaitItem().step shouldBe MemoGeminiStep.PROMPT

                    viewModel.generate(parameter = PARAMETER)
                    advanceUntilIdle()

                    awaitItem().step shouldBe MemoGeminiStep.GENERATING

                    completion.complete(Result.success(DRAFT))
                    advanceUntilIdle()

                    awaitItem().step shouldBe MemoGeminiStep.RESULT
                }
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-006 생성 중에는 같은 생성 요청을 처리하지 않는다") {
            runTest(mainDispatcher) {
                val fetchMemoDraftUseCase =
                    mockk<FetchMemoDraftUseCase> {
                        coEvery { this@mockk(any()) } coAnswers { CompletableDeferred<Result<MemoDraft>>().await() }
                    }
                val viewModel = createViewModel(fetchMemoDraftUseCase = fetchMemoDraftUseCase)
                advanceUntilIdle()
                viewModel.open()

                viewModel.generate(parameter = PARAMETER)
                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchMemoDraftUseCase(any()) }
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-007 생성에 성공하면 결과를 확인할 수 있다") {
            runTest(mainDispatcher) {
                val viewModel = createViewModel()
                advanceUntilIdle()
                viewModel.open()

                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()

                val uiState = viewModel.uiState.value

                uiState.step shouldBe MemoGeminiStep.RESULT
                uiState.draft shouldBe DRAFT
                uiState.hasDraft shouldBe true
                uiState.appliedFieldSet shouldBe emptySet()
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-017 생성된 내용이 하나도 없어도 실패로 다루지 않는다") {
            runTest(mainDispatcher) {
                val viewModel = createViewModel(draft = MemoDraft.EMPTY)
                advanceUntilIdle()
                viewModel.open()

                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()

                val uiState = viewModel.uiState.value

                uiState.step shouldBe MemoGeminiStep.RESULT
                uiState.failure shouldBe null
                uiState.hasDraft shouldBe false
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-008 취소하면 요청을 멈추고 프롬프트 입력으로 돌아간다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<MemoDraft>>()
                val viewModel =
                    createViewModel(
                        fetchMemoDraftUseCase =
                            mockk {
                                coEvery { this@mockk(any()) } coAnswers { completion.await() }
                            },
                    )
                advanceUntilIdle()
                viewModel.open()
                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()

                viewModel.cancel()
                completion.complete(Result.success(DRAFT))
                advanceUntilIdle()

                val uiState = viewModel.uiState.value

                uiState.step shouldBe MemoGeminiStep.PROMPT
                uiState.draft shouldBe MemoDraft.EMPTY
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-009 취소한 뒤 다시 생성할 수 있다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<MemoDraft>>()
                val answers =
                    mutableListOf<suspend () -> Result<MemoDraft>>(
                        { completion.await() },
                        { Result.success(DRAFT) },
                    )
                val fetchMemoDraftUseCase =
                    mockk<FetchMemoDraftUseCase> {
                        coEvery { this@mockk(any()) } coAnswers { answers.removeFirst().invoke() }
                    }
                val viewModel = createViewModel(fetchMemoDraftUseCase = fetchMemoDraftUseCase)
                advanceUntilIdle()
                viewModel.open()

                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()
                viewModel.cancel()
                advanceUntilIdle()

                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()

                coVerify(exactly = 2) { fetchMemoDraftUseCase(any()) }
                viewModel.uiState.value.step shouldBe MemoGeminiStep.RESULT
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-010 생성 실패의 원인을 구분해 알린다") {
            listOf(
                GeminiApiKeyInvalidException() to MemoGeminiFailure.INVALID_API_KEY,
                IllegalStateException("generate failed") to MemoGeminiFailure.UNKNOWN,
            ).forEach { (throwable, expected) ->
                runTest(mainDispatcher) {
                    val viewModel =
                        createViewModel(
                            fetchMemoDraftUseCase =
                                mockk {
                                    coEvery { this@mockk(any()) } returns Result.failure(throwable)
                                },
                        )
                    advanceUntilIdle()
                    viewModel.open()

                    viewModel.generate(parameter = PARAMETER)
                    advanceUntilIdle()

                    val uiState = viewModel.uiState.value

                    uiState.step shouldBe MemoGeminiStep.PROMPT
                    uiState.failure shouldBe expected
                }
            }
        }

        test("다시 생성을 실행하면 직전 실패 안내를 지운다") {
            runTest(mainDispatcher) {
                val results =
                    mutableListOf(
                        Result.failure<MemoDraft>(IllegalStateException("generate failed")),
                        Result.success(DRAFT),
                    )
                val viewModel =
                    createViewModel(
                        fetchMemoDraftUseCase =
                            mockk {
                                coEvery { this@mockk(any()) } coAnswers { results.removeFirst() }
                            },
                    )
                advanceUntilIdle()
                viewModel.open()

                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()
                viewModel.uiState.value.failure shouldBe MemoGeminiFailure.UNKNOWN

                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()

                viewModel.uiState.value.failure shouldBe null
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-014 반영해도 결과 확인 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = createViewModel()
                advanceUntilIdle()
                viewModel.open()
                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()

                viewModel.markApplied(field = MemoGeminiField.TITLE)
                advanceUntilIdle()

                val uiState = viewModel.uiState.value

                uiState.step shouldBe MemoGeminiStep.RESULT
                uiState.draft shouldBe DRAFT
                uiState.appliedFieldSet shouldBe setOf(MemoGeminiField.TITLE)
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-019 닫으면 결과를 버린다") {
            runTest(mainDispatcher) {
                val viewModel = createViewModel()
                advanceUntilIdle()
                viewModel.open()
                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()
                viewModel.markApplied(field = MemoGeminiField.TITLE)
                advanceUntilIdle()

                viewModel.close()
                advanceUntilIdle()
                viewModel.open()
                advanceUntilIdle()

                val uiState = viewModel.uiState.value

                uiState.step shouldBe MemoGeminiStep.PROMPT
                uiState.draft shouldBe MemoDraft.EMPTY
                uiState.appliedFieldSet shouldBe emptySet()
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-025 도우미를 닫으면 반영 표시가 사라진다") {
            runTest(mainDispatcher) {
                val viewModel = createViewModel()
                advanceUntilIdle()
                viewModel.open()
                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()
                viewModel.markApplied(field = MemoGeminiField.TITLE)
                advanceUntilIdle()

                viewModel.close()
                viewModel.open()
                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()

                val uiState = viewModel.uiState.value

                uiState.step shouldBe MemoGeminiStep.RESULT
                uiState.appliedFieldSet shouldBe emptySet()
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-026 다른 화면에서 저장한 설정으로 다음 시작을 판정한다") {
            runTest(mainDispatcher) {
                val settingFlow = MutableStateFlow(Result.success(COMPLETE_SETTING.copy(apiKey = "")))
                val viewModel = createViewModel(settingFlow = settingFlow)
                advanceUntilIdle()

                viewModel.effect.test {
                    viewModel.open()
                    advanceUntilIdle()
                    awaitItem() shouldBe MemoGeminiEffect.SettingRequired

                    settingFlow.value = Result.success(COMPLETE_SETTING)
                    advanceUntilIdle()
                    viewModel.open()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                viewModel.uiState.value.step shouldBe MemoGeminiStep.PROMPT
            }
        }

        test("TC-MEMO-GEMINI-FEATURE-020 생성 중에 닫으면 요청을 멈춘다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<MemoDraft>>()
                val viewModel =
                    createViewModel(
                        fetchMemoDraftUseCase =
                            mockk {
                                coEvery { this@mockk(any()) } coAnswers { completion.await() }
                            },
                    )
                advanceUntilIdle()
                viewModel.open()
                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()

                viewModel.close()
                completion.complete(Result.success(DRAFT))
                advanceUntilIdle()

                val uiState = viewModel.uiState.value

                uiState.step shouldBe MemoGeminiStep.CLOSED
                uiState.draft shouldBe MemoDraft.EMPTY
            }
        }
    }

    private companion object {
        private fun createFetchUseCase(draft: MemoDraft = DRAFT): FetchMemoDraftUseCase =
            mockk {
                coEvery { this@mockk(any()) } returns Result.success(draft)
            }
    }
}

// 화면이 늘 상태를 구독하므로, 테스트도 같은 조건에서 관찰하도록 구독을 먼저 시작한다.
private fun TestScope.createViewModel(
    setting: GeminiSetting = COMPLETE_SETTING,
    settingResult: Result<GeminiSetting> = Result.success(setting),
    settingFlow: Flow<Result<GeminiSetting>> = flowOf(settingResult),
    draft: MemoDraft = DRAFT,
    fetchMemoDraftUseCase: FetchMemoDraftUseCase =
        mockk {
            coEvery { this@mockk(any()) } returns Result.success(draft)
        },
): MemoGeminiViewModel {
    val viewModel =
        MemoGeminiViewModel(
            getGeminiSettingUseCase =
                mockk {
                    every { this@mockk(Unit) } returns settingFlow
                },
            fetchMemoDraftUseCase = fetchMemoDraftUseCase,
        )

    backgroundScope.launch { viewModel.uiState.collect { } }

    return viewModel
}
