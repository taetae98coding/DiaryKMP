@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.dday.ui.home

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class DDayHomeScaffoldFilterViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-dba071a6-eb8f-4196-979e-76536b2fdc50 저장된 선택이 모두 선택할 수 없게 되면 필터가 적용되지 않은 것으로 노출한다") {
            runTest(mainDispatcher) {
                val tag = Arb.tag().next()
                val viewModel =
                    viewModel(
                        filterFlow = flowOf(Result.success(emptyList())),
                        filterTagIdFlow = flowOf(Result.success(setOf(tag.id))),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe DDayHomeScaffoldFilterUiState(isLoaded = false)
                    advanceUntilIdle()

                    val uiState = awaitItem()
                    uiState.isApplied shouldBe false
                    uiState.selectedTagIdSet.shouldBeEmpty()
                    uiState.listQueryFilter shouldBe setOf(tag.id)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-911975f0-716c-451b-95bf-aa91e58af35b 태그가 다시 선택할 수 있게 되면 필터가 적용된 것으로 노출한다") {
            runTest(mainDispatcher) {
                val tag = Arb.tag().next()
                val filterFlow = MutableStateFlow<Result<List<Tag>>>(Result.success(emptyList()))
                val viewModel =
                    viewModel(
                        filterFlow = filterFlow,
                        filterTagIdFlow = flowOf(Result.success(setOf(tag.id))),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe DDayHomeScaffoldFilterUiState(isLoaded = false)
                    advanceUntilIdle()
                    awaitItem().isApplied shouldBe false

                    filterFlow.value = Result.success(listOf(tag))
                    advanceUntilIdle()

                    val uiState = awaitItem()
                    uiState.isApplied shouldBe true
                    uiState.selectedTagIdSet shouldBe setOf(tag.id)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private fun viewModel(
        filterFlow: Flow<Result<List<Tag>>> = flowOf(Result.success(emptyList())),
        filterTagIdFlow: Flow<Result<Set<Uuid>>> = flowOf(Result.success(emptySet())),
    ): DDayHomeScaffoldFilterViewModel {
        val getDDayFilterUseCase = mockk<GetDDayFilterUseCase>()
        every { getDDayFilterUseCase() } returns filterFlow
        val getDDayFilterTagIdUseCase = mockk<GetDDayFilterTagIdUseCase>()
        every { getDDayFilterTagIdUseCase() } returns filterTagIdFlow

        return DDayHomeScaffoldFilterViewModel(
            getDDayFilterUseCase = getDDayFilterUseCase,
            getDDayFilterTagIdUseCase = getDDayFilterTagIdUseCase,
        )
    }
}
