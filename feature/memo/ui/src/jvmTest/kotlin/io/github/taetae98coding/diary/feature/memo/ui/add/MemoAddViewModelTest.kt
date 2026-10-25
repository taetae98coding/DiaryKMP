@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.domain.memo.exception.MemoTitleBlankException
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagSelectionUiState
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

class MemoAddViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-34163b1f-6c82-4cdb-b647-76c72eeb7906 추가 처리 중 전달된 추가 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val firstDetail = detail(titlePrefix = "first")
                val secondDetail = detail(titlePrefix = "second")
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddMemoUseCase>()
                coEvery { useCase(any<AddMemoUseCase.Parameter>()) } coAnswers { completion.await() }
                val viewModel = viewModel(addMemoUseCase = useCase)

                viewModel.uiState.test {
                    runCurrent()
                    viewModel.add(detail = firstDetail)
                    runCurrent()

                    expectMostRecentItem().isInProgress.shouldBeTrue()

                    viewModel.add(detail = secondDetail)
                    runCurrent()

                    coVerify(exactly = 1) { useCase(AddMemoUseCase.Parameter(detail = firstDetail)) }
                    coVerify(exactly = 0) { useCase(AddMemoUseCase.Parameter(detail = secondDetail)) }

                    completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("추가에 성공하면 성공 Effect를 한 번 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = detail()
                val useCase = mockk<AddMemoUseCase>()
                coEvery { useCase(any<AddMemoUseCase.Parameter>()) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = viewModel(addMemoUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail)
                    advanceUntilIdle()

                    awaitItem() shouldBe MemoAddEffect.AddSucceeded
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("제목이 공백이면 제목 미입력 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "  ")
                val useCase = mockk<AddMemoUseCase>()
                coEvery { useCase(any<AddMemoUseCase.Parameter>()) } returns Result.failure(MemoTitleBlankException())
                val viewModel = viewModel(addMemoUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail)
                    advanceUntilIdle()

                    awaitItem() shouldBe MemoAddEffect.TitleBlank
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("추가를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val detail = detail()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddMemoUseCase>()
                coEvery { useCase(any<AddMemoUseCase.Parameter>()) } coAnswers { completion.await() }
                val viewModel = viewModel(addMemoUseCase = useCase)

                viewModel.uiState.test {
                    runCurrent()
                    viewModel.add(detail = detail)
                    runCurrent()

                    expectMostRecentItem().isInProgress.shouldBeTrue()

                    completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                    advanceUntilIdle()

                    expectMostRecentItem().isInProgress.shouldBeFalse()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("추가가 취소되면 진행 상태를 해제하고 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val firstDetail = detail(titlePrefix = "first")
                val secondDetail = detail(titlePrefix = "second")
                val useCase = mockk<AddMemoUseCase>()
                coEvery { useCase(AddMemoUseCase.Parameter(detail = firstDetail)) } throws CancellationException()
                coEvery { useCase(AddMemoUseCase.Parameter(detail = secondDetail)) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = viewModel(addMemoUseCase = useCase)

                viewModel.add(detail = firstDetail)
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()

                viewModel.add(detail = secondDetail)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(AddMemoUseCase.Parameter(detail = firstDetail)) }
                coVerify(exactly = 1) { useCase(AddMemoUseCase.Parameter(detail = secondDetail)) }
            }
        }

        test("TC-2ccf8ea0-9927-4e73-b8d1-a5f8c9122e46 TC-a632ab5b-934c-427c-8376-08a52e5cb182 태그와 장소를 선택하지 않아도 추가할 수 있다") {
            runTest(mainDispatcher) {
                val detail = detail()
                val useCase = mockk<AddMemoUseCase>()
                coEvery { useCase(any<AddMemoUseCase.Parameter>()) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = viewModel(addMemoUseCase = useCase)

                viewModel.add(detail = detail)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    useCase(
                        AddMemoUseCase.Parameter(
                            detail = detail,
                            primaryTagId = null,
                            tagIdSet = emptySet(),
                            placeIdSet = emptySet(),
                        ),
                    )
                }
            }
        }

        test("TC-c776dc42-2be2-4593-9942-e6efef1a71b2 TC-93aba386-40ce-4f81-a0e8-98ecc148b468 TC-b26a374f-dbe3-4ad8-9e4f-b36db2d3d639 전달받은 태그 선택을 그대로 추가 요청에 담는다") {
            runTest(mainDispatcher) {
                val detail = detail()
                val primaryTagId = fixtureMonkey.giveMeOne<Uuid>()
                val otherTagId = fixtureMonkey.giveMeOne<Uuid>()
                val useCase = mockk<AddMemoUseCase>()
                coEvery { useCase(any<AddMemoUseCase.Parameter>()) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = viewModel(addMemoUseCase = useCase)

                viewModel.add(
                    detail = detail,
                    tagSelection = MemoTagSelectionUiState(tagIdSet = setOf(primaryTagId, otherTagId), primaryTagId = primaryTagId),
                )
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    useCase(
                        AddMemoUseCase.Parameter(
                            detail = detail,
                            primaryTagId = primaryTagId,
                            tagIdSet = setOf(primaryTagId, otherTagId),
                        ),
                    )
                }
            }
        }

        test("TC-5cd9d853-1b39-4712-9282-b33a84585338 전달받은 장소 선택을 그대로 추가 요청에 담는다") {
            runTest(mainDispatcher) {
                val detail = detail()
                val placeIdSet = setOf(fixtureMonkey.giveMeOne<Uuid>(), fixtureMonkey.giveMeOne<Uuid>())
                val useCase = mockk<AddMemoUseCase>()
                coEvery { useCase(any<AddMemoUseCase.Parameter>()) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = viewModel(addMemoUseCase = useCase)

                viewModel.add(detail = detail, placeIdSet = placeIdSet)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    useCase(
                        AddMemoUseCase.Parameter(
                            detail = detail,
                            placeIdSet = placeIdSet,
                        ),
                    )
                }
            }
        }

        test("알 수 없는 실패에는 Effect를 보내지 않고 진행 상태만 해제한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<AddMemoUseCase>()
                coEvery { useCase(any<AddMemoUseCase.Parameter>()) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(addMemoUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail())
                    advanceUntilIdle()

                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun detail(titlePrefix: String = "title"): MemoDetail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "$titlePrefix-${fixtureMonkey.giveMeOne<String>()}")

        private fun viewModel(addMemoUseCase: AddMemoUseCase = mockk()): MemoAddViewModel = MemoAddViewModel(addMemoUseCase = addMemoUseCase)

        // 태그와 웹, 연락처, 장소 선택은 각 ViewModel이 보관하므로, 추가 요청 자체를 검증할 때는 선택 없이 호출한다.
        private fun MemoAddViewModel.add(
            detail: MemoDetail,
            tagSelection: MemoTagSelectionUiState = MemoTagSelectionUiState(),
            webIdSet: Set<Uuid> = emptySet(),
            contactIdSet: Set<Uuid> = emptySet(),
            placeIdSet: Set<Uuid> = emptySet(),
        ) = add(detail = detail, tagSelection = tagSelection, webIdSet = webIdSet, contactIdSet = contactIdSet, placeIdSet = placeIdSet)
    }
}
