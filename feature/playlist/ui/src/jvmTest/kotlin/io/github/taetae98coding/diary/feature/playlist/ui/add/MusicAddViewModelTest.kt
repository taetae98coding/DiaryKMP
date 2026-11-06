@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui.add

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.core.testing.playlist.musicDetail
import io.github.taetae98coding.diary.core.testing.playlist.youtubeVideo
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkBlankException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkNotYoutubeException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicTitleBlankException
import io.github.taetae98coding.diary.domain.playlist.usecase.AddMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FetchYoutubeVideoUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
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

        test("TC-ec339b8e-d5e1-41a0-9f64-bb21cdb4675a 추가 처리 중 전달된 추가 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val firstDetail = Arb.musicDetail().next()
                val secondDetail = Arb.musicDetail().next()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(addMusicUseCase = useCase)

                viewModel.add(firstDetail, tagIdSet = emptySet())
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                viewModel.add(secondDetail, tagIdSet = emptySet())
                runCurrent()

                coVerify(exactly = 1) { useCase(AddMusicUseCase.Parameter(detail = firstDetail, tagIdSet = emptySet())) }
                coVerify(exactly = 0) { useCase(AddMusicUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) }

                completion.complete(Result.success(Arb.kotlinUuid().next()))
                advanceUntilIdle()

                viewModel.add(secondDetail, tagIdSet = emptySet())
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(AddMusicUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) }
            }
        }

        test("TC-4e3078a1-33db-496b-acbe-4605d36cde4f 추가에 성공하면 성공 Effect를 한 번 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = Arb.musicDetail().next()
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(any()) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = viewModel(addMusicUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail, tagIdSet = emptySet())
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
            test("TC-054310e3-a1cf-42f4-8119-3c37ef3f751e 성립하지 않는 입력으로 추가하면 ${expected::class.simpleName} Effect를 보내고 진행 상태를 해제한다") {
                runTest(mainDispatcher) {
                    val detail = Arb.musicDetail().next()
                    val useCase = mockk<AddMusicUseCase>()
                    coEvery { useCase(any()) } returns Result.failure(throwable)
                    val viewModel = viewModel(addMusicUseCase = useCase)

                    viewModel.effect.test {
                        viewModel.add(detail, tagIdSet = emptySet())
                        advanceUntilIdle()

                        awaitItem() shouldBe expected
                        expectNoEvents()
                    }

                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()
                }
            }
        }

        test("TC-5b5aa3a7-7e8a-4654-b8cd-51779e37bbc2 기기 저장에 실패하면 Effect 없이 진행 상태만 해제하고 같은 내용으로 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val detail = MusicDetail(title = "title-${Arb.string().next()}", artist = "artist-${Arb.string().next()}", link = YOUTUBE_LINK)
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(addMusicUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail, tagIdSet = emptySet())
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()

                    viewModel.add(detail, tagIdSet = emptySet())
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 2) { useCase(AddMusicUseCase.Parameter(detail = detail, tagIdSet = emptySet())) }
            }
        }

        test("TC-d238d5f0-3c05-415c-9fe4-b902861ca981 추가를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val detail = Arb.musicDetail().next()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(addMusicUseCase = useCase)

                viewModel.add(detail, tagIdSet = emptySet())
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                completion.complete(Result.success(Arb.kotlinUuid().next()))
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-22b0c5f3-6b9d-402d-a599-8140be1226e2 고른 태그를 새 곡과 함께 추가하도록 넘긴다") {
            runTest(mainDispatcher) {
                val detail = Arb.musicDetail().next()
                val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(any()) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = viewModel(addMusicUseCase = useCase)

                viewModel.add(detail = detail, tagIdSet = tagIdSet)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(AddMusicUseCase.Parameter(detail = detail, tagIdSet = tagIdSet)) }
            }
        }

        test("추가가 취소되면 진행 상태를 해제하고 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val firstDetail = Arb.musicDetail().next()
                val secondDetail = Arb.musicDetail().next()
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(AddMusicUseCase.Parameter(detail = firstDetail, tagIdSet = emptySet())) } throws CancellationException()
                coEvery { useCase(AddMusicUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) } returns
                    Result.success(Arb.kotlinUuid().next())
                val viewModel = viewModel(addMusicUseCase = useCase)

                viewModel.add(firstDetail, tagIdSet = emptySet())
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()

                viewModel.add(secondDetail, tagIdSet = emptySet())
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(AddMusicUseCase.Parameter(detail = firstDetail, tagIdSet = emptySet())) }
                coVerify(exactly = 1) { useCase(AddMusicUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) }
            }
        }

        test("TC-a4483895-1bff-43b1-98af-3787cc0ca408 불러오기에 성공하면 영상 제목과 채널 이름을 담은 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val video = Arb.youtubeVideo().next()
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
            test("TC-0b094f6b-7d0a-4647-93bb-40303a7f2b1f 링크가 성립하지 않으면 ${expected::class.simpleName} Effect를 보낸다") {
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

        test("TC-16709a4a-2bf6-4296-83c6-d143b4b60fa7 TC-c5c2a1a7-9368-4c94-90a5-8f64decd79c8 영상 정보를 가져오지 못하면 불러오기 실패 Effect를 보낸다") {
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

        test("TC-56ed0cae-fbed-437c-9328-febed1ba5ddb 불러오기를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<YoutubeVideo>>()
                val useCase = mockk<FetchYoutubeVideoUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(fetchYoutubeVideoUseCase = useCase)

                viewModel.fetchLink(YOUTUBE_LINK)
                runCurrent()

                viewModel.uiState.value.isLinkFetchInProgress
                    .shouldBeTrue()

                completion.complete(Result.success(Arb.youtubeVideo().next()))
                advanceUntilIdle()

                viewModel.uiState.value.isLinkFetchInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-7e835b40-a616-4b2e-a3f5-744bba933477 불러오기 처리 중 전달된 불러오기 요청은 처리하지 않는다") {
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

                completion.complete(Result.success(Arb.youtubeVideo().next()))
                advanceUntilIdle()

                viewModel.fetchLink(YOUTUBE_LINK)
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(YOUTUBE_LINK) }
            }
        }

        test("TC-4cc4744d-8904-458b-a533-3105fe3c1743 곡 추가에 성공하면 앞선 불러오기의 결과를 전달하지 않는다") {
            runTest(mainDispatcher) {
                val fetchCompletion = CompletableDeferred<Result<YoutubeVideo>>()
                val fetchUseCase = mockk<FetchYoutubeVideoUseCase>()
                coEvery { fetchUseCase(any()) } coAnswers { fetchCompletion.await() }
                val addUseCase = mockk<AddMusicUseCase>()
                coEvery { addUseCase(any()) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel =
                    viewModel(
                        addMusicUseCase = addUseCase,
                        fetchYoutubeVideoUseCase = fetchUseCase,
                    )

                viewModel.effect.test {
                    viewModel.fetchLink(YOUTUBE_LINK)
                    runCurrent()

                    viewModel.add(Arb.musicDetail().next(), tagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe MusicAddEffect.AddSucceeded

                    fetchCompletion.complete(Result.success(Arb.youtubeVideo().next()))
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }
    }

    private companion object {
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
