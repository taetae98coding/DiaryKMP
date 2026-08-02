@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.tag.usecase.DeleteTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FinishTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestartTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.UpdateTagUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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

class TagDetailViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-TAG-DETAIL-FEATURE-022 조회 결과가 없으면 로딩 상태를 유지한다") {
            runTest(mainDispatcher) {
                val findTagUseCase = mockk<FindTagUseCase>()
                every { findTagUseCase(any()) } returns flowOf(Result.success(null))
                val viewModel = viewModel(findTagUseCase = findTagUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe TagDetailUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-TAG-DETAIL-FEATURE-022 조회가 실패하면 로딩 상태를 유지한다") {
            runTest(mainDispatcher) {
                val findTagUseCase = mockk<FindTagUseCase>()
                every { findTagUseCase(any()) } returns flowOf(Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())))
                val viewModel = viewModel(findTagUseCase = findTagUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe TagDetailUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-TAG-DETAIL-FEATURE-001 조회한 태그의 제목, 설명, 컬러를 상태에 채우고 후속 변경도 반영한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val changedTag = tag.copy(detail = fixtureMonkey.giveMeOne<TagDetail>())
                val resultFlow = MutableSharedFlow<Result<Tag?>>(replay = 1)
                val findTagUseCase = mockk<FindTagUseCase>()
                every { findTagUseCase(any()) } returns resultFlow
                val viewModel =
                    viewModel(
                        id = tag.id,
                        findTagUseCase = findTagUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe TagDetailUiState.Loading

                    resultFlow.emit(Result.success(tag))
                    awaitItem() shouldBe
                        TagDetailUiState.Content(
                            id = tag.id,
                            detail = tag.detail,
                            isFinished = tag.isFinished,
                        )

                    resultFlow.emit(Result.success(changedTag))
                    awaitItem() shouldBe
                        TagDetailUiState.Content(
                            id = changedTag.id,
                            detail = changedTag.detail,
                            isFinished = changedTag.isFinished,
                        )
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { findTagUseCase(tag.id) }
            }
        }

        test("TC-TAG-DETAIL-FEATURE-015 조회한 태그의 완료 상태를 상태에 채우고 후속 변경도 반영한다") {
            runTest(mainDispatcher) {
                val tag = tag().copy(isFinished = false)
                val finishedTag = tag.copy(isFinished = true)
                val resultFlow = MutableSharedFlow<Result<Tag?>>(replay = 1)
                val findTagUseCase = mockk<FindTagUseCase>()
                every { findTagUseCase(any()) } returns resultFlow
                val viewModel =
                    viewModel(
                        id = tag.id,
                        findTagUseCase = findTagUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe TagDetailUiState.Loading

                    resultFlow.emit(Result.success(tag))
                    (awaitItem() as TagDetailUiState.Content).isFinished shouldBe false

                    resultFlow.emit(Result.success(finishedTag))
                    (awaitItem() as TagDetailUiState.Content).isFinished shouldBe true
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-TAG-DETAIL-DOMAIN-002 저장 처리 중 전달된 저장 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val firstDetail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = "first-${fixtureMonkey.giveMeOne<String>()}")
                val secondDetail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = "second-${fixtureMonkey.giveMeOne<String>()}")
                val completion = CompletableDeferred<Result<Int>>()
                val updateTagUseCase = mockk<UpdateTagUseCase>()
                coEvery { updateTagUseCase(any()) } coAnswers { completion.await() }
                val viewModel =
                    viewModel(
                        id = id,
                        updateTagUseCase = updateTagUseCase,
                    )

                viewModel.update(firstDetail)
                runCurrent()

                viewModel.update(secondDetail)
                runCurrent()

                coVerify(exactly = 1) { updateTagUseCase(UpdateTagUseCase.Parameter(id = id, detail = firstDetail)) }
                coVerify(exactly = 0) { updateTagUseCase(UpdateTagUseCase.Parameter(id = id, detail = secondDetail)) }

                completion.complete(Result.success(1))
                advanceUntilIdle()
            }
        }

        test("TC-TAG-DETAIL-DOMAIN-003 완료 처리 중 전달된 완료와 다시 시작 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val completion = CompletableDeferred<Result<Int>>()
                val finishTagUseCase = mockk<FinishTagUseCase>()
                coEvery { finishTagUseCase(any()) } coAnswers { completion.await() }
                val restartTagUseCase = mockk<RestartTagUseCase>()
                coEvery { restartTagUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        finishTagUseCase = finishTagUseCase,
                        restartTagUseCase = restartTagUseCase,
                    )

                viewModel.finish()
                runCurrent()

                viewModel.finish()
                viewModel.restart()
                runCurrent()

                coVerify(exactly = 1) { finishTagUseCase(parameter = id) }
                coVerify(exactly = 0) { restartTagUseCase(parameter = any()) }

                completion.complete(Result.success(1))
                advanceUntilIdle()

                viewModel.restart()
                advanceUntilIdle()

                coVerify(exactly = 1) { restartTagUseCase(parameter = id) }
            }
        }

        test("TC-TAG-DETAIL-DOMAIN-003 삭제 처리 중 전달된 삭제 요청은 처리하지 않고 완료 요청은 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val completion = CompletableDeferred<Result<Int>>()
                val deleteTagUseCase = mockk<DeleteTagUseCase>()
                coEvery { deleteTagUseCase(any()) } coAnswers { completion.await() }
                val finishTagUseCase = mockk<FinishTagUseCase>()
                coEvery { finishTagUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        finishTagUseCase = finishTagUseCase,
                        deleteTagUseCase = deleteTagUseCase,
                    )

                viewModel.delete()
                runCurrent()

                viewModel.delete()
                viewModel.finish()
                runCurrent()

                coVerify(exactly = 1) { deleteTagUseCase(parameter = id) }
                coVerify(exactly = 1) { finishTagUseCase(parameter = id) }

                completion.complete(Result.success(1))
                advanceUntilIdle()
            }
        }

        test("TC-TAG-DETAIL-DOMAIN-003 완료 처리 중 전달된 삭제와 수정 요청은 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val detail = fixtureMonkey.giveMeOne<TagDetail>()
                val completion = CompletableDeferred<Result<Int>>()
                val finishTagUseCase = mockk<FinishTagUseCase>()
                coEvery { finishTagUseCase(any()) } coAnswers { completion.await() }
                val deleteTagUseCase = mockk<DeleteTagUseCase>()
                coEvery { deleteTagUseCase(any()) } returns Result.success(1)
                val updateTagUseCase = mockk<UpdateTagUseCase>()
                coEvery { updateTagUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        updateTagUseCase = updateTagUseCase,
                        finishTagUseCase = finishTagUseCase,
                        deleteTagUseCase = deleteTagUseCase,
                    )

                viewModel.finish()
                runCurrent()

                viewModel.delete()
                viewModel.update(detail)
                runCurrent()

                coVerify(exactly = 1) { deleteTagUseCase(parameter = id) }
                coVerify(exactly = 1) { updateTagUseCase(parameter = UpdateTagUseCase.Parameter(id = id, detail = detail)) }

                completion.complete(Result.success(1))
                advanceUntilIdle()
            }
        }

        test("TC-TAG-DETAIL-DOMAIN-003 수정 처리 중 전달된 완료 요청은 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val completion = CompletableDeferred<Result<Int>>()
                val updateTagUseCase = mockk<UpdateTagUseCase>()
                coEvery { updateTagUseCase(any()) } coAnswers { completion.await() }
                val finishTagUseCase = mockk<FinishTagUseCase>()
                coEvery { finishTagUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        updateTagUseCase = updateTagUseCase,
                        finishTagUseCase = finishTagUseCase,
                    )

                viewModel.update(fixtureMonkey.giveMeOne<TagDetail>())
                runCurrent()

                viewModel.finish()
                runCurrent()

                coVerify(exactly = 1) { finishTagUseCase(parameter = id) }

                completion.complete(Result.success(1))
                advanceUntilIdle()
            }
        }

        test("수정이 취소되면 다음 수정 요청을 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val firstDetail = fixtureMonkey.giveMeOne<TagDetail>()
                val secondDetail = fixtureMonkey.giveMeOne<TagDetail>()
                val updateTagUseCase = mockk<UpdateTagUseCase>()
                coEvery {
                    updateTagUseCase(UpdateTagUseCase.Parameter(id = id, detail = firstDetail))
                } throws CancellationException()
                coEvery {
                    updateTagUseCase(UpdateTagUseCase.Parameter(id = id, detail = secondDetail))
                } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        updateTagUseCase = updateTagUseCase,
                    )

                viewModel.update(firstDetail)
                advanceUntilIdle()
                viewModel.update(secondDetail)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    updateTagUseCase(UpdateTagUseCase.Parameter(id = id, detail = firstDetail))
                }
                coVerify(exactly = 1) {
                    updateTagUseCase(UpdateTagUseCase.Parameter(id = id, detail = secondDetail))
                }
            }
        }

        test("완료가 취소되면 다음 다시 시작 요청을 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val finishTagUseCase = mockk<FinishTagUseCase>()
                coEvery { finishTagUseCase(parameter = id) } throws CancellationException()
                val restartTagUseCase = mockk<RestartTagUseCase>()
                coEvery { restartTagUseCase(parameter = id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        finishTagUseCase = finishTagUseCase,
                        restartTagUseCase = restartTagUseCase,
                    )

                viewModel.finish()
                advanceUntilIdle()
                viewModel.restart()
                advanceUntilIdle()

                coVerify(exactly = 1) { finishTagUseCase(parameter = id) }
                coVerify(exactly = 1) { restartTagUseCase(parameter = id) }
            }
        }

        test("다시 시작이 취소되면 다음 완료 요청을 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restartTagUseCase = mockk<RestartTagUseCase>()
                coEvery { restartTagUseCase(parameter = id) } throws CancellationException()
                val finishTagUseCase = mockk<FinishTagUseCase>()
                coEvery { finishTagUseCase(parameter = id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        finishTagUseCase = finishTagUseCase,
                        restartTagUseCase = restartTagUseCase,
                    )

                viewModel.restart()
                advanceUntilIdle()
                viewModel.finish()
                advanceUntilIdle()

                coVerify(exactly = 1) { restartTagUseCase(parameter = id) }
                coVerify(exactly = 1) { finishTagUseCase(parameter = id) }
            }
        }

        test("삭제가 취소되면 다음 삭제 요청을 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteTagUseCase = mockk<DeleteTagUseCase>()
                coEvery { deleteTagUseCase(parameter = id) } throws CancellationException()
                val viewModel =
                    viewModel(
                        id = id,
                        deleteTagUseCase = deleteTagUseCase,
                    )

                viewModel.delete()
                advanceUntilIdle()

                coEvery { deleteTagUseCase(parameter = id) } returns Result.success(1)
                viewModel.delete()
                advanceUntilIdle()

                coVerify(exactly = 2) { deleteTagUseCase(parameter = id) }
            }
        }

        test("TC-TAG-DETAIL-FEATURE-010 수정에 성공하면 수정 성공 Effect를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val updateTagUseCase = mockk<UpdateTagUseCase>()
                coEvery { updateTagUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(updateTagUseCase = updateTagUseCase)

                viewModel.effect.test {
                    viewModel.update(fixtureMonkey.giveMeOne<TagDetail>().copy(title = "title-${fixtureMonkey.giveMeOne<String>()}"))
                    advanceUntilIdle()

                    awaitItem() shouldBe TagDetailEffect.UpdateSucceeded
                    expectNoEvents()
                }
            }
        }

        test("TC-TAG-DETAIL-DATA-002 완료를 실행하면 현재 태그의 완료를 요청하고 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val finishTagUseCase = mockk<FinishTagUseCase>()
                coEvery { finishTagUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        finishTagUseCase = finishTagUseCase,
                    )

                viewModel.effect.test {
                    viewModel.finish()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { finishTagUseCase(parameter = id) }
            }
        }

        test("TC-TAG-DETAIL-DATA-002 다시 시작을 실행하면 현재 태그의 다시 시작을 요청하고 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restartTagUseCase = mockk<RestartTagUseCase>()
                coEvery { restartTagUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        restartTagUseCase = restartTagUseCase,
                    )

                viewModel.effect.test {
                    viewModel.restart()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { restartTagUseCase(parameter = id) }
            }
        }

        test("TC-TAG-DETAIL-FEATURE-019 TC-TAG-DETAIL-DATA-002 삭제에 성공하면 현재 태그의 삭제를 요청하고 삭제 성공 Effect를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteTagUseCase = mockk<DeleteTagUseCase>()
                coEvery { deleteTagUseCase(any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        deleteTagUseCase = deleteTagUseCase,
                    )

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()

                    awaitItem() shouldBe TagDetailEffect.DeleteSucceeded
                    expectNoEvents()
                }

                coVerify(exactly = 1) { deleteTagUseCase(parameter = id) }
            }
        }

        test("삭제에 실패하면 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val deleteTagUseCase = mockk<DeleteTagUseCase>()
                coEvery { deleteTagUseCase(any()) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(deleteTagUseCase = deleteTagUseCase)

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-TAG-DETAIL-FEATURE-013 수정에 성공해 저장 제목이 바뀌면 상단 바 제목이 갱신된다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val newTitle = "new-${fixtureMonkey.giveMeOne<String>()}"
                val resultFlow = MutableSharedFlow<Result<Tag?>>(replay = 1)
                val findTagUseCase = mockk<FindTagUseCase>()
                every { findTagUseCase(any()) } returns resultFlow
                val updateTagUseCase = mockk<UpdateTagUseCase>()
                coEvery { updateTagUseCase(any()) } coAnswers {
                    resultFlow.emit(Result.success(tag.copy(detail = tag.detail.copy(title = newTitle))))
                    Result.success(1)
                }
                val viewModel =
                    viewModel(
                        id = tag.id,
                        findTagUseCase = findTagUseCase,
                        updateTagUseCase = updateTagUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe TagDetailUiState.Loading

                    resultFlow.emit(Result.success(tag))
                    (awaitItem() as TagDetailUiState.Content).detail.title shouldBe tag.detail.title

                    viewModel.update(tag.detail.copy(title = newTitle))
                    advanceUntilIdle()

                    (awaitItem() as TagDetailUiState.Content).detail.title shouldBe newTitle
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun emptyFindTagUseCase(): FindTagUseCase {
            val findTagUseCase = mockk<FindTagUseCase>()
            every { findTagUseCase(any()) } returns flowOf(Result.success(null))
            return findTagUseCase
        }

        private fun viewModel(
            id: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            findTagUseCase: FindTagUseCase = emptyFindTagUseCase(),
            updateTagUseCase: UpdateTagUseCase = mockk(relaxed = true),
            finishTagUseCase: FinishTagUseCase = mockk(relaxed = true),
            restartTagUseCase: RestartTagUseCase = mockk(relaxed = true),
            deleteTagUseCase: DeleteTagUseCase = mockk(relaxed = true),
        ): TagDetailViewModel =
            TagDetailViewModel(
                id = id,
                findTagUseCase = findTagUseCase,
                updateTagUseCase = updateTagUseCase,
                finishTagUseCase = finishTagUseCase,
                restartTagUseCase = restartTagUseCase,
                deleteTagUseCase = deleteTagUseCase,
            )
    }
}
