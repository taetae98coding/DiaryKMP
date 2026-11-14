@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.home.filter

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineViewScopeUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.SelectRoutineFilterTagUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.SetRoutineViewScopeUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UnselectAllRoutineFilterTagUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UnselectRoutineFilterTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.routine.ui.home.routineViewScopeUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class RoutineHomeFilterViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-65a5f4df-ef5f-483c-961b-a8fdbe5541fc 검색어 없이 조회한 선택할 수 있는 태그를 순서대로 노출한다") {
            runTest(mainDispatcher) {
                val tagList = List(3) { Arb.tag().next() }
                val pageTagUseCase = mockk<PageTagUseCase>()
                every { pageTagUseCase(parameter = "") } returns flowOf(Result.success(PagingData.from(tagList)))
                val viewModel = viewModel(pageTagUseCase = pageTagUseCase)

                flowOf(viewModel.tagPagingData.first()).asSnapshot() shouldBe tagList

                verify(exactly = 1) { pageTagUseCase(parameter = "") }
            }
        }

        test("TC-00f9ccc5-62c8-4f55-8a1f-5ea23dcd2137 필터를 다시 열면 저장된 두 태그 선택을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val selectedTagList = List(2) { Arb.tag().next() }
                val filterFlow = flowOf(Result.success(selectedTagList))
                val expected = RoutineHomeFilterUiState(selectedTagIdSet = selectedTagList.map { tag -> tag.id }.toSet())

                repeat(2) {
                    val viewModel = viewModel(filterFlow = filterFlow)

                    viewModel.uiState.test {
                        awaitItem() shouldBe RoutineHomeFilterUiState()
                        advanceUntilIdle()
                        awaitItem() shouldBe expected
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("TC-b44f19f0-b15a-4e76-8115-63ae5013a07f 태그를 선택하면 선택 저장을 한 번 요청한다") {
            runTest(mainDispatcher) {
                val tag = Arb.tag().next()
                val selectUseCase = mockk<SelectRoutineFilterTagUseCase>()
                coEvery { selectUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(selectRoutineFilterTagUseCase = selectUseCase)

                viewModel.selectTag(id = tag.id)
                advanceUntilIdle()

                coVerify(exactly = 1) { selectUseCase(parameter = tag.id) }
            }
        }

        test("TC-e0a4632d-5772-4d2a-82d1-c555027f3aec 필터 선택을 저장하지 못하면 바꾸기 전의 선택을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val tagA = Arb.tag().next()
                val tagB = Arb.tag().next()
                val selectUseCase = mockk<SelectRoutineFilterTagUseCase>()
                coEvery { selectUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel =
                    viewModel(
                        filterFlow = flowOf(Result.success(listOf(tagA))),
                        selectRoutineFilterTagUseCase = selectUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineHomeFilterUiState()
                    advanceUntilIdle()
                    awaitItem() shouldBe RoutineHomeFilterUiState(selectedTagIdSet = setOf(tagA.id))

                    viewModel.selectTag(id = tagB.id)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value shouldBe RoutineHomeFilterUiState(selectedTagIdSet = setOf(tagA.id))
                }

                coVerify(exactly = 1) { selectUseCase(parameter = tagB.id) }
            }
        }

        test("TC-1e0ead9c-3a07-4d75-aeea-e9f1e17b33fa 선택 전체 해제를 실행하면 전체 해제를 한 번 요청한다") {
            runTest(mainDispatcher) {
                val unselectAllUseCase = mockk<UnselectAllRoutineFilterTagUseCase>()
                coEvery { unselectAllUseCase() } returns Result.success(Unit)
                val viewModel = viewModel(unselectAllRoutineFilterTagUseCase = unselectAllUseCase)

                viewModel.unselectAllTag()
                advanceUntilIdle()

                coVerify(exactly = 1) { unselectAllUseCase() }
            }
        }

        test("태그 선택을 해제하면 선택 해제를 한 번 요청한다") {
            runTest(mainDispatcher) {
                val tag = Arb.tag().next()
                val unselectUseCase = mockk<UnselectRoutineFilterTagUseCase>()
                coEvery { unselectUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(unselectRoutineFilterTagUseCase = unselectUseCase)

                viewModel.unselectTag(id = tag.id)
                advanceUntilIdle()

                coVerify(exactly = 1) { unselectUseCase(parameter = tag.id) }
            }
        }

        viewScopeTests()
        viewScopeSaveTests()
        viewScopeFailureTests()
        viewScopeTagTests()
    }

    private fun viewScopeTests() {
        test("TC-829e32df-b32a-4101-812c-58ee288ec037 보기 범위를 한 번도 바꾸지 않았으면 오늘 할 루틴을 고른 것으로 노출한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel()

                viewModel.uiState.test {
                    awaitItem().viewScope shouldBe RoutineViewScope.TODAY
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-0bff665b-1451-463f-886f-9e8bd7d230ec 필터를 다시 열면 저장된 보기 범위 선택을 그대로 노출한다") {
            runTest(mainDispatcher) {
                // 필터를 닫았다가 다시 열면 필터의 상태 홀더가 새로 만들어진다.
                repeat(2) {
                    val viewModel = viewModel(viewScopeFlow = flowOf(RoutineViewScope.ALL))

                    viewModel.uiState.test {
                        awaitItem() shouldBe RoutineHomeFilterUiState()
                        advanceUntilIdle()
                        awaitItem() shouldBe RoutineHomeFilterUiState(viewScope = RoutineViewScope.ALL)
                    }
                }
            }
        }

        test("TC-07fef83b-4cac-4e3a-8c90-ed97fcd1cf84 보기 범위를 고르면 확정 없이 저장을 요청하고 저장된 선택을 노출한다") {
            runTest(mainDispatcher) {
                val viewScopeFlow = MutableStateFlow(RoutineViewScope.TODAY)
                val setUseCase = mockk<SetRoutineViewScopeUseCase>()
                coEvery { setUseCase(parameter = any()) } answers {
                    viewScopeFlow.value = firstArg()
                    Result.success(Unit)
                }
                val viewModel = viewModel(viewScopeFlow = viewScopeFlow, setRoutineViewScopeUseCase = setUseCase)

                viewModel.uiState.test {
                    awaitItem().viewScope shouldBe RoutineViewScope.TODAY

                    viewModel.setViewScope(viewScope = RoutineViewScope.ALL)
                    advanceUntilIdle()
                    expectMostRecentItem().viewScope shouldBe RoutineViewScope.ALL

                    viewModel.setViewScope(viewScope = RoutineViewScope.TODAY)
                    advanceUntilIdle()
                    expectMostRecentItem().viewScope shouldBe RoutineViewScope.TODAY
                }

                coVerifyOrder {
                    setUseCase(parameter = RoutineViewScope.ALL)
                    setUseCase(parameter = RoutineViewScope.TODAY)
                }
            }
        }
    }

    private fun viewScopeSaveTests() {
        test("TC-80220c94-0abb-4324-ba26-4297993a422d 보기 범위를 고르면 고른 값으로 저장을 한 번 요청한다") {
            runTest(mainDispatcher) {
                val setUseCase = mockk<SetRoutineViewScopeUseCase>()
                coEvery { setUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(setRoutineViewScopeUseCase = setUseCase)

                viewModel.setViewScope(viewScope = RoutineViewScope.ALL)
                advanceUntilIdle()

                coVerify(exactly = 1) { setUseCase(parameter = RoutineViewScope.ALL) }
            }
        }

        test("TC-8389da95-7521-4222-9754-2e7007fb3d13 이미 고른 보기 범위를 다시 골라도 노출하는 선택은 바뀌지 않는다") {
            runTest(mainDispatcher) {
                RoutineViewScope.entries.forEach { viewScope ->
                    val viewScopeFlow = MutableStateFlow(viewScope)
                    val setUseCase = mockk<SetRoutineViewScopeUseCase>()
                    coEvery { setUseCase(parameter = any()) } answers {
                        viewScopeFlow.value = firstArg()
                        Result.success(Unit)
                    }
                    val viewModel = viewModel(viewScopeFlow = viewScopeFlow, setRoutineViewScopeUseCase = setUseCase)

                    val emittedList = mutableListOf<RoutineHomeFilterUiState>()
                    val job = backgroundScope.launch { viewModel.uiState.collect { uiState -> emittedList += uiState } }
                    advanceUntilIdle()
                    val emittedCount = emittedList.size
                    viewModel.uiState.value.viewScope shouldBe viewScope

                    viewModel.setViewScope(viewScope = viewScope)
                    advanceUntilIdle()

                    emittedList.size shouldBe emittedCount
                    viewModel.uiState.value.viewScope shouldBe viewScope
                    job.cancel()
                }
            }
        }
    }

    private fun viewScopeFailureTests() {
        test("TC-ab4d2410-0156-42b1-b2f5-e77016b9d15e 보기 범위 선택을 저장하지 못하면 바꾸기 전의 선택을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val setUseCase = mockk<SetRoutineViewScopeUseCase>()
                coEvery { setUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(setRoutineViewScopeUseCase = setUseCase)

                viewModel.uiState.test {
                    awaitItem().viewScope shouldBe RoutineViewScope.TODAY

                    viewModel.setViewScope(viewScope = RoutineViewScope.ALL)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value.viewScope shouldBe RoutineViewScope.TODAY
                }

                coVerify(exactly = 1) { setUseCase(parameter = RoutineViewScope.ALL) }
            }
        }

        test("보기 범위 저장에 실패한 뒤에도 다시 고르면 저장을 다시 요청한다") {
            runTest(mainDispatcher) {
                val setUseCase = mockk<SetRoutineViewScopeUseCase>()
                coEvery { setUseCase(parameter = any()) } returns Result.failure(IllegalStateException()) andThen Result.success(Unit)
                val viewModel = viewModel(setRoutineViewScopeUseCase = setUseCase)

                viewModel.setViewScope(viewScope = RoutineViewScope.ALL)
                advanceUntilIdle()
                viewModel.setViewScope(viewScope = RoutineViewScope.ALL)
                advanceUntilIdle()

                coVerify(exactly = 2) { setUseCase(parameter = RoutineViewScope.ALL) }
            }
        }

        test("보기 범위 저장을 처리하는 동안 다시 고른 값은 저장을 요청하지 않는다") {
            runTest(mainDispatcher) {
                val gate = CompletableDeferred<Unit>()
                val setUseCase = mockk<SetRoutineViewScopeUseCase>()
                coEvery { setUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(Unit)
                }
                val viewModel = viewModel(setRoutineViewScopeUseCase = setUseCase)

                viewModel.setViewScope(viewScope = RoutineViewScope.ALL)
                advanceUntilIdle()
                viewModel.setViewScope(viewScope = RoutineViewScope.TODAY)
                advanceUntilIdle()
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { setUseCase(parameter = any()) }
                coVerify(exactly = 1) { setUseCase(parameter = RoutineViewScope.ALL) }
            }
        }
    }

    private fun viewScopeTagTests() {
        test("TC-d86bcda0-2d05-4e0b-b4d8-57ad3926daa0 선택 전체 해제는 보기 범위 저장을 요청하지 않는다") {
            runTest(mainDispatcher) {
                val unselectAllUseCase = mockk<UnselectAllRoutineFilterTagUseCase>()
                coEvery { unselectAllUseCase() } returns Result.success(Unit)
                val setUseCase = mockk<SetRoutineViewScopeUseCase>()
                RoutineViewScope.entries.forEach { viewScope ->
                    val viewModel =
                        viewModel(
                            viewScopeFlow = flowOf(viewScope),
                            unselectAllRoutineFilterTagUseCase = unselectAllUseCase,
                            setRoutineViewScopeUseCase = setUseCase,
                        )

                    val job = backgroundScope.launch { viewModel.uiState.collect {} }
                    advanceUntilIdle()
                    viewModel.uiState.value.viewScope shouldBe viewScope

                    viewModel.unselectAllTag()
                    advanceUntilIdle()

                    viewModel.uiState.value.viewScope shouldBe viewScope
                    job.cancel()
                }

                coVerify(exactly = 0) { setUseCase(parameter = any()) }
            }
        }

        test("보기 범위를 조회하지 못하면 오늘 할 루틴을 고른 것으로 노출한다") {
            runTest(mainDispatcher) {
                val getRoutineViewScopeUseCase = mockk<GetRoutineViewScopeUseCase>()
                every { getRoutineViewScopeUseCase() } returns flowOf(Result.failure(IllegalStateException()))
                val viewModel = viewModel(getRoutineViewScopeUseCase = getRoutineViewScopeUseCase)

                viewModel.uiState.test {
                    awaitItem().viewScope shouldBe RoutineViewScope.TODAY
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }
    }

    private fun viewModel(
        pageTagUseCase: PageTagUseCase =
            mockk<PageTagUseCase>().also { useCase ->
                every { useCase(parameter = any()) } returns flowOf(Result.success(PagingData.empty()))
            },
        filterFlow: Flow<Result<List<Tag>>> = flowOf(Result.success(emptyList())),
        selectRoutineFilterTagUseCase: SelectRoutineFilterTagUseCase = mockk(),
        unselectRoutineFilterTagUseCase: UnselectRoutineFilterTagUseCase = mockk(),
        unselectAllRoutineFilterTagUseCase: UnselectAllRoutineFilterTagUseCase = mockk(),
        viewScopeFlow: Flow<RoutineViewScope> = flowOf(RoutineViewScope.TODAY),
        getRoutineViewScopeUseCase: GetRoutineViewScopeUseCase = routineViewScopeUseCase(viewScope = viewScopeFlow),
        setRoutineViewScopeUseCase: SetRoutineViewScopeUseCase = mockk(),
    ): RoutineHomeFilterViewModel {
        val getRoutineFilterUseCase = mockk<GetRoutineFilterUseCase>()
        every { getRoutineFilterUseCase() } returns filterFlow

        return RoutineHomeFilterViewModel(
            pageTagUseCase = pageTagUseCase,
            getRoutineFilterUseCase = getRoutineFilterUseCase,
            getRoutineViewScopeUseCase = getRoutineViewScopeUseCase,
            selectRoutineFilterTagUseCase = selectRoutineFilterTagUseCase,
            unselectRoutineFilterTagUseCase = unselectRoutineFilterTagUseCase,
            unselectAllRoutineFilterTagUseCase = unselectAllRoutineFilterTagUseCase,
            setRoutineViewScopeUseCase = setRoutineViewScopeUseCase,
        )
    }
}
