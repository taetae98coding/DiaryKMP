@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoHomeUseCase
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
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoHomeViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("메모 완료에 성공하면 완료 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(finishMemoUseCase = finishMemoUseCase)

                viewModel.effect.test {
                    viewModel.finish(id = memoId)
                    advanceUntilIdle()

                    awaitItem() shouldBe MemoListEffect.Finished(id = memoId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { finishMemoUseCase(parameter = memoId) }
            }
        }

        test("TC-MEMO-HOME-FEATURE-068 메모 완료가 저장되지 못하면 안내 Effect를 보내지 않고 메모가 목록에 남는다") {
            runTest(mainDispatcher) {
                val memo = datedMemo()
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                every { pageMemoHomeUseCase(parameter = ListSort.DEFAULT) } returns flowOf(Result.success(PagingData.from(listOf(memo))))
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(pageMemoHomeUseCase = pageMemoHomeUseCase, finishMemoUseCase = finishMemoUseCase)

                viewModel.effect.test {
                    viewModel.finish(id = memo.id)
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

        test("완료 실행 취소는 완료 취소를 요청하고 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                coEvery { restartMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(restartMemoUseCase = restartMemoUseCase)

                viewModel.effect.test {
                    viewModel.restart(id = memoId)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { restartMemoUseCase(parameter = memoId) }
            }
        }

        test("메모 삭제에 성공하면 삭제 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(deleteMemoUseCase = deleteMemoUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = memoId)
                    advanceUntilIdle()

                    awaitItem() shouldBe MemoListEffect.Deleted(id = memoId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { deleteMemoUseCase(parameter = memoId) }
            }
        }

        test("TC-MEMO-HOME-FEATURE-068 메모 삭제가 저장되지 못하면 안내 Effect를 보내지 않고 메모가 목록에 남는다") {
            runTest(mainDispatcher) {
                val memo = datedMemo()
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                every { pageMemoHomeUseCase(parameter = ListSort.DEFAULT) } returns flowOf(Result.success(PagingData.from(listOf(memo))))
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(pageMemoHomeUseCase = pageMemoHomeUseCase, deleteMemoUseCase = deleteMemoUseCase)

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

        test("삭제 실행 취소는 복구를 요청하고 Effect를 보내지 않는다") {
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

        test("MemoHome 목록 UseCase를 한 번 조회한다") {
            runTest(mainDispatcher) {
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                every { pageMemoHomeUseCase(parameter = ListSort.DEFAULT) } returns
                    flowOf(Result.success(PagingData.empty()))
                val viewModel =
                    viewModel(
                        pageMemoHomeUseCase = pageMemoHomeUseCase,
                    )

                viewModel.memoPagingData.test {
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { pageMemoHomeUseCase(parameter = ListSort.DEFAULT) }
            }
        }

        test("선택한 태그가 있으면 필터 적용 상태를 노출한다") {
            runTest(mainDispatcher) {
                val selectedTagList = listOf(tag())
                val viewModel =
                    viewModel(
                        getMemoFilterUseCase =
                            filterUseCase(
                                filterFlow =
                                    flowOf(
                                        Result.success(selectedTagList),
                                    ),
                            ),
                    )

                viewModel.filterUiState.test {
                    awaitItem() shouldBe MemoHomeScaffoldFilterUiState()
                    advanceUntilIdle()
                    awaitItem() shouldBe MemoHomeScaffoldFilterUiState(selectedTagIdSet = selectedTagList.map { tag -> tag.id }.toSet())
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("선택한 태그가 선택할 수 없게 되어도 목록 위치 되돌림 기준은 바뀌지 않는다") {
            runTest(mainDispatcher) {
                val selectedTag = tag()
                val filterFlow = MutableStateFlow(Result.success(listOf(selectedTag)))
                val viewModel =
                    viewModel(
                        getMemoFilterUseCase = filterUseCase(filterFlow = filterFlow),
                        getMemoFilterTagIdUseCase = filterTagIdUseCase(tagIdSetFlow = flowOf(Result.success(setOf(selectedTag.id)))),
                    )

                viewModel.filterUiState.test {
                    awaitItem()
                    advanceUntilIdle()
                    val selected = expectMostRecentItem()

                    filterFlow.value = Result.success(emptyList())
                    advanceUntilIdle()
                    val ignored = awaitItem()

                    ignored.isApplied shouldBe false
                    ignored.listQueryFilter shouldBe selected.listQueryFilter
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("저장된 선택이 바뀌면 목록 위치 되돌림 기준도 바뀐다") {
            runTest(mainDispatcher) {
                val firstTagId = fixtureMonkey.giveMeOne<Uuid>()
                val secondTagId = fixtureMonkey.giveMeOne<Uuid>()
                val tagIdSetFlow = MutableStateFlow(Result.success(setOf(firstTagId)))
                val viewModel =
                    viewModel(
                        getMemoFilterUseCase = filterUseCase(filterFlow = flowOf(Result.success(emptyList()))),
                        getMemoFilterTagIdUseCase = filterTagIdUseCase(tagIdSetFlow = tagIdSetFlow),
                    )

                viewModel.filterUiState.test {
                    awaitItem()
                    advanceUntilIdle()
                    expectMostRecentItem().listQueryFilter.storedTagIdSet shouldBe setOf(firstTagId)

                    tagIdSetFlow.value = Result.success(setOf(firstTagId, secondTagId))
                    advanceUntilIdle()

                    awaitItem().listQueryFilter.storedTagIdSet shouldBe setOf(firstTagId, secondTagId)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("필터 조회에 실패하면 적용되지 않은 상태를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getMemoFilterUseCase =
                            filterUseCase(
                                filterFlow = flowOf(Result.failure(IllegalStateException())),
                            ),
                    )

                viewModel.filterUiState.test {
                    awaitItem() shouldBe MemoHomeScaffoldFilterUiState()
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("유무 필터를 고르면 필터가 적용된 상태를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getMemoFilterUseCase = filterUseCase(filterFlow = flowOf(Result.success(emptyList()))),
                        getMemoExistenceFilterUseCase =
                            existenceFilterUseCase(
                                existenceFlow =
                                    flowOf(
                                        Result.success(MemoExistenceFilter(date = MemoFilterExistence.EXIST)),
                                    ),
                            ),
                    )

                viewModel.filterUiState.test {
                    awaitItem() shouldBe MemoHomeScaffoldFilterUiState()
                    advanceUntilIdle()
                    awaitItem() shouldBe MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(date = MemoFilterExistence.EXIST))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("유무 필터 조회에 실패하면 적용되지 않은 상태를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getMemoFilterUseCase = filterUseCase(filterFlow = flowOf(Result.success(emptyList()))),
                        getMemoExistenceFilterUseCase =
                            existenceFilterUseCase(
                                existenceFlow = flowOf(Result.failure(IllegalStateException())),
                            ),
                    )

                viewModel.filterUiState.test {
                    awaitItem() shouldBe MemoHomeScaffoldFilterUiState()
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-MEMO-HOME-FEATURE-061 처음 정렬은 기본순이다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel()

                viewModel.sort.value shouldBe ListSort.DEFAULT
            }
        }

        test("TC-MEMO-HOME-DATA-013 정렬을 바꾸면 그 정렬로 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                every { pageMemoHomeUseCase(parameter = ListSort.DEFAULT) } returns flowOf(Result.success(PagingData.empty()))
                every { pageMemoHomeUseCase(parameter = ListSort.TITLE) } returns flowOf(Result.success(PagingData.empty()))
                val viewModel = viewModel(pageMemoHomeUseCase = pageMemoHomeUseCase)

                viewModel.memoPagingData.test {
                    awaitItem()

                    viewModel.select(sort = ListSort.TITLE)

                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.sort.value shouldBe ListSort.TITLE
                verify(exactly = 1) { pageMemoHomeUseCase(parameter = ListSort.TITLE) }
            }
        }

        test("TC-MEMO-HOME-FEATURE-051 기본순이 아닌 정렬에서는 날짜 헤더를 표시하지 않는다") {
            runTest(mainDispatcher) {
                val memo = datedMemo()
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                every { pageMemoHomeUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(listOf(memo))))
                val viewModel = viewModel(pageMemoHomeUseCase = pageMemoHomeUseCase)

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

        test("MemoHome 목록 조회에 실패하면 빈 PagingData를 노출한다") {
            runTest(mainDispatcher) {
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                every { pageMemoHomeUseCase(parameter = ListSort.DEFAULT) } returns
                    flowOf(Result.failure(IllegalStateException()))
                val viewModel = viewModel(pageMemoHomeUseCase = pageMemoHomeUseCase)

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
            getMemoFilterUseCase: GetMemoFilterUseCase = filterUseCase(filterFlow = emptyFlow()),
            getMemoFilterTagIdUseCase: GetMemoFilterTagIdUseCase = filterTagIdUseCase(),
            getMemoExistenceFilterUseCase: GetMemoExistenceFilterUseCase = existenceFilterUseCase(),
            pageMemoHomeUseCase: PageMemoHomeUseCase = memoUseCase(),
            finishMemoUseCase: FinishMemoUseCase = mockk(),
            restartMemoUseCase: RestartMemoUseCase = mockk(),
            deleteMemoUseCase: DeleteMemoUseCase = mockk(),
            restoreMemoUseCase: RestoreMemoUseCase = mockk(),
        ): MemoHomeViewModel =
            MemoHomeViewModel(
                getMemoFilterUseCase = getMemoFilterUseCase,
                getMemoFilterTagIdUseCase = getMemoFilterTagIdUseCase,
                getMemoExistenceFilterUseCase = getMemoExistenceFilterUseCase,
                pageMemoHomeUseCase = pageMemoHomeUseCase,
                finishMemoUseCase = finishMemoUseCase,
                restartMemoUseCase = restartMemoUseCase,
                deleteMemoUseCase = deleteMemoUseCase,
                restoreMemoUseCase = restoreMemoUseCase,
            )

        private fun existenceFilterUseCase(existenceFlow: Flow<Result<MemoExistenceFilter>> = flowOf(Result.success(MemoExistenceFilter()))): GetMemoExistenceFilterUseCase {
            val getMemoExistenceFilterUseCase = mockk<GetMemoExistenceFilterUseCase>()
            every { getMemoExistenceFilterUseCase(parameter = Unit) } returns existenceFlow

            return getMemoExistenceFilterUseCase
        }

        private fun filterTagIdUseCase(tagIdSetFlow: Flow<Result<Set<Uuid>>> = flowOf(Result.success(emptySet()))): GetMemoFilterTagIdUseCase {
            val getMemoFilterTagIdUseCase = mockk<GetMemoFilterTagIdUseCase>()
            every { getMemoFilterTagIdUseCase(parameter = Unit) } returns tagIdSetFlow

            return getMemoFilterTagIdUseCase
        }

        private fun filterUseCase(filterFlow: Flow<Result<List<Tag>>>): GetMemoFilterUseCase {
            val getMemoFilterUseCase = mockk<GetMemoFilterUseCase>()
            every { getMemoFilterUseCase(parameter = Unit) } returns filterFlow

            return getMemoFilterUseCase
        }

        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()

        private fun datedMemo(): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(
                    Memo::detail,
                    fixtureMonkey.giveMeOne<MemoDetail>().copy(dateTime = MemoDateTime.AllDay(dateRange = LocalDate(2026, 1, 1)..LocalDate(2026, 1, 1))),
                ).setExp(Memo::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Memo::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()

        private fun memoUseCase(): PageMemoHomeUseCase {
            val pageMemoUseCase = mockk<PageMemoHomeUseCase>()
            every { pageMemoUseCase(parameter = ListSort.DEFAULT) } returns emptyFlow()

            return pageMemoUseCase
        }
    }
}
