@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.core.routine

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class RoutinePagingStateTest : FunSpec() {
    init {
        test("오늘이 정해지기 전에는 조회하지 않는다") {
            runTest {
                val requestedTodayList = mutableListOf<LocalDate>()
                val state =
                    RoutinePagingState(
                        pageRoutine = { today ->
                            requestedTodayList += today
                            flowOf(Result.success(PagingData.empty()))
                        },
                    )

                state.routinePagingData.test {
                    awaitItem()
                    advanceUntilIdle()

                    expectNoEvents()
                    requestedTodayList.shouldBeEmpty()
                }
            }
        }

        test("오늘이 정해지면 그 오늘로 조회하고 오늘이 바뀌면 새 오늘로 다시 조회한다") {
            runTest {
                val today = Arb.kotlinxLocalDate().next()
                val nextDay = today.plus(1, DateTimeUnit.DAY)
                val nextDaySummaryList = List(2) { Arb.routineSummary().next() }
                val requestedTodayList = mutableListOf<LocalDate>()
                val state =
                    RoutinePagingState(
                        pageRoutine = { requestedToday ->
                            requestedTodayList += requestedToday
                            flowOf(Result.success(PagingData.from(if (requestedToday == nextDay) nextDaySummaryList else emptyList())))
                        },
                    )

                state.routinePagingData.test {
                    state.updateToday(today = today)
                    advanceUntilIdle()
                    state.updateToday(today = nextDay)
                    advanceUntilIdle()

                    flowOf(expectMostRecentItem()).asSnapshot() shouldBe nextDaySummaryList
                    requestedTodayList shouldBe listOf(today, nextDay)
                }
            }
        }

        test("조회가 실패하면 조회를 마친 빈 목록을 노출한다") {
            runTest {
                val failure: Flow<Result<PagingData<RoutineSummary>>> = flowOf(Result.failure(IllegalStateException()))
                val state = RoutinePagingState(pageRoutine = { failure })

                state.routinePagingData.test {
                    state.updateToday(today = Arb.kotlinxLocalDate().next())
                    advanceUntilIdle()

                    flowOf(expectMostRecentItem()).asSnapshot().shouldBeEmpty()
                }
            }
        }

        test("목록을 다시 불러오다 실패하면 앞서 보이던 루틴을 남기지 않는다") {
            runTest {
                val pageFlow = MutableStateFlow<Result<PagingData<RoutineSummary>>>(Result.success(PagingData.from(List(2) { Arb.routineSummary().next() })))
                val state = RoutinePagingState(pageRoutine = { pageFlow })

                state.routinePagingData.test {
                    state.updateToday(today = Arb.kotlinxLocalDate().next())
                    advanceUntilIdle()
                    pageFlow.value = Result.failure(IllegalStateException())
                    advanceUntilIdle()

                    flowOf(expectMostRecentItem()).asSnapshot().shouldBeEmpty()
                }
            }
        }
    }
}
