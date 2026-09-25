@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.memo.finished

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedTagMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class TagMemoFinishedListViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DATA-001 태그별 완료 메모 paging에 주입된 태그 ID를 전달한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo()
                val pageFinishedTagMemoUseCase = mockk<PageFinishedTagMemoUseCase>()
                every { pageFinishedTagMemoUseCase(parameter = PageFinishedTagMemoUseCase.Parameter(tagId = tagId, sort = ListSort.DEFAULT)) } returns
                    flowOf(Result.success(PagingData.from(listOf(memo))))
                val viewModel = viewModel(tagId = tagId, pageFinishedTagMemoUseCase = pageFinishedTagMemoUseCase)

                val itemList = flowOf(viewModel.memoPagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList.filterIsInstance<MemoListItem.Content>().map { it.memo } shouldBe listOf(memo)
                verify(exactly = 1) { pageFinishedTagMemoUseCase(parameter = PageFinishedTagMemoUseCase.Parameter(tagId = tagId, sort = ListSort.DEFAULT)) }
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-FEATURE-003 조회한 태그의 이모지와 제목을 상단 바 제목으로 노출한다") {
            listOf(
                tagDetail(emoji = TAG_EMOJI, title = TAG_TITLE) to "$TAG_EMOJI $TAG_TITLE",
                tagDetail(emoji = "", title = TAG_TITLE) to TAG_TITLE,
            ).forEach { (detail, expectedTitle) ->
                runTest(mainDispatcher) {
                    val tag = tag(isFinished = false, isDeleted = false).copy(detail = detail)
                    val findTagUseCase = mockk<FindTagUseCase>()
                    every { findTagUseCase(parameter = tag.id) } returns flowOf(Result.success(tag))
                    val viewModel = viewModel(tagId = tag.id, findTagUseCase = findTagUseCase)

                    viewModel.uiState.test {
                        awaitItem() shouldBe TagMemoFinishedListUiState()
                        advanceUntilIdle()
                        awaitItem().title shouldBe expectedTitle
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-FEATURE-004 태그를 조회할 수 없거나 조회에 실패하면 상단 바 태그 표시를 비운다") {
            listOf<Result<Tag?>>(
                Result.success(null),
                Result.failure(IllegalStateException()),
            ).forEach { result ->
                runTest(mainDispatcher) {
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val findTagUseCase = mockk<FindTagUseCase>()
                    every { findTagUseCase(parameter = tagId) } returns flowOf(result)
                    val viewModel = viewModel(tagId = tagId, findTagUseCase = findTagUseCase)

                    viewModel.uiState.test {
                        awaitItem() shouldBe TagMemoFinishedListUiState()
                        advanceUntilIdle()
                        expectNoEvents()
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-FEATURE-005 태그의 저장 이모지나 제목이 바뀌면 상단 바 표시를 갱신한다") {
            runTest(mainDispatcher) {
                val tag =
                    tag(isFinished = false, isDeleted = false)
                        .copy(detail = tagDetail(emoji = TAG_EMOJI, title = TAG_TITLE))
                val tagFlow = MutableStateFlow<Result<Tag?>>(Result.success(tag))
                val findTagUseCase = mockk<FindTagUseCase>()
                every { findTagUseCase(parameter = tag.id) } returns tagFlow
                val viewModel = viewModel(tagId = tag.id, findTagUseCase = findTagUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe TagMemoFinishedListUiState()
                    advanceUntilIdle()
                    awaitItem().title shouldBe "$TAG_EMOJI $TAG_TITLE"

                    tagFlow.value =
                        Result.success(
                            tag.copy(detail = tagDetail(emoji = UPDATED_TAG_EMOJI, title = UPDATED_TAG_TITLE)),
                        )
                    advanceUntilIdle()
                    awaitItem().title shouldBe "$UPDATED_TAG_EMOJI $UPDATED_TAG_TITLE"
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DOMAIN-001 태그의 완료·삭제 상태는 화면 상태에 영향을 주지 않는다") {
            listOf(
                tag(isFinished = false, isDeleted = false),
                tag(isFinished = true, isDeleted = false),
                tag(isFinished = false, isDeleted = true),
            ).forEach { tag ->
                runTest(mainDispatcher) {
                    val findTagUseCase = mockk<FindTagUseCase>()
                    every { findTagUseCase(parameter = tag.id) } returns flowOf(Result.success(tag))
                    val viewModel = viewModel(tagId = tag.id, findTagUseCase = findTagUseCase)

                    viewModel.uiState.test {
                        awaitItem() shouldBe TagMemoFinishedListUiState()
                        advanceUntilIdle()
                        awaitItem() shouldBe TagMemoFinishedListUiState(title = tag.detail.emojiWithTitle)
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-FEATURE-009 태그별 완료 메모 최초 조회가 실패하면 PagingData를 내보내지 않아 초기 빈 목록을 유지한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val pageFinishedTagMemoUseCase = mockk<PageFinishedTagMemoUseCase>()
                every { pageFinishedTagMemoUseCase(parameter = PageFinishedTagMemoUseCase.Parameter(tagId = tagId, sort = ListSort.DEFAULT)) } returns
                    flowOf(Result.failure<PagingData<Memo>>(IllegalStateException()))
                val viewModel = viewModel(tagId = tagId, pageFinishedTagMemoUseCase = pageFinishedTagMemoUseCase)

                viewModel.memoPagingData.test {
                    advanceUntilIdle()
                    expectNoEvents()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-FEATURE-009 태그별 완료 메모 조회가 성공한 뒤 실패하면 마지막 성공 목록을 유지한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo()
                val pageFinishedTagMemoUseCase = mockk<PageFinishedTagMemoUseCase>()
                every { pageFinishedTagMemoUseCase(parameter = PageFinishedTagMemoUseCase.Parameter(tagId = tagId, sort = ListSort.DEFAULT)) } returns
                    flowOf(
                        Result.success(PagingData.from(listOf(memo))),
                        Result.failure(IllegalStateException()),
                    )
                val viewModel = viewModel(tagId = tagId, pageFinishedTagMemoUseCase = pageFinishedTagMemoUseCase)

                viewModel.memoPagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList.filterIsInstance<MemoListItem.Content>().map { it.memo } shouldBe listOf(memo)
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-FEATURE-016 메모 다시 시작에 성공하면 다시 시작 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                coEvery { restartMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(restartMemoUseCase = restartMemoUseCase)

                viewModel.effect.test {
                    viewModel.restart(id = memoId)
                    advanceUntilIdle()

                    awaitItem() shouldBe TagMemoFinishedListEffect.Restarted(id = memoId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { restartMemoUseCase(parameter = memoId) }
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-FEATURE-021 메모 다시 시작 저장에 실패하면 안내 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                coEvery { restartMemoUseCase(any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(restartMemoUseCase = restartMemoUseCase)

                viewModel.effect.test {
                    viewModel.restart(id = memoId)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-FEATURE-018 다시 시작 실행 취소는 완료를 요청하고 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(finishMemoUseCase = finishMemoUseCase)

                viewModel.effect.test {
                    viewModel.finish(id = memoId)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { finishMemoUseCase(parameter = memoId) }
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-FEATURE-017 메모 삭제에 성공하면 삭제 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(deleteMemoUseCase = deleteMemoUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = memoId)
                    advanceUntilIdle()

                    awaitItem() shouldBe TagMemoFinishedListEffect.Deleted(id = memoId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { deleteMemoUseCase(parameter = memoId) }
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-FEATURE-021 메모 삭제 저장에 실패하면 안내 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(deleteMemoUseCase = deleteMemoUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = memoId)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-FEATURE-018 삭제 실행 취소는 복구를 요청하고 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val restoreMemoUseCase = mockk<RestoreMemoUseCase>()
                coEvery { restoreMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(restoreMemoUseCase = restoreMemoUseCase)

                viewModel.effect.test {
                    viewModel.restore(id = memoId)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { restoreMemoUseCase(parameter = memoId) }
            }
        }
    }

    private fun viewModel(
        tagId: Uuid = fixtureMonkey.giveMeOne(),
        pageFinishedTagMemoUseCase: PageFinishedTagMemoUseCase =
            mockk {
                every { this@mockk(parameter = PageFinishedTagMemoUseCase.Parameter(tagId = tagId, sort = ListSort.DEFAULT)) } returns emptyFlow()
            },
        findTagUseCase: FindTagUseCase =
            mockk {
                every { this@mockk(parameter = tagId) } returns flowOf(Result.success(null))
            },
        restartMemoUseCase: RestartMemoUseCase = mockk(),
        finishMemoUseCase: FinishMemoUseCase = mockk(),
        deleteMemoUseCase: DeleteMemoUseCase = mockk(),
        restoreMemoUseCase: RestoreMemoUseCase = mockk(),
    ): TagMemoFinishedListViewModel =
        TagMemoFinishedListViewModel(
            tagId = tagId,
            pageFinishedTagMemoUseCase = pageFinishedTagMemoUseCase,
            findTagUseCase = findTagUseCase,
            restartMemoUseCase = restartMemoUseCase,
            finishMemoUseCase = finishMemoUseCase,
            deleteMemoUseCase = deleteMemoUseCase,
            restoreMemoUseCase = restoreMemoUseCase,
        )

    private fun tagDetail(
        emoji: String,
        title: String,
    ): TagDetail = fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = emoji, title = title)

    private fun tag(
        isFinished: Boolean,
        isDeleted: Boolean,
    ): Tag =
        fixtureMonkey
            .giveMeKotlinBuilder<Tag>()
            .setExp(Tag::isFinished, isFinished)
            .setExp(Tag::isDeleted, isDeleted)
            .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
            .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
            .sample()

    private fun memo(): Memo =
        fixtureMonkey
            .giveMeKotlinBuilder<Memo>()
            .setExp(
                Memo::detail,
                fixtureMonkey.giveMeOne<MemoDetail>().copy(dateTime = null),
            ).setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
            .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
            .sample()

    private companion object {
        const val TAG_EMOJI = "📌"
        const val TAG_TITLE = "TagTitle"
        const val UPDATED_TAG_EMOJI = "📎"
        const val UPDATED_TAG_TITLE = "UpdatedTagTitle"
    }
}
