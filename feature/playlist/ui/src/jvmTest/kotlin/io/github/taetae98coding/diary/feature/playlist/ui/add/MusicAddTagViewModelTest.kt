@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui.add

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class MusicAddTagViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-d113c69d-c978-40b0-8d56-d1f53e85cda1 초기 태그가 없으면 태그를 하나도 고르지 않은 상태로 시작한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(initialTagId = null)

                viewModel.selectionUiState.value.tagIdSet
                    .shouldBeEmpty()
                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().tagList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-c53d7ea7-b39e-4ebe-8da5-5a810a15e2e0 초기 태그가 있으면 그 태그만 고른 상태로 시작한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(initialTagId = tag.id, selectedTagList = listOf(tag))

                viewModel.selectionUiState.value.tagIdSet shouldBe setOf(tag.id)
                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().tagList shouldBe listOf(tag)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-323272aa-beb3-4532-8851-a8d719e8aa59 고른 태그는 선택 상태로만 반영되고 해제하면 선택에서 빠진다") {
            runTest(mainDispatcher) {
                val firstTag = tag()
                val secondTag = tag()
                val viewModel = viewModel(initialTagId = null, selectedTagList = listOf(firstTag, secondTag))

                viewModel.add(tagId = firstTag.id)
                viewModel.add(tagId = secondTag.id)
                advanceUntilIdle()

                viewModel.selectionUiState.value.tagIdSet shouldBe setOf(firstTag.id, secondTag.id)

                viewModel.remove(tagId = firstTag.id)
                advanceUntilIdle()

                viewModel.selectionUiState.value.tagIdSet shouldBe setOf(secondTag.id)
            }
        }

        test("TC-aa29f2a1-c20b-4eda-ace6-1056baac2f92 완료·삭제된 대상 태그는 선택으로 보이지 않지만 선택은 남는다") {
            listOf<(Tag) -> Tag>(
                { value -> value.copy(isFinished = true) },
                { value -> value.copy(isDeleted = true) },
            ).forEach { change ->
                runTest(mainDispatcher) {
                    val tag = change(tag())
                    val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
                    every { getSelectedTagUseCase(parameter = any()) } answers {
                        val idSet = firstArg<Set<Uuid>>()
                        flowOf(Result.success(listOf(tag).filter { value -> value.id in idSet && !value.isFinished && !value.isDeleted }))
                    }
                    val viewModel =
                        EntityAddTagViewModel(
                            initialTagId = tag.id,
                            pageTagUseCase = mockk<PageTagUseCase>(relaxed = true),
                            getSelectedTagUseCase = getSelectedTagUseCase,
                        )

                    viewModel.selectionUiState.value.tagIdSet shouldBe setOf(tag.id)
                    viewModel.uiState.test {
                        advanceUntilIdle()
                        expectMostRecentItem().tagList.shouldBeEmpty()
                        cancelAndIgnoreRemainingEvents()
                    }
                    viewModel.selectionUiState.value.tagIdSet shouldBe setOf(tag.id)
                }
            }
        }

        test("TC-a3d83b22-b123-44fd-b130-9b77672f92fb 고른 태그 조회에서 빠진 태그는 제외된 것으로 표시한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val selectedTagList = MutableStateFlow(listOf(tag))
                val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
                every { getSelectedTagUseCase(parameter = setOf(tag.id)) } returns selectedTagList.map { tagList -> Result.success(tagList) }
                every { getSelectedTagUseCase(parameter = emptySet()) } returns flowOf(Result.success(emptyList()))
                val viewModel =
                    EntityAddTagViewModel(
                        initialTagId = null,
                        pageTagUseCase = mockk<PageTagUseCase>(relaxed = true),
                        getSelectedTagUseCase = getSelectedTagUseCase,
                    )

                viewModel.uiState.test {
                    viewModel.add(tagId = tag.id)
                    advanceUntilIdle()
                    expectMostRecentItem().tagList shouldBe listOf(tag)

                    selectedTagList.value = emptyList()
                    advanceUntilIdle()
                    expectMostRecentItem().tagList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private fun viewModel(
        initialTagId: Uuid?,
        selectedTagList: List<Tag> = emptyList(),
    ): EntityAddTagViewModel {
        val pageTagUseCase = mockk<PageTagUseCase>(relaxed = true)
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } answers {
            val idSet = firstArg<Set<Uuid>>()
            flowOf(Result.success(selectedTagList.filter { tag -> tag.id in idSet }))
        }

        return EntityAddTagViewModel(
            initialTagId = initialTagId,
            pageTagUseCase = pageTagUseCase,
            getSelectedTagUseCase = getSelectedTagUseCase,
        )
    }

    private companion object {
        fun tag(): Tag = Arb.tag().next().copy(isDeleted = false)
    }
}
