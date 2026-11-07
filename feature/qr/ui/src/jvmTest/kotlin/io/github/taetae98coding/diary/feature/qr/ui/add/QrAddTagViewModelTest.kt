@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.qr.ui.add

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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class QrAddTagViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-9912664a-b3ae-4d42-9e7d-61643a05cfde 초기 태그가 없으면 태그를 하나도 고르지 않은 상태로 시작한다") {
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

        test("TC-d1ef08a5-ea34-45c9-b01c-9ac8707215c1 초기 태그가 있으면 그 태그만 고른 상태로 시작한다") {
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

        test("TC-c33112e6-c1a1-4cbd-bd5c-07c3b43e20a2 고른 태그는 선택 상태로만 반영되고 해제하면 선택에서 빠진다") {
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

        test("TC-9d78dddb-b452-4337-a332-0b278fabadaa 완료·삭제된 대상 태그는 선택으로 보이지 않지만 선택은 남는다") {
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
