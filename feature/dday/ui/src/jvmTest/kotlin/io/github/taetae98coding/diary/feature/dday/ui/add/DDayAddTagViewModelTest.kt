@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.dday.ui.add

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.dday.usecase.AddDDayUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.core.tag.PrimaryTagSelectionUiState
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayInput
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class DDayAddTagViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-a568196b-0005-42d2-97ad-0b1a618c0575 DDayHome에서 진입하면 태그를 하나도 고르지 않은 상태로 시작한다") {
            runTest(mainDispatcher) {
                val tagList = List(2) { Arb.tag(isFinished = false, isDeleted = false).next() }
                val viewModel = viewModel(initialTagId = null, allTagList = tagList)

                viewModel.selectionUiState.value.tagIdSet
                    .shouldBeEmpty()
                viewModel.selectionUiState.value.primaryTagId
                    .shouldBeNull()
                viewModel.uiState.test {
                    advanceUntilIdle()
                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList.shouldBeEmpty()
                    uiState.primaryTagId shouldBe null
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-889939c1-ec4d-4cb5-8a21-c8043686613e TagDetail 디데이 탭에서 진입하면 그 태그만 고른 상태로 시작한다") {
            runTest(mainDispatcher) {
                val tag = Arb.tag(isFinished = false, isDeleted = false).next()
                val viewModel = viewModel(initialTagId = tag.id, allTagList = listOf(tag, Arb.tag(isFinished = false, isDeleted = false).next()))

                viewModel.selectionUiState.value.tagIdSet shouldBe setOf(tag.id)
                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().selectedTagList shouldBe listOf(tag)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-37ec0d12-3e91-467b-b992-8756009ff4ad TagDetail 디데이 탭에서 진입하면 그 태그를 대표 태그로도 지정한다") {
            runTest(mainDispatcher) {
                val tag = Arb.tag(isFinished = false, isDeleted = false).next()
                val viewModel = viewModel(initialTagId = tag.id, allTagList = listOf(tag, Arb.tag(isFinished = false, isDeleted = false).next()))

                viewModel.selectionUiState.value.primaryTagId shouldBe tag.id
                viewModel.uiState.test {
                    advanceUntilIdle()
                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList shouldBe listOf(tag)
                    uiState.primaryTagId shouldBe tag.id
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-36b298f9-80db-4c4f-8825-ac3aa22408aa 대상 태그의 대표 지정을 해제하거나 다른 태그로 바꿀 수 있다") {
            runTest(mainDispatcher) {
                val targetTag = Arb.tag(isFinished = false, isDeleted = false).next()
                val otherTag = Arb.tag(isFinished = false, isDeleted = false).next()
                val viewModel = viewModel(initialTagId = targetTag.id, allTagList = listOf(targetTag, otherTag))

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().primaryTagId shouldBe targetTag.id

                    viewModel.unselectPrimaryTag()
                    advanceUntilIdle()

                    val unsetUiState = expectMostRecentItem()
                    unsetUiState.selectedTagList shouldBe listOf(targetTag)
                    unsetUiState.primaryTagId shouldBe null

                    viewModel.selectPrimaryTag(tagId = otherTag.id)
                    advanceUntilIdle()

                    val changedUiState = expectMostRecentItem()
                    changedUiState.selectedTagList shouldContainExactlyInAnyOrder listOf(targetTag, otherTag)
                    changedUiState.primaryTagId shouldBe otherTag.id
                    cancelAndIgnoreRemainingEvents()
                }
                viewModel.selectionUiState.value shouldBe PrimaryTagSelectionUiState(tagIdSet = setOf(targetTag.id, otherTag.id), primaryTagId = otherTag.id)
            }
        }

        test("TC-90705652-99d7-4107-9ce1-2ec66cea7311 화면이 다시 구독해도 고른 태그와 대표 태그 지정을 유지한다") {
            runTest(mainDispatcher) {
                val tagA = Arb.tag(isFinished = false, isDeleted = false).next()
                val tagB = Arb.tag(isFinished = false, isDeleted = false).next()
                val viewModel = viewModel(initialTagId = null, allTagList = listOf(tagA, tagB))

                viewModel.uiState.test {
                    advanceUntilIdle()
                    viewModel.selectTag(tagId = tagA.id)
                    viewModel.selectTag(tagId = tagB.id)
                    viewModel.selectPrimaryTag(tagId = tagB.id)
                    advanceUntilIdle()

                    expectMostRecentItem().primaryTagId shouldBe tagB.id
                    cancelAndIgnoreRemainingEvents()
                }

                // 화면이 재생성되거나 백그라운드에 다녀오면 같은 ViewModel을 새로 구독한다.
                viewModel.uiState.test {
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList shouldContainExactlyInAnyOrder listOf(tagA, tagB)
                    uiState.primaryTagId shouldBe tagB.id
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-263c0762-b1c1-4181-9d6f-dda08fd317f0 태그를 고르면 선택으로만 반영되고 연결을 저장하지 않는다") {
            runTest(mainDispatcher) {
                val tagList = List(2) { Arb.tag(isFinished = false, isDeleted = false).next() }
                // 추가 화면의 태그 입력은 연결을 저장하는 협력 객체를 갖지 않으므로, 고른 결과는 선택 상태로만 남는다.
                val viewModel = viewModel(initialTagId = null, allTagList = tagList)

                viewModel.uiState.test {
                    viewModel.selectTag(tagId = tagList.first().id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedTagList shouldBe listOf(tagList.first())
                    cancelAndIgnoreRemainingEvents()
                }
                viewModel.selectionUiState.value.tagIdSet shouldBe setOf(tagList.first().id)
            }
        }

        listOf<Pair<String, (Tag) -> Tag>>(
            "완료되어도" to { tag -> tag.copy(isFinished = true) },
            "삭제되어도" to { tag -> tag.copy(isDeleted = true) },
        ).forEach { (label, change) ->
            test("TC-993b4edc-09a1-4450-a83e-cf4280104a60 대표 태그로 고른 태그가 $label 그 태그를 대표 태그로 추가를 요청한다") {
                runTest(mainDispatcher) {
                    val tag = Arb.tag(isFinished = false, isDeleted = false).next()
                    val storedTagList = MutableStateFlow(listOf(tag))
                    val tagViewModel =
                        DDayAddTagViewModel(
                            initialPrimaryTagId = null,
                            pageTagUseCase = mockk<PageTagUseCase>(relaxed = true),
                            getSelectedTagUseCase = getSelectedTagUseCase(storedTagList = storedTagList),
                        )
                    val addDDayUseCase = mockk<AddDDayUseCase>()
                    coEvery { addDDayUseCase(parameter = any()) } returns Result.success(Arb.kotlinUuid().next())
                    val addViewModel = DDayAddViewModel(addDDayUseCase = addDDayUseCase)
                    val input = Arb.ddayInput().next()

                    tagViewModel.uiState.test {
                        tagViewModel.selectPrimaryTag(tagId = tag.id)
                        advanceUntilIdle()
                        expectMostRecentItem().primaryTagId shouldBe tag.id

                        storedTagList.value = listOf(change(tag))
                        advanceUntilIdle()
                        expectMostRecentItem().selectedTagList.shouldBeEmpty()
                        cancelAndIgnoreRemainingEvents()
                    }
                    addViewModel.add(input = input, tagSelection = tagViewModel.selectionUiState.value)
                    advanceUntilIdle()

                    coVerify(exactly = 1) {
                        addDDayUseCase(parameter = AddDDayUseCase.Parameter(input = input, tagIdSet = setOf(tag.id), primaryTagId = tag.id))
                    }
                }
            }
        }
    }

    private fun viewModel(
        initialTagId: Uuid?,
        allTagList: List<Tag>,
    ): DDayAddTagViewModel =
        DDayAddTagViewModel(
            initialPrimaryTagId = initialTagId,
            pageTagUseCase = mockk<PageTagUseCase>(relaxed = true),
            getSelectedTagUseCase = getSelectedTagUseCase(storedTagList = MutableStateFlow(allTagList)),
        )

    // 고른 태그의 조회는 완료되거나 삭제된 태그를 돌려주지 않는다.
    private fun getSelectedTagUseCase(storedTagList: StateFlow<List<Tag>>): GetSelectedTagUseCase {
        val useCase = mockk<GetSelectedTagUseCase>()
        every { useCase(parameter = any()) } answers {
            val idSet = firstArg<Set<Uuid>>()
            storedTagList.map { tagList -> Result.success(tagList.filter { tag -> tag.id in idSet && !tag.isFinished && !tag.isDeleted }) }
        }
        return useCase
    }
}
