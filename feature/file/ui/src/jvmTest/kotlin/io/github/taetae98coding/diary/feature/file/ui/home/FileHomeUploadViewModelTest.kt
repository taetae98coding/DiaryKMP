@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.file.ui.home

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadEventUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadStateUseCase
import io.github.taetae98coding.diary.domain.file.usecase.RequestFileUploadUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StartViewingFileHomeUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StopViewingFileHomeUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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

        test("TC-FILE-HOME-FEATURE-017 고른 파일로 올리기를 맡기고 맡기는 동안과 올리는 동안 진행 중 상태가 된다") {
            runTest(mainDispatcher) {
                val uri = fixtureMonkey.fileUri()
                val stateFlow = MutableStateFlow<Result<FileUploadState>>(Result.success(FileUploadState.Idle))
                val requested = CompletableDeferred<Unit>()
                val requestUseCase = mockk<RequestFileUploadUseCase>()
                coEvery { requestUseCase(parameter = any()) } coAnswers {
                    requested.await()
                    stateFlow.value = Result.success(FileUploadState.Uploading(percent = null))
                    Result.success(Unit)
                }
                val viewModel = viewModel(requestUseCase = requestUseCase, stateFlow = stateFlow)

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.upload(uri = uri)
                runCurrent()
                viewModel.uiState.value.isUploading
                    .shouldBeTrue()

                requested.complete(Unit)
                advanceUntilIdle()
                viewModel.uiState.value.isUploading
                    .shouldBeTrue()
                coVerify(exactly = 1) { requestUseCase(parameter = uri) }

                stateFlow.value = Result.success(FileUploadState.Idle)
                advanceUntilIdle()
                viewModel.uiState.value.isUploading
                    .shouldBeFalse()
            }
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

        test("올리기를 맡기지 못해도 진행 중 상태가 끝난다") {
            runTest(mainDispatcher) {
                val requestUseCase = mockk<RequestFileUploadUseCase>()
                coEvery { requestUseCase(parameter = any()) } returns Result.failure(IllegalStateException("guest"))
                val viewModel = viewModel(requestUseCase = requestUseCase)

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.upload(uri = fixtureMonkey.fileUri())
                advanceUntilIdle()

                viewModel.uiState.value.isUploading
                    .shouldBeFalse()
            }
        }

        test("TC-FILE-HOME-FEATURE-041 올리기를 맡기지 못하면 올리지 못했다고 알린다") {
            runTest(mainDispatcher) {
                val requestUseCase = mockk<RequestFileUploadUseCase>()
                coEvery { requestUseCase(parameter = any()) } returns Result.failure(SecurityException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(requestUseCase = requestUseCase)

                viewModel.effect.test {
                    viewModel.upload(uri = fixtureMonkey.fileUri())
                    awaitItem() shouldBe FileHomeUploadEffect.UploadFailed
                    expectNoEvents()
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
                val startUseCase = mockk<StartViewingFileHomeUseCase>()
                coEvery { startUseCase(parameter = Unit) } returns Result.success(Unit)
                val stopUseCase = mockk<StopViewingFileHomeUseCase>()
                coEvery { stopUseCase(parameter = Unit) } returns Result.success(Unit)
                val viewModel = viewModel(startUseCase = startUseCase, stopUseCase = stopUseCase)

                viewModel.startViewing()
                advanceUntilIdle()
                coVerify(exactly = 1) { startUseCase(parameter = Unit) }
                coVerify(exactly = 0) { stopUseCase(parameter = Unit) }

                viewModel.stopViewing()
                advanceUntilIdle()
                coVerify(exactly = 1) { stopUseCase(parameter = Unit) }
            }
        }
    }

    private fun viewModel(
        requestUseCase: RequestFileUploadUseCase = mockk<RequestFileUploadUseCase>().also { useCase -> coEvery { useCase(parameter = any()) } returns Result.success(Unit) },
        stateFlow: MutableStateFlow<Result<FileUploadState>> = MutableStateFlow(Result.success(FileUploadState.Idle)),
        eventChannel: Channel<Result<FileUploadEvent>> = Channel(Channel.UNLIMITED),
        startUseCase: StartViewingFileHomeUseCase = mockk(relaxed = true),
        stopUseCase: StopViewingFileHomeUseCase = mockk(relaxed = true),
    ): FileHomeUploadViewModel {
        val getStateUseCase = mockk<GetFileUploadStateUseCase>()
        every { getStateUseCase(parameter = Unit) } returns stateFlow
        val getEventUseCase = mockk<GetFileUploadEventUseCase>()
        every { getEventUseCase(parameter = Unit) } returns eventChannel.receiveAsFlow()

        return FileHomeUploadViewModel(
            requestFileUploadUseCase = requestUseCase,
            startViewingFileHomeUseCase = startUseCase,
            stopViewingFileHomeUseCase = stopUseCase,
            getFileUploadStateUseCase = getStateUseCase,
            getFileUploadEventUseCase = getEventUseCase,
        )
    }
}
