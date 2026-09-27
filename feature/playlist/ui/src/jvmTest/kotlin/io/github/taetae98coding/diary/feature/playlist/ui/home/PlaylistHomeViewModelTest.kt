@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.domain.playlist.usecase.DeleteMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.GetMusicDownloadEventUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.GetMusicDownloadStateUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.PageMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.RequestMusicDownloadUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.RestoreMusicUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class PlaylistHomeViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-PLAYLIST-HOME-FEATURE-004 조회한 곡 페이지를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val musicList = listOf(music(title = "Alpha"), music(title = "Bravo"))
                val viewModel = viewModel(pageMusicUseCase = pageMusicUseCase(musicListFlow = flowOf(Result.success(musicList))))

                flowOf(viewModel.musicPagingData.first()).asSnapshot() shouldBe musicList
            }
        }

        test("TC-PLAYLIST-HOME-FEATURE-021 곡 페이지 조회에 실패하면 빈 페이지를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        pageMusicUseCase = pageMusicUseCase(musicListFlow = flowOf(Result.failure(IllegalStateException()))),
                    )

                flowOf(viewModel.musicPagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-PLAYLIST-HOME-DATA-002 저장된 곡이 바뀌면 바뀐 페이지를 노출한다") {
            runTest(mainDispatcher) {
                val beforeMusicList = listOf(music(title = "Alpha"))
                val afterMusicList = listOf(music(title = "Alpha"), music(title = "Bravo"))
                val musicListFlow = MutableStateFlow(Result.success(beforeMusicList))
                val viewModel = viewModel(pageMusicUseCase = pageMusicUseCase(musicListFlow = musicListFlow))

                viewModel.musicPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe beforeMusicList

                    musicListFlow.value = Result.success(afterMusicList)

                    flowOf(awaitItem()).asSnapshot() shouldBe afterMusicList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-PLAYLIST-HOME-FEATURE-011 곡을 추가하면 별도 조작 없이 그 곡이 든 목록을 노출한다") {
            runTest(mainDispatcher) {
                val existing = music(title = "existing" + fixtureMonkey.giveMeOne<Int>().toString().filter(Char::isDigit))
                val added = music(title = "added" + fixtureMonkey.giveMeOne<Int>().toString().filter(Char::isDigit))
                val musicListFlow = MutableStateFlow(Result.success(listOf(existing)))
                val viewModel = viewModel(pageMusicUseCase = pageMusicUseCase(musicListFlow = musicListFlow))

                viewModel.musicPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe listOf(existing)

                    musicListFlow.value = Result.success(listOf(existing, added))

                    flowOf(awaitItem()).asSnapshot() shouldBe listOf(existing, added)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-PLAYLIST-HOME-DOMAIN-003 처음 정렬은 제목순이다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(pageMusicUseCase = pageMusicUseCase(musicListFlow = flowOf(Result.success(emptyList()))))

                viewModel.sort.value shouldBe ListSort.TITLE
            }
        }

        test("정렬을 바꾸면 그 정렬로 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val titleMusicList = listOf(music(title = "Alpha"), music(title = "Bravo"))
                val recentlyUpdatedMusicList = listOf(music(title = "Bravo"), music(title = "Alpha"))
                val pageMusicUseCase = mockk<PageMusicUseCase>()
                every { pageMusicUseCase(parameter = ListSort.TITLE) } returns flowOf(Result.success(PagingData.from(titleMusicList)))
                every { pageMusicUseCase(parameter = ListSort.RECENTLY_UPDATED) } returns
                    flowOf(Result.success(PagingData.from(recentlyUpdatedMusicList)))
                val viewModel = viewModel(pageMusicUseCase = pageMusicUseCase)

                viewModel.musicPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe titleMusicList

                    viewModel.select(sort = ListSort.RECENTLY_UPDATED)

                    flowOf(awaitItem()).asSnapshot() shouldBe recentlyUpdatedMusicList
                    viewModel.sort.value shouldBe ListSort.RECENTLY_UPDATED
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-PLAYLIST-HOME-FEATURE-022 삭제에 성공하면 그 곡의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteMusicUseCase = mockk<DeleteMusicUseCase>()
                coEvery { deleteMusicUseCase(parameter = id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        pageMusicUseCase = pageMusicUseCase(musicListFlow = flowOf(Result.success(emptyList()))),
                        deleteMusicUseCase = deleteMusicUseCase,
                    )

                viewModel.effect.test {
                    viewModel.delete(id = id)

                    awaitItem() shouldBe PlaylistHomeEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { deleteMusicUseCase(parameter = id) }
            }
        }

        test("TC-PLAYLIST-HOME-FEATURE-028 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteMusicUseCase = mockk<DeleteMusicUseCase>()
                coEvery { deleteMusicUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel =
                    viewModel(
                        pageMusicUseCase = pageMusicUseCase(musicListFlow = flowOf(Result.success(emptyList()))),
                        deleteMusicUseCase = deleteMusicUseCase,
                    )

                viewModel.effect.test {
                    viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-PLAYLIST-HOME-DOMAIN-007 실행 취소를 저장하지 못하면 별도 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restoreMusicUseCase = mockk<RestoreMusicUseCase>()
                coEvery { restoreMusicUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel =
                    viewModel(
                        pageMusicUseCase = pageMusicUseCase(musicListFlow = flowOf(Result.success(emptyList()))),
                        restoreMusicUseCase = restoreMusicUseCase,
                    )

                viewModel.effect.test {
                    viewModel.restore(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { restoreMusicUseCase(parameter = id) }
            }
        }

        test("TC-PLAYLIST-HOME-DOMAIN-008 목록에서 삭제하고 실행 취소해도 곡의 다운로드 상태가 바뀌지 않고 다운로드를 요청하지 않는다") {
            runTest(mainDispatcher) {
                val music = music(title = "Alpha")
                val stateMap = mapOf(MusicDownloadTarget(id = music.id, videoId = "dQw4w9WgXcQ") to MusicDownloadState.Running(progress = 0.62F))
                val getMusicDownloadStateUseCase = mockk<GetMusicDownloadStateUseCase>()
                every { getMusicDownloadStateUseCase(parameter = Unit) } returns MutableStateFlow(Result.success(stateMap))
                val getMusicDownloadEventUseCase = mockk<GetMusicDownloadEventUseCase>()
                every { getMusicDownloadEventUseCase(parameter = Unit) } returns emptyFlow()
                val requestMusicDownloadUseCase = mockk<RequestMusicDownloadUseCase>()
                val downloadViewModel =
                    PlaylistHomeDownloadViewModel(
                        getMusicDownloadStateUseCase = getMusicDownloadStateUseCase,
                        getMusicDownloadEventUseCase = getMusicDownloadEventUseCase,
                        requestMusicDownloadUseCase = requestMusicDownloadUseCase,
                    )
                val deleteMusicUseCase = mockk<DeleteMusicUseCase>()
                coEvery { deleteMusicUseCase(parameter = music.id) } returns Result.success(1)
                val restoreMusicUseCase = mockk<RestoreMusicUseCase>()
                coEvery { restoreMusicUseCase(parameter = music.id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        pageMusicUseCase = pageMusicUseCase(musicListFlow = flowOf(Result.success(listOf(music)))),
                        deleteMusicUseCase = deleteMusicUseCase,
                        restoreMusicUseCase = restoreMusicUseCase,
                    )

                downloadViewModel.uiState.test {
                    awaitItem()
                    advanceUntilIdle()
                    awaitItem() shouldBe PlaylistHomeDownloadUiState(stateMap = stateMap)

                    viewModel.delete(id = music.id)
                    advanceUntilIdle()
                    viewModel.restore(id = music.id)
                    advanceUntilIdle()

                    expectNoEvents()
                    downloadViewModel.uiState.value shouldBe PlaylistHomeDownloadUiState(stateMap = stateMap)
                }
                coVerify(exactly = 1) { deleteMusicUseCase(parameter = music.id) }
                coVerify(exactly = 1) { restoreMusicUseCase(parameter = music.id) }
                coVerify(exactly = 0) { requestMusicDownloadUseCase(parameter = any()) }
            }
        }

        test("TC-PLAYLIST-HOME-FEATURE-023 실행 취소하면 그 곡의 삭제를 되돌리는 요청을 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restoreMusicUseCase = mockk<RestoreMusicUseCase>()
                coEvery { restoreMusicUseCase(parameter = id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        pageMusicUseCase = pageMusicUseCase(musicListFlow = flowOf(Result.success(emptyList()))),
                        restoreMusicUseCase = restoreMusicUseCase,
                    )

                viewModel.restore(id = id)
                advanceUntilIdle()

                coVerify(exactly = 1) { restoreMusicUseCase(parameter = id) }
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            pageMusicUseCase: PageMusicUseCase,
            deleteMusicUseCase: DeleteMusicUseCase = mockk(),
            restoreMusicUseCase: RestoreMusicUseCase = mockk(),
        ): PlaylistHomeViewModel =
            PlaylistHomeViewModel(
                pageMusicUseCase = pageMusicUseCase,
                deleteMusicUseCase = deleteMusicUseCase,
                restoreMusicUseCase = restoreMusicUseCase,
            )

        private fun pageMusicUseCase(musicListFlow: Flow<Result<List<Music>>>): PageMusicUseCase {
            val pageMusicUseCase = mockk<PageMusicUseCase>()
            every { pageMusicUseCase(parameter = ListSort.TITLE) } returns
                musicListFlow.map { result -> result.map { musicList -> PagingData.from(musicList) } }

            return pageMusicUseCase
        }

        private fun music(title: String): Music {
            val detail =
                fixtureMonkey
                    .giveMeKotlinBuilder<MusicDetail>()
                    .setExp(MusicDetail::title, title)
                    .sample()

            return Music(
                id = Uuid.random(),
                detail = detail,
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                createdAt = fixtureMonkey.giveMeOne<Instant>(),
            )
        }
    }
}
