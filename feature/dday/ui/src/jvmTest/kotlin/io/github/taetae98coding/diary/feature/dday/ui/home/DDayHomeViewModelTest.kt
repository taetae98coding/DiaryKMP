@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.RestoreDDayUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class DDayHomeViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("조회한 디데이 페이지를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val ddayList = List(3) { Arb.dday(isDeleted = false).next() }
                val viewModel = viewModel(pageFlow = flowOf(Result.success(PagingData.from(ddayList))))

                flowOf(viewModel.ddayPagingData.first()).asSnapshot() shouldBe ddayList
            }
        }

        test("TC-22f92e42-b828-4b15-ac9f-7447050f4090 현재 계정을 확인할 수 없으면 노출하는 디데이가 없다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(pageFlow = flowOf(Result.failure(IllegalStateException())))

                flowOf(viewModel.ddayPagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-2a38eccb-d6fd-4c10-b3ba-ef1fb0e3e885 목록에서 삭제하면 그 디데이의 삭제를 한 번 요청하고 삭제 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val ddayId = Arb.kotlinUuid().next()
                val deleteDDayUseCase = mockk<DeleteDDayUseCase>()
                coEvery { deleteDDayUseCase(parameter = any()) } returns Result.success(1)
                val viewModel = viewModel(deleteDDayUseCase = deleteDDayUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = ddayId)
                    advanceUntilIdle()

                    awaitItem() shouldBe DDayListEffect.Deleted(id = ddayId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { deleteDDayUseCase(parameter = ddayId) }
            }
        }

        test("TC-c4afd5d9-8a2a-4ffa-8934-2b0786c16f03 실행 취소가 저장되지 않아도 별도 안내 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val ddayId = Arb.kotlinUuid().next()
                val restoreDDayUseCase = mockk<RestoreDDayUseCase>()
                coEvery { restoreDDayUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(restoreDDayUseCase = restoreDDayUseCase)

                viewModel.effect.test {
                    viewModel.restore(id = ddayId)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { restoreDDayUseCase(parameter = ddayId) }
            }
        }
    }

    private fun viewModel(
        pageFlow: Flow<Result<PagingData<DDay>>> = flowOf(Result.success(PagingData.empty())),
        deleteDDayUseCase: DeleteDDayUseCase = mockk(),
        restoreDDayUseCase: RestoreDDayUseCase = mockk(),
    ): DDayHomeViewModel {
        val pageDDayUseCase = mockk<PageDDayUseCase>()
        every { pageDDayUseCase() } returns pageFlow

        return DDayHomeViewModel(
            pageDDayUseCase = pageDDayUseCase,
            deleteDDayUseCase = deleteDDayUseCase,
            restoreDDayUseCase = restoreDDayUseCase,
        )
    }
}
