@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.routine

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageTagRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.feature.tag.ui.appendFailingPagingData
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
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
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

class TagDetailRoutineViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-c6eef3a9-261c-4525-98f7-dcfac6e409d0 TC-d6db5574-94bc-44f0-8a9c-f6de4cfd94f1 상세 대상 태그로 조회한 루틴 목록을 순서 그대로 전달한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(3) { item() }
                val page = mockk<PageTagRoutineUseCase>()
                every { page(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, today = today)) } returns flowOf(Result.success(PagingData.from(itemList)))
                val viewModel = TagDetailRoutineViewModel(tagId = tagId, pageTagRoutineUseCase = page, finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())
                viewModel.updateToday(today = today)

                viewModel.routinePagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { page(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, today = today)) }
            }
        }

        test("TC-d854877e-29e2-49dd-8930-df977c2eea03 상세 대상 태그마다 그 태그로만 조회한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val firstTagId = Arb.kotlinUuid().next()
                val secondTagId = Arb.kotlinUuid().next()
                val firstList = List(2) { item() }
                val secondList = List(2) { item() }
                val page = mockk<PageTagRoutineUseCase>()
                every { page(parameter = PageTagRoutineUseCase.Parameter(tagId = firstTagId, scope = TagScope.SELF, today = today)) } returns flowOf(Result.success(PagingData.from(firstList)))
                every { page(parameter = PageTagRoutineUseCase.Parameter(tagId = secondTagId, scope = TagScope.SELF, today = today)) } returns flowOf(Result.success(PagingData.from(secondList)))

                // 화면은 상세 대상 태그마다 따로 ViewModel을 얻으므로 대상이 바뀌면 새 태그의 ViewModel이 목록을 낸다.
                val secondViewModel = TagDetailRoutineViewModel(tagId = secondTagId, pageTagRoutineUseCase = page, finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())
                secondViewModel.updateToday(today = today)

                secondViewModel.routinePagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe secondList
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 0) { page(parameter = match { parameter -> parameter.tagId == firstTagId }) }
            }
        }

        test("TC-6e914ebb-0357-4604-87f2-6dc4438a9e03 표시 범위를 넓혔다 되돌리면 루틴 목록을 각 범위 기준으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val tagId = Arb.kotlinUuid().next()
                val selfList = List(2) { item() }
                val childList = selfList + List(2) { item() }
                val page = mockk<PageTagRoutineUseCase>()
                every { page(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, today = today)) } returns flowOf(Result.success(PagingData.from(selfList)))
                every { page(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = TagScope.CHILD, today = today)) } returns flowOf(Result.success(PagingData.from(childList)))
                val viewModel = TagDetailRoutineViewModel(tagId = tagId, pageTagRoutineUseCase = page, finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())
                viewModel.updateToday(today = today)

                viewModel.routinePagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe selfList

                    viewModel.select(scope = TagScope.CHILD)
                    flowOf(awaitItem()).asSnapshot() shouldBe childList

                    viewModel.select(scope = TagScope.SELF)
                    flowOf(awaitItem()).asSnapshot() shouldBe selfList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("오늘이 정해지기 전에는 루틴 목록을 조회하지 않는다") {
            runTest(mainDispatcher) {
                val page = mockk<PageTagRoutineUseCase>()
                val viewModel = TagDetailRoutineViewModel(tagId = Arb.kotlinUuid().next(), pageTagRoutineUseCase = page, finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())

                viewModel.routinePagingData.test {
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    advanceUntilIdle()
                    expectNoEvents()
                }

                verify(exactly = 0) { page(parameter = any()) }
            }
        }

        test("TC-91435776-71d2-476b-b211-619f1bf8faf0 오늘이 바뀌면 바뀐 오늘로 루틴 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val today = Arb.kotlinxLocalDate().next()
                val nextDay = today.plus(1, DateTimeUnit.DAY)
                val todayList = List(2) { item() }
                val nextDayList = todayList.reversed()
                val page = mockk<PageTagRoutineUseCase>()
                every { page(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, today = today)) } returns flowOf(Result.success(PagingData.from(todayList)))
                every { page(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, today = nextDay)) } returns flowOf(Result.success(PagingData.from(nextDayList)))
                val viewModel = TagDetailRoutineViewModel(tagId = tagId, pageTagRoutineUseCase = page, finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())
                viewModel.updateToday(today = today)

                viewModel.routinePagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe todayList

                    viewModel.updateToday(today = nextDay)
                    flowOf(awaitItem()).asSnapshot() shouldBe nextDayList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-bdfcd20b-ecb1-4559-a9f7-13d0ae2fe74d 같은 오늘을 다시 받으면 루틴 목록을 다시 조회하지 않는다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val today = Arb.kotlinxLocalDate().next()
                val itemList = List(2) { item() }
                val page = mockk<PageTagRoutineUseCase>()
                every { page(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, today = today)) } returns flowOf(Result.success(PagingData.from(itemList)))
                val viewModel = TagDetailRoutineViewModel(tagId = tagId, pageTagRoutineUseCase = page, finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())
                viewModel.updateToday(today = today)

                viewModel.routinePagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList

                    viewModel.updateToday(today = today)
                    advanceUntilIdle()
                    expectNoEvents()
                }

                verify(exactly = 1) { page(parameter = any()) }
            }
        }

        test("TC-36a384cc-de03-4b96-afa5-908992377649 최초 조회가 실패하면 조회가 끝난 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val tagId = Arb.kotlinUuid().next()
                val page = mockk<PageTagRoutineUseCase>()
                every { page(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, today = today)) } returns flowOf(Result.failure(IllegalStateException(Arb.string().next())))
                val viewModel = TagDetailRoutineViewModel(tagId = tagId, pageTagRoutineUseCase = page, finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())
                viewModel.updateToday(today = today)

                viewModel.routinePagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList.shouldBeEmpty()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-36a384cc-de03-4b96-afa5-908992377649 이어서 불러오기에 실패해도 이미 불러온 루틴을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(2) { item() }
                val page = mockk<PageTagRoutineUseCase>()
                every { page(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, today = today)) } returns
                    appendFailingPagingData(firstPage = itemList).map { pagingData -> Result.success(pagingData) }
                val viewModel = TagDetailRoutineViewModel(tagId = tagId, pageTagRoutineUseCase = page, finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())
                viewModel.updateToday(today = today)

                val snapshot =
                    flowOf(viewModel.routinePagingData.first()).asSnapshot(onError = { ErrorRecovery.RETURN_CURRENT_SNAPSHOT }) {
                        appendScrollWhile { true }
                    }

                snapshot shouldBe itemList
            }
        }

        test("TC-36a384cc-de03-4b96-afa5-908992377649 조회가 성공한 뒤 다시 조회하다 실패하면 이전 루틴을 남기지 않고 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(2) { item() }
                val page = mockk<PageTagRoutineUseCase>()
                val resultFlow = MutableStateFlow<Result<PagingData<RoutineSummary>>>(Result.success(PagingData.from(itemList)))
                every { page(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, today = today)) } returns resultFlow
                val viewModel = TagDetailRoutineViewModel(tagId = tagId, pageTagRoutineUseCase = page, finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())
                viewModel.updateToday(today = today)

                viewModel.routinePagingData.test {
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

        test("TC-78c2f045-7fd9-423f-ba6d-5de1e37027d2 삭제하면 태그 연결 해제가 아니라 그 루틴의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val delete = mockk<DeleteRoutineUseCase>()
                coEvery { delete(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailRoutineViewModel(tagId = Arb.kotlinUuid().next(), pageTagRoutineUseCase = mockk(), finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = delete, restoreRoutineUseCase = mockk())

                viewModel.effect.test {
                    viewModel.delete(id = id)

                    awaitItem() shouldBe RoutineListEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { delete(parameter = id) }
            }
        }

        test("TC-587c148b-1d6a-4fd5-9636-a72536722eb1 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val delete = mockk<DeleteRoutineUseCase>()
                coEvery { delete(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = TagDetailRoutineViewModel(tagId = Arb.kotlinUuid().next(), pageTagRoutineUseCase = mockk(), finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = delete, restoreRoutineUseCase = mockk())

                viewModel.effect.test {
                    viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-cec32aea-92d1-4049-b1f1-95d6c0ddb624 실행 취소하면 그 루틴의 삭제를 되돌리는 요청을 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val restore = mockk<RestoreRoutineUseCase>()
                coEvery { restore(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailRoutineViewModel(tagId = Arb.kotlinUuid().next(), pageTagRoutineUseCase = mockk(), finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = restore)

                viewModel.restore(id = id)
                advanceUntilIdle()

                coVerify(exactly = 1) { restore(parameter = id) }
            }
        }

        test("TC-648019bb-ee48-40e7-9ad8-62a28ae546d8 완료하면 삭제가 아니라 그 루틴의 완료를 요청하고 완료 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val finish = mockk<FinishRoutineUseCase>()
                val delete = mockk<DeleteRoutineUseCase>()
                coEvery { finish(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailRoutineViewModel(tagId = Arb.kotlinUuid().next(), pageTagRoutineUseCase = mockk(), finishRoutineUseCase = finish, restartRoutineUseCase = mockk(), deleteRoutineUseCase = delete, restoreRoutineUseCase = mockk())

                viewModel.effect.test {
                    viewModel.finish(id = id)

                    awaitItem() shouldBe RoutineListEffect.Finished(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { finish(parameter = id) }
                coVerify(exactly = 0) { delete(parameter = any()) }
            }
        }

        test("TC-17f1723e-ce7c-46fa-ac4d-5693c8b622f2 완료가 저장되지 못하면 완료 안내를 보내지 않고 다시 완료를 요청할 수 있다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val finish = mockk<FinishRoutineUseCase>()
                coEvery { finish(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = TagDetailRoutineViewModel(tagId = Arb.kotlinUuid().next(), pageTagRoutineUseCase = mockk(), finishRoutineUseCase = finish, restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())

                viewModel.effect.test {
                    viewModel.finish(id = id)
                    advanceUntilIdle()
                    viewModel.finish(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 2) { finish(parameter = id) }
            }
        }

        test("TC-c3202b93-f1d8-45fc-8c20-7760d467065a 완료를 실행 취소하면 그 루틴을 다시 시작하는 요청을 한 번 보내고 안내는 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val restart = mockk<RestartRoutineUseCase>()
                coEvery { restart(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailRoutineViewModel(tagId = Arb.kotlinUuid().next(), pageTagRoutineUseCase = mockk(), finishRoutineUseCase = mockk(), restartRoutineUseCase = restart, deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())

                viewModel.effect.test {
                    viewModel.restart(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { restart(parameter = id) }
            }
        }

        test("완료를 처리하는 동안 같은 루틴의 완료를 다시 요청해도 완료 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val finish = mockk<FinishRoutineUseCase>()
                coEvery { finish(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = TagDetailRoutineViewModel(tagId = Arb.kotlinUuid().next(), pageTagRoutineUseCase = mockk(), finishRoutineUseCase = finish, restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())

                viewModel.finish(id = id)
                runCurrent()
                viewModel.finish(id = id)
                runCurrent()
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { finish(parameter = id) }
            }
        }

        test("삭제를 처리하는 동안 같은 루틴의 삭제를 다시 요청해도 삭제 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val delete = mockk<DeleteRoutineUseCase>()
                coEvery { delete(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = TagDetailRoutineViewModel(tagId = Arb.kotlinUuid().next(), pageTagRoutineUseCase = mockk(), finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = delete, restoreRoutineUseCase = mockk())

                viewModel.delete(id = id)
                runCurrent()
                viewModel.delete(id = id)
                runCurrent()
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { delete(parameter = id) }
            }
        }

        test("복구를 처리하는 동안 같은 루틴의 복구를 다시 요청해도 복구 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val restore = mockk<RestoreRoutineUseCase>()
                coEvery { restore(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = TagDetailRoutineViewModel(tagId = Arb.kotlinUuid().next(), pageTagRoutineUseCase = mockk(), finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = restore)

                viewModel.restore(id = id)
                runCurrent()
                viewModel.restore(id = id)
                runCurrent()
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { restore(parameter = id) }
            }
        }
    }

    private companion object {
        private fun item(): RoutineSummary = Arb.routineSummary().next().let { summary -> summary.copy(routine = summary.routine.copy(isFinished = false, isDeleted = false)) }
    }
}
