@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.core.model.memo.MemoDraftProgress
import io.github.taetae98coding.diary.domain.memo.usecase.FetchMemoDraftUseCase
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import io.github.taetae98coding.diary.domain.setting.exception.GeminiRequestException
import io.github.taetae98coding.diary.library.coroutines.flow.UI_STOP_TIMEOUT_MILLIS
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
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

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

private const val PROGRESS_COUNT = 3

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

        test("TC-25d935e9-1872-457d-9d58-087d86704c99 설정이 갖춰져 있으면 프롬프트 입력으로 시작한다") {
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

        test("TC-061303d8-59e2-40eb-aec4-eca114a536e4 설정이 갖춰지지 않으면 열지 않고 설정 화면 이동을 요청한다") {
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

        test("TC-89a9d719-1f85-46a3-abb4-9d2edb552fb6 설정을 확인하지 못하면 시작 동작을 제공하지 않는다") {
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

        test("TC-6be65451-4f00-4dcf-bbb9-42443bb9ffbd 인증 정보와 모델이 모두 있어야 시작할 수 있다") {
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

        test("TC-634c4200-23c0-46b4-93ed-0627f341002f 프롬프트가 비어 있어도 생성을 실행한다") {
            runTest(mainDispatcher) {
                val parameter = PARAMETER.copy(prompt = "")
                val fetchMemoDraftUseCase = createFetchUseCase()
                val viewModel = createViewModel(fetchMemoDraftUseCase = fetchMemoDraftUseCase)
                advanceUntilIdle()

                viewModel.open()
                viewModel.generate(parameter = parameter)
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchMemoDraftUseCase(match { sent -> sent.copy(onProgress = parameter.onProgress) == parameter }) }
            }
        }

        test("TC-b1c391bd-de35-495f-8346-0a07ef6f7fe6 생성하는 동안 진행 상태를 표시한다") {
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

        test("TC-513a5d46-2cf0-4247-b95e-0d30f6c0a9de 생성 중에는 같은 생성 요청을 처리하지 않는다") {
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

        test("TC-7e318c4b-ead9-4441-9f6e-fab7d4ed4ae2 생성에 성공하면 결과를 확인할 수 있다") {
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

        test("TC-75b0f674-af63-4ec0-b280-668fdbca6b45 생성된 내용이 하나도 없어도 실패로 다루지 않는다") {
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

        test("TC-9bd9cf15-aeb8-4f21-ad73-2e0b0bd4614c 취소하면 요청을 멈추고 프롬프트 입력으로 돌아간다") {
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

        test("TC-cf48e3fc-d248-401d-9071-50664f845d32 취소한 뒤 다시 생성할 수 있다") {
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

        test("TC-37fa34eb-5306-4a2d-af56-d0e3f60b7c7c 생성 실패의 이유를 구분해 알린다") {
            listOf(
                GeminiApiKeyInvalidException() to MemoGeminiFailure.INVALID_API_KEY,
                GeminiRequestException.QuotaExceeded(cause = null) to MemoGeminiFailure.QUOTA_EXCEEDED,
                GeminiRequestException.ModelNotFound(cause = null) to MemoGeminiFailure.MODEL_NOT_FOUND,
                GeminiRequestException.Blocked(cause = null) to MemoGeminiFailure.BLOCKED,
                GeminiRequestException.Truncated(cause = null) to MemoGeminiFailure.TRUNCATED,
                GeminiRequestException.Timeout(cause = null) to MemoGeminiFailure.TIMEOUT,
                GeminiRequestException.Network(cause = null) to MemoGeminiFailure.NETWORK,
                GeminiRequestException.Server(cause = null) to MemoGeminiFailure.SERVER,
                GeminiRequestException.Other(description = "", cause = null) to MemoGeminiFailure.UNKNOWN,
                IllegalStateException("generate failed") to MemoGeminiFailure.UNKNOWN,
            ).forEach { (throwable, expected) ->
                runTest(mainDispatcher) {
                    val viewModel = failingViewModel(throwable)
                    advanceUntilIdle()
                    viewModel.open()

                    viewModel.generate(parameter = PARAMETER)
                    advanceUntilIdle()

                    val uiState = viewModel.uiState.value

                    uiState.step shouldBe MemoGeminiStep.PROMPT
                    uiState.failure shouldBe expected
                    uiState.failureDescription shouldBe ""
                }
            }
        }

        test("TC-da9bb546-e1b1-4d67-a91e-42cf5e0ae8c3 그 밖의 실패에서 Gemini가 돌려준 설명을 함께 알린다") {
            runTest(mainDispatcher) {
                val description = fixtureMonkey.nonBlankString()
                val viewModel = failingViewModel(GeminiRequestException.Other(description = description, cause = null))
                advanceUntilIdle()
                viewModel.open()

                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()

                val uiState = viewModel.uiState.value

                uiState.failure shouldBe MemoGeminiFailure.UNKNOWN
                uiState.failureDescription shouldBe description
            }
        }

        test("TC-11c3ed9d-2f48-41c6-8682-ddb76b0b5379 생성하는 동안 받은 제목과 설명을 알린다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<MemoDraft>>()
                val parameterSlot = slot<FetchMemoDraftUseCase.Parameter>()
                val viewModel =
                    createViewModel(
                        fetchMemoDraftUseCase =
                            mockk {
                                coEvery { this@mockk(capture(parameterSlot)) } coAnswers { completion.await() }
                            },
                    )
                advanceUntilIdle()
                viewModel.open()
                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()

                List(PROGRESS_COUNT) { fixtureMonkey.giveMeOne<MemoDraftProgress>() }.forEach { progress ->
                    parameterSlot.captured.onProgress(progress)
                    advanceUntilIdle()

                    viewModel.uiState.value.step shouldBe MemoGeminiStep.GENERATING
                    viewModel.uiState.value.progress shouldBe progress
                }

                completion.complete(Result.success(DRAFT))
                advanceUntilIdle()

                viewModel.uiState.value.step shouldBe MemoGeminiStep.RESULT
                viewModel.uiState.value.progress shouldBe MemoDraftProgress.EMPTY
            }
        }

        test("TC-9a7e13eb-a822-47f3-812b-adc76fbc39d3 생성 도중 실패하면 받던 내용을 버리고 실패 이유를 알린다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<MemoDraft>>()
                val parameterSlot = slot<FetchMemoDraftUseCase.Parameter>()
                val viewModel =
                    createViewModel(
                        fetchMemoDraftUseCase =
                            mockk {
                                coEvery { this@mockk(capture(parameterSlot)) } coAnswers { completion.await() }
                            },
                    )
                advanceUntilIdle()
                viewModel.open()
                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()
                parameterSlot.captured.onProgress(fixtureMonkey.giveMeOne<MemoDraftProgress>())
                advanceUntilIdle()

                completion.complete(Result.failure(GeminiRequestException.Network(cause = null)))
                advanceUntilIdle()

                val uiState = viewModel.uiState.value

                uiState.step shouldBe MemoGeminiStep.PROMPT
                uiState.failure shouldBe MemoGeminiFailure.NETWORK
                uiState.progress shouldBe MemoDraftProgress.EMPTY
                uiState.draft shouldBe MemoDraft.EMPTY
            }
        }

        test("TC-9bd9cf15-aeb8-4f21-ad73-2e0b0bd4614c 취소하면 받아 보여 주던 내용도 버린다") {
            runTest(mainDispatcher) {
                val parameterSlot = slot<FetchMemoDraftUseCase.Parameter>()
                val viewModel =
                    createViewModel(
                        fetchMemoDraftUseCase =
                            mockk {
                                coEvery { this@mockk(capture(parameterSlot)) } coAnswers { CompletableDeferred<Result<MemoDraft>>().await() }
                            },
                    )
                advanceUntilIdle()
                viewModel.open()
                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()
                parameterSlot.captured.onProgress(fixtureMonkey.giveMeOne<MemoDraftProgress>())
                advanceUntilIdle()

                viewModel.cancel()
                advanceUntilIdle()

                viewModel.uiState.value.step shouldBe MemoGeminiStep.PROMPT
                viewModel.uiState.value.progress shouldBe MemoDraftProgress.EMPTY
            }
        }

        test("생성 요청에 화면이 넘긴 입력 내용을 그대로 담는다") {
            runTest(mainDispatcher) {
                val parameterSlot = slot<FetchMemoDraftUseCase.Parameter>()
                val viewModel =
                    createViewModel(
                        fetchMemoDraftUseCase =
                            mockk {
                                coEvery { this@mockk(capture(parameterSlot)) } returns Result.success(DRAFT)
                            },
                    )
                advanceUntilIdle()
                viewModel.open()

                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()

                parameterSlot.captured.copy(onProgress = PARAMETER.onProgress) shouldBe PARAMETER
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

        test("TC-048257f1-7281-4c7f-8ef3-20c08483899e 반영해도 결과 확인 상태를 유지한다") {
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

        test("TC-0725149c-6215-42e7-b736-99bcc3c1b1fb 닫으면 결과를 버린다") {
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

        test("TC-ae6f3e42-1063-44e2-ae80-5d51d07013d4 도우미를 닫으면 반영 표시가 사라진다") {
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

        test("TC-0ec6cbee-1d11-4ba6-821f-d497a54fa715 다른 화면에서 저장한 설정으로 다음 시작을 판정한다") {
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

        test("TC-90e1c2d1-7fc4-43a0-b09c-2623bb1975a6 생성 중에 닫으면 요청을 멈춘다") {
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

        // 화면을 떠나 전환 이력에서 사라지거나, 앱을 종료하거나, 시스템이 앱을 종료하면 도우미 상태를 들고 있던 객체가 모두 사라진다.
        test("TC-183e93b6-9153-48ad-b109-91ac15144cef 생성 중에 화면을 떠나거나 앱이 종료되면 진행 중이던 생성이 멈춘다") {
            runTest(mainDispatcher) {
                var isCancelled = false
                val viewModel =
                    createViewModel(
                        fetchMemoDraftUseCase =
                            mockk {
                                coEvery { this@mockk(any()) } coAnswers {
                                    try {
                                        awaitCancellation()
                                    } catch (exception: CancellationException) {
                                        isCancelled = true
                                        throw exception
                                    }
                                }
                            },
                    )
                val store = ViewModelStore().apply { put(key = "MemoGemini", viewModel = viewModel) }
                advanceUntilIdle()
                viewModel.open()
                viewModel.generate(parameter = PARAMETER)
                advanceUntilIdle()

                store.clear()
                advanceUntilIdle()

                isCancelled.shouldBeTrue()
            }
        }

        test("TC-678af699-2eff-4bfc-b1d3-8bf813eee5c5 다른 앱에 다녀와도 받아 둔 결과가 유지된다") {
            runTest(mainDispatcher) {
                val viewModel = createUnsubscribedViewModel(fetchMemoDraftUseCase = createFetchUseCase())
                viewModel.uiState.test {
                    awaitItem()
                    advanceUntilIdle()
                    viewModel.open()
                    viewModel.generate(parameter = PARAMETER)
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }

                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS * 2)

                viewModel.uiState.test {
                    advanceUntilIdle()
                    val uiState = expectMostRecentItem()

                    uiState.step shouldBe MemoGeminiStep.RESULT
                    uiState.draft shouldBe DRAFT
                }
            }
        }

        test("TC-678af699-2eff-4bfc-b1d3-8bf813eee5c5 다른 앱에 가 있는 동안 진행 중인 생성이 이어져 돌아오면 결과가 보인다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<MemoDraft>>()
                val viewModel =
                    createUnsubscribedViewModel(
                        fetchMemoDraftUseCase =
                            mockk {
                                coEvery { this@mockk(any()) } coAnswers { completion.await() }
                            },
                    )
                viewModel.uiState.test {
                    awaitItem()
                    advanceUntilIdle()
                    viewModel.open()
                    viewModel.generate(parameter = PARAMETER)
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }

                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS * 2)
                completion.complete(Result.success(DRAFT))
                advanceUntilIdle()

                viewModel.uiState.test {
                    advanceUntilIdle()
                    val uiState = expectMostRecentItem()

                    uiState.step shouldBe MemoGeminiStep.RESULT
                    uiState.draft shouldBe DRAFT
                }
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

// 다른 앱에 다녀오는 동안 화면이 구독을 멈추는 경계를 재현하려고 구독을 시작하지 않은 채로 만든다.
private fun createUnsubscribedViewModel(fetchMemoDraftUseCase: FetchMemoDraftUseCase): MemoGeminiViewModel =
    MemoGeminiViewModel(
        getGeminiSettingUseCase =
            mockk {
                every { this@mockk(Unit) } returns flowOf(Result.success(COMPLETE_SETTING))
            },
        fetchMemoDraftUseCase = fetchMemoDraftUseCase,
    )

private fun TestScope.failingViewModel(throwable: Throwable): MemoGeminiViewModel =
    createViewModel(
        fetchMemoDraftUseCase =
            mockk {
                coEvery { this@mockk(any()) } returns Result.failure(throwable)
            },
    )

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
