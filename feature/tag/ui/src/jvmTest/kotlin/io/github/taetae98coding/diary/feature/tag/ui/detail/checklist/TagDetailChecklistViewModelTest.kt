@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.checklist

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.checklist.ChecklistListEffect
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.PageTagChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.RestoreChecklistUseCase
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
import kotlin.uuid.Uuid

class TagDetailChecklistViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-c2a90aea-0184-45eb-90f7-ac9326d34dfa TC-f7d0f491-98c6-4b46-a9a9-764d162c736c 상세 대상 태그로 조회한 체크리스트 목록을 순서 그대로 전달한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(3) { item() }
                val page = mockk<PageTagChecklistUseCase>()
                every { page(parameter = PageTagChecklistUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns flowOf(Result.success(PagingData.from(itemList)))
                val viewModel = TagDetailChecklistViewModel(tagId = tagId, pageChecklistUseCase = page, deleteChecklistUseCase = mockk(), restoreChecklistUseCase = mockk())

                viewModel.checklistPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { page(parameter = PageTagChecklistUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) }
            }
        }

        test("TC-d94ab6a2-043c-4ce2-b8c6-85c6b0ad96bd 상세 대상 태그로만 조회한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val otherTagId = Arb.kotlinUuid().next()
                val page = mockk<PageTagChecklistUseCase>()
                every { page(parameter = any()) } returns flowOf(Result.success(PagingData.from(emptyList<ChecklistSummary>())))
                val viewModel = TagDetailChecklistViewModel(tagId = tagId, pageChecklistUseCase = page, deleteChecklistUseCase = mockk(), restoreChecklistUseCase = mockk())

                viewModel.checklistPagingData.test {
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { page(parameter = PageTagChecklistUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) }
                verify(exactly = 0) { page(parameter = PageTagChecklistUseCase.Parameter(tagId = otherTagId, scope = TagScope.SELF)) }
            }
        }

        test("TC-6e914ebb-0357-4604-87f2-6dc4438a9e03 표시 범위를 넓혔다 되돌리면 체크리스트 목록을 각 범위 기준으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val selfList = List(2) { item() }
                val childList = selfList + List(2) { item() }
                val page = mockk<PageTagChecklistUseCase>()
                every { page(parameter = PageTagChecklistUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns flowOf(Result.success(PagingData.from(selfList)))
                every { page(parameter = PageTagChecklistUseCase.Parameter(tagId = tagId, scope = TagScope.CHILD)) } returns flowOf(Result.success(PagingData.from(childList)))
                val viewModel = TagDetailChecklistViewModel(tagId = tagId, pageChecklistUseCase = page, deleteChecklistUseCase = mockk(), restoreChecklistUseCase = mockk())

                viewModel.checklistPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe selfList

                    viewModel.select(scope = TagScope.CHILD)
                    flowOf(awaitItem()).asSnapshot() shouldBe childList

                    viewModel.select(scope = TagScope.SELF)
                    flowOf(awaitItem()).asSnapshot() shouldBe selfList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-49e49fc0-cd1e-44c6-8910-8f2a37732a30 최초 조회가 실패하면 조회가 끝난 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val page = mockk<PageTagChecklistUseCase>()
                every { page(parameter = PageTagChecklistUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns flowOf(Result.failure(IllegalStateException(Arb.string().next())))
                val viewModel = TagDetailChecklistViewModel(tagId = tagId, pageChecklistUseCase = page, deleteChecklistUseCase = mockk(), restoreChecklistUseCase = mockk())

                viewModel.checklistPagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList.shouldBeEmpty()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-49e49fc0-cd1e-44c6-8910-8f2a37732a30 이어서 불러오기에 실패해도 이미 불러온 체크리스트을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(2) { item() }
                val page = mockk<PageTagChecklistUseCase>()
                every { page(parameter = PageTagChecklistUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns
                    appendFailingPagingData(firstPage = itemList).map { pagingData -> Result.success(pagingData) }
                val viewModel = TagDetailChecklistViewModel(tagId = tagId, pageChecklistUseCase = page, deleteChecklistUseCase = mockk(), restoreChecklistUseCase = mockk())

                val snapshot =
                    flowOf(viewModel.checklistPagingData.first()).asSnapshot(onError = { ErrorRecovery.RETURN_CURRENT_SNAPSHOT }) {
                        appendScrollWhile { true }
                    }

                snapshot shouldBe itemList
            }
        }

        test("TC-49e49fc0-cd1e-44c6-8910-8f2a37732a30 조회가 성공한 뒤 다시 조회하다 실패하면 이전 체크리스트을 남기지 않고 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(2) { item() }
                val page = mockk<PageTagChecklistUseCase>()
                val resultFlow = MutableStateFlow<Result<PagingData<ChecklistSummary>>>(Result.success(PagingData.from(itemList)))
                every { page(parameter = PageTagChecklistUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns resultFlow
                val viewModel = TagDetailChecklistViewModel(tagId = tagId, pageChecklistUseCase = page, deleteChecklistUseCase = mockk(), restoreChecklistUseCase = mockk())

                viewModel.checklistPagingData.test {
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

        test("삭제를 처리하는 동안 같은 체크리스트의 삭제를 다시 요청해도 삭제 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val delete = mockk<DeleteChecklistUseCase>()
                coEvery { delete(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = TagDetailChecklistViewModel(tagId = Arb.kotlinUuid().next(), pageChecklistUseCase = mockk(), deleteChecklistUseCase = delete, restoreChecklistUseCase = mockk())

                viewModel.delete(id = id)
                viewModel.delete(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { delete(parameter = id) }
            }
        }

        test("복구를 처리하는 동안 같은 체크리스트의 복구를 다시 요청해도 복구 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val restore = mockk<RestoreChecklistUseCase>()
                coEvery { restore(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = TagDetailChecklistViewModel(tagId = Arb.kotlinUuid().next(), pageChecklistUseCase = mockk(), deleteChecklistUseCase = mockk(), restoreChecklistUseCase = restore)

                viewModel.restore(id = id)
                viewModel.restore(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { restore(parameter = id) }
            }
        }

        test("TC-7e3282b8-98a9-41f0-bee2-7bd0fcbfe608 삭제하면 태그 연결 해제가 아니라 그 체크리스트의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val delete = mockk<DeleteChecklistUseCase>()
                coEvery { delete(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailChecklistViewModel(tagId = Arb.kotlinUuid().next(), pageChecklistUseCase = mockk(), deleteChecklistUseCase = delete, restoreChecklistUseCase = mockk())

                viewModel.effect.test {
                    viewModel.delete(id = id)

                    awaitItem() shouldBe ChecklistListEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { delete(parameter = id) }
            }
        }

        test("TC-0a131c9e-1960-448f-8160-68595b0781ec 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val delete = mockk<DeleteChecklistUseCase>()
                coEvery { delete(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = TagDetailChecklistViewModel(tagId = Arb.kotlinUuid().next(), pageChecklistUseCase = mockk(), deleteChecklistUseCase = delete, restoreChecklistUseCase = mockk())

                viewModel.effect.test {
                    viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-a531816b-4c56-4189-b2ce-16a1e8e47c8e 실행 취소하면 그 체크리스트의 삭제를 되돌리는 요청을 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val restore = mockk<RestoreChecklistUseCase>()
                coEvery { restore(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailChecklistViewModel(tagId = Arb.kotlinUuid().next(), pageChecklistUseCase = mockk(), deleteChecklistUseCase = mockk(), restoreChecklistUseCase = restore)

                viewModel.restore(id = id)
                advanceUntilIdle()

                coVerify(exactly = 1) { restore(parameter = id) }
            }
        }
    }

    private companion object {
        private fun item(): ChecklistSummary =
            ChecklistSummary(
                checklist = Arb.checklist(isDeleted = false).next(),
                checkedCount = 1,
                totalCount = 2,
            )
    }
}
