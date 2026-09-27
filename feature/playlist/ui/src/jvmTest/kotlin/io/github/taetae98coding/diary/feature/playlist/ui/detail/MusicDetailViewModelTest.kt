@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui.detail

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkBlankException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkNotYoutubeException
import io.github.taetae98coding.diary.domain.playlist.usecase.DeleteMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FetchYoutubeVideoUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.UpdateMusicUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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

private const val YOUTUBE_LINK: String = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

class MusicDetailViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MUSIC-DETAIL-FEATURE-001 조회에 성공하면 저장된 곡을 내용 표시 상태로 전달한다") {
            runTest(mainDispatcher) {
                val music = music()
                val viewModel = viewModel(id = music.id, music = music)

                viewModel.uiState.test {
                    awaitItem() shouldBe MusicDetailUiState.Loading
                    advanceUntilIdle()

                    awaitItem() shouldBe MusicDetailUiState.Content(id = music.id, detail = music.detail)
                }
            }
        }

        test("TC-MUSIC-DETAIL-FEATURE-002 TC-MUSIC-DETAIL-FEATURE-003 곡을 조회할 수 없으면 조회 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(music = null)

                viewModel.uiState.test {
                    awaitItem() shouldBe MusicDetailUiState.Loading
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-MUSIC-DETAIL-FEATURE-024 대상 곡이 삭제 상태가 되어도 내용 표시 상태를 유지한다") {
            runTest(mainDispatcher) {
                val music = music()
                val musicFlow = MutableStateFlow<Result<Music?>>(Result.success(music))
                val findMusicUseCase = mockk<FindMusicUseCase>()
                every { findMusicUseCase(parameter = music.id) } returns musicFlow
                val viewModel = viewModel(id = music.id, findMusicUseCase = findMusicUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe MusicDetailUiState.Loading
                    advanceUntilIdle()
                    awaitItem() shouldBe MusicDetailUiState.Content(id = music.id, detail = music.detail)

                    musicFlow.value = Result.success(music.copy(isDeleted = true))
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-MUSIC-DETAIL-FEATURE-023 저장된 제목이 바뀌면 내용 표시 상태의 제목이 갱신된다") {
            runTest(mainDispatcher) {
                val music = music()
                val renamed = music.copy(detail = music.detail.copy(title = "renamed-${fixtureMonkey.giveMeOne<String>()}"))
                val musicFlow = MutableStateFlow<Result<Music?>>(Result.success(music))
                val findMusicUseCase = mockk<FindMusicUseCase>()
                every { findMusicUseCase(parameter = music.id) } returns musicFlow
                val viewModel = viewModel(id = music.id, findMusicUseCase = findMusicUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe MusicDetailUiState.Loading
                    advanceUntilIdle()
                    awaitItem() shouldBe MusicDetailUiState.Content(id = music.id, detail = music.detail)

                    musicFlow.value = Result.success(renamed)
                    advanceUntilIdle()

                    awaitItem() shouldBe MusicDetailUiState.Content(id = music.id, detail = renamed.detail)
                }
            }
        }

        test("TC-MUSIC-DETAIL-FEATURE-007 수정에 성공하면 성공 Effect를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val useCase = mockk<UpdateMusicUseCase>()
                coEvery { useCase(any()) } returns Result.success(1)
                val viewModel = viewModel(updateMusicUseCase = useCase)

                viewModel.effect.test {
                    viewModel.update(detail = fixtureMonkey.giveMeOne<MusicDetail>())
                    advanceUntilIdle()

                    awaitItem() shouldBe MusicDetailEffect.UpdateSucceeded
                    expectNoEvents()
                }
            }
        }

        test("TC-MUSIC-DETAIL-FEATURE-010 링크 형식이 성립하지 않으면 링크 형식 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val useCase = mockk<UpdateMusicUseCase>()
                coEvery { useCase(any()) } returns Result.failure(MusicLinkNotYoutubeException())
                val viewModel = viewModel(updateMusicUseCase = useCase)

                viewModel.effect.test {
                    viewModel.update(detail = fixtureMonkey.giveMeOne<MusicDetail>())
                    advanceUntilIdle()

                    awaitItem() shouldBe MusicDetailEffect.LinkNotYoutube
                    expectNoEvents()
                }
            }
        }

        test("TC-MUSIC-DETAIL-FEATURE-008 TC-MUSIC-DETAIL-FEATURE-009 수정을 처리하는 동안 진행 상태를 유지하고 실패해도 해제한다") {
            runTest(mainDispatcher) {
                val music = music()
                val completion = CompletableDeferred<Result<Int>>()
                val useCase = mockk<UpdateMusicUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(id = music.id, music = music, updateMusicUseCase = useCase)

                viewModel.uiState.test {
                    awaitItem()
                    advanceUntilIdle()
                    awaitItem()

                    viewModel.update(detail = fixtureMonkey.giveMeOne<MusicDetail>())
                    runCurrent()

                    awaitItem()
                        .shouldBeInstanceOf<MusicDetailUiState.Content>()
                        .isUpdateInProgress
                        .shouldBeTrue()

                    completion.complete(Result.failure(IllegalStateException("update failed")))
                    advanceUntilIdle()

                    awaitItem()
                        .shouldBeInstanceOf<MusicDetailUiState.Content>()
                        .isUpdateInProgress
                        .shouldBeFalse()
                }
            }
        }

        test("TC-MUSIC-DETAIL-FEATURE-021 삭제에 성공하면 삭제 성공 Effect를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val useCase = mockk<DeleteMusicUseCase>()
                coEvery { useCase(any()) } returns Result.success(1)
                val viewModel = viewModel(deleteMusicUseCase = useCase)

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()

                    awaitItem() shouldBe MusicDetailEffect.DeleteSucceeded
                    expectNoEvents()
                }
            }
        }

        test("TC-MUSIC-DETAIL-FEATURE-020 TC-MUSIC-DETAIL-FEATURE-022 삭제를 처리하는 동안 진행 상태를 유지하고 실패해도 해제한다") {
            runTest(mainDispatcher) {
                val music = music()
                val completion = CompletableDeferred<Result<Int>>()
                val useCase = mockk<DeleteMusicUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(id = music.id, music = music, deleteMusicUseCase = useCase)

                viewModel.uiState.test {
                    awaitItem()
                    advanceUntilIdle()
                    awaitItem()

                    viewModel.delete()
                    runCurrent()

                    awaitItem()
                        .shouldBeInstanceOf<MusicDetailUiState.Content>()
                        .isDeleteInProgress
                        .shouldBeTrue()

                    completion.complete(Result.failure(IllegalStateException("delete failed")))
                    advanceUntilIdle()

                    awaitItem()
                        .shouldBeInstanceOf<MusicDetailUiState.Content>()
                        .isDeleteInProgress
                        .shouldBeFalse()
                }
            }
        }

        test("TC-MUSIC-DETAIL-FEATURE-020 삭제를 처리하는 동안에도 수정과 불러오기를 실행한다") {
            runTest(mainDispatcher) {
                val deleteCompletion = CompletableDeferred<Result<Int>>()
                val deleteMusicUseCase = mockk<DeleteMusicUseCase>()
                coEvery { deleteMusicUseCase(any()) } coAnswers { deleteCompletion.await() }
                val updateMusicUseCase = mockk<UpdateMusicUseCase>()
                coEvery { updateMusicUseCase(any()) } returns Result.success(1)
                val fetchYoutubeVideoUseCase = mockk<FetchYoutubeVideoUseCase>()
                coEvery { fetchYoutubeVideoUseCase(any()) } returns Result.success(fixtureMonkey.giveMeOne<YoutubeVideo>())
                val viewModel =
                    viewModel(
                        updateMusicUseCase = updateMusicUseCase,
                        deleteMusicUseCase = deleteMusicUseCase,
                        fetchYoutubeVideoUseCase = fetchYoutubeVideoUseCase,
                    )
                val detail = fixtureMonkey.giveMeOne<MusicDetail>()

                viewModel.delete()
                runCurrent()
                viewModel.update(detail = detail)
                viewModel.fetchLink(YOUTUBE_LINK)
                advanceUntilIdle()

                coVerify(exactly = 1) { updateMusicUseCase(UpdateMusicUseCase.Parameter(id = DEFAULT_ID, detail = detail)) }
                coVerify(exactly = 1) { fetchYoutubeVideoUseCase(YOUTUBE_LINK) }

                deleteCompletion.complete(Result.success(1))
                advanceUntilIdle()
            }
        }

        test("TC-MUSIC-DETAIL-FEATURE-012 TC-MUSIC-DETAIL-FEATURE-013 다시 불러오기에 성공하면 불러온 값 Effect만 보낸다") {
            runTest(mainDispatcher) {
                val video = fixtureMonkey.giveMeOne<YoutubeVideo>()
                val fetchYoutubeVideoUseCase = mockk<FetchYoutubeVideoUseCase>()
                coEvery { fetchYoutubeVideoUseCase(any()) } returns Result.success(video)
                val updateMusicUseCase = mockk<UpdateMusicUseCase>(relaxed = true)
                val viewModel =
                    viewModel(
                        updateMusicUseCase = updateMusicUseCase,
                        fetchYoutubeVideoUseCase = fetchYoutubeVideoUseCase,
                    )

                viewModel.effect.test {
                    viewModel.fetchLink(YOUTUBE_LINK)
                    advanceUntilIdle()

                    awaitItem() shouldBe
                        MusicDetailEffect.LinkFetched(
                            link = YOUTUBE_LINK,
                            title = video.title,
                            artist = video.channelName,
                        )
                    expectNoEvents()
                }

                coVerify(exactly = 0) { updateMusicUseCase(any()) }
            }
        }

        listOf(
            MusicLinkBlankException() to MusicDetailEffect.LinkBlank,
            MusicLinkNotYoutubeException() to MusicDetailEffect.LinkNotYoutube,
        ).forEach { (throwable, expected) ->
            test("TC-MUSIC-DETAIL-FEATURE-031 링크가 성립하지 않으면 ${expected::class.simpleName} Effect를 보낸다") {
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

        test("TC-MUSIC-DETAIL-FEATURE-032 영상 정보를 가져오지 못하면 불러오기 실패 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val useCase = mockk<FetchYoutubeVideoUseCase>()
                coEvery { useCase(any()) } returns Result.failure(IllegalStateException("fetch failed"))
                val viewModel = viewModel(fetchYoutubeVideoUseCase = useCase)

                viewModel.effect.test {
                    viewModel.fetchLink(YOUTUBE_LINK)
                    advanceUntilIdle()

                    awaitItem() shouldBe MusicDetailEffect.LinkFetchFailed
                    expectNoEvents()
                }
            }
        }

        test("TC-MUSIC-DETAIL-DOMAIN-010 수정 처리 중 전달된 수정 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val firstDetail = fixtureMonkey.giveMeOne<MusicDetail>()
                val secondDetail = fixtureMonkey.giveMeOne<MusicDetail>()
                val completion = CompletableDeferred<Result<Int>>()
                val useCase = mockk<UpdateMusicUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(updateMusicUseCase = useCase)

                viewModel.update(detail = firstDetail)
                runCurrent()
                viewModel.update(detail = secondDetail)
                runCurrent()

                coVerify(exactly = 1) { useCase(UpdateMusicUseCase.Parameter(id = DEFAULT_ID, detail = firstDetail)) }
                coVerify(exactly = 0) { useCase(UpdateMusicUseCase.Parameter(id = DEFAULT_ID, detail = secondDetail)) }

                completion.complete(Result.success(1))
                advanceUntilIdle()

                viewModel.update(detail = secondDetail)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(UpdateMusicUseCase.Parameter(id = DEFAULT_ID, detail = secondDetail)) }
            }
        }

        test("TC-MUSIC-DETAIL-DOMAIN-010 삭제 처리 중 전달된 삭제 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Int>>()
                val useCase = mockk<DeleteMusicUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(deleteMusicUseCase = useCase)

                viewModel.delete()
                runCurrent()
                viewModel.delete()
                runCurrent()

                coVerify(exactly = 1) { useCase(DEFAULT_ID) }

                completion.complete(Result.failure(IllegalStateException("delete failed")))
                advanceUntilIdle()

                viewModel.delete()
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(DEFAULT_ID) }
            }
        }

        test("TC-MUSIC-DETAIL-DOMAIN-010 불러오기 처리 중 전달된 불러오기 요청은 처리하지 않는다") {
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

        test("TC-MUSIC-DETAIL-DOMAIN-011 수정을 처리하는 동안에도 삭제와 불러오기를 실행한다") {
            runTest(mainDispatcher) {
                val updateCompletion = CompletableDeferred<Result<Int>>()
                val updateMusicUseCase = mockk<UpdateMusicUseCase>()
                coEvery { updateMusicUseCase(any()) } coAnswers { updateCompletion.await() }
                val deleteMusicUseCase = mockk<DeleteMusicUseCase>()
                coEvery { deleteMusicUseCase(any()) } returns Result.success(1)
                val fetchYoutubeVideoUseCase = mockk<FetchYoutubeVideoUseCase>()
                coEvery { fetchYoutubeVideoUseCase(any()) } returns Result.success(fixtureMonkey.giveMeOne<YoutubeVideo>())
                val viewModel =
                    viewModel(
                        updateMusicUseCase = updateMusicUseCase,
                        deleteMusicUseCase = deleteMusicUseCase,
                        fetchYoutubeVideoUseCase = fetchYoutubeVideoUseCase,
                    )

                viewModel.update(detail = fixtureMonkey.giveMeOne<MusicDetail>())
                runCurrent()
                viewModel.delete()
                viewModel.fetchLink(YOUTUBE_LINK)
                advanceUntilIdle()

                coVerify(exactly = 1) { deleteMusicUseCase(DEFAULT_ID) }
                coVerify(exactly = 1) { fetchYoutubeVideoUseCase(YOUTUBE_LINK) }

                updateCompletion.complete(Result.success(1))
                advanceUntilIdle()
            }
        }
    }

    private companion object {
        private val DEFAULT_ID: Uuid = Uuid.random()

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun music(): Music =
            fixtureMonkey
                .giveMeKotlinBuilder<Music>()
                .setExp(Music::isDeleted, false)
                .setExp(Music::updatedAt, instant())
                .setExp(Music::createdAt, instant())
                .sample()

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()

        private fun viewModel(
            id: Uuid = DEFAULT_ID,
            music: Music? = null,
            findMusicUseCase: FindMusicUseCase = findMusicUseCase(id = id, music = music),
            updateMusicUseCase: UpdateMusicUseCase = mockk(relaxed = true),
            deleteMusicUseCase: DeleteMusicUseCase = mockk(relaxed = true),
            fetchYoutubeVideoUseCase: FetchYoutubeVideoUseCase = mockk(relaxed = true),
        ): MusicDetailViewModel =
            MusicDetailViewModel(
                id = id,
                updateMusicUseCase = updateMusicUseCase,
                deleteMusicUseCase = deleteMusicUseCase,
                fetchYoutubeVideoUseCase = fetchYoutubeVideoUseCase,
                findMusicUseCase = findMusicUseCase,
            )

        private fun findMusicUseCase(
            id: Uuid,
            music: Music?,
        ): FindMusicUseCase {
            val useCase = mockk<FindMusicUseCase>()
            every { useCase(parameter = id) } returns flowOf(Result.success(music))

            return useCase
        }
    }
}
