@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.checklist.ui.detail

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.core.testing.checklist.checklistItem
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.CheckChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.GetChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.ResetChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.UncheckChecklistItemUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class ChecklistDetailItemViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-271b9278-c2bc-42c9-a49c-f801d7fb1abb 체크 항목을 불러오는 동안에는 빈 상태로 보지 않는다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(itemListBySort = mapOf(ChecklistItemSort.ADDED to emptyFlow()))

                viewModel.uiState.test {
                    val uiState = awaitItem()

                    uiState.isLoaded shouldBe false
                    uiState.isEmpty shouldBe false
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-f31a7738-9ef8-413d-821c-34b150e0955e 노출할 체크 항목이 없으면 불러오기가 끝난 빈 상태다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(itemListBySort = mapOf(ChecklistItemSort.ADDED to flowOf(Result.success(emptyList()))))

                viewModel.uiState.test {
                    awaitItem().isEmpty shouldBe false
                    awaitItem().isEmpty shouldBe true
                }
            }
        }

        test("TC-b006bc65-8aca-4f2d-8318-5642d536c61f 체크 항목을 조회하지 못하면 항목이 없는 빈 상태로 본다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(itemListBySort = mapOf(ChecklistItemSort.ADDED to flowOf(Result.failure(IllegalStateException()))))

                viewModel.uiState.test {
                    skipItems(1)
                    awaitItem() shouldBe ChecklistDetailItemUiState(isLoaded = true)
                }
            }
        }

        test("TC-931c50e0-f873-486a-96a9-7ba8a3d655cd 처음 정렬은 추가순이고 체크 안 된 항목 우선으로 바꾸면 그 정렬로 조회한 목록을 노출한다") {
            runTest(mainDispatcher) {
                val checklistId = fixtureMonkey.giveMeOne<Uuid>()
                val addedList = itemList(checklistId = checklistId)
                val uncheckedFirstList = addedList.sortedBy(ChecklistItem::isChecked)
                val viewModel =
                    viewModel(
                        checklistId = checklistId,
                        itemListBySort =
                            mapOf(
                                ChecklistItemSort.ADDED to flowOf(Result.success(addedList)),
                                ChecklistItemSort.UNCHECKED_FIRST to flowOf(Result.success(uncheckedFirstList)),
                            ),
                    )

                viewModel.uiState.test {
                    skipItems(1)
                    awaitItem() shouldBe ChecklistDetailItemUiState(itemList = addedList, isLoaded = true, sort = ChecklistItemSort.ADDED)

                    viewModel.selectSort(sort = ChecklistItemSort.UNCHECKED_FIRST)

                    advanceUntilIdle()
                    expectMostRecentItem() shouldBe ChecklistDetailItemUiState(itemList = uncheckedFirstList, isLoaded = true, sort = ChecklistItemSort.UNCHECKED_FIRST)
                }
            }
        }

        test("TC-a4312274-e3e0-4f1a-86d0-c510baefba8c TC-546a404f-b57e-4a88-9208-f2208d629640 정렬을 바꾼 뒤 새로 만든 화면 상태는 추가순으로 시작한다") {
            runTest(mainDispatcher) {
                val checklistId = fixtureMonkey.giveMeOne<Uuid>()
                val addedList = itemList(checklistId = checklistId)
                val itemListBySort =
                    mapOf(
                        ChecklistItemSort.ADDED to flowOf(Result.success(addedList)),
                        ChecklistItemSort.UNCHECKED_FIRST to flowOf(Result.success(addedList.sortedBy(ChecklistItem::isChecked))),
                    )
                val before = viewModel(checklistId = checklistId, itemListBySort = itemListBySort)
                before.uiState.test {
                    skipItems(1)
                    before.selectSort(sort = ChecklistItemSort.UNCHECKED_FIRST)
                    advanceUntilIdle()
                    expectMostRecentItem().sort shouldBe ChecklistItemSort.UNCHECKED_FIRST
                }

                val after = viewModel(checklistId = checklistId, itemListBySort = itemListBySort)

                after.uiState.test {
                    skipItems(1)
                    awaitItem() shouldBe ChecklistDetailItemUiState(itemList = addedList, isLoaded = true, sort = ChecklistItemSort.ADDED)
                }
            }
        }

        test("TC-b1fba11a-2793-40a4-b7df-24d0e36b311f 다른 경로에서 바뀐 체크 항목을 조작 없이 노출한다") {
            runTest(mainDispatcher) {
                val checklistId = fixtureMonkey.giveMeOne<Uuid>()
                val before = itemList(checklistId = checklistId)
                val added = before + fixtureMonkey.checklistItem(isChecked = false, isDeleted = false, checklistId = checklistId)
                val removed = added.drop(1)
                val toggled = removed.map { item -> item.copy(isChecked = !item.isChecked) }
                val itemListFlow = MutableStateFlow(Result.success(before))
                val viewModel = viewModel(checklistId = checklistId, itemListBySort = mapOf(ChecklistItemSort.ADDED to itemListFlow))

                viewModel.uiState.test {
                    skipItems(1)
                    awaitItem().itemList shouldBe before

                    listOf(added, removed, toggled).forEach { itemList ->
                        itemListFlow.value = Result.success(itemList)

                        awaitItem().itemList shouldBe itemList
                    }
                }
            }
        }

        test("TC-5ab08f6f-bc21-44ac-b28b-5f555cc7b256 체크된 항목이 하나라도 있을 때만 리셋을 실행할 수 있다") {
            val checklistId = fixtureMonkey.giveMeOne<Uuid>()
            val expectedByItemList =
                listOf(
                    listOf(
                        fixtureMonkey.checklistItem(isChecked = true, isDeleted = false, checklistId = checklistId),
                        fixtureMonkey.checklistItem(isChecked = false, isDeleted = false, checklistId = checklistId),
                    ) to true,
                    List(ITEM_COUNT) { fixtureMonkey.checklistItem(isChecked = false, isDeleted = false, checklistId = checklistId) } to false,
                    emptyList<ChecklistItem>() to false,
                )

            expectedByItemList.forEach { (itemList, expected) ->
                ChecklistDetailItemUiState(itemList = itemList, isLoaded = true).isResetEnabled shouldBe expected
            }
        }

        test("TC-91c04294-1047-4551-ac4e-49792a4ffa10 체크 안 된 항목을 선택하면 체크를, 체크된 항목을 선택하면 체크 해제를 한 번 요청한다") {
            runTest(mainDispatcher) {
                val unchecked = fixtureMonkey.checklistItem(isChecked = false, isDeleted = false)
                val checked = fixtureMonkey.checklistItem(isChecked = true, isDeleted = false)
                val checkChecklistItemUseCase = mockk<CheckChecklistItemUseCase>()
                coEvery { checkChecklistItemUseCase(parameter = unchecked.id) } returns Result.success(1)
                val uncheckChecklistItemUseCase = mockk<UncheckChecklistItemUseCase>()
                coEvery { uncheckChecklistItemUseCase(parameter = checked.id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        checkChecklistItemUseCase = checkChecklistItemUseCase,
                        uncheckChecklistItemUseCase = uncheckChecklistItemUseCase,
                    )

                viewModel.toggle(id = unchecked.id, isChecked = unchecked.isChecked)
                viewModel.toggle(id = checked.id, isChecked = checked.isChecked)
                advanceUntilIdle()

                coVerify(exactly = 1) { checkChecklistItemUseCase(parameter = unchecked.id) }
                coVerify(exactly = 1) { uncheckChecklistItemUseCase(parameter = checked.id) }
            }
        }

        test("TC-1338f2e3-c194-4674-94c8-1080d348f33e 체크 변경을 저장하지 못하면 안내 없이 다시 선택할 수 있다") {
            runTest(mainDispatcher) {
                val item = fixtureMonkey.checklistItem(isChecked = false, isDeleted = false)
                val checkChecklistItemUseCase = mockk<CheckChecklistItemUseCase>()
                coEvery { checkChecklistItemUseCase(parameter = item.id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(checkChecklistItemUseCase = checkChecklistItemUseCase)

                viewModel.effect.test {
                    viewModel.toggle(id = item.id, isChecked = item.isChecked)
                    advanceUntilIdle()
                    viewModel.toggle(id = item.id, isChecked = item.isChecked)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 2) { checkChecklistItemUseCase(parameter = item.id) }
            }
        }

        test("TC-24f673e1-181e-4255-830d-ed71b458017d 항목 넣기에 성공하면 대상 체크리스트에 그 내용으로 넣기를 한 번 요청하고 성공을 한 번 알린다") {
            runTest(mainDispatcher) {
                val checklistId = fixtureMonkey.giveMeOne<Uuid>()
                val content = fixtureMonkey.nonBlankString()
                val parameter = AddChecklistItemUseCase.Parameter(checklistId = checklistId, content = content)
                val addChecklistItemUseCase = mockk<AddChecklistItemUseCase>()
                coEvery { addChecklistItemUseCase(parameter = parameter) } returns Result.success(1)
                val viewModel = viewModel(checklistId = checklistId, addChecklistItemUseCase = addChecklistItemUseCase)

                viewModel.effect.test {
                    viewModel.add(content = content)

                    awaitItem() shouldBe ChecklistDetailItemEffect.AddSucceeded
                    expectNoEvents()
                }
                coVerify(exactly = 1) { addChecklistItemUseCase(parameter = parameter) }
            }
        }

        test("TC-3909f080-7f22-4bc3-b8b2-ac3c67c7cd3f 항목 넣기를 저장하지 못하면 성공을 알리지 않는다") {
            runTest(mainDispatcher) {
                val addChecklistItemUseCase = mockk<AddChecklistItemUseCase>()
                coEvery { addChecklistItemUseCase(parameter = any()) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(addChecklistItemUseCase = addChecklistItemUseCase)

                viewModel.effect.test {
                    viewModel.add(content = fixtureMonkey.nonBlankString())
                    advanceUntilIdle()

                    expectNoEvents()
                }
                viewModel.uiState.value.isAddInProgress shouldBe false
            }
        }

        test("TC-1ffe5e8c-f3fa-47c8-99fe-09c4183e2269 항목을 빼면 그 항목의 빼기를 한 번 요청한다") {
            runTest(mainDispatcher) {
                val item = fixtureMonkey.checklistItem(isChecked = false, isDeleted = false)
                val deleteChecklistItemUseCase = mockk<DeleteChecklistItemUseCase>()
                coEvery { deleteChecklistItemUseCase(parameter = item.id) } returns Result.success(1)
                val viewModel = viewModel(deleteChecklistItemUseCase = deleteChecklistItemUseCase)

                viewModel.delete(itemId = item.id)
                advanceUntilIdle()

                coVerify(exactly = 1) { deleteChecklistItemUseCase(parameter = item.id) }
            }
        }

        test("TC-e2a22549-de97-4584-b123-e360ac322a56 항목 빼기를 저장하지 못하면 안내 없이 다시 뺄 수 있다") {
            runTest(mainDispatcher) {
                val item = fixtureMonkey.checklistItem(isChecked = false, isDeleted = false)
                val deleteChecklistItemUseCase = mockk<DeleteChecklistItemUseCase>()
                coEvery { deleteChecklistItemUseCase(parameter = item.id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(deleteChecklistItemUseCase = deleteChecklistItemUseCase)

                viewModel.effect.test {
                    viewModel.delete(itemId = item.id)
                    advanceUntilIdle()
                    viewModel.delete(itemId = item.id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 2) { deleteChecklistItemUseCase(parameter = item.id) }
            }
        }

        test("TC-75ac5b8f-d6cb-4afe-bf51-4f5add29b549 리셋을 확정하면 대상 체크리스트의 리셋을 한 번 요청한다") {
            runTest(mainDispatcher) {
                val checklistId = fixtureMonkey.giveMeOne<Uuid>()
                val resetChecklistUseCase = mockk<ResetChecklistUseCase>()
                coEvery { resetChecklistUseCase(parameter = checklistId) } returns Result.success(1)
                val viewModel = viewModel(checklistId = checklistId, resetChecklistUseCase = resetChecklistUseCase)

                viewModel.reset()
                advanceUntilIdle()

                coVerify(exactly = 1) { resetChecklistUseCase(parameter = checklistId) }
            }
        }

        test("TC-13c34f1c-d8a2-4156-b876-acf69b25f0be 리셋을 처리하는 동안 진행 상태를 노출하고 끝나면 해제한다") {
            runTest(mainDispatcher) {
                val gate = CompletableDeferred<Unit>()
                val resetChecklistUseCase = mockk<ResetChecklistUseCase>()
                coEvery { resetChecklistUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel =
                    viewModel(
                        itemListBySort = mapOf(ChecklistItemSort.ADDED to flowOf(Result.success(emptyList()))),
                        resetChecklistUseCase = resetChecklistUseCase,
                    )

                viewModel.uiState.test {
                    skipItems(1)
                    awaitItem().isResetInProgress shouldBe false

                    viewModel.reset()
                    awaitItem().isResetInProgress shouldBe true

                    gate.complete(Unit)
                    awaitItem().isResetInProgress shouldBe false
                }
            }
        }

        test("TC-9fadccf2-e1cf-4678-bd7d-c79144f2b643 항목 넣기를 처리하는 동안 진행 상태를 노출하고 끝나면 해제한다") {
            runTest(mainDispatcher) {
                val gate = CompletableDeferred<Unit>()
                val addChecklistItemUseCase = mockk<AddChecklistItemUseCase>()
                coEvery { addChecklistItemUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel =
                    viewModel(
                        itemListBySort = mapOf(ChecklistItemSort.ADDED to flowOf(Result.success(emptyList()))),
                        addChecklistItemUseCase = addChecklistItemUseCase,
                    )

                viewModel.uiState.test {
                    skipItems(1)
                    awaitItem().isAddInProgress shouldBe false

                    viewModel.add(content = fixtureMonkey.nonBlankString())
                    awaitItem().isAddInProgress shouldBe true

                    gate.complete(Unit)
                    awaitItem().isAddInProgress shouldBe false
                }
            }
        }

        test("TC-7884b5f4-c64b-4bb7-8b16-00b41bcbfcdc 정렬을 고른 뒤 항목이 모두 없어졌다가 다시 생겨도 고른 정렬로 조회한다") {
            runTest(mainDispatcher) {
                val checklistId = fixtureMonkey.giveMeOne<Uuid>()
                val restored = itemList(checklistId = checklistId).sortedBy(ChecklistItem::isChecked)
                val uncheckedFirstFlow = MutableStateFlow(Result.success(restored))
                val viewModel =
                    viewModel(
                        checklistId = checklistId,
                        itemListBySort =
                            mapOf(
                                ChecklistItemSort.ADDED to flowOf(Result.success(itemList(checklistId = checklistId))),
                                ChecklistItemSort.UNCHECKED_FIRST to uncheckedFirstFlow,
                            ),
                    )

                viewModel.uiState.test {
                    skipItems(1)
                    viewModel.selectSort(sort = ChecklistItemSort.UNCHECKED_FIRST)
                    advanceUntilIdle()
                    expectMostRecentItem().sort shouldBe ChecklistItemSort.UNCHECKED_FIRST

                    uncheckedFirstFlow.value = Result.success(emptyList())
                    advanceUntilIdle()
                    expectMostRecentItem() shouldBe ChecklistDetailItemUiState(itemList = emptyList(), isLoaded = true, sort = ChecklistItemSort.UNCHECKED_FIRST)

                    uncheckedFirstFlow.value = Result.success(restored)
                    advanceUntilIdle()
                    expectMostRecentItem() shouldBe ChecklistDetailItemUiState(itemList = restored, isLoaded = true, sort = ChecklistItemSort.UNCHECKED_FIRST)
                }
            }
        }

        test("TC-0a0cf3fa-afb8-48dd-9d97-5cca495ff251 리셋을 저장하지 못하면 안내 없이 진행 상태만 해제하고 다시 리셋할 수 있다") {
            runTest(mainDispatcher) {
                val checklistId = fixtureMonkey.giveMeOne<Uuid>()
                val resetChecklistUseCase = mockk<ResetChecklistUseCase>()
                coEvery { resetChecklistUseCase(parameter = checklistId) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(checklistId = checklistId, resetChecklistUseCase = resetChecklistUseCase)

                viewModel.effect.test {
                    viewModel.reset()
                    advanceUntilIdle()
                    viewModel.reset()
                    advanceUntilIdle()

                    expectNoEvents()
                }
                viewModel.uiState.value.isResetInProgress shouldBe false
                coVerify(exactly = 2) { resetChecklistUseCase(parameter = checklistId) }
            }
        }

        test("TC-dbb55974-a5c4-4272-b99b-52ecb3c30bf0 리셋이나 항목 넣기를 처리하는 동안 같은 종류는 다시 요청하지 않고 다른 종류는 요청한다") {
            runTest(mainDispatcher) {
                val gate = CompletableDeferred<Unit>()
                val resetChecklistUseCase = mockk<ResetChecklistUseCase>()
                coEvery { resetChecklistUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val addChecklistItemUseCase = mockk<AddChecklistItemUseCase>()
                coEvery { addChecklistItemUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = viewModel(resetChecklistUseCase = resetChecklistUseCase, addChecklistItemUseCase = addChecklistItemUseCase)

                viewModel.reset()
                runCurrent()
                viewModel.reset()
                viewModel.add(content = fixtureMonkey.nonBlankString())
                runCurrent()
                viewModel.add(content = fixtureMonkey.nonBlankString())
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { resetChecklistUseCase(parameter = any()) }
                coVerify(exactly = 1) { addChecklistItemUseCase(parameter = any()) }
            }
        }

        test("TC-dbb55974-a5c4-4272-b99b-52ecb3c30bf0 한 항목의 체크 변경과 빼기는 그 항목에만 제한하고 다른 항목은 요청한다") {
            runTest(mainDispatcher) {
                val first = fixtureMonkey.checklistItem(isChecked = false, isDeleted = false)
                val second = fixtureMonkey.checklistItem(isChecked = false, isDeleted = false)
                val gate = CompletableDeferred<Unit>()
                val checkChecklistItemUseCase = mockk<CheckChecklistItemUseCase>()
                coEvery { checkChecklistItemUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val deleteChecklistItemUseCase = mockk<DeleteChecklistItemUseCase>()
                coEvery { deleteChecklistItemUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = viewModel(checkChecklistItemUseCase = checkChecklistItemUseCase, deleteChecklistItemUseCase = deleteChecklistItemUseCase)

                viewModel.toggle(id = first.id, isChecked = first.isChecked)
                viewModel.toggle(id = first.id, isChecked = first.isChecked)
                viewModel.toggle(id = second.id, isChecked = second.isChecked)
                viewModel.delete(itemId = first.id)
                viewModel.delete(itemId = first.id)
                viewModel.delete(itemId = second.id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { checkChecklistItemUseCase(parameter = first.id) }
                coVerify(exactly = 1) { checkChecklistItemUseCase(parameter = second.id) }
                coVerify(exactly = 1) { deleteChecklistItemUseCase(parameter = first.id) }
                coVerify(exactly = 1) { deleteChecklistItemUseCase(parameter = second.id) }
            }
        }
    }
}

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private const val ITEM_COUNT = 3

private fun itemList(checklistId: Uuid): List<ChecklistItem> =
    listOf(
        fixtureMonkey.checklistItem(isChecked = true, isDeleted = false, checklistId = checklistId),
        fixtureMonkey.checklistItem(isChecked = false, isDeleted = false, checklistId = checklistId),
        fixtureMonkey.checklistItem(isChecked = true, isDeleted = false, checklistId = checklistId),
    )

private fun viewModel(
    checklistId: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
    itemListBySort: Map<ChecklistItemSort, Flow<Result<List<ChecklistItem>>>> = emptyMap(),
    addChecklistItemUseCase: AddChecklistItemUseCase = mockk(),
    checkChecklistItemUseCase: CheckChecklistItemUseCase = mockk(),
    uncheckChecklistItemUseCase: UncheckChecklistItemUseCase = mockk(),
    deleteChecklistItemUseCase: DeleteChecklistItemUseCase = mockk(),
    resetChecklistUseCase: ResetChecklistUseCase = mockk(),
): ChecklistDetailItemViewModel {
    val getChecklistItemUseCase = mockk<GetChecklistItemUseCase>()
    ChecklistItemSort.entries.forEach { sort ->
        every { getChecklistItemUseCase(parameter = GetChecklistItemUseCase.Parameter(checklistId = checklistId, sort = sort)) } returns
            itemListBySort.getOrElse(sort) { emptyFlow<Result<List<ChecklistItem>>>() }
    }

    return ChecklistDetailItemViewModel(
        checklistId = checklistId,
        addChecklistItemUseCase = addChecklistItemUseCase,
        checkChecklistItemUseCase = checkChecklistItemUseCase,
        uncheckChecklistItemUseCase = uncheckChecklistItemUseCase,
        deleteChecklistItemUseCase = deleteChecklistItemUseCase,
        resetChecklistUseCase = resetChecklistUseCase,
        getChecklistItemUseCase = getChecklistItemUseCase,
    )
}
