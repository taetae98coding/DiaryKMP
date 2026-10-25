@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.PageChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.RestoreChecklistUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
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

class ChecklistHomeViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-5458d371-b068-4e29-b8b4-0b48ef52c120 조회한 체크리스트 페이지를 진행 정도와 함께 그대로 노출한다") {
            runTest(mainDispatcher) {
                val summaryList = listOf(summary(), summary())
                val viewModel = viewModel(pageChecklistUseCase = pageChecklistUseCase(summaryListFlow = flowOf(Result.success(summaryList))))

                flowOf(viewModel.checklistPagingData.first()).asSnapshot() shouldBe summaryList
            }
        }

        test("TC-861a291e-2dec-411e-9a64-fdfc48607696 체크리스트 페이지 조회에 실패하면 빈 페이지를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(pageChecklistUseCase = pageChecklistUseCase(summaryListFlow = flowOf(Result.failure(IllegalStateException()))))

                flowOf(viewModel.checklistPagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-9d075582-e4a5-464d-b83b-2ad2a4e23669 체크리스트를 추가하면 별도 조작 없이 그 체크리스트가 든 목록을 노출한다") {
            runTest(mainDispatcher) {
                val existing = summary()
                val added = summary()
                val summaryListFlow = MutableStateFlow(Result.success(listOf(existing)))
                val viewModel = viewModel(pageChecklistUseCase = pageChecklistUseCase(summaryListFlow = summaryListFlow))

                viewModel.checklistPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe listOf(existing)

                    summaryListFlow.value = Result.success(listOf(added, existing))

                    flowOf(awaitItem()).asSnapshot() shouldBe listOf(added, existing)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-ddca765c-69f3-4113-b1c5-31b12926b3a3 체크 항목이 바뀌면 진행 정도가 바뀐 목록을 노출한다") {
            runTest(mainDispatcher) {
                val before = summary()
                val after = before.copy(checkedCount = before.checkedCount + 1, totalCount = before.totalCount + 1)
                val summaryListFlow = MutableStateFlow(Result.success(listOf(before)))
                val viewModel = viewModel(pageChecklistUseCase = pageChecklistUseCase(summaryListFlow = summaryListFlow))

                viewModel.checklistPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe listOf(before)

                    summaryListFlow.value = Result.success(listOf(after))

                    flowOf(awaitItem()).asSnapshot() shouldBe listOf(after)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-c7d4827a-f896-4048-b403-21e2cf5f12c9 삭제에 성공하면 그 체크리스트의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteChecklistUseCase = mockk<DeleteChecklistUseCase>()
                coEvery { deleteChecklistUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(deleteChecklistUseCase = deleteChecklistUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = id)

                    awaitItem() shouldBe ChecklistHomeEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { deleteChecklistUseCase(parameter = id) }
            }
        }

        test("TC-388cee7d-d25c-4b17-bba6-85048cb39130 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteChecklistUseCase = mockk<DeleteChecklistUseCase>()
                coEvery { deleteChecklistUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(deleteChecklistUseCase = deleteChecklistUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("같은 체크리스트의 삭제를 진행하는 중에 다시 삭제하면 삭제를 한 번만 요청한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val gate = CompletableDeferred<Unit>()
                val deleteChecklistUseCase = mockk<DeleteChecklistUseCase>()
                coEvery { deleteChecklistUseCase(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = viewModel(deleteChecklistUseCase = deleteChecklistUseCase)

                viewModel.delete(id = id)
                viewModel.delete(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { deleteChecklistUseCase(parameter = id) }
            }
        }

        test("TC-c68a8936-d158-4027-8c75-49723b99d0e1 실행 취소하면 그 체크리스트의 삭제를 되돌리는 요청을 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restoreChecklistUseCase = mockk<RestoreChecklistUseCase>()
                coEvery { restoreChecklistUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(restoreChecklistUseCase = restoreChecklistUseCase)

                viewModel.restore(id = id)
                advanceUntilIdle()

                coVerify(exactly = 1) { restoreChecklistUseCase(parameter = id) }
            }
        }

        test("같은 체크리스트의 실행 취소를 진행하는 중에 다시 실행 취소하면 한 번만 요청한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val gate = CompletableDeferred<Unit>()
                val restoreChecklistUseCase = mockk<RestoreChecklistUseCase>()
                coEvery { restoreChecklistUseCase(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = viewModel(restoreChecklistUseCase = restoreChecklistUseCase)

                viewModel.restore(id = id)
                viewModel.restore(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { restoreChecklistUseCase(parameter = id) }
            }
        }

        test("TC-17730eb2-1d22-4810-99d2-aca75dfe6c84 실행 취소를 저장하지 못하면 별도 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restoreChecklistUseCase = mockk<RestoreChecklistUseCase>()
                coEvery { restoreChecklistUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(restoreChecklistUseCase = restoreChecklistUseCase)

                viewModel.effect.test {
                    viewModel.restore(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { restoreChecklistUseCase(parameter = id) }
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private const val MAX_COUNT = 50

        private fun summary(): ChecklistSummary {
            val totalCount = fixtureMonkey.giveMeOne<Int>().mod(MAX_COUNT)

            return ChecklistSummary(
                checklist = fixtureMonkey.checklist(isDeleted = false),
                checkedCount = fixtureMonkey.giveMeOne<Int>().mod(totalCount + 1),
                totalCount = totalCount,
            )
        }

        private fun viewModel(
            pageChecklistUseCase: PageChecklistUseCase = pageChecklistUseCase(summaryListFlow = flowOf(Result.success(emptyList()))),
            deleteChecklistUseCase: DeleteChecklistUseCase = mockk(),
            restoreChecklistUseCase: RestoreChecklistUseCase = mockk(),
        ): ChecklistHomeViewModel =
            ChecklistHomeViewModel(
                pageChecklistUseCase = pageChecklistUseCase,
                deleteChecklistUseCase = deleteChecklistUseCase,
                restoreChecklistUseCase = restoreChecklistUseCase,
            )

        private fun pageChecklistUseCase(summaryListFlow: Flow<Result<List<ChecklistSummary>>>): PageChecklistUseCase {
            val pageChecklistUseCase = mockk<PageChecklistUseCase>()
            every { pageChecklistUseCase(parameter = Unit) } returns
                summaryListFlow.map { result -> result.map { summaryList -> PagingData.from(summaryList) } }

            return pageChecklistUseCase
        }
    }
}
