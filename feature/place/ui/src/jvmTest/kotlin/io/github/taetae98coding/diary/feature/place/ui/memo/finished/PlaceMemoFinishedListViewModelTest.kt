@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.memo.finished

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedPlaceMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListEffect
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListUiState
import io.github.taetae98coding.diary.feature.place.ui.appendFailingPagingData
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class PlaceMemoFinishedListViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-e446b0d4-096a-4328-a3fa-1e26b34bd16f 항목별 완료 메모 paging에 주입된 Place ID를 전달한다") {
            runTest(mainDispatcher) {
                val placeId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = fixtureMonkey.memo(title = fixtureMonkey.nonBlankString())
                val pageUseCase = mockk<PageFinishedPlaceMemoUseCase>()
                every { pageUseCase(parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)) } returns
                    flowOf(Result.success(PagingData.from(listOf(memo))))
                val viewModel = viewModel(placeId = placeId, pageUseCase = pageUseCase)

                val itemList = flowOf(viewModel.memoPagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList.filterIsInstance<MemoListItem.Content>().map { it.memo } shouldBe listOf(memo)
                verify(exactly = 1) { pageUseCase(parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)) }
            }
        }

        test("TC-0fe6f585-5b4f-4239-b7fe-1107d3fc26ef 조회한 Place의 저장 이름을 상단 바 제목으로 노출한다") {
            runTest(mainDispatcher) {
                val name = fixtureMonkey.nonBlankString()
                val target = fixtureMonkey.place(title = name, isDeleted = false)
                val findUseCase = mockk<FindPlaceUseCase>()
                every { findUseCase(parameter = target.id) } returns flowOf(Result.success(target))
                val viewModel = viewModel(placeId = target.id, findUseCase = findUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe EntityMemoFinishedListUiState()
                    advanceUntilIdle()
                    awaitItem().title shouldBe name
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-3a803196-7a36-45a1-a26a-a896965402f3 Place를 조회할 수 없거나 조회에 실패하면 상단 바 이름을 비운다") {
            listOf<Result<Place?>>(
                Result.success(null),
                Result.failure(IllegalStateException()),
            ).forEach { result ->
                runTest(mainDispatcher) {
                    val placeId = fixtureMonkey.giveMeOne<Uuid>()
                    val findUseCase = mockk<FindPlaceUseCase>()
                    every { findUseCase(parameter = placeId) } returns flowOf(result)
                    val viewModel = viewModel(placeId = placeId, findUseCase = findUseCase)

                    viewModel.uiState.test {
                        awaitItem() shouldBe EntityMemoFinishedListUiState()
                        advanceUntilIdle()
                        expectNoEvents()
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("TC-7cb4d67b-a6e8-474a-a12c-457476e3212b Place의 저장 이름이 바뀌면 상단 바 이름을 갱신한다") {
            runTest(mainDispatcher) {
                val name = fixtureMonkey.nonBlankString()
                val target = fixtureMonkey.place(title = name, isDeleted = false)
                val targetFlow = MutableStateFlow<Result<Place?>>(Result.success(target))
                val findUseCase = mockk<FindPlaceUseCase>()
                every { findUseCase(parameter = target.id) } returns targetFlow
                val viewModel = viewModel(placeId = target.id, findUseCase = findUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe EntityMemoFinishedListUiState()
                    advanceUntilIdle()
                    awaitItem().title shouldBe name

                    val updatedName = name + fixtureMonkey.nonBlankString()

                    targetFlow.value = Result.success(target.let { it.copy(detail = it.detail.copy(title = updatedName)) })
                    advanceUntilIdle()
                    awaitItem().title shouldBe updatedName
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("Place의 상태 변화는 화면 상태에 영향을 주지 않는다") {
            listOf(
                fixtureMonkey.place(title = fixtureMonkey.nonBlankString(), isDeleted = true),
                fixtureMonkey.place(title = fixtureMonkey.nonBlankString(), isDeleted = false),
            ).forEach { target ->
                runTest(mainDispatcher) {
                    val findUseCase = mockk<FindPlaceUseCase>()
                    every { findUseCase(parameter = target.id) } returns flowOf(Result.success(target))
                    val viewModel = viewModel(placeId = target.id, findUseCase = findUseCase)

                    viewModel.uiState.test {
                        awaitItem() shouldBe EntityMemoFinishedListUiState()
                        advanceUntilIdle()
                        awaitItem() shouldBe EntityMemoFinishedListUiState(title = target.detail.title)
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("TC-a0008899-ace5-47f4-9c4c-279a5e617f7c 항목별 완료 메모 최초 조회가 실패하면 조회가 끝난 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val placeId = fixtureMonkey.giveMeOne<Uuid>()
                val pageUseCase = mockk<PageFinishedPlaceMemoUseCase>()
                every { pageUseCase(parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)) } returns
                    flowOf(Result.failure<PagingData<Memo>>(IllegalStateException()))
                val viewModel = viewModel(placeId = placeId, pageUseCase = pageUseCase)

                viewModel.memoPagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList shouldBe emptyList()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-a0008899-ace5-47f4-9c4c-279a5e617f7c 항목별 완료 메모 조회가 이어서 불러오기에 실패해도 이미 불러온 목록을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val placeId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = fixtureMonkey.memo(title = fixtureMonkey.nonBlankString())
                val pageUseCase = mockk<PageFinishedPlaceMemoUseCase>()
                every { pageUseCase(parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)) } returns
                    appendFailingPagingData(firstPage = listOf(memo)).map { pagingData -> Result.success(pagingData) }
                val viewModel = viewModel(placeId = placeId, pageUseCase = pageUseCase)

                val itemList =
                    flowOf(viewModel.memoPagingData.first()).asSnapshot(onError = { ErrorRecovery.RETURN_CURRENT_SNAPSHOT }) {
                        appendScrollWhile { true }
                    }

                itemList.filterIsInstance<MemoListItem.Content>().map { it.memo } shouldBe listOf(memo)
            }
        }

        test("TC-a0008899-ace5-47f4-9c4c-279a5e617f7c 항목별 완료 메모 조회가 다시 조회하다 실패하면 이전 목록을 남기지 않고 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val placeId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = fixtureMonkey.memo(title = fixtureMonkey.nonBlankString())
                val pageUseCase = mockk<PageFinishedPlaceMemoUseCase>()
                val resultFlow = MutableStateFlow<Result<PagingData<Memo>>>(Result.success(PagingData.from(listOf(memo))))
                every { pageUseCase(parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)) } returns resultFlow
                val viewModel = viewModel(placeId = placeId, pageUseCase = pageUseCase)

                viewModel.memoPagingData.test {
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot().filterIsInstance<MemoListItem.Content>().map { it.memo } shouldBe listOf(memo)

                    resultFlow.value = Result.failure(IllegalStateException())
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot() shouldBe emptyList()
                    expectNoEvents()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-3162b5e5-0c0b-4397-80b5-34b37ec951d6 메모 다시 시작에 성공하면 다시 시작 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                coEvery { restartMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(restartMemoUseCase = restartMemoUseCase)

                viewModel.effect.test {
                    viewModel.restart(id = memoId)
                    advanceUntilIdle()

                    awaitItem() shouldBe EntityMemoFinishedListEffect.Restarted(id = memoId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { restartMemoUseCase(parameter = memoId) }
            }
        }

        test("TC-2a3cb935-f956-466d-a1ad-c62843bda562 메모 다시 시작 저장에 실패하면 안내 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                coEvery { restartMemoUseCase(any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(restartMemoUseCase = restartMemoUseCase)

                viewModel.effect.test {
                    viewModel.restart(id = memoId)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-97bb8371-2b38-487e-b546-3e66dc21dcc8 다시 시작 실행 취소는 완료를 요청하고 Effect를 보내지 않는다") {
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

        test("TC-de3ead05-f69f-4c5e-9001-934c11e284b1 메모 삭제에 성공하면 삭제 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(deleteMemoUseCase = deleteMemoUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = memoId)
                    advanceUntilIdle()

                    awaitItem() shouldBe EntityMemoFinishedListEffect.Deleted(id = memoId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { deleteMemoUseCase(parameter = memoId) }
            }
        }

        test("TC-2a3cb935-f956-466d-a1ad-c62843bda562 메모 삭제 저장에 실패하면 안내 Effect를 보내지 않는다") {
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

        test("TC-97bb8371-2b38-487e-b546-3e66dc21dcc8 삭제 실행 취소는 복구를 요청하고 Effect를 보내지 않는다") {
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
        pageUseCase: PageFinishedPlaceMemoUseCase =
            mockk {
                every { this@mockk(parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)) } returns emptyFlow()
            },
        findUseCase: FindPlaceUseCase =
            mockk {
                every { this@mockk(parameter = placeId) } returns flowOf(Result.success(null))
            },
        restartMemoUseCase: RestartMemoUseCase = mockk(),
        finishMemoUseCase: FinishMemoUseCase = mockk(),
        deleteMemoUseCase: DeleteMemoUseCase = mockk(),
        restoreMemoUseCase: RestoreMemoUseCase = mockk(),
    ): PlaceMemoFinishedListViewModel =
        PlaceMemoFinishedListViewModel(
            placeId = placeId,
            pageFinishedPlaceMemoUseCase = pageUseCase,
            findPlaceUseCase = findUseCase,
            finishMemoUseCase = finishMemoUseCase,
            restartMemoUseCase = restartMemoUseCase,
            deleteMemoUseCase = deleteMemoUseCase,
            restoreMemoUseCase = restoreMemoUseCase,
        )
}
