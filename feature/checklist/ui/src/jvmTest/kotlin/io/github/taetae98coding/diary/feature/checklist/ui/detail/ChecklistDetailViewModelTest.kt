@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.checklist.ui.detail

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklistDetail
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.UpdateChecklistUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
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

class ChecklistDetailViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-CHECKLIST-DETAIL-FEATURE-001 조회에 성공하면 저장된 제목과 설명으로 내용 표시 상태가 된다") {
            runTest(mainDispatcher) {
                val checklist = fixtureMonkey.checklist(isDeleted = false)
                val viewModel = viewModel(checklist = flowOf(Result.success(checklist)), id = checklist.id)

                viewModel.uiState.test {
                    awaitItem() shouldBe ChecklistDetailUiState.Loading
                    awaitItem() shouldBe ChecklistDetailUiState.Content(id = checklist.id, detail = checklist.detail)
                }
            }
        }

        test("TC-CHECKLIST-DETAIL-FEATURE-004 대상 체크리스트가 없거나 조회에 실패하면 조회 중 상태를 유지한다") {
            listOf<Result<Checklist?>>(Result.success(null), Result.failure(IllegalStateException())).forEach { result ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(checklist = flowOf(result))

                    viewModel.uiState.test {
                        awaitItem() shouldBe ChecklistDetailUiState.Loading
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-CHECKLIST-DETAIL-FEATURE-003 조회가 끝나지 않으면 조회 중 상태다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(checklist = emptyFlow())

                viewModel.uiState.test {
                    awaitItem() shouldBe ChecklistDetailUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-CHECKLIST-DETAIL-FEATURE-034 저장된 제목이 다른 경로에서 바뀌면 바뀐 제목으로 갱신한다") {
            runTest(mainDispatcher) {
                val checklist = fixtureMonkey.checklist(isDeleted = false)
                val changed = checklist.copy(detail = fixtureMonkey.checklistDetail())
                val checklistFlow = MutableStateFlow(Result.success<Checklist?>(checklist))
                val viewModel = viewModel(checklist = checklistFlow, id = checklist.id)

                viewModel.uiState.test {
                    awaitItem() shouldBe ChecklistDetailUiState.Loading
                    awaitItem() shouldBe ChecklistDetailUiState.Content(id = checklist.id, detail = checklist.detail)

                    checklistFlow.value = Result.success(changed)

                    awaitItem() shouldBe ChecklistDetailUiState.Content(id = checklist.id, detail = changed.detail)
                }
            }
        }

        test("TC-CHECKLIST-DETAIL-FEATURE-036 대상 체크리스트가 삭제 상태가 되어도 내용 표시 상태를 유지한다") {
            runTest(mainDispatcher) {
                val checklist = fixtureMonkey.checklist(isDeleted = false)
                val checklistFlow = MutableStateFlow(Result.success<Checklist?>(checklist))
                val viewModel = viewModel(checklist = checklistFlow, id = checklist.id)

                viewModel.uiState.test {
                    awaitItem() shouldBe ChecklistDetailUiState.Loading
                    awaitItem() shouldBe ChecklistDetailUiState.Content(id = checklist.id, detail = checklist.detail)

                    checklistFlow.value = Result.success(checklist.copy(isDeleted = true))
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value shouldBe ChecklistDetailUiState.Content(id = checklist.id, detail = checklist.detail)
                }
            }
        }

        test("TC-CHECKLIST-DETAIL-FEATURE-026 수정에 성공하면 입력한 내용으로 수정을 한 번 요청하고 성공을 한 번 알린다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val detail = fixtureMonkey.checklistDetail()
                val updateChecklistUseCase = mockk<UpdateChecklistUseCase>()
                coEvery { updateChecklistUseCase(parameter = UpdateChecklistUseCase.Parameter(id = id, detail = detail)) } returns Result.success(1)
                val viewModel = viewModel(id = id, updateChecklistUseCase = updateChecklistUseCase)

                viewModel.effect.test {
                    viewModel.update(detail = detail)

                    awaitItem() shouldBe ChecklistDetailEffect.UpdateSucceeded
                    expectNoEvents()
                }
                coVerify(exactly = 1) { updateChecklistUseCase(parameter = UpdateChecklistUseCase.Parameter(id = id, detail = detail)) }
            }
        }

        test("TC-CHECKLIST-DETAIL-FEATURE-027 수정을 처리하는 동안 진행 상태를 노출하고 끝나면 해제한다") {
            runTest(mainDispatcher) {
                val checklist = fixtureMonkey.checklist(isDeleted = false)
                val gate = CompletableDeferred<Unit>()
                val updateChecklistUseCase = mockk<UpdateChecklistUseCase>()
                coEvery { updateChecklistUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = viewModel(checklist = flowOf(Result.success(checklist)), id = checklist.id, updateChecklistUseCase = updateChecklistUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe ChecklistDetailUiState.Loading
                    awaitItem() shouldBe ChecklistDetailUiState.Content(id = checklist.id, detail = checklist.detail)

                    viewModel.update(detail = fixtureMonkey.checklistDetail())
                    awaitItem() shouldBe ChecklistDetailUiState.Content(id = checklist.id, detail = checklist.detail, isUpdateInProgress = true)

                    gate.complete(Unit)
                    awaitItem() shouldBe ChecklistDetailUiState.Content(id = checklist.id, detail = checklist.detail)
                }
            }
        }

        test("TC-CHECKLIST-DETAIL-FEATURE-028 수정에 실패하면 안내 없이 진행 상태만 해제하고 다시 수정할 수 있다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.checklistDetail()
                val updateChecklistUseCase = mockk<UpdateChecklistUseCase>()
                coEvery { updateChecklistUseCase(parameter = any()) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(updateChecklistUseCase = updateChecklistUseCase)

                viewModel.effect.test {
                    viewModel.update(detail = detail)
                    advanceUntilIdle()
                    viewModel.update(detail = detail)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 2) { updateChecklistUseCase(parameter = any()) }
            }
        }

        test("TC-CHECKLIST-DETAIL-FEATURE-030 삭제에 성공하면 삭제를 한 번 요청하고 화면을 떠나도록 한 번 알린다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteChecklistUseCase = mockk<DeleteChecklistUseCase>()
                coEvery { deleteChecklistUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(id = id, deleteChecklistUseCase = deleteChecklistUseCase)

                viewModel.effect.test {
                    viewModel.delete()

                    awaitItem() shouldBe ChecklistDetailEffect.DeleteSucceeded
                    expectNoEvents()
                }
                coVerify(exactly = 1) { deleteChecklistUseCase(parameter = id) }
            }
        }

        test("TC-CHECKLIST-DETAIL-FEATURE-031 삭제를 처리하는 동안 진행 상태를 노출하고 끝나면 해제한다") {
            runTest(mainDispatcher) {
                val checklist = fixtureMonkey.checklist(isDeleted = false)
                val gate = CompletableDeferred<Unit>()
                val deleteChecklistUseCase = mockk<DeleteChecklistUseCase>()
                coEvery { deleteChecklistUseCase(parameter = checklist.id) } coAnswers {
                    gate.await()
                    Result.failure(IllegalStateException())
                }
                val viewModel = viewModel(checklist = flowOf(Result.success(checklist)), id = checklist.id, deleteChecklistUseCase = deleteChecklistUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe ChecklistDetailUiState.Loading
                    awaitItem() shouldBe ChecklistDetailUiState.Content(id = checklist.id, detail = checklist.detail)

                    viewModel.delete()
                    awaitItem() shouldBe ChecklistDetailUiState.Content(id = checklist.id, detail = checklist.detail, isDeleteInProgress = true)

                    gate.complete(Unit)
                    awaitItem() shouldBe ChecklistDetailUiState.Content(id = checklist.id, detail = checklist.detail)
                }
            }
        }

        test("TC-CHECKLIST-DETAIL-FEATURE-032 삭제에 실패하면 화면에 머무르고 다시 삭제할 수 있다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteChecklistUseCase = mockk<DeleteChecklistUseCase>()
                coEvery { deleteChecklistUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(id = id, deleteChecklistUseCase = deleteChecklistUseCase)

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()
                    viewModel.delete()
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 2) { deleteChecklistUseCase(parameter = id) }
            }
        }

        test("TC-CHECKLIST-DETAIL-DOMAIN-013 수정이나 삭제를 처리하는 동안 같은 종류는 다시 요청하지 않고 다른 종류는 요청한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val gate = CompletableDeferred<Unit>()
                val updateChecklistUseCase = mockk<UpdateChecklistUseCase>()
                coEvery { updateChecklistUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val deleteChecklistUseCase = mockk<DeleteChecklistUseCase>()
                coEvery { deleteChecklistUseCase(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = viewModel(id = id, updateChecklistUseCase = updateChecklistUseCase, deleteChecklistUseCase = deleteChecklistUseCase)

                viewModel.update(detail = fixtureMonkey.checklistDetail())
                runCurrent()
                viewModel.update(detail = fixtureMonkey.checklistDetail())
                viewModel.delete()
                runCurrent()
                viewModel.delete()
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { updateChecklistUseCase(parameter = any()) }
                coVerify(exactly = 1) { deleteChecklistUseCase(parameter = id) }
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            checklist: Flow<Result<Checklist?>> = emptyFlow(),
            id: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            updateChecklistUseCase: UpdateChecklistUseCase = mockk(),
            deleteChecklistUseCase: DeleteChecklistUseCase = mockk(),
        ): ChecklistDetailViewModel {
            val findChecklistUseCase = mockk<FindChecklistUseCase>()
            every { findChecklistUseCase(parameter = id) } returns checklist

            return ChecklistDetailViewModel(
                id = id,
                updateChecklistUseCase = updateChecklistUseCase,
                deleteChecklistUseCase = deleteChecklistUseCase,
                findChecklistUseCase = findChecklistUseCase,
            )
        }
    }
}
