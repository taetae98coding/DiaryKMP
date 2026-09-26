@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.memo

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageTagMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
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

class TagDetailMemoViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("태그별 메모 paging에 주입된 태그 ID를 전달한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo()
                val pageTagMemoUseCase = mockk<PageTagMemoUseCase>()
                every { pageTagMemoUseCase(parameter = PageTagMemoUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.DEFAULT)) } returns
                    flowOf(Result.success(PagingData.from(listOf(memo))))
                val viewModel = viewModel(tagId = tagId, pageTagMemoUseCase = pageTagMemoUseCase)

                val itemList = flowOf(viewModel.memoPagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList.filterIsInstance<MemoListItem.Content>().map { it.memo } shouldBe listOf(memo)
                verify(exactly = 1) { pageTagMemoUseCase(parameter = PageTagMemoUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.DEFAULT)) }
            }
        }

        test("TC-TAG-DETAIL-DATA-005 표시 범위를 넓혔다 되돌리면 메모 목록을 각 범위 기준으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val selfMemoList = List(2) { memo() }
                val childMemoList = selfMemoList + List(2) { memo() }
                val pageTagMemoUseCase = mockk<PageTagMemoUseCase>()
                every { pageTagMemoUseCase(parameter = PageTagMemoUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.DEFAULT)) } returns
                    flowOf(Result.success(PagingData.from(selfMemoList)))
                every { pageTagMemoUseCase(parameter = PageTagMemoUseCase.Parameter(tagId = tagId, scope = TagScope.CHILD, sort = ListSort.DEFAULT)) } returns
                    flowOf(Result.success(PagingData.from(childMemoList)))
                val viewModel = viewModel(tagId = tagId, pageTagMemoUseCase = pageTagMemoUseCase)

                viewModel.memoPagingData.test {
                    flowOf(awaitItem()).asSnapshot().memoList() shouldContainExactlyInAnyOrder selfMemoList

                    viewModel.select(scope = TagScope.CHILD)
                    flowOf(awaitItem()).asSnapshot().memoList() shouldContainExactlyInAnyOrder childMemoList

                    viewModel.select(scope = TagScope.SELF)
                    flowOf(awaitItem()).asSnapshot().memoList() shouldContainExactlyInAnyOrder selfMemoList
                    cancelAndIgnoreRemainingEvents()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }
        test("TC-TAG-DETAIL-DATA-004 표시 범위를 바꿔도 저장소에 기록하지 않는다") {
            runTest(mainDispatcher) {
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                val restoreMemoUseCase = mockk<RestoreMemoUseCase>()
                val pageTagMemoUseCase = mockk<PageTagMemoUseCase>()
                every { pageTagMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(emptyList<Memo>())))
                val viewModel =
                    viewModel(
                        pageTagMemoUseCase = pageTagMemoUseCase,
                        finishMemoUseCase = finishMemoUseCase,
                        restartMemoUseCase = restartMemoUseCase,
                        deleteMemoUseCase = deleteMemoUseCase,
                        restoreMemoUseCase = restoreMemoUseCase,
                    )

                viewModel.memoPagingData.test {
                    awaitItem()
                    viewModel.select(scope = TagScope.DESCENDANT)
                    advanceUntilIdle()
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                // 표시 범위는 조회 기준만 바꾸므로 저장을 맡는 UseCase는 하나도 호출되지 않는다.
                coVerify(exactly = 0) { finishMemoUseCase(parameter = any()) }
                coVerify(exactly = 0) { restartMemoUseCase(parameter = any()) }
                coVerify(exactly = 0) { deleteMemoUseCase(parameter = any()) }
                coVerify(exactly = 0) { restoreMemoUseCase(parameter = any()) }
            }
        }

        test("TC-TAG-DETAIL-MEMO-FEATURE-006 태그별 메모 최초 조회가 실패하면 조회가 끝난 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val pageTagMemoUseCase = mockk<PageTagMemoUseCase>()
                every { pageTagMemoUseCase(parameter = PageTagMemoUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.DEFAULT)) } returns
                    flowOf(Result.failure<PagingData<Memo>>(IllegalStateException()))
                val viewModel = viewModel(tagId = tagId, pageTagMemoUseCase = pageTagMemoUseCase)

                viewModel.memoPagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList shouldBe emptyList()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-TAG-DETAIL-MEMO-FEATURE-006 태그별 메모 조회가 성공한 뒤 실패하면 마지막 성공 목록을 유지한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo()
                val pageTagMemoUseCase = mockk<PageTagMemoUseCase>()
                every { pageTagMemoUseCase(parameter = PageTagMemoUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.DEFAULT)) } returns
                    flowOf(
                        Result.success(PagingData.from(listOf(memo))),
                        Result.failure(IllegalStateException()),
                    )
                val viewModel = viewModel(tagId = tagId, pageTagMemoUseCase = pageTagMemoUseCase)

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

        test("메모 완료에 성공하면 완료 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(finishMemoUseCase = finishMemoUseCase)

                viewModel.effect.test {
                    viewModel.finish(id = memoId)
                    advanceUntilIdle()

                    awaitItem() shouldBe MemoListEffect.Finished(id = memoId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { finishMemoUseCase(parameter = memoId) }
            }
        }

        test("메모 완료에 실패하면 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                coEvery { finishMemoUseCase(any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(finishMemoUseCase = finishMemoUseCase)

                viewModel.effect.test {
                    viewModel.finish(id = memoId)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("완료 실행 취소는 다시 시작을 요청하고 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                coEvery { restartMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(restartMemoUseCase = restartMemoUseCase)

                viewModel.effect.test {
                    viewModel.restart(id = memoId)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { restartMemoUseCase(parameter = memoId) }
            }
        }

        test("메모 삭제에 성공하면 삭제 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(deleteMemoUseCase = deleteMemoUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = memoId)
                    advanceUntilIdle()

                    awaitItem() shouldBe MemoListEffect.Deleted(id = memoId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { deleteMemoUseCase(parameter = memoId) }
            }
        }

        test("메모 삭제에 실패하면 Effect를 보내지 않는다") {
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

        test("삭제 실행 취소는 복구를 요청하고 Effect를 보내지 않는다") {
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
        pageTagMemoUseCase: PageTagMemoUseCase =
            mockk {
                every { this@mockk(parameter = PageTagMemoUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.DEFAULT)) } returns emptyFlow()
            },
        finishMemoUseCase: FinishMemoUseCase = mockk(),
        restartMemoUseCase: RestartMemoUseCase = mockk(),
        deleteMemoUseCase: DeleteMemoUseCase = mockk(),
        restoreMemoUseCase: RestoreMemoUseCase = mockk(),
    ): TagDetailMemoViewModel =
        TagDetailMemoViewModel(
            tagId = tagId,
            pageTagMemoUseCase = pageTagMemoUseCase,
            finishMemoUseCase = finishMemoUseCase,
            restartMemoUseCase = restartMemoUseCase,
            deleteMemoUseCase = deleteMemoUseCase,
            restoreMemoUseCase = restoreMemoUseCase,
        )

    private fun List<MemoListItem>.memoList(): List<Memo> = filterIsInstance<MemoListItem.Content>().map { item -> item.memo }

    private fun memo(): Memo =
        fixtureMonkey
            .giveMeKotlinBuilder<Memo>()
            .setExp(
                Memo::detail,
                fixtureMonkey.giveMeOne<MemoDetail>().copy(dateTime = null),
            ).setExp(Memo::updatedAt, fixtureMonkey.giveMeOne<Instant>())
            .setExp(Memo::createdAt, fixtureMonkey.giveMeOne<Instant>())
            .sample()
}
