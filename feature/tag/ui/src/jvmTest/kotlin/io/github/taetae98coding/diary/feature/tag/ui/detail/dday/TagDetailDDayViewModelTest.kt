@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.dday

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageTagDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.RestoreDDayUseCase
import io.github.taetae98coding.diary.feature.tag.ui.appendFailingPagingData
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class TagDetailDDayViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-08893b99-d83c-4ef2-8400-eb88e6695b68 TC-ad79fabc-7400-4ea8-9a8e-3bf040031533 상세 대상 태그로 조회한 디데이 목록을 순서 그대로 전달한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(3) { item() }
                val page = mockk<PageTagDDayUseCase>()
                every { page(parameter = PageTagDDayUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns flowOf(Result.success(PagingData.from(itemList)))
                val viewModel = TagDetailDDayViewModel(tagId = tagId, pageTagDDayUseCase = page, deleteDDayUseCase = mockk(), restoreDDayUseCase = mockk())

                viewModel.ddayPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { page(parameter = PageTagDDayUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) }
            }
        }

        test("TC-7ecf16c5-8c79-4fbd-818c-bae1fe9ebbbf 상세 대상 태그마다 그 태그로만 조회한다") {
            runTest(mainDispatcher) {
                val firstTagId = Arb.kotlinUuid().next()
                val secondTagId = Arb.kotlinUuid().next()
                val firstList = List(2) { item() }
                val secondList = List(2) { item() }
                val page = mockk<PageTagDDayUseCase>()
                every { page(parameter = PageTagDDayUseCase.Parameter(tagId = firstTagId, scope = TagScope.SELF)) } returns flowOf(Result.success(PagingData.from(firstList)))
                every { page(parameter = PageTagDDayUseCase.Parameter(tagId = secondTagId, scope = TagScope.SELF)) } returns flowOf(Result.success(PagingData.from(secondList)))

                // 화면은 상세 대상 태그마다 따로 ViewModel을 얻으므로 대상이 바뀌면 새 태그의 ViewModel이 목록을 낸다.
                val secondViewModel = TagDetailDDayViewModel(tagId = secondTagId, pageTagDDayUseCase = page, deleteDDayUseCase = mockk(), restoreDDayUseCase = mockk())

                secondViewModel.ddayPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe secondList
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 0) { page(parameter = match { parameter -> parameter.tagId == firstTagId }) }
            }
        }

        test("TC-6e914ebb-0357-4604-87f2-6dc4438a9e03 표시 범위를 넓혔다 되돌리면 디데이 목록을 각 범위 기준으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val selfList = List(2) { item() }
                val childList = selfList + List(2) { item() }
                val page = mockk<PageTagDDayUseCase>()
                every { page(parameter = PageTagDDayUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns flowOf(Result.success(PagingData.from(selfList)))
                every { page(parameter = PageTagDDayUseCase.Parameter(tagId = tagId, scope = TagScope.CHILD)) } returns flowOf(Result.success(PagingData.from(childList)))
                val viewModel = TagDetailDDayViewModel(tagId = tagId, pageTagDDayUseCase = page, deleteDDayUseCase = mockk(), restoreDDayUseCase = mockk())

                viewModel.ddayPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe selfList

                    viewModel.select(scope = TagScope.CHILD)
                    flowOf(awaitItem()).asSnapshot() shouldBe childList

                    viewModel.select(scope = TagScope.SELF)
                    flowOf(awaitItem()).asSnapshot() shouldBe selfList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-8e4c6495-f011-4ca9-a9b2-f8d1af9fef39 최초 조회가 실패하면 조회가 끝난 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val page = mockk<PageTagDDayUseCase>()
                every { page(parameter = PageTagDDayUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns flowOf(Result.failure(IllegalStateException(Arb.string().next())))
                val viewModel = TagDetailDDayViewModel(tagId = tagId, pageTagDDayUseCase = page, deleteDDayUseCase = mockk(), restoreDDayUseCase = mockk())

                viewModel.ddayPagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList.shouldBeEmpty()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-8e4c6495-f011-4ca9-a9b2-f8d1af9fef39 이어서 불러오기에 실패해도 이미 불러온 디데이를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(2) { item() }
                val page = mockk<PageTagDDayUseCase>()
                every { page(parameter = PageTagDDayUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns
                    appendFailingPagingData(firstPage = itemList).map { pagingData -> Result.success(pagingData) }
                val viewModel = TagDetailDDayViewModel(tagId = tagId, pageTagDDayUseCase = page, deleteDDayUseCase = mockk(), restoreDDayUseCase = mockk())

                val snapshot =
                    flowOf(viewModel.ddayPagingData.first()).asSnapshot(onError = { ErrorRecovery.RETURN_CURRENT_SNAPSHOT }) {
                        appendScrollWhile { true }
                    }

                snapshot shouldBe itemList
            }
        }

        test("TC-8e4c6495-f011-4ca9-a9b2-f8d1af9fef39 조회가 성공한 뒤 다시 조회하다 실패하면 이전 디데이를 남기지 않고 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(2) { item() }
                val page = mockk<PageTagDDayUseCase>()
                val resultFlow = MutableStateFlow<Result<PagingData<DDay>>>(Result.success(PagingData.from(itemList)))
                every { page(parameter = PageTagDDayUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns resultFlow
                val viewModel = TagDetailDDayViewModel(tagId = tagId, pageTagDDayUseCase = page, deleteDDayUseCase = mockk(), restoreDDayUseCase = mockk())

                viewModel.ddayPagingData.test {
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList

                    resultFlow.value = Result.failure(IllegalStateException(Arb.string().next()))
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    expectNoEvents()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-4382250e-5614-4d93-b8e0-3a543a092105 삭제하면 태그 연결 해제가 아니라 그 디데이의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val delete = mockk<DeleteDDayUseCase>()
                coEvery { delete(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailDDayViewModel(tagId = Arb.kotlinUuid().next(), pageTagDDayUseCase = mockk(), deleteDDayUseCase = delete, restoreDDayUseCase = mockk())

                viewModel.effect.test {
                    viewModel.delete(id = id)

                    awaitItem() shouldBe DDayListEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { delete(parameter = id) }
            }
        }

        test("TC-4e316c58-2cf0-4839-bf4a-c0a401062ea7 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val delete = mockk<DeleteDDayUseCase>()
                coEvery { delete(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = TagDetailDDayViewModel(tagId = Arb.kotlinUuid().next(), pageTagDDayUseCase = mockk(), deleteDDayUseCase = delete, restoreDDayUseCase = mockk())

                viewModel.effect.test {
                    viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-cec6fa57-4f18-4ca8-9343-2cfb0249e203 실행 취소하면 그 디데이의 삭제를 되돌리는 요청을 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val restore = mockk<RestoreDDayUseCase>()
                coEvery { restore(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailDDayViewModel(tagId = Arb.kotlinUuid().next(), pageTagDDayUseCase = mockk(), deleteDDayUseCase = mockk(), restoreDDayUseCase = restore)

                viewModel.restore(id = id)
                advanceUntilIdle()

                coVerify(exactly = 1) { restore(parameter = id) }
            }
        }

        test("삭제를 처리하는 동안 같은 디데이의 삭제를 다시 요청해도 삭제 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val delete = mockk<DeleteDDayUseCase>()
                coEvery { delete(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = TagDetailDDayViewModel(tagId = Arb.kotlinUuid().next(), pageTagDDayUseCase = mockk(), deleteDDayUseCase = delete, restoreDDayUseCase = mockk())

                viewModel.delete(id = id)
                viewModel.delete(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { delete(parameter = id) }
            }
        }

        test("복구를 처리하는 동안 같은 디데이의 복구를 다시 요청해도 복구 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val restore = mockk<RestoreDDayUseCase>()
                coEvery { restore(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = TagDetailDDayViewModel(tagId = Arb.kotlinUuid().next(), pageTagDDayUseCase = mockk(), deleteDDayUseCase = mockk(), restoreDDayUseCase = restore)

                viewModel.restore(id = id)
                viewModel.restore(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { restore(parameter = id) }
            }
        }
    }

    private companion object {
        private fun item(): DDay = Arb.dday().next().copy(isDeleted = false)
    }
}
