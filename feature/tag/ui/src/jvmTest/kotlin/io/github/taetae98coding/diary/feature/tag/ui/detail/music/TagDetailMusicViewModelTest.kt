@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.music

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.music.MusicListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.testing.playlist.music
import io.github.taetae98coding.diary.domain.playlist.usecase.DeleteMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.PageTagMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.RestoreMusicUseCase
import io.github.taetae98coding.diary.feature.tag.ui.appendFailingPagingData
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
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

class TagDetailMusicViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-b69fefe3-176e-4958-bd02-f98ca8e1894b 상세 대상 태그로 조회한 곡 목록을 순서 그대로 전달한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(3) { item() }
                val page = mockk<PageTagMusicUseCase>()
                every { page(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns flowOf(Result.success(PagingData.from(itemList)))
                val viewModel = TagDetailMusicViewModel(tagId = tagId, pageMusicUseCase = page, deleteMusicUseCase = mockk(), restoreMusicUseCase = mockk())

                viewModel.musicPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { page(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
            }
        }

        test("TC-0bbaa7d1-74a9-48d7-92d6-b49ed5c2e162 상세 대상 태그로만 조회한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val otherTagId = Arb.kotlinUuid().next()
                val page = mockk<PageTagMusicUseCase>()
                every { page(parameter = any()) } returns flowOf(Result.success(PagingData.from(emptyList<Music>())))
                val viewModel = TagDetailMusicViewModel(tagId = tagId, pageMusicUseCase = page, deleteMusicUseCase = mockk(), restoreMusicUseCase = mockk())

                viewModel.musicPagingData.test {
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { page(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
                verify(exactly = 0) { page(parameter = PageTagMusicUseCase.Parameter(tagId = otherTagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
            }
        }

        test("TC-6e914ebb-0357-4604-87f2-6dc4438a9e03 표시 범위를 넓혔다 되돌리면 곡 목록을 각 범위 기준으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val selfList = List(2) { item() }
                val childList = selfList + List(2) { item() }
                val page = mockk<PageTagMusicUseCase>()
                every { page(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns flowOf(Result.success(PagingData.from(selfList)))
                every { page(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = TagScope.CHILD, sort = ListSort.TITLE)) } returns flowOf(Result.success(PagingData.from(childList)))
                val viewModel = TagDetailMusicViewModel(tagId = tagId, pageMusicUseCase = page, deleteMusicUseCase = mockk(), restoreMusicUseCase = mockk())

                viewModel.musicPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe selfList

                    viewModel.select(scope = TagScope.CHILD)
                    flowOf(awaitItem()).asSnapshot() shouldBe childList

                    viewModel.select(scope = TagScope.SELF)
                    flowOf(awaitItem()).asSnapshot() shouldBe selfList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("정렬을 바꾸면 고른 기준으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val defaultList = List(2) { item() }
                val changedList = defaultList.reversed()
                val page = mockk<PageTagMusicUseCase>()
                every { page(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns flowOf(Result.success(PagingData.from(defaultList)))
                every { page(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.RECENTLY_UPDATED)) } returns flowOf(Result.success(PagingData.from(changedList)))
                val viewModel = TagDetailMusicViewModel(tagId = tagId, pageMusicUseCase = page, deleteMusicUseCase = mockk(), restoreMusicUseCase = mockk())

                viewModel.musicPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe defaultList

                    viewModel.select(sort = ListSort.RECENTLY_UPDATED)
                    flowOf(awaitItem()).asSnapshot() shouldBe changedList
                    cancelAndIgnoreRemainingEvents()
                }
                viewModel.sortUiState.value.sort shouldBe ListSort.RECENTLY_UPDATED
            }
        }

        test("TC-452fda2a-9823-41b0-9bb4-a49e0920b026 최초 조회가 실패하면 조회가 끝난 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val page = mockk<PageTagMusicUseCase>()
                every { page(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns flowOf(Result.failure(IllegalStateException(Arb.string().next())))
                val viewModel = TagDetailMusicViewModel(tagId = tagId, pageMusicUseCase = page, deleteMusicUseCase = mockk(), restoreMusicUseCase = mockk())

                viewModel.musicPagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList.shouldBeEmpty()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-452fda2a-9823-41b0-9bb4-a49e0920b026 이어서 불러오기에 실패해도 이미 불러온 곡을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(2) { item() }
                val page = mockk<PageTagMusicUseCase>()
                every { page(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns
                    appendFailingPagingData(firstPage = itemList).map { pagingData -> Result.success(pagingData) }
                val viewModel = TagDetailMusicViewModel(tagId = tagId, pageMusicUseCase = page, deleteMusicUseCase = mockk(), restoreMusicUseCase = mockk())

                val snapshot =
                    flowOf(viewModel.musicPagingData.first()).asSnapshot(onError = { ErrorRecovery.RETURN_CURRENT_SNAPSHOT }) {
                        appendScrollWhile { true }
                    }

                snapshot shouldBe itemList
            }
        }

        test("TC-452fda2a-9823-41b0-9bb4-a49e0920b026 조회가 성공한 뒤 다시 조회하다 실패하면 이전 곡을 남기지 않고 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(2) { item() }
                val page = mockk<PageTagMusicUseCase>()
                val resultFlow = MutableStateFlow<Result<PagingData<Music>>>(Result.success(PagingData.from(itemList)))
                every { page(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns resultFlow
                val viewModel = TagDetailMusicViewModel(tagId = tagId, pageMusicUseCase = page, deleteMusicUseCase = mockk(), restoreMusicUseCase = mockk())

                viewModel.musicPagingData.test {
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList

                    resultFlow.value = Result.failure(IllegalStateException(Arb.string().next()))
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    expectNoEvents()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("삭제를 처리하는 동안 같은 곡의 삭제를 다시 요청해도 삭제 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val delete = mockk<DeleteMusicUseCase>()
                coEvery { delete(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = TagDetailMusicViewModel(tagId = Arb.kotlinUuid().next(), pageMusicUseCase = mockk(), deleteMusicUseCase = delete, restoreMusicUseCase = mockk())

                viewModel.delete(id = id)
                viewModel.delete(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { delete(parameter = id) }
            }
        }

        test("복구를 처리하는 동안 같은 곡의 복구를 다시 요청해도 복구 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val restore = mockk<RestoreMusicUseCase>()
                coEvery { restore(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = TagDetailMusicViewModel(tagId = Arb.kotlinUuid().next(), pageMusicUseCase = mockk(), deleteMusicUseCase = mockk(), restoreMusicUseCase = restore)

                viewModel.restore(id = id)
                viewModel.restore(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { restore(parameter = id) }
            }
        }

        test("TC-13f5cb56-189d-4536-9d2b-339c2028be0d 삭제하면 태그 연결 해제가 아니라 그 곡의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val delete = mockk<DeleteMusicUseCase>()
                coEvery { delete(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailMusicViewModel(tagId = Arb.kotlinUuid().next(), pageMusicUseCase = mockk(), deleteMusicUseCase = delete, restoreMusicUseCase = mockk())

                viewModel.effect.test {
                    viewModel.delete(id = id)

                    awaitItem() shouldBe MusicListEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { delete(parameter = id) }
            }
        }

        test("TC-3722fea1-e98b-40e8-a05d-de33869bb7f2 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val delete = mockk<DeleteMusicUseCase>()
                coEvery { delete(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = TagDetailMusicViewModel(tagId = Arb.kotlinUuid().next(), pageMusicUseCase = mockk(), deleteMusicUseCase = delete, restoreMusicUseCase = mockk())

                viewModel.effect.test {
                    viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-a77376f1-8465-4033-8404-0312b6b8548a 실행 취소하면 그 곡의 삭제를 되돌리는 요청을 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val restore = mockk<RestoreMusicUseCase>()
                coEvery { restore(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailMusicViewModel(tagId = Arb.kotlinUuid().next(), pageMusicUseCase = mockk(), deleteMusicUseCase = mockk(), restoreMusicUseCase = restore)

                viewModel.restore(id = id)
                advanceUntilIdle()

                coVerify(exactly = 1) { restore(parameter = id) }
            }
        }
    }

    private companion object {
        private fun item(): Music =
            Arb
                .music()
                .next()
                .copy(
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )
    }
}
