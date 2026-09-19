@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui.add

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.playlist.exception.MusicArtistBlankException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicTitleBlankException
import io.github.taetae98coding.diary.domain.playlist.usecase.AddMusicUseCase
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
                val viewModel = MusicAddViewModel(addMusicUseCase = useCase)

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
            }
        }

        test("TC-MUSIC-ADD-FEATURE-005 추가에 성공하면 성공 Effect를 한 번 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<MusicDetail>()
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(any()) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = MusicAddViewModel(addMusicUseCase = useCase)

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

        test("TC-MUSIC-ADD-FEATURE-008 제목이 공백이면 제목 미입력 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<MusicDetail>().copy(title = "  ")
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(any()) } returns Result.failure(MusicTitleBlankException())
                val viewModel = MusicAddViewModel(addMusicUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail)
                    advanceUntilIdle()

                    awaitItem() shouldBe MusicAddEffect.TitleBlank
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-MUSIC-ADD-FEATURE-008 가수가 공백이면 가수 미입력 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<MusicDetail>().copy(artist = "  ")
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(any()) } returns Result.failure(MusicArtistBlankException())
                val viewModel = MusicAddViewModel(addMusicUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail)
                    advanceUntilIdle()

                    awaitItem() shouldBe MusicAddEffect.ArtistBlank
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-MUSIC-ADD-FEATURE-007 추가를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<MusicDetail>()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddMusicUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = MusicAddViewModel(addMusicUseCase = useCase)

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
                val viewModel = MusicAddViewModel(addMusicUseCase = useCase)

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
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
