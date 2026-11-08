@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.dday.ui.add

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
                val tagList = List(2) { tag() }
                val viewModel = viewModel(initialTagId = null, allTagList = tagList)

                viewModel.selectionUiState.value.tagIdSet
                    .shouldBeEmpty()
                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().tagList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-889939c1-ec4d-4cb5-8a21-c8043686613e TagDetail 디데이 탭에서 진입하면 그 태그만 고른 상태로 시작한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(initialTagId = tag.id, allTagList = listOf(tag, tag()))

                viewModel.selectionUiState.value.tagIdSet shouldBe setOf(tag.id)
                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().tagList shouldBe listOf(tag)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-263c0762-b1c1-4181-9d6f-dda08fd317f0 태그를 고르면 선택으로만 반영되고 연결을 저장하지 않는다") {
            runTest(mainDispatcher) {
                val tagList = List(2) { tag() }
                // 추가 화면의 태그 입력은 연결을 저장하는 협력 객체를 갖지 않으므로, 고른 결과는 선택 상태로만 남는다.
                val viewModel = viewModel(initialTagId = null, allTagList = tagList)

                viewModel.uiState.test {
                    viewModel.add(tagId = tagList.first().id)
                    advanceUntilIdle()

                    expectMostRecentItem().tagList shouldBe listOf(tagList.first())
                    cancelAndIgnoreRemainingEvents()
                }
                viewModel.selectionUiState.value.tagIdSet shouldBe setOf(tagList.first().id)
            }
        }
    }

    private fun viewModel(
        initialTagId: Uuid?,
        allTagList: List<Tag>,
    ): EntityAddTagViewModel =
        EntityAddTagViewModel(
            initialTagId = initialTagId,
            pageTagUseCase = mockk<PageTagUseCase>(relaxed = true),
            getSelectedTagUseCase = getSelectedTagUseCase(allTagList = allTagList),
        )

    private fun getSelectedTagUseCase(allTagList: List<Tag>): GetSelectedTagUseCase {
        val useCase = mockk<GetSelectedTagUseCase>()
        every { useCase(parameter = any()) } answers {
            val idSet = firstArg<Set<Uuid>>()
            flowOf(Result.success(allTagList.filter { tag -> tag.id in idSet }))
        }
        return useCase
    }

    private companion object {
        fun tag(): Tag = Arb.tag().next().copy(isFinished = false, isDeleted = false)
    }
}
