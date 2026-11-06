@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.library.coroutines.flow.INPUT_IDLE_DELAY
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class SearchHomeResultStateTest : FunSpec() {
    init {
        test("질의는 입력을 멈춘 뒤에 적용된다") {
            runTest {
                val state = SearchHomeResultState<Memo>(search = { _, _ -> successPagingDataFlowOf(emptyList()) })

                state.uiState.map { uiState -> uiState.appliedQuery }.distinctUntilChanged().test {
                    awaitItem() shouldBe ""

                    state.updateQuery(QUERY)
                    runCurrent()
                    expectNoEvents()

                    advanceTimeBy(INPUT_IDLE_DELAY)
                    runCurrent()
                    awaitItem() shouldBe QUERY
                }
            }
        }

        test("적용된 질의와 고른 정렬로 조회한다") {
            runTest {
                val memoList = listOf(searchMemo())
                val requestList = mutableListOf<Pair<String, ListSort>>()
                val state =
                    SearchHomeResultState(
                        search = { query, sort ->
                            requestList += query to sort
                            if (query == QUERY && sort == ListSort.RECENTLY_UPDATED) successPagingDataFlowOf(memoList) else successPagingDataFlowOf(emptyList())
                        },
                    )

                state.pagingData.test {
                    awaitItem()

                    state.select(ListSort.RECENTLY_UPDATED)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()

                    state.updateQuery(QUERY)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe memoList
                    requestList.last() shouldBe (QUERY to ListSort.RECENTLY_UPDATED)

                    cancelAndIgnoreRemainingEvents()
                }

                state.uiState.first() shouldBe SearchHomeResultUiState(sort = ListSort.RECENTLY_UPDATED, appliedQuery = QUERY)
            }
        }

        test("조회에 실패하면 빈 결과를 낸다") {
            runTest {
                val state = SearchHomeResultState<Memo>(search = { _, _ -> failurePagingDataFlow() })

                state.pagingData.test {
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("같은 대상의 작업은 진행 중이면 다시 시작하지 않고 끝나면 다시 받는다") {
            runTest {
                val state = SearchHomeResultState<Memo>(search = { _, _ -> emptyPagingDataFlow() })
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                var startCount = 0

                val first =
                    launch {
                        state.runOnce(id = id) {
                            startCount++
                            gate.await()
                        }
                    }
                runCurrent()
                state.runOnce(id = id) { startCount++ }
                startCount shouldBe 1

                gate.complete(Unit)
                first.join()
                state.runOnce(id = id) { startCount++ }
                startCount shouldBe 2
            }
        }

        test("다른 대상의 작업은 함께 진행한다") {
            runTest {
                val state = SearchHomeResultState<Memo>(search = { _, _ -> emptyPagingDataFlow() })
                val gate = CompletableDeferred<Unit>()
                var startCount = 0

                launch {
                    state.runOnce(id = Arb.kotlinUuid().next()) {
                        startCount++
                        gate.await()
                    }
                }
                runCurrent()
                state.runOnce(id = Arb.kotlinUuid().next()) { startCount++ }

                startCount shouldBe 2
                gate.complete(Unit)
            }
        }
    }

    private fun emptyPagingDataFlow(): Flow<Result<PagingData<Memo>>> = successPagingDataFlowOf(emptyList())
}
