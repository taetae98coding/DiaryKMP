@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.web.ui.add

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.domain.web.exception.WebHeaderNameBlankException
import io.github.taetae98coding.diary.domain.web.exception.WebTitleBlankException
import io.github.taetae98coding.diary.domain.web.exception.WebUrlBlankException
import io.github.taetae98coding.diary.domain.web.usecase.AddWebUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class WebAddViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-WEB-ADD-DOMAIN-013 추가 처리 중 전달된 추가 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val firstDetail = fixtureMonkey.giveMeOne<WebDetail>()
                val secondDetail = fixtureMonkey.giveMeOne<WebDetail>()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddWebUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = WebAddViewModel(addWebUseCase = useCase)

                viewModel.add(firstDetail, tagIdSet = emptySet())
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                viewModel.add(secondDetail, tagIdSet = emptySet())
                runCurrent()

                coVerify(exactly = 1) { useCase(AddWebUseCase.Parameter(detail = firstDetail, tagIdSet = emptySet())) }
                coVerify(exactly = 0) { useCase(AddWebUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) }

                completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                advanceUntilIdle()
            }
        }

        test("TC-WEB-ADD-FEATURE-006 추가에 성공하면 성공 Effect를 한 번 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<WebDetail>()
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val useCase = mockk<AddWebUseCase>()
                coEvery { useCase(any()) } returns Result.success(id)
                val viewModel = WebAddViewModel(addWebUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail, tagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe WebAddEffect.AddSucceeded(id = id)
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-WEB-ADD-FEATURE-008 제목이 공백이면 제목 미입력 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<WebDetail>().copy(title = "  ")
                val useCase = mockk<AddWebUseCase>()
                coEvery { useCase(any()) } returns Result.failure(WebTitleBlankException())
                val viewModel = WebAddViewModel(addWebUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail, tagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe WebAddEffect.TitleBlank
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-WEB-ADD-FEATURE-008 URL이 공백이면 URL 미입력 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<WebDetail>().copy(url = "  ")
                val useCase = mockk<AddWebUseCase>()
                coEvery { useCase(any()) } returns Result.failure(WebUrlBlankException())
                val viewModel = WebAddViewModel(addWebUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail, tagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe WebAddEffect.UrlBlank
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-WEB-ADD-FEATURE-008 헤더 이름이 공백이면 헤더 이름 미입력 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<WebDetail>().copy(headerList = listOf(WebHeader(name = "  ", value = "value")))
                val useCase = mockk<AddWebUseCase>()
                coEvery { useCase(any()) } returns Result.failure(WebHeaderNameBlankException())
                val viewModel = WebAddViewModel(addWebUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail, tagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe WebAddEffect.HeaderNameBlank
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-WEB-ADD-FEATURE-021 기기 저장에 실패하면 Effect 없이 진행 상태만 해제하고 같은 내용으로 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<WebDetail>()
                val tagIdSet = setOf(fixtureMonkey.giveMeOne<Uuid>())
                val useCase = mockk<AddWebUseCase>()
                coEvery { useCase(any()) } returns Result.failure(IllegalStateException("저장 실패"))
                val viewModel = WebAddViewModel(addWebUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail, tagIdSet = tagIdSet)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()

                    viewModel.add(detail, tagIdSet = tagIdSet)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 2) { useCase(AddWebUseCase.Parameter(detail = detail, tagIdSet = tagIdSet)) }
            }
        }

        test("TC-WEB-ADD-FEATURE-007 추가를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<WebDetail>()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddWebUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = WebAddViewModel(addWebUseCase = useCase)

                viewModel.add(detail, tagIdSet = emptySet())
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("추가가 취소되면 진행 상태를 해제하고 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val firstDetail = fixtureMonkey.giveMeOne<WebDetail>()
                val secondDetail = fixtureMonkey.giveMeOne<WebDetail>()
                val useCase = mockk<AddWebUseCase>()
                coEvery { useCase(AddWebUseCase.Parameter(detail = firstDetail, tagIdSet = emptySet())) } throws CancellationException()
                coEvery { useCase(AddWebUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = WebAddViewModel(addWebUseCase = useCase)

                viewModel.add(firstDetail, tagIdSet = emptySet())
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()

                viewModel.add(secondDetail, tagIdSet = emptySet())
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(AddWebUseCase.Parameter(detail = firstDetail, tagIdSet = emptySet())) }
                coVerify(exactly = 1) { useCase(AddWebUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
