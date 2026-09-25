@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.detail

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.domain.memo.usecase.CopyMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UpdateMemoUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoDetailViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MEMO-DETAIL-FEATURE-029 조회 결과가 없으면 로딩 상태를 유지한다") {
            runTest(mainDispatcher) {
                val findMemoUseCase = mockk<FindMemoUseCase>()
                every { findMemoUseCase(any()) } returns flowOf(Result.success(null))
                val viewModel = viewModel(findMemoUseCase = findMemoUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoDetailUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-029 조회가 실패하면 로딩 상태를 유지한다") {
            runTest(mainDispatcher) {
                val findMemoUseCase = mockk<FindMemoUseCase>()
                every { findMemoUseCase(any()) } returns flowOf(Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())))
                val viewModel = viewModel(findMemoUseCase = findMemoUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoDetailUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-030 조회가 완료되면 로딩 상태가 끝나고 조회한 내용이 표시된다") {
            runTest(mainDispatcher) {
                val memo = memo()
                val resultFlow = MutableSharedFlow<Result<Memo?>>(replay = 1)
                val findMemoUseCase = mockk<FindMemoUseCase>()
                every { findMemoUseCase(any()) } returns resultFlow
                val viewModel = viewModel(findMemoUseCase = findMemoUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoDetailUiState.Loading

                    resultFlow.emit(Result.success(memo))
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem().shouldBeInstanceOf<MemoDetailUiState.Content>()
                    uiState.detail shouldBe memo.detail
                    uiState.isFinished shouldBe memo.isFinished
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-001 조회한 메모의 제목, 설명, 컬러를 상태에 채우고 후속 변경도 반영한다") {
            runTest(mainDispatcher) {
                val memo = memo()
                val changedMemo = memo.copy(detail = fixtureMonkey.giveMeOne<MemoDetail>())
                val resultFlow = MutableSharedFlow<Result<Memo?>>(replay = 1)
                val findMemoUseCase = mockk<FindMemoUseCase>()
                every { findMemoUseCase(any()) } returns resultFlow
                val viewModel =
                    viewModel(
                        id = memo.id,
                        findMemoUseCase = findMemoUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoDetailUiState.Loading

                    resultFlow.emit(Result.success(memo))
                    awaitItem() shouldBe
                        MemoDetailUiState.Content(
                            id = memo.id,
                            detail = memo.detail,
                            isFinished = memo.isFinished,
                        )

                    resultFlow.emit(Result.success(changedMemo))
                    awaitItem() shouldBe
                        MemoDetailUiState.Content(
                            id = changedMemo.id,
                            detail = changedMemo.detail,
                            isFinished = changedMemo.isFinished,
                        )
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { findMemoUseCase(memo.id) }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-073 같은 메모가 다른 경로로 완료되거나 다시 시작되면 저장된 완료 여부를 상태에 반영한다") {
            runTest(mainDispatcher) {
                val memo = memo().copy(isFinished = false)
                val finishedMemo = memo.copy(isFinished = true)
                val resultFlow = MutableSharedFlow<Result<Memo?>>(replay = 1)
                val findMemoUseCase = mockk<FindMemoUseCase>()
                every { findMemoUseCase(any()) } returns resultFlow
                val viewModel =
                    viewModel(
                        id = memo.id,
                        findMemoUseCase = findMemoUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoDetailUiState.Loading

                    resultFlow.emit(Result.success(memo))
                    (awaitItem() as MemoDetailUiState.Content).isFinished shouldBe false

                    resultFlow.emit(Result.success(finishedMemo))
                    (awaitItem() as MemoDetailUiState.Content).isFinished shouldBe true

                    resultFlow.emit(Result.success(memo))
                    (awaitItem() as MemoDetailUiState.Content).isFinished shouldBe false
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-074 같은 메모가 다른 경로로 삭제되어도 화면을 닫지 않고 마지막 저장 내용을 계속 표시한다") {
            runTest(mainDispatcher) {
                val memo = memo().copy(isDeleted = false)
                val deletedMemo = memo.copy(isDeleted = true)
                val resultFlow = MutableSharedFlow<Result<Memo?>>(replay = 1)
                val findMemoUseCase = mockk<FindMemoUseCase>()
                every { findMemoUseCase(any()) } returns resultFlow
                val viewModel =
                    viewModel(
                        id = memo.id,
                        findMemoUseCase = findMemoUseCase,
                    )

                viewModel.effect.test {
                    viewModel.uiState.test {
                        awaitItem() shouldBe MemoDetailUiState.Loading

                        resultFlow.emit(Result.success(memo))
                        (awaitItem() as MemoDetailUiState.Content).detail shouldBe memo.detail

                        resultFlow.emit(Result.success(deletedMemo))
                        advanceUntilIdle()

                        val uiState = viewModel.uiState.value.shouldBeInstanceOf<MemoDetailUiState.Content>()
                        uiState.detail shouldBe deletedMemo.detail
                        cancelAndIgnoreRemainingEvents()
                    }
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-DOMAIN-002 저장 처리 중 전달된 저장 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val firstDetail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "first-${fixtureMonkey.giveMeOne<String>()}")
                val secondDetail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "second-${fixtureMonkey.giveMeOne<String>()}")
                val completion = CompletableDeferred<Result<Int>>()
                val updateMemoUseCase = mockk<UpdateMemoUseCase>()
                coEvery { updateMemoUseCase(any()) } coAnswers { completion.await() }
                val viewModel =
                    viewModel(
                        id = id,
                        updateMemoUseCase = updateMemoUseCase,
                    )

                viewModel.update(firstDetail)
                runCurrent()

                viewModel.update(secondDetail)
                runCurrent()

                coVerify(exactly = 1) { updateMemoUseCase(UpdateMemoUseCase.Parameter(id = id, detail = firstDetail)) }
                coVerify(exactly = 0) { updateMemoUseCase(UpdateMemoUseCase.Parameter(id = id, detail = secondDetail)) }

                completion.complete(Result.success(1))
                advanceUntilIdle()
            }
        }

        test("TC-MEMO-DETAIL-DOMAIN-004 완료 처리 중 전달된 완료와 다시 시작 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val completion = CompletableDeferred<Result<Int>>()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } coAnswers { completion.await() }
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                coEvery { restartMemoUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        finishMemoUseCase = finishMemoUseCase,
                        restartMemoUseCase = restartMemoUseCase,
                    )

                viewModel.finish()
                runCurrent()

                viewModel.finish()
                viewModel.restart()
                runCurrent()

                coVerify(exactly = 1) { finishMemoUseCase(parameter = id) }
                coVerify(exactly = 0) { restartMemoUseCase(parameter = any()) }

                completion.complete(Result.success(1))
                advanceUntilIdle()

                viewModel.restart()
                advanceUntilIdle()

                coVerify(exactly = 1) { restartMemoUseCase(parameter = id) }
            }
        }

        test("TC-MEMO-DETAIL-DOMAIN-004 삭제 처리 중 전달된 삭제 요청은 처리하지 않고 완료 요청은 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val completion = CompletableDeferred<Result<Int>>()
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(any()) } coAnswers { completion.await() }
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        finishMemoUseCase = finishMemoUseCase,
                        deleteMemoUseCase = deleteMemoUseCase,
                    )

                viewModel.delete()
                runCurrent()

                viewModel.delete()
                viewModel.finish()
                runCurrent()

                coVerify(exactly = 1) { deleteMemoUseCase(parameter = id) }
                coVerify(exactly = 1) { finishMemoUseCase(parameter = id) }

                completion.complete(Result.success(1))
                advanceUntilIdle()
            }
        }

        test("TC-MEMO-DETAIL-DOMAIN-004 완료 처리 중 전달된 삭제와 수정 요청은 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val detail = fixtureMonkey.giveMeOne<MemoDetail>()
                val completion = CompletableDeferred<Result<Int>>()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } coAnswers { completion.await() }
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(any()) } returns Result.success(1)
                val updateMemoUseCase = mockk<UpdateMemoUseCase>()
                coEvery { updateMemoUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        updateMemoUseCase = updateMemoUseCase,
                        finishMemoUseCase = finishMemoUseCase,
                        deleteMemoUseCase = deleteMemoUseCase,
                    )

                viewModel.finish()
                runCurrent()

                viewModel.delete()
                viewModel.update(detail)
                runCurrent()

                coVerify(exactly = 1) { deleteMemoUseCase(parameter = id) }
                coVerify(exactly = 1) { updateMemoUseCase(parameter = UpdateMemoUseCase.Parameter(id = id, detail = detail)) }

                completion.complete(Result.success(1))
                advanceUntilIdle()
            }
        }

        test("TC-MEMO-DETAIL-DOMAIN-004 수정 처리 중 전달된 완료 요청은 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val completion = CompletableDeferred<Result<Int>>()
                val updateMemoUseCase = mockk<UpdateMemoUseCase>()
                coEvery { updateMemoUseCase(any()) } coAnswers { completion.await() }
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        updateMemoUseCase = updateMemoUseCase,
                        finishMemoUseCase = finishMemoUseCase,
                    )

                viewModel.update(fixtureMonkey.giveMeOne<MemoDetail>())
                runCurrent()

                viewModel.finish()
                runCurrent()

                coVerify(exactly = 1) { finishMemoUseCase(parameter = id) }

                completion.complete(Result.success(1))
                advanceUntilIdle()
            }
        }

        test("수정이 취소되면 다음 수정 요청을 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val firstDetail = fixtureMonkey.giveMeOne<MemoDetail>()
                val secondDetail = fixtureMonkey.giveMeOne<MemoDetail>()
                val updateMemoUseCase = mockk<UpdateMemoUseCase>()
                coEvery {
                    updateMemoUseCase(UpdateMemoUseCase.Parameter(id = id, detail = firstDetail))
                } throws CancellationException()
                coEvery {
                    updateMemoUseCase(UpdateMemoUseCase.Parameter(id = id, detail = secondDetail))
                } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        updateMemoUseCase = updateMemoUseCase,
                    )

                viewModel.update(firstDetail)
                advanceUntilIdle()
                viewModel.update(secondDetail)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    updateMemoUseCase(UpdateMemoUseCase.Parameter(id = id, detail = firstDetail))
                }
                coVerify(exactly = 1) {
                    updateMemoUseCase(UpdateMemoUseCase.Parameter(id = id, detail = secondDetail))
                }
            }
        }

        test("완료가 취소되면 다음 다시 시작 요청을 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(parameter = id) } throws CancellationException()
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                coEvery { restartMemoUseCase(parameter = id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        finishMemoUseCase = finishMemoUseCase,
                        restartMemoUseCase = restartMemoUseCase,
                    )

                viewModel.finish()
                advanceUntilIdle()
                viewModel.restart()
                advanceUntilIdle()

                coVerify(exactly = 1) { finishMemoUseCase(parameter = id) }
                coVerify(exactly = 1) { restartMemoUseCase(parameter = id) }
            }
        }

        test("다시 시작이 취소되면 다음 완료 요청을 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                coEvery { restartMemoUseCase(parameter = id) } throws CancellationException()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(parameter = id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        finishMemoUseCase = finishMemoUseCase,
                        restartMemoUseCase = restartMemoUseCase,
                    )

                viewModel.restart()
                advanceUntilIdle()
                viewModel.finish()
                advanceUntilIdle()

                coVerify(exactly = 1) { restartMemoUseCase(parameter = id) }
                coVerify(exactly = 1) { finishMemoUseCase(parameter = id) }
            }
        }

        test("삭제가 취소되면 다음 삭제 요청을 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(parameter = id) } throws CancellationException()
                val viewModel =
                    viewModel(
                        id = id,
                        deleteMemoUseCase = deleteMemoUseCase,
                    )

                viewModel.delete()
                advanceUntilIdle()

                coEvery { deleteMemoUseCase(parameter = id) } returns Result.success(1)
                viewModel.delete()
                advanceUntilIdle()

                coVerify(exactly = 2) { deleteMemoUseCase(parameter = id) }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-012 수정에 성공하면 수정 성공 Effect를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val updateMemoUseCase = mockk<UpdateMemoUseCase>()
                coEvery { updateMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(updateMemoUseCase = updateMemoUseCase)

                viewModel.effect.test {
                    viewModel.update(fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "title-${fixtureMonkey.giveMeOne<String>()}"))
                    advanceUntilIdle()

                    awaitItem() shouldBe MemoDetailEffect.UpdateSucceeded
                    expectNoEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-DATA-003 완료를 실행하면 현재 메모의 완료를 요청하고 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        finishMemoUseCase = finishMemoUseCase,
                    )

                viewModel.effect.test {
                    viewModel.finish()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { finishMemoUseCase(parameter = id) }
            }
        }

        test("TC-MEMO-DETAIL-DATA-003 다시 시작을 실행하면 현재 메모의 다시 시작을 요청하고 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                coEvery { restartMemoUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        restartMemoUseCase = restartMemoUseCase,
                    )

                viewModel.effect.test {
                    viewModel.restart()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { restartMemoUseCase(parameter = id) }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-026 TC-MEMO-DETAIL-DATA-003 삭제에 성공하면 현재 메모의 삭제를 요청하고 삭제 성공 Effect를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        deleteMemoUseCase = deleteMemoUseCase,
                    )

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()

                    awaitItem() shouldBe MemoDetailEffect.DeleteSucceeded
                    expectNoEvents()
                }

                coVerify(exactly = 1) { deleteMemoUseCase(parameter = id) }
            }
        }

        test("삭제에 실패하면 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(any()) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(deleteMemoUseCase = deleteMemoUseCase)

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-044 복사에 성공하면 복사본 메모의 상세로 전환하는 Effect를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val copiedId = fixtureMonkey.giveMeOne<Uuid>()
                val copyMemoUseCase = mockk<CopyMemoUseCase>()
                coEvery { copyMemoUseCase(any()) } returns Result.success(copiedId)
                val viewModel =
                    viewModel(
                        id = id,
                        copyMemoUseCase = copyMemoUseCase,
                    )

                viewModel.effect.test {
                    viewModel.copy()
                    advanceUntilIdle()

                    awaitItem() shouldBe MemoDetailEffect.CopySucceeded(id = copiedId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { copyMemoUseCase(parameter = id) }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-046 복사에 실패하면 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val copyMemoUseCase = mockk<CopyMemoUseCase>()
                coEvery { copyMemoUseCase(any()) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(copyMemoUseCase = copyMemoUseCase)

                viewModel.effect.test {
                    viewModel.copy()
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-DOMAIN-004 복사 처리 중 전달된 복사 요청은 처리하지 않고 완료 요청은 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val completion = CompletableDeferred<Result<Uuid>>()
                val copyMemoUseCase = mockk<CopyMemoUseCase>()
                coEvery { copyMemoUseCase(any()) } coAnswers { completion.await() }
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        finishMemoUseCase = finishMemoUseCase,
                        copyMemoUseCase = copyMemoUseCase,
                    )

                viewModel.copy()
                runCurrent()

                viewModel.copy()
                viewModel.finish()
                runCurrent()

                coVerify(exactly = 1) { copyMemoUseCase(parameter = id) }
                coVerify(exactly = 1) { finishMemoUseCase(parameter = id) }

                completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                advanceUntilIdle()

                viewModel.copy()
                advanceUntilIdle()

                coVerify(exactly = 2) { copyMemoUseCase(parameter = id) }
            }
        }

        test("TC-MEMO-DETAIL-DOMAIN-004 완료 처리 중 전달된 복사 요청은 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val completion = CompletableDeferred<Result<Int>>()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } coAnswers { completion.await() }
                val copyMemoUseCase = mockk<CopyMemoUseCase>()
                coEvery { copyMemoUseCase(any()) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel =
                    viewModel(
                        id = id,
                        finishMemoUseCase = finishMemoUseCase,
                        copyMemoUseCase = copyMemoUseCase,
                    )

                viewModel.finish()
                runCurrent()

                viewModel.copy()
                runCurrent()

                coVerify(exactly = 1) { copyMemoUseCase(parameter = id) }

                completion.complete(Result.success(1))
                advanceUntilIdle()
            }
        }

        test("복사가 취소되면 다음 복사 요청을 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val copyMemoUseCase = mockk<CopyMemoUseCase>()
                coEvery { copyMemoUseCase(parameter = id) } throws CancellationException()
                val viewModel =
                    viewModel(
                        id = id,
                        copyMemoUseCase = copyMemoUseCase,
                    )

                viewModel.copy()
                advanceUntilIdle()

                coEvery { copyMemoUseCase(parameter = id) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                viewModel.copy()
                advanceUntilIdle()

                coVerify(exactly = 2) { copyMemoUseCase(parameter = id) }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-028 복사를 처리하는 동안 복사 진행 상태를 노출한다") {
            runTest(mainDispatcher) {
                val memo = memo()
                val completion = CompletableDeferred<Result<Uuid>>()
                val findMemoUseCase = mockk<FindMemoUseCase>()
                every { findMemoUseCase(any()) } returns flowOf(Result.success(memo))
                val copyMemoUseCase = mockk<CopyMemoUseCase>()
                coEvery { copyMemoUseCase(any()) } coAnswers { completion.await() }
                val viewModel =
                    viewModel(
                        id = memo.id,
                        findMemoUseCase = findMemoUseCase,
                        copyMemoUseCase = copyMemoUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoDetailUiState.Loading
                    runCurrent()
                    (awaitItem() as MemoDetailUiState.Content).isCopyInProgress shouldBe false

                    viewModel.copy()
                    runCurrent()

                    (awaitItem() as MemoDetailUiState.Content).isCopyInProgress shouldBe true

                    completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                    advanceUntilIdle()

                    (awaitItem() as MemoDetailUiState.Content).isCopyInProgress shouldBe false
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-017 수정에 성공해 저장 제목이 바뀌면 상단 바 제목이 갱신된다") {
            runTest(mainDispatcher) {
                val memo = memo()
                val newTitle = "new-${fixtureMonkey.giveMeOne<String>()}"
                val resultFlow = MutableSharedFlow<Result<Memo?>>(replay = 1)
                val findMemoUseCase = mockk<FindMemoUseCase>()
                every { findMemoUseCase(any()) } returns resultFlow
                val updateMemoUseCase = mockk<UpdateMemoUseCase>()
                coEvery { updateMemoUseCase(any()) } coAnswers {
                    resultFlow.emit(Result.success(memo.copy(detail = memo.detail.copy(title = newTitle))))
                    Result.success(1)
                }
                val viewModel =
                    viewModel(
                        id = memo.id,
                        findMemoUseCase = findMemoUseCase,
                        updateMemoUseCase = updateMemoUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoDetailUiState.Loading

                    resultFlow.emit(Result.success(memo))
                    (awaitItem() as MemoDetailUiState.Content).detail.title shouldBe memo.detail.title

                    viewModel.update(memo.detail.copy(title = newTitle))
                    advanceUntilIdle()

                    (awaitItem() as MemoDetailUiState.Content).detail.title shouldBe newTitle
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun emptyFindMemoUseCase(): FindMemoUseCase {
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(any()) } returns flowOf(Result.success(null))
            return findMemoUseCase
        }

        private fun viewModel(
            id: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            findMemoUseCase: FindMemoUseCase = emptyFindMemoUseCase(),
            updateMemoUseCase: UpdateMemoUseCase = mockk(relaxed = true),
            finishMemoUseCase: FinishMemoUseCase = mockk(relaxed = true),
            restartMemoUseCase: RestartMemoUseCase = mockk(relaxed = true),
            copyMemoUseCase: CopyMemoUseCase = mockk(relaxed = true),
            deleteMemoUseCase: DeleteMemoUseCase = mockk(relaxed = true),
        ): MemoDetailViewModel =
            MemoDetailViewModel(
                id = id,
                findMemoUseCase = findMemoUseCase,
                updateMemoUseCase = updateMemoUseCase,
                finishMemoUseCase = finishMemoUseCase,
                restartMemoUseCase = restartMemoUseCase,
                copyMemoUseCase = copyMemoUseCase,
                deleteMemoUseCase = deleteMemoUseCase,
            )
    }
}
