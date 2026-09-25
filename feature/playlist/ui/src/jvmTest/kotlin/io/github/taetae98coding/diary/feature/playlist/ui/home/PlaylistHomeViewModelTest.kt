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
import io.github.taetae98coding.diary.domain.playlist.usecase.PageMusicUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(pageMusicUseCase: PageMusicUseCase): PlaylistHomeViewModel = PlaylistHomeViewModel(pageMusicUseCase = pageMusicUseCase)

        private fun pageMusicUseCase(musicListFlow: Flow<Result<List<Music>>>): PageMusicUseCase {
            val pageMusicUseCase = mockk<PageMusicUseCase>()
            every { pageMusicUseCase(parameter = ListSort.TITLE) } returns
                musicListFlow.map { result -> result.map { musicList -> PagingData.from(musicList) } }

            return pageMusicUseCase
        }

        // FixtureMonkey가 Instant를 생성하지 못하므로 곡은 직접 만든다.
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
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            )
        }
    }
}
