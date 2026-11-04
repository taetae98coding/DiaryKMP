@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.add

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.testing.tag.tagDetail
import io.github.taetae98coding.diary.domain.tag.exception.TagTitleBlankException
import io.github.taetae98coding.diary.domain.tag.usecase.AddTagUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
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

class TagAddViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-37208ce8-2af8-4e72-8aca-92066b31a989 추가 처리 중 전달된 추가 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val firstDetail = Arb.tagDetail().next().copy(title = nonBlankTitle())
                val secondDetail = Arb.tagDetail().next().copy(title = nonBlankTitle())
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddTagUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = TagAddViewModel(addTagUseCase = useCase)

                viewModel.add(detail = firstDetail, linkedTagIdSet = emptySet())
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                viewModel.add(detail = secondDetail, linkedTagIdSet = emptySet())
                runCurrent()

                coVerify(exactly = 1) { useCase(AddTagUseCase.Parameter(detail = firstDetail)) }
                coVerify(exactly = 0) { useCase(AddTagUseCase.Parameter(detail = secondDetail)) }

                completion.complete(Result.success(Arb.kotlinUuid().next()))
                advanceUntilIdle()
            }
        }

        test("추가에 성공하면 성공 Effect를 한 번 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = Arb.tagDetail().next().copy(title = nonBlankTitle())
                val id = Arb.kotlinUuid().next()
                val useCase = mockk<AddTagUseCase>()
                coEvery { useCase(any()) } returns Result.success(id)
                val viewModel = TagAddViewModel(addTagUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail, linkedTagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe TagAddEffect.AddSucceeded(id = id)
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("제목이 공백이면 제목 미입력 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = Arb.tagDetail().next().copy(title = "  ")
                val useCase = mockk<AddTagUseCase>()
                coEvery { useCase(any()) } returns Result.failure(TagTitleBlankException())
                val viewModel = TagAddViewModel(addTagUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail, linkedTagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe TagAddEffect.TitleBlank
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-7ba61220-06e2-42de-8f81-2b9454545a6e 기기 저장에 실패하면 Effect 없이 진행 상태만 해제하고 같은 내용으로 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val detail = Arb.tagDetail().next().copy(title = nonBlankTitle())
                val linkedTagIdSet = setOf(Arb.kotlinUuid().next())
                val useCase = mockk<AddTagUseCase>()
                coEvery { useCase(any()) } returns Result.failure(IllegalStateException("저장 실패"))
                val viewModel = TagAddViewModel(addTagUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail, linkedTagIdSet = linkedTagIdSet)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()

                    viewModel.add(detail = detail, linkedTagIdSet = linkedTagIdSet)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 2) { useCase(AddTagUseCase.Parameter(detail = detail, linkedTagIdSet = linkedTagIdSet)) }
            }
        }

        test("추가를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val detail = Arb.tagDetail().next().copy(title = nonBlankTitle())
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddTagUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = TagAddViewModel(addTagUseCase = useCase)

                viewModel.add(detail = detail, linkedTagIdSet = emptySet())
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                completion.complete(Result.success(Arb.kotlinUuid().next()))
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-1e53dccb-f364-43cc-9777-8428a3247b7c 고른 태그를 연결 대상으로 함께 전달한다") {
            runTest(mainDispatcher) {
                val detail = Arb.tagDetail().next().copy(title = nonBlankTitle())
                val linkedTagIdSet = List(2) { Arb.kotlinUuid().next() }.toSet()
                val useCase = mockk<AddTagUseCase>()
                coEvery { useCase(any()) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = TagAddViewModel(addTagUseCase = useCase)

                viewModel.add(detail = detail, linkedTagIdSet = linkedTagIdSet)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    useCase(AddTagUseCase.Parameter(detail = detail, linkedTagIdSet = linkedTagIdSet))
                }
            }
        }

        test("추가가 취소되면 진행 상태를 해제하고 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val firstDetail = Arb.tagDetail().next().copy(title = nonBlankTitle())
                val secondDetail = Arb.tagDetail().next().copy(title = nonBlankTitle())
                val useCase = mockk<AddTagUseCase>()
                coEvery { useCase(AddTagUseCase.Parameter(detail = firstDetail)) } throws CancellationException()
                coEvery { useCase(AddTagUseCase.Parameter(detail = secondDetail)) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = TagAddViewModel(addTagUseCase = useCase)

                viewModel.add(detail = firstDetail, linkedTagIdSet = emptySet())
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()

                viewModel.add(detail = secondDetail, linkedTagIdSet = emptySet())
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(AddTagUseCase.Parameter(detail = firstDetail)) }
                coVerify(exactly = 1) { useCase(AddTagUseCase.Parameter(detail = secondDetail)) }
            }
        }
    }

    public companion object {
        private fun nonBlankTitle(): String = "title-${Arb.string().next()}"
    }
}
