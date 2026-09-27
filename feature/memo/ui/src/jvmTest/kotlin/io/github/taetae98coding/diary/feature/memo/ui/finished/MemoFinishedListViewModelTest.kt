@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

class MemoFinishedListViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MEMO-FINISHED-LIST-DATA-001 완료된 메모 목록 UseCase를 한 번 조회한다") {
            runTest(mainDispatcher) {
                val pageFinishedMemoUseCase = mockk<PageFinishedMemoUseCase>()
                every { pageFinishedMemoUseCase(parameter = ListSort.DEFAULT) } returns flowOf(Result.success(PagingData.empty()))
                val viewModel = viewModel(pageFinishedMemoUseCase = pageFinishedMemoUseCase)

                viewModel.memoPagingData.test {
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { pageFinishedMemoUseCase(parameter = ListSort.DEFAULT) }
            }
        }

        test("TC-MEMO-FINISHED-LIST-FEATURE-015 메모 다시 시작에 성공하면 다시 시작 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                coEvery { restartMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(restartMemoUseCase = restartMemoUseCase)

                viewModel.effect.test {
                    viewModel.restart(id = memoId)
                    advanceUntilIdle()

                    awaitItem() shouldBe MemoFinishedListEffect.Restarted(id = memoId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { restartMemoUseCase(parameter = memoId) }
            }
        }

        test("TC-MEMO-FINISHED-LIST-FEATURE-026 메모 다시 시작이 저장되지 못하면 안내 Effect를 보내지 않고 메모가 목록에 남는다") {
            runTest(mainDispatcher) {
                val memo = fixtureMonkey.giveMeOne<Memo>()
                val pageFinishedMemoUseCase = mockk<PageFinishedMemoUseCase>()
                every { pageFinishedMemoUseCase(parameter = ListSort.DEFAULT) } returns flowOf(Result.success(PagingData.from(listOf(memo))))
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                coEvery { restartMemoUseCase(any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(pageFinishedMemoUseCase = pageFinishedMemoUseCase, restartMemoUseCase = restartMemoUseCase)

                viewModel.effect.test {
                    viewModel.restart(id = memo.id)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                viewModel.memoPagingData.test {
                    flowOf(awaitItem())
                        .asSnapshot()
                        .filterIsInstance<MemoListItem.Content>()
                        .map { item -> item.memo } shouldBe listOf(memo)
                    expectNoEvents()
                }
            }
        }

        test("TC-MEMO-FINISHED-LIST-FEATURE-016 다시 시작 실행 취소는 완료를 요청하고 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(finishMemoUseCase = finishMemoUseCase)

                viewModel.effect.test {
                    viewModel.finish(id = memoId)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { finishMemoUseCase(parameter = memoId) }
            }
        }

        test("TC-MEMO-FINISHED-LIST-FEATURE-017 메모 삭제에 성공하면 삭제 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(deleteMemoUseCase = deleteMemoUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = memoId)
                    advanceUntilIdle()

                    awaitItem() shouldBe MemoFinishedListEffect.Deleted(id = memoId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { deleteMemoUseCase(parameter = memoId) }
            }
        }

        test("TC-MEMO-FINISHED-LIST-FEATURE-026 메모 삭제가 저장되지 못하면 안내 Effect를 보내지 않고 메모가 목록에 남는다") {
            runTest(mainDispatcher) {
                val memo = fixtureMonkey.giveMeOne<Memo>()
                val pageFinishedMemoUseCase = mockk<PageFinishedMemoUseCase>()
                every { pageFinishedMemoUseCase(parameter = ListSort.DEFAULT) } returns flowOf(Result.success(PagingData.from(listOf(memo))))
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(pageFinishedMemoUseCase = pageFinishedMemoUseCase, deleteMemoUseCase = deleteMemoUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = memo.id)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                viewModel.memoPagingData.test {
                    flowOf(awaitItem())
                        .asSnapshot()
                        .filterIsInstance<MemoListItem.Content>()
                        .map { item -> item.memo } shouldBe listOf(memo)
                    expectNoEvents()
                }
            }
        }

        test("TC-MEMO-FINISHED-LIST-FEATURE-018 삭제 실행 취소는 복구를 요청하고 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val restoreMemoUseCase = mockk<RestoreMemoUseCase>()
                coEvery { restoreMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(restoreMemoUseCase = restoreMemoUseCase)

                viewModel.effect.test {
                    viewModel.restore(id = memoId)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { restoreMemoUseCase(parameter = memoId) }
            }
        }

        test("TC-MEMO-FINISHED-LIST-FEATURE-030 정렬을 바꾸면 그 정렬로 목록을 다시 조회한다") {
            listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED).forEach { sort ->
                runTest(mainDispatcher) {
                    val pageFinishedMemoUseCase = mockk<PageFinishedMemoUseCase>()
                    every { pageFinishedMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.empty()))
                    val viewModel = viewModel(pageFinishedMemoUseCase = pageFinishedMemoUseCase)

                    viewModel.memoPagingData.test {
                        awaitItem()

                        viewModel.select(sort = sort)

                        awaitItem()
                        cancelAndIgnoreRemainingEvents()
                    }

                    viewModel.sort.value shouldBe sort
                    verify(exactly = 1) { pageFinishedMemoUseCase(parameter = sort) }
                }
            }
        }

        test("TC-MEMO-FINISHED-LIST-FEATURE-031 기본순이 아닌 정렬에서는 날짜 헤더를 표시하지 않는다") {
            runTest(mainDispatcher) {
                val memo =
                    fixtureMonkey
                        .giveMeOne<Memo>()
                        .let { value ->
                            value.copy(
                                detail = value.detail.copy(dateTime = MemoDateTime.AllDay(dateRange = LocalDate(2026, 1, 1)..LocalDate(2026, 1, 1))),
                            )
                        }
                val pageFinishedMemoUseCase = mockk<PageFinishedMemoUseCase>()
                every { pageFinishedMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(listOf(memo))))
                val viewModel = viewModel(pageFinishedMemoUseCase = pageFinishedMemoUseCase)

                viewModel.memoPagingData.test {
                    val defaultItemList = flowOf(awaitItem()).asSnapshot()
                    defaultItemList.filterIsInstance<MemoListItem.DateHeader>().size shouldBe 1

                    viewModel.select(sort = ListSort.TITLE)

                    val titleItemList = flowOf(awaitItem()).asSnapshot()
                    titleItemList.filterIsInstance<MemoListItem.DateHeader>().shouldBeEmpty()
                    titleItemList.filterIsInstance<MemoListItem.Content>().map { item -> item.memo } shouldBe listOf(memo)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-FINISHED-LIST-DATA-005 완료된 메모 목록 조회에 실패하면 빈 PagingData를 노출한다") {
            runTest(mainDispatcher) {
                val pageFinishedMemoUseCase = mockk<PageFinishedMemoUseCase>()
                every { pageFinishedMemoUseCase(parameter = ListSort.DEFAULT) } returns flowOf(Result.failure(IllegalStateException()))
                val viewModel = viewModel(pageFinishedMemoUseCase = pageFinishedMemoUseCase)

                viewModel.memoPagingData.test {
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            pageFinishedMemoUseCase: PageFinishedMemoUseCase = finishedMemoUseCase(),
            restartMemoUseCase: RestartMemoUseCase = mockk(),
            finishMemoUseCase: FinishMemoUseCase = mockk(),
            deleteMemoUseCase: DeleteMemoUseCase = mockk(),
            restoreMemoUseCase: RestoreMemoUseCase = mockk(),
        ): MemoFinishedListViewModel =
            MemoFinishedListViewModel(
                pageFinishedMemoUseCase = pageFinishedMemoUseCase,
                restartMemoUseCase = restartMemoUseCase,
                finishMemoUseCase = finishMemoUseCase,
                deleteMemoUseCase = deleteMemoUseCase,
                restoreMemoUseCase = restoreMemoUseCase,
            )

        private fun finishedMemoUseCase(): PageFinishedMemoUseCase {
            val pageFinishedMemoUseCase = mockk<PageFinishedMemoUseCase>()
            every { pageFinishedMemoUseCase(parameter = ListSort.DEFAULT) } returns emptyFlow()

            return pageFinishedMemoUseCase
        }
    }
}
