@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.file.ui.home

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadEventUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadStateUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StartViewingFileScreenUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StopViewingFileScreenUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileHomeUploadViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-FILE-HOME-FEATURE-032 화면에 들어오기 전에 시작한 올리기가 끝나지 않았으면 진행 중 상태다") {
            runTest(mainDispatcher) {
                val stateFlow = MutableStateFlow<Result<FileUploadState>>(Result.success(FileUploadState.Uploading(percent = fixtureMonkey.giveMeOne<Int>())))
                val viewModel = viewModel(stateFlow = stateFlow)

                viewModel.uiState.test {
                    awaitItem()
                        .isUploading
                        .shouldBeFalse()
                    awaitItem()
                        .isUploading
                        .shouldBeTrue()
                }
            }
        }

        test("TC-FILE-HOME-FEATURE-018 올리기에 성공하면 올린 파일과 함께 성공을 알린다") {
            runTest(mainDispatcher) {
                val fileId = fixtureMonkey.giveMeOne<Uuid>()
                val eventChannel = Channel<Result<FileUploadEvent>>(Channel.UNLIMITED)
                val viewModel = viewModel(eventChannel = eventChannel)

                viewModel.effect.test {
                    eventChannel.send(Result.success(FileUploadEvent.Succeeded(fileId = fileId)))
                    awaitItem() shouldBe FileHomeUploadEffect.UploadSucceeded(id = fileId)
                    expectNoEvents()
                }
            }
        }

        test("TC-FILE-HOME-FEATURE-049 FileAdd를 보는 동안 올리기에 성공하면 FileAdd에서 성공한 것으로 알린다") {
            runTest(mainDispatcher) {
                val fileId = fixtureMonkey.giveMeOne<Uuid>()
                val eventChannel = Channel<Result<FileUploadEvent>>(Channel.UNLIMITED)
                val viewModel = viewModel(eventChannel = eventChannel)

                viewModel.effect.test {
                    eventChannel.send(Result.success(FileUploadEvent.SucceededOnFileAdd(fileId = fileId)))
                    awaitItem() shouldBe FileHomeUploadEffect.UploadSucceededOnFileAdd(id = fileId)
                    expectNoEvents()
                }
            }
        }

        test("TC-FILE-HOME-FEATURE-019 올리기에 실패하면 실패 이유를 구분해 알린다") {
            runTest(mainDispatcher) {
                val eventChannel = Channel<Result<FileUploadEvent>>(Channel.UNLIMITED)
                val viewModel = viewModel(eventChannel = eventChannel)

                viewModel.effect.test {
                    mapOf(
                        FileUploadEvent.TooLarge to FileHomeUploadEffect.UploadTooLarge,
                        FileUploadEvent.Failed to FileHomeUploadEffect.UploadFailed,
                    ).forEach { (event, effect) ->
                        eventChannel.send(Result.success(event))
                        awaitItem() shouldBe effect
                    }
                    expectNoEvents()
                }
            }
        }

        test("결과를 받지 못한 흐름은 알리지 않는다") {
            runTest(mainDispatcher) {
                val eventChannel = Channel<Result<FileUploadEvent>>(Channel.UNLIMITED)
                val viewModel = viewModel(eventChannel = eventChannel)

                viewModel.effect.test {
                    eventChannel.send(Result.failure(IllegalStateException("event")))
                    expectNoEvents()
                }
            }
        }

        test("화면을 보기 시작하고 그만 보는 것을 알린다") {
            runTest(mainDispatcher) {
                val startUseCase = mockk<StartViewingFileScreenUseCase>()
                coEvery { startUseCase(parameter = FileScreen.HOME) } returns Result.success(Unit)
                val stopUseCase = mockk<StopViewingFileScreenUseCase>()
                coEvery { stopUseCase(parameter = FileScreen.HOME) } returns Result.success(Unit)
                val viewModel = viewModel(startUseCase = startUseCase, stopUseCase = stopUseCase)

                viewModel.startViewing()
                advanceUntilIdle()
                coVerify(exactly = 1) { startUseCase(parameter = FileScreen.HOME) }
                coVerify(exactly = 0) { stopUseCase(parameter = any()) }

                viewModel.stopViewing()
                advanceUntilIdle()
                coVerify(exactly = 1) { stopUseCase(parameter = FileScreen.HOME) }
            }
        }
    }

    private fun viewModel(
        stateFlow: MutableStateFlow<Result<FileUploadState>> = MutableStateFlow(Result.success(FileUploadState.Idle)),
        eventChannel: Channel<Result<FileUploadEvent>> = Channel(Channel.UNLIMITED),
        startUseCase: StartViewingFileScreenUseCase = mockk(relaxed = true),
        stopUseCase: StopViewingFileScreenUseCase = mockk(relaxed = true),
    ): FileHomeUploadViewModel {
        val getStateUseCase = mockk<GetFileUploadStateUseCase>()
        every { getStateUseCase(parameter = Unit) } returns stateFlow
        val getEventUseCase = mockk<GetFileUploadEventUseCase>()
        every { getEventUseCase(parameter = FileScreen.HOME) } returns eventChannel.receiveAsFlow()

        return FileHomeUploadViewModel(
            startViewingFileScreenUseCase = startUseCase,
            stopViewingFileScreenUseCase = stopUseCase,
            getFileUploadStateUseCase = getStateUseCase,
            getFileUploadEventUseCase = getEventUseCase,
        )
    }
}
