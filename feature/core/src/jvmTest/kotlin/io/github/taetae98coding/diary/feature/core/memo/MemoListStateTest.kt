package io.github.taetae98coding.diary.feature.core.memo

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

class MemoListStateTest : FunSpec() {
    init {
        test("처음에는 기본 정렬로 조회하고 정렬을 고르면 그 정렬로 다시 조회한다") {
            runTest {
                val requestedSortList = mutableListOf<ListSort>()
                val state =
                    MemoListState(
                        pageMemo = { sort ->
                            requestedSortList += sort
                            flowOf(Result.success(PagingData.empty()))
                        },
                    )

                state.sortUiState.value shouldBe ListSortUiState()
                state.memoPagingData.test {
                    awaitItem()
                    val nextSort = ListSort.entries.first { sort -> sort != ListSort.DEFAULT }

                    state.select(sort = nextSort)
                    advanceUntilIdle()
                    awaitItem()

                    state.sortUiState.value shouldBe ListSortUiState(sort = nextSort)
                    requestedSortList shouldBe listOf(ListSort.DEFAULT, nextSort)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("조회한 메모를 목록 항목으로 바꿔 노출한다") {
            runTest {
                val memo = Arb.memo().next()
                val state = MemoListState(pageMemo = { flowOf(Result.success(PagingData.from(listOf(memo)))) })

                flowOf(state.memoPagingData.first())
                    .asSnapshot()
                    .filterIsInstance<MemoListItem.Content>()
                    .map { item -> item.memo } shouldBe listOf(memo)
            }
        }

        test("조회가 실패하면 빈 목록을 노출한다") {
            runTest {
                val failure: Flow<Result<PagingData<Memo>>> = flowOf(Result.failure(IllegalStateException()))
                val state = MemoListState(pageMemo = { failure })

                flowOf(state.memoPagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }
    }
}
