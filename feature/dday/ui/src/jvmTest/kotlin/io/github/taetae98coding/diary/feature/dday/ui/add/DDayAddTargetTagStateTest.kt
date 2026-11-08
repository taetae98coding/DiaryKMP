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

class DDayAddTargetTagStateTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-a6e1dac4-50a4-4334-993f-edad0f653c4d 완료·삭제된 대상 태그는 선택으로 보이지 않지만 선택은 남은 채 디데이 추가를 시작한다") {
            listOf<(Tag) -> Tag>(
                { value -> value.copy(isFinished = true) },
                { value -> value.copy(isDeleted = true) },
            ).forEach { change ->
                runTest(mainDispatcher) {
                    val tag = change(Arb.tag().next().copy(isFinished = false, isDeleted = false))
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
}
