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

        test("TC-ad2f259f-b081-4960-8a73-f4f580b1b753 조회에 성공하면 저장된 제목과 설명으로 내용 표시 상태가 된다") {
            runTest(mainDispatcher) {
                val checklist = fixtureMonkey.checklist(isDeleted = false)
                val viewModel = viewModel(checklist = flowOf(Result.success(checklist)), id = checklist.id)

                viewModel.uiState.test {
                    awaitItem() shouldBe ChecklistDetailUiState.Loading
                    awaitItem() shouldBe ChecklistDetailUiState.Content(id = checklist.id, detail = checklist.detail)
                }
            }
        }

        test("TC-ecf387d2-03d7-42e3-a607-c955f7b00752 대상 체크리스트가 없거나 조회에 실패하면 조회 중 상태를 유지한다") {
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

        test("TC-dd99c2ba-8c3d-43f9-b917-2018af7063f4 조회가 끝나지 않으면 조회 중 상태다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(checklist = emptyFlow())

                viewModel.uiState.test {
                    awaitItem() shouldBe ChecklistDetailUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-3eb8b261-f734-4f7d-a894-d55aeae3c8fd 저장된 제목이 다른 경로에서 바뀌면 바뀐 제목으로 갱신한다") {
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

        test("TC-5d2eaa57-6b77-4ff5-864c-fc563eab34bc 대상 체크리스트가 삭제 상태가 되어도 내용 표시 상태를 유지한다") {
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

        test("TC-09f51c5f-c2fc-4b70-8fc0-f223cd0c8496 수정에 성공하면 입력한 내용으로 수정을 한 번 요청하고 성공을 한 번 알린다") {
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

        test("TC-cc83beda-9642-4d59-8661-12a415c67e7c 수정을 처리하는 동안 진행 상태를 노출하고 끝나면 해제한다") {
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

        test("TC-c93605ca-6b04-4408-85c5-55f71d6b6638 수정에 실패하면 안내 없이 진행 상태만 해제하고 다시 수정할 수 있다") {
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

        test("TC-95a0e2bf-e1e2-4e69-b3d1-77f9eaa4ee7d 삭제에 성공하면 삭제를 한 번 요청하고 화면을 떠나도록 한 번 알린다") {
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

        test("TC-ff222760-12c7-4ef6-a3e9-cea2a7ad15da 삭제를 처리하는 동안 진행 상태를 노출하고 끝나면 해제한다") {
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

        test("TC-ec088c83-4fba-4ce9-99fe-b65e4ebae0b6 삭제에 실패하면 화면에 머무르고 다시 삭제할 수 있다") {
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

        test("TC-dbb55974-a5c4-4272-b99b-52ecb3c30bf0 수정이나 삭제를 처리하는 동안 같은 종류는 다시 요청하지 않고 다른 종류는 요청한다") {
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
