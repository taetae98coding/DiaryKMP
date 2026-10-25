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

        test("TC-7bed6726-c5fb-4bbb-a9cd-d3fbb9b763d4 조회에 성공하면 저장된 곡을 내용 표시 상태로 전달한다") {
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

        test("TC-418fdb5f-3a74-468a-a30a-7110fb585c24 TC-06f5e020-4087-4297-9504-8472dd1b762d 곡을 조회할 수 없으면 조회 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(music = null)

                viewModel.uiState.test {
                    awaitItem() shouldBe MusicDetailUiState.Loading
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-0c2c9c05-bd19-4f01-bbc1-8634c245be67 대상 곡이 삭제 상태가 되어도 내용 표시 상태를 유지한다") {
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

        test("TC-1b34c164-6cc9-496d-b3ec-6c173ac0afd6 저장된 제목이 바뀌면 내용 표시 상태의 제목이 갱신된다") {
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

        test("TC-0aee8edc-ff76-4fa3-93a6-65a162a9b45b 수정에 성공하면 성공 Effect를 한 번 보낸다") {
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

        test("TC-2d9e4d4d-0af3-4df2-9bc8-cb83bdba7cf5 링크 형식이 성립하지 않으면 링크 형식 Effect를 보낸다") {
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

        test("TC-f453fd31-eee2-42b8-bf14-94ab5693f604 TC-abbc197b-b8ec-44d8-b4ef-e1b1ab4cfc6d 수정을 처리하는 동안 진행 상태를 유지하고 실패해도 해제한다") {
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

        test("TC-ca8e572f-1080-4a8e-91c0-e4ac4de48a43 삭제에 성공하면 삭제 성공 Effect를 한 번 보낸다") {
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

        test("TC-5821617e-dd84-4d81-a046-c09757c3cee6 TC-4deab72f-3495-4fda-ac27-98130928f0a0 삭제를 처리하는 동안 진행 상태를 유지하고 실패해도 해제한다") {
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

        test("TC-5821617e-dd84-4d81-a046-c09757c3cee6 삭제를 처리하는 동안에도 수정과 불러오기를 실행한다") {
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

        test("TC-0048b9b6-69e2-47f9-8551-c7ad8a5c3e4f TC-25f3da4c-db8c-4913-9855-9a06da0dc324 다시 불러오기에 성공하면 불러온 값 Effect만 보낸다") {
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
            test("TC-d7bfce7e-9215-4a4b-b359-bc67b377b3ce 링크가 성립하지 않으면 ${expected::class.simpleName} Effect를 보낸다") {
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

        test("TC-1975ff66-3587-47d3-b1e7-57d24a284022 영상 정보를 가져오지 못하면 불러오기 실패 Effect를 보낸다") {
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

        test("TC-d0f4ecb8-0869-4067-8fe9-0244d8e4b1b5 수정 처리 중 전달된 수정 요청은 처리하지 않는다") {
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

        test("TC-d0f4ecb8-0869-4067-8fe9-0244d8e4b1b5 삭제 처리 중 전달된 삭제 요청은 처리하지 않는다") {
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

        test("TC-d0f4ecb8-0869-4067-8fe9-0244d8e4b1b5 불러오기 처리 중 전달된 불러오기 요청은 처리하지 않는다") {
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

        test("TC-58452686-1ab1-4484-a6b3-064be2317bc6 수정을 처리하는 동안에도 삭제와 불러오기를 실행한다") {
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
