@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.detail.memo

import androidx.lifecycle.viewModelScope
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
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PagePlaceMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class PlaceDetailMemoViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-PLACE-DETAIL-MEMO-DATA-001 장소별 메모 paging에 주입된 장소 ID를 전달한다") {
            runTest(mainDispatcher) {
                val placeId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo()
                val pagePlaceMemoUseCase = mockk<PagePlaceMemoUseCase>()
                every { pagePlaceMemoUseCase(parameter = PagePlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)) } returns
                    flowOf(Result.success(PagingData.from(listOf(memo))))
                val viewModel = viewModel(placeId = placeId, pagePlaceMemoUseCase = pagePlaceMemoUseCase)

                val itemList = flowOf(viewModel.memoPagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList.filterIsInstance<MemoListItem.Content>().map { it.memo } shouldBe listOf(memo)
                verify(exactly = 1) { pagePlaceMemoUseCase(parameter = PagePlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)) }
            }
        }

        test("장소별 메모 최초 조회가 실패하면 PagingData를 내보내지 않아 초기 빈 목록을 유지한다") {
            runTest(mainDispatcher) {
                val placeId = fixtureMonkey.giveMeOne<Uuid>()
                val pagePlaceMemoUseCase = mockk<PagePlaceMemoUseCase>()
                every { pagePlaceMemoUseCase(parameter = PagePlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)) } returns
                    flowOf(Result.failure<PagingData<Memo>>(IllegalStateException()))
                val viewModel = viewModel(placeId = placeId, pagePlaceMemoUseCase = pagePlaceMemoUseCase)

                viewModel.memoPagingData.test {
                    advanceUntilIdle()
                    expectNoEvents()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("장소별 메모 조회가 성공한 뒤 실패하면 마지막 성공 목록을 유지한다") {
            runTest(mainDispatcher) {
                val placeId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo()
                val pagePlaceMemoUseCase = mockk<PagePlaceMemoUseCase>()
                every { pagePlaceMemoUseCase(parameter = PagePlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)) } returns
                    flowOf(
                        Result.success(PagingData.from(listOf(memo))),
                        Result.failure(IllegalStateException()),
                    )
                val viewModel = viewModel(placeId = placeId, pagePlaceMemoUseCase = pagePlaceMemoUseCase)

                viewModel.memoPagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList.filterIsInstance<MemoListItem.Content>().map { it.memo } shouldBe listOf(memo)
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
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

        test("메모 완료에 실패하면 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(finishMemoUseCase = finishMemoUseCase)

                viewModel.effect.test {
                    viewModel.finish(id = memoId)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("완료 실행 취소는 다시 시작을 요청하고 Effect를 보내지 않는다") {
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

        test("메모 삭제에 실패하면 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(deleteMemoUseCase = deleteMemoUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = memoId)
                    advanceUntilIdle()

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
    }

    private fun viewModel(
        placeId: Uuid = fixtureMonkey.giveMeOne(),
        pagePlaceMemoUseCase: PagePlaceMemoUseCase =
            mockk {
                every { this@mockk(parameter = PagePlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)) } returns emptyFlow()
            },
        finishMemoUseCase: FinishMemoUseCase = mockk(),
        restartMemoUseCase: RestartMemoUseCase = mockk(),
        deleteMemoUseCase: DeleteMemoUseCase = mockk(),
        restoreMemoUseCase: RestoreMemoUseCase = mockk(),
    ): PlaceDetailMemoViewModel =
        PlaceDetailMemoViewModel(
            placeId = placeId,
            pagePlaceMemoUseCase = pagePlaceMemoUseCase,
            finishMemoUseCase = finishMemoUseCase,
            restartMemoUseCase = restartMemoUseCase,
            deleteMemoUseCase = deleteMemoUseCase,
            restoreMemoUseCase = restoreMemoUseCase,
        )

    private fun memo(): Memo =
        fixtureMonkey
            .giveMeKotlinBuilder<Memo>()
            .setExp(
                Memo::detail,
                fixtureMonkey.giveMeOne<MemoDetail>().copy(dateTime = null),
            ).setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
            .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
            .sample()
}
