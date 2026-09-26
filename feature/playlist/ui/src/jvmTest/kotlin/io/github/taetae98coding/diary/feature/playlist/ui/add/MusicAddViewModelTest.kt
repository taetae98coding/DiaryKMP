@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui.add

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkBlankException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkNotYoutubeException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicTitleBlankException
import io.github.taetae98coding.diary.domain.playlist.usecase.AddMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FetchYoutubeVideoUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

private const val YOUTUBE_LINK: String = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

class MusicAddViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MUSIC-ADD-DOMAIN-009 추가 처리 중 전달된 추가 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val firstDetail = fixtureMonkey.giveMeOne<MusicDetail>()
                val secondDetail = fixtureMonkey.giveMeOne<MusicDetail>()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(addMusicUseCase = useCase)

                viewModel.add(firstDetail)
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                viewModel.add(secondDetail)
                runCurrent()

                coVerify(exactly = 1) { useCase(firstDetail) }
                coVerify(exactly = 0) { useCase(secondDetail) }

                completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                advanceUntilIdle()

                viewModel.add(secondDetail)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(secondDetail) }
            }
        }

        test("TC-MUSIC-ADD-FEATURE-005 추가에 성공하면 성공 Effect를 한 번 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<MusicDetail>()
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(any()) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = viewModel(addMusicUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail)
                    advanceUntilIdle()

                    awaitItem() shouldBe MusicAddEffect.AddSucceeded
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        listOf(
            MusicTitleBlankException() to MusicAddEffect.TitleBlank,
            MusicLinkNotYoutubeException() to MusicAddEffect.LinkNotYoutube,
        ).forEach { (throwable, expected) ->
            test("TC-MUSIC-ADD-FEATURE-008 성립하지 않는 입력으로 추가하면 ${expected::class.simpleName} Effect를 보내고 진행 상태를 해제한다") {
                runTest(mainDispatcher) {
                    val detail = fixtureMonkey.giveMeOne<MusicDetail>()
                    val useCase = mockk<AddMusicUseCase>()
                    coEvery { useCase(any()) } returns Result.failure(throwable)
                    val viewModel = viewModel(addMusicUseCase = useCase)

                    viewModel.effect.test {
                        viewModel.add(detail)
                        advanceUntilIdle()

                        awaitItem() shouldBe expected
                        expectNoEvents()
                    }

                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()
                }
            }
        }

        test("TC-MUSIC-ADD-FEATURE-031 기기 저장에 실패하면 Effect 없이 진행 상태만 해제하고 같은 내용으로 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val detail = MusicDetail(title = "title-${fixtureMonkey.giveMeOne<String>()}", artist = "artist-${fixtureMonkey.giveMeOne<String>()}", link = YOUTUBE_LINK)
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(addMusicUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()

                    viewModel.add(detail)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 2) { useCase(detail) }
            }
        }

        test("TC-MUSIC-ADD-FEATURE-007 추가를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<MusicDetail>()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(addMusicUseCase = useCase)

                viewModel.add(detail)
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("추가가 취소되면 진행 상태를 해제하고 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val firstDetail = fixtureMonkey.giveMeOne<MusicDetail>()
                val secondDetail = fixtureMonkey.giveMeOne<MusicDetail>()
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(firstDetail) } throws CancellationException()
                coEvery { useCase(secondDetail) } returns
                    Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = viewModel(addMusicUseCase = useCase)

                viewModel.add(firstDetail)
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()

                viewModel.add(secondDetail)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(firstDetail) }
                coVerify(exactly = 1) { useCase(secondDetail) }
            }
        }

        test("TC-MUSIC-ADD-DATA-008 불러오기에 성공하면 영상 제목과 채널 이름을 담은 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val video = fixtureMonkey.giveMeOne<YoutubeVideo>()
                val useCase = mockk<FetchYoutubeVideoUseCase>()
                coEvery { useCase(YOUTUBE_LINK) } returns Result.success(video)
                val viewModel = viewModel(fetchYoutubeVideoUseCase = useCase)

                viewModel.effect.test {
                    viewModel.fetchLink(YOUTUBE_LINK)
                    advanceUntilIdle()

                    awaitItem() shouldBe
                        MusicAddEffect.LinkFetched(
                            link = YOUTUBE_LINK,
                            title = video.title,
                            artist = video.channelName,
                        )
                    expectNoEvents()
                }

                viewModel.uiState.value.isLinkFetchInProgress
                    .shouldBeFalse()
            }
        }

        listOf(
            MusicLinkBlankException() to MusicAddEffect.LinkBlank,
            MusicLinkNotYoutubeException() to MusicAddEffect.LinkNotYoutube,
        ).forEach { (throwable, expected) ->
            test("TC-MUSIC-ADD-FEATURE-019 링크가 성립하지 않으면 ${expected::class.simpleName} Effect를 보낸다") {
                runTest(mainDispatcher) {
                    val useCase = mockk<FetchYoutubeVideoUseCase>()
                    coEvery { useCase(any()) } returns Result.failure(throwable)
                    val viewModel = viewModel(fetchYoutubeVideoUseCase = useCase)

                    viewModel.effect.test {
                        viewModel.fetchLink(YOUTUBE_LINK)
                        advanceUntilIdle()

                        awaitItem() shouldBe expected
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-MUSIC-ADD-FEATURE-020 TC-MUSIC-ADD-DATA-009 영상 정보를 가져오지 못하면 불러오기 실패 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val useCase = mockk<FetchYoutubeVideoUseCase>()
                coEvery { useCase(any()) } returns Result.failure(IllegalStateException("fetch failed"))
                val viewModel = viewModel(fetchYoutubeVideoUseCase = useCase)

                viewModel.effect.test {
                    viewModel.fetchLink(YOUTUBE_LINK)
                    advanceUntilIdle()

                    awaitItem() shouldBe MusicAddEffect.LinkFetchFailed
                    expectNoEvents()
                }

                viewModel.uiState.value.isLinkFetchInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-MUSIC-ADD-FEATURE-018 불러오기를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<YoutubeVideo>>()
                val useCase = mockk<FetchYoutubeVideoUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(fetchYoutubeVideoUseCase = useCase)

                viewModel.fetchLink(YOUTUBE_LINK)
                runCurrent()

                viewModel.uiState.value.isLinkFetchInProgress
                    .shouldBeTrue()

                completion.complete(Result.success(fixtureMonkey.giveMeOne<YoutubeVideo>()))
                advanceUntilIdle()

                viewModel.uiState.value.isLinkFetchInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-MUSIC-ADD-DOMAIN-012 불러오기 처리 중 전달된 불러오기 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<YoutubeVideo>>()
                val useCase = mockk<FetchYoutubeVideoUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(fetchYoutubeVideoUseCase = useCase)

                viewModel.fetchLink(YOUTUBE_LINK)
                runCurrent()

                viewModel.fetchLink(YOUTUBE_LINK)
                runCurrent()

                coVerify(exactly = 1) { useCase(YOUTUBE_LINK) }

                completion.complete(Result.success(fixtureMonkey.giveMeOne<YoutubeVideo>()))
                advanceUntilIdle()

                viewModel.fetchLink(YOUTUBE_LINK)
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(YOUTUBE_LINK) }
            }
        }

        test("TC-MUSIC-ADD-FEATURE-021 곡 추가에 성공하면 앞선 불러오기의 결과를 전달하지 않는다") {
            runTest(mainDispatcher) {
                val fetchCompletion = CompletableDeferred<Result<YoutubeVideo>>()
                val fetchUseCase = mockk<FetchYoutubeVideoUseCase>()
                coEvery { fetchUseCase(any()) } coAnswers { fetchCompletion.await() }
                val addUseCase = mockk<AddMusicUseCase>()
                coEvery { addUseCase(any()) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel =
                    viewModel(
                        addMusicUseCase = addUseCase,
                        fetchYoutubeVideoUseCase = fetchUseCase,
                    )

                viewModel.effect.test {
                    viewModel.fetchLink(YOUTUBE_LINK)
                    runCurrent()

                    viewModel.add(fixtureMonkey.giveMeOne<MusicDetail>())
                    advanceUntilIdle()

                    awaitItem() shouldBe MusicAddEffect.AddSucceeded

                    fetchCompletion.complete(Result.success(fixtureMonkey.giveMeOne<YoutubeVideo>()))
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            addMusicUseCase: AddMusicUseCase = mockk(relaxed = true),
            fetchYoutubeVideoUseCase: FetchYoutubeVideoUseCase = mockk(relaxed = true),
        ): MusicAddViewModel =
            MusicAddViewModel(
                addMusicUseCase = addMusicUseCase,
                fetchYoutubeVideoUseCase = fetchYoutubeVideoUseCase,
            )
    }
}
