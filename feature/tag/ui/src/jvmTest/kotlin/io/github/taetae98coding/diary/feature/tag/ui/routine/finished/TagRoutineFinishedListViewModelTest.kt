@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.routine.finished

import androidx.paging.PagingData
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.tag.tagDetail
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedTagRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.feature.tag.ui.appendFailingPagingData
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.nonBlankString
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
import kotlin.uuid.Uuid

class TagRoutineFinishedListViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-ae30f4e2-3f16-45d8-8969-7a43f8cddc57 대상 태그와 오늘로 조회한 완료된 루틴 목록을 순서 그대로 전달한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(4) { item() }
                val page = mockk<PageFinishedTagRoutineUseCase>()
                every { page(parameter = PageFinishedTagRoutineUseCase.Parameter(tagId = tagId, today = today)) } returns flowOf(Result.success(PagingData.from(itemList)))
                val viewModel = viewModel(tagId = tagId, pageFinishedTagRoutineUseCase = page)
                viewModel.updateToday(today = today)

                viewModel.routinePagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("오늘이 정해지기 전에는 완료된 루틴 목록을 조회하지 않는다") {
            runTest(mainDispatcher) {
                val page = mockk<PageFinishedTagRoutineUseCase>()
                val viewModel = viewModel(pageFinishedTagRoutineUseCase = page)

                viewModel.routinePagingData.test {
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    advanceUntilIdle()
                    expectNoEvents()
                }

                verify(exactly = 0) { page(parameter = any()) }
            }
        }

        test("TC-97a3a421-ee15-4c00-8b37-8970debded9d 오늘이 바뀌면 바뀐 오늘로 완료된 루틴 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val today = Arb.kotlinxLocalDate().next()
                val tomorrow = today.plus(1, DateTimeUnit.DAY)
                val todayList = List(2) { item() }
                val tomorrowList = todayList.reversed()
                val page = mockk<PageFinishedTagRoutineUseCase>()
                every { page(parameter = PageFinishedTagRoutineUseCase.Parameter(tagId = tagId, today = today)) } returns flowOf(Result.success(PagingData.from(todayList)))
                every { page(parameter = PageFinishedTagRoutineUseCase.Parameter(tagId = tagId, today = tomorrow)) } returns flowOf(Result.success(PagingData.from(tomorrowList)))
                val viewModel = viewModel(tagId = tagId, pageFinishedTagRoutineUseCase = page)
                viewModel.updateToday(today = today)

                viewModel.routinePagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe todayList

                    viewModel.updateToday(today = tomorrow)
                    flowOf(awaitItem()).asSnapshot() shouldBe tomorrowList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-5a72635e-bc1e-417d-be4c-2ffd293c95fb 최초 조회가 실패하면 루틴 없이 조회를 마친 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val page = mockk<PageFinishedTagRoutineUseCase>()
                every { page(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(Arb.string().next())))
                val viewModel = viewModel(pageFinishedTagRoutineUseCase = page)
                viewModel.updateToday(today = today)

                flowOf(viewModel.routinePagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-5a72635e-bc1e-417d-be4c-2ffd293c95fb 추가 조회가 실패하면 이미 조회한 루틴을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val itemList = List(2) { item() }
                val page = mockk<PageFinishedTagRoutineUseCase>()
                every { page(parameter = any()) } returns appendFailingPagingData(firstPage = itemList).map { pagingData -> Result.success(pagingData) }
                val viewModel = viewModel(pageFinishedTagRoutineUseCase = page)
                viewModel.updateToday(today = today)

                val snapshot =
                    flowOf(viewModel.routinePagingData.first()).asSnapshot(onError = { ErrorRecovery.RETURN_CURRENT_SNAPSHOT }) {
                        appendScrollWhile { true }
                    }

                snapshot shouldBe itemList
            }
        }

        test("TC-5a72635e-bc1e-417d-be4c-2ffd293c95fb 조회가 성공한 뒤 다시 조회하다 실패하면 이전 루틴을 남기지 않고 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val resultFlow = MutableStateFlow(Result.success(PagingData.from(List(2) { item() })))
                val page = mockk<PageFinishedTagRoutineUseCase>()
                every { page(parameter = any()) } returns resultFlow
                val viewModel = viewModel(pageFinishedTagRoutineUseCase = page)
                viewModel.updateToday(today = today)

                viewModel.routinePagingData.test {
                    flowOf(awaitItem()).asSnapshot().size shouldBe 2

                    resultFlow.value = Result.failure(IllegalStateException(Arb.string().next()))
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-156d6fab-eebe-40ad-aef5-54f471c7e311 조회한 태그의 이모지와 제목을 상단 바 제목으로 노출한다") {
            listOf(
                tagDetail(emoji = TAG_EMOJI, title = TAG_TITLE) to "$TAG_EMOJI $TAG_TITLE",
                tagDetail(emoji = "", title = TAG_TITLE) to TAG_TITLE,
            ).forEach { (detail, expectedTitle) ->
                runTest(mainDispatcher) {
                    val tag = tag().copy(detail = detail)
                    val findTagUseCase = mockk<FindTagUseCase>()
                    every { findTagUseCase(parameter = tag.id) } returns flowOf(Result.success(tag))
                    val viewModel = viewModel(tagId = tag.id, findTagUseCase = findTagUseCase)

                    viewModel.uiState.test {
                        awaitItem() shouldBe TagRoutineFinishedListUiState()
                        advanceUntilIdle()
                        awaitItem().title shouldBe expectedTitle
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("TC-804fb661-78ff-462e-bcf9-66eb81b795ec 태그를 조회할 수 없거나 조회에 실패하면 상단 바 태그 표시를 비운다") {
            listOf<Result<Tag?>>(
                Result.success(null),
                Result.failure(IllegalStateException()),
            ).forEach { result ->
                runTest(mainDispatcher) {
                    val tagId = Arb.kotlinUuid().next()
                    val findTagUseCase = mockk<FindTagUseCase>()
                    every { findTagUseCase(parameter = tagId) } returns flowOf(result)
                    val viewModel = viewModel(tagId = tagId, findTagUseCase = findTagUseCase)

                    viewModel.uiState.test {
                        awaitItem() shouldBe TagRoutineFinishedListUiState()
                        advanceUntilIdle()
                        expectNoEvents()
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("TC-8715c030-ff56-4724-ad52-b39984842c13 태그의 저장 이모지나 제목이 바뀌면 상단 바 표시를 갱신한다") {
            runTest(mainDispatcher) {
                val tag = tag().copy(detail = tagDetail(emoji = TAG_EMOJI, title = TAG_TITLE))
                val tagFlow = MutableStateFlow<Result<Tag?>>(Result.success(tag))
                val findTagUseCase = mockk<FindTagUseCase>()
                every { findTagUseCase(parameter = tag.id) } returns tagFlow
                val viewModel = viewModel(tagId = tag.id, findTagUseCase = findTagUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe TagRoutineFinishedListUiState()
                    advanceUntilIdle()
                    awaitItem().title shouldBe "$TAG_EMOJI $TAG_TITLE"

                    tagFlow.value = Result.success(tag.copy(detail = tagDetail(emoji = UPDATED_TAG_EMOJI, title = UPDATED_TAG_TITLE)))
                    advanceUntilIdle()
                    awaitItem().title shouldBe "$UPDATED_TAG_EMOJI $UPDATED_TAG_TITLE"
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-877460ce-f53f-45b1-a823-31ac37cc3c8a 대상 태그 조회가 실패해도 완료된 루틴 목록은 조회한 그대로 노출한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(2) { item() }
                val page = mockk<PageFinishedTagRoutineUseCase>()
                every { page(parameter = PageFinishedTagRoutineUseCase.Parameter(tagId = tagId, today = today)) } returns flowOf(Result.success(PagingData.from(itemList)))
                val findTagUseCase = mockk<FindTagUseCase>()
                every { findTagUseCase(parameter = tagId) } returns flowOf(Result.failure(IllegalStateException(Arb.string().next())))
                val viewModel = viewModel(tagId = tagId, pageFinishedTagRoutineUseCase = page, findTagUseCase = findTagUseCase)
                viewModel.updateToday(today = today)

                viewModel.uiState.test {
                    awaitItem() shouldBe TagRoutineFinishedListUiState()
                    advanceUntilIdle()
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
                flowOf(viewModel.routinePagingData.first()).asSnapshot() shouldBe itemList
            }
        }

        test("TC-03e6e171-84b7-4c94-92e9-af4d89ff5b40 다시 시작하면 그 루틴의 다시 시작을 요청하고 다시 시작 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val restart = mockk<RestartRoutineUseCase>()
                coEvery { restart(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(restartRoutineUseCase = restart)

                viewModel.effect.test {
                    viewModel.restart(id = id)

                    awaitItem() shouldBe RoutineListEffect.Restarted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { restart(parameter = id) }
            }
        }

        test("TC-63a0dbf9-4df1-4c8f-b222-96ede3ccba1f 삭제하면 그 루틴의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val delete = mockk<DeleteRoutineUseCase>()
                coEvery { delete(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(deleteRoutineUseCase = delete)

                viewModel.effect.test {
                    viewModel.delete(id = id)

                    awaitItem() shouldBe RoutineListEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { delete(parameter = id) }
            }
        }

        test("TC-2373eda6-54d7-4791-85cb-0f04f46f82af 다시 시작을 실행 취소하면 완료를, 삭제를 실행 취소하면 복구를 한 번 요청하고 안내는 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val finish = mockk<FinishRoutineUseCase>()
                val restore = mockk<RestoreRoutineUseCase>()
                coEvery { finish(parameter = id) } returns Result.success(1)
                coEvery { restore(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(finishRoutineUseCase = finish, restoreRoutineUseCase = restore)

                viewModel.effect.test {
                    viewModel.finish(id = id)
                    viewModel.restore(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { finish(parameter = id) }
                coVerify(exactly = 1) { restore(parameter = id) }
            }
        }

        test("TC-bb771e7c-c591-45d6-9227-e528841c8b61 다시 시작이나 삭제가 저장되지 못하면 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val restart = mockk<RestartRoutineUseCase>()
                val delete = mockk<DeleteRoutineUseCase>()
                coEvery { restart(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                coEvery { delete(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = viewModel(restartRoutineUseCase = restart, deleteRoutineUseCase = delete)

                viewModel.effect.test {
                    viewModel.restart(id = id)
                    viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("다시 시작을 처리하는 동안 같은 루틴의 다시 시작을 다시 요청해도 다시 시작 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val restart = mockk<RestartRoutineUseCase>()
                coEvery { restart(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = viewModel(restartRoutineUseCase = restart)

                viewModel.restart(id = id)
                runCurrent()
                viewModel.restart(id = id)
                runCurrent()
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { restart(parameter = id) }
            }
        }
    }

    private fun viewModel(
        tagId: Uuid = Arb.kotlinUuid().next(),
        pageFinishedTagRoutineUseCase: PageFinishedTagRoutineUseCase = mockk(),
        findTagUseCase: FindTagUseCase = mockk<FindTagUseCase>().also { useCase -> every { useCase(parameter = any()) } returns flowOf(Result.success(null)) },
        finishRoutineUseCase: FinishRoutineUseCase = mockk(),
        restartRoutineUseCase: RestartRoutineUseCase = mockk(),
        deleteRoutineUseCase: DeleteRoutineUseCase = mockk(),
        restoreRoutineUseCase: RestoreRoutineUseCase = mockk(),
    ): TagRoutineFinishedListViewModel =
        TagRoutineFinishedListViewModel(
            tagId = tagId,
            pageFinishedTagRoutineUseCase = pageFinishedTagRoutineUseCase,
            findTagUseCase = findTagUseCase,
            finishRoutineUseCase = finishRoutineUseCase,
            restartRoutineUseCase = restartRoutineUseCase,
            deleteRoutineUseCase = deleteRoutineUseCase,
            restoreRoutineUseCase = restoreRoutineUseCase,
        )

    private fun tagDetail(
        emoji: String,
        title: String,
    ): TagDetail = Arb.tagDetail().next().copy(emoji = emoji, title = title)

    // 이모지와 제목이 모두 비면 화면 상태가 초깃값과 같아져 새 상태가 전달되지 않는다.
    private fun tag(): Tag = Arb.tag().next().copy(detail = tagDetail(emoji = Arb.string().next(), title = Arb.nonBlankString().next()))

    private fun item(): RoutineSummary = Arb.routineSummary().next().let { summary -> summary.copy(routine = summary.routine.copy(isFinished = true, isDeleted = false)) }

    private companion object {
        const val TAG_EMOJI = "📌"
        const val TAG_TITLE = "TagTitle"
        const val UPDATED_TAG_EMOJI = "📎"
        const val UPDATED_TAG_TITLE = "UpdatedTagTitle"
    }
}
