@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.file.ui.add

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.testing.file.fileUploadSource
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.file.exception.FileNotSelectedException
import io.github.taetae98coding.diary.domain.file.exception.FileTitleBlankException
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.exception.FileUnreadableException
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadEventUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadStateUseCase
import io.github.taetae98coding.diary.domain.file.usecase.ReadFileUploadSourceUseCase
import io.github.taetae98coding.diary.domain.file.usecase.RequestFileUploadUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
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

class FileAddViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-b2c327f8-dfbf-4980-a61a-2ac6582903b6 처음에는 고른 파일이 없고 올리는 중이 아니다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel()

                backgroundScope.launch { viewModel.uiState.collect {} }
                advanceUntilIdle()

                viewModel.uiState.value shouldBe FileAddUiState()
            }
        }

        test("TC-0751a03c-734b-4264-adcb-ece99ed4ed1b 파일을 고르면 읽은 이름과 크기의 파일이 고른 파일이 된다") {
            runTest(mainDispatcher) {
                val uri = fixtureMonkey.fileUri()
                val source = fixtureMonkey.fileUploadSource().copy(uri = uri)
                val readUseCase = mockk<ReadFileUploadSourceUseCase>()
                coEvery { readUseCase(parameter = uri) } returns Result.success(source)
                val viewModel = viewModel(readUseCase = readUseCase)

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.select(uri = uri)
                advanceUntilIdle()

                viewModel.uiState.value.selectedFile shouldBe source
            }
        }

        test("TC-0c908f3c-7a9d-4a76-82c6-2c23c2dbc398 이미 고른 파일이 있어도 다시 고르면 새 파일로 바뀐다") {
            runTest(mainDispatcher) {
                val first = fixtureMonkey.fileUploadSource()
                val second = fixtureMonkey.fileUploadSource()
                val readUseCase = mockk<ReadFileUploadSourceUseCase>()
                coEvery { readUseCase(parameter = first.uri) } returns Result.success(first)
                coEvery { readUseCase(parameter = second.uri) } returns Result.success(second)
                val viewModel = viewModel(readUseCase = readUseCase)

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.select(uri = first.uri)
                advanceUntilIdle()
                viewModel.select(uri = second.uri)
                advanceUntilIdle()

                viewModel.uiState.value.selectedFile shouldBe second
            }
        }

        test("TC-c89e24fb-6233-4119-80c9-9444a789c6d6 고를 수 없는 파일을 고르면 이유를 알리고 이전에 고른 파일을 둔다") {
            runTest(mainDispatcher) {
                val previous = fixtureMonkey.fileUploadSource()
                val readUseCase = mockk<ReadFileUploadSourceUseCase>()
                coEvery { readUseCase(parameter = previous.uri) } returns Result.success(previous)
                val viewModel = viewModel(readUseCase = readUseCase)

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.select(uri = previous.uri)
                advanceUntilIdle()

                viewModel.effect.test {
                    mapOf(
                        FileUnreadableException(name = "", cause = IllegalStateException("read")) to FileAddEffect.FileUnreadable,
                        FileTooLargeException() to FileAddEffect.FileTooLarge,
                    ).forEach { (exception, effect) ->
                        val uri = fixtureMonkey.fileUri()
                        coEvery { readUseCase(parameter = uri) } returns Result.failure(exception)

                        viewModel.select(uri = uri)
                        advanceUntilIdle()

                        awaitItem() shouldBe effect
                        viewModel.uiState.value.selectedFile shouldBe previous
                    }
                    expectNoEvents()
                }
            }
        }

        test("TC-13958209-1811-4ea0-b20a-56f57c8a9f75 TC-3dc2f4fc-8d3e-4a5b-9348-d5c731e2e72e TC-e9ed04a3-cd3b-48fd-900c-a41bcca35a4a 올리기 성립 조건을 어기면 이유를 알리고 고른 파일을 둔다") {
            runTest(mainDispatcher) {
                val source = fixtureMonkey.fileUploadSource()
                val readUseCase = mockk<ReadFileUploadSourceUseCase>()
                coEvery { readUseCase(parameter = source.uri) } returns Result.success(source)
                val requestUseCase = mockk<RequestFileUploadUseCase>()
                val viewModel = viewModel(readUseCase = readUseCase, requestUseCase = requestUseCase)

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.select(uri = source.uri)
                advanceUntilIdle()

                viewModel.effect.test {
                    mapOf(
                        FileTitleBlankException() to FileAddEffect.TitleBlank,
                        FileNotSelectedException() to FileAddEffect.FileNotSelected,
                    ).forEach { (exception, effect) ->
                        coEvery { requestUseCase(parameter = any()) } returns Result.failure(exception)

                        viewModel.upload(title = "", description = "")
                        advanceUntilIdle()

                        awaitItem() shouldBe effect
                        viewModel.uiState.value.selectedFile shouldBe source
                    }
                    expectNoEvents()
                }
            }
        }

        test("TC-4c8c007d-7359-4fa6-9caf-57f28b16a319 올리기를 실행하면 고른 파일과 입력한 제목·설명으로 맡기고 고른 파일을 비운다") {
            runTest(mainDispatcher) {
                val source = fixtureMonkey.fileUploadSource()
                val title = "title-${fixtureMonkey.giveMeOne<String>()}"
                val description = fixtureMonkey.giveMeOne<String>()
                val readUseCase = mockk<ReadFileUploadSourceUseCase>()
                coEvery { readUseCase(parameter = source.uri) } returns Result.success(source)
                val requestUseCase = mockk<RequestFileUploadUseCase>()
                coEvery { requestUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(readUseCase = readUseCase, requestUseCase = requestUseCase)

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.select(uri = source.uri)
                advanceUntilIdle()

                viewModel.effect.test {
                    viewModel.upload(title = title, description = description)
                    advanceUntilIdle()

                    awaitItem() shouldBe FileAddEffect.UploadStarted
                    expectNoEvents()
                }
                coVerify(exactly = 1) {
                    requestUseCase(parameter = RequestFileUploadUseCase.Parameter(uri = source.uri, title = title, description = description))
                }
                viewModel.uiState.value.selectedFile
                    .shouldBeNull()
            }
        }

        test("TC-50400477-91a5-4e1e-ad53-9b488e23801c 올리는 중에는 진행 중이고 올리기를 실행해도 새로 맡기지 않는다") {
            runTest(mainDispatcher) {
                listOf(
                    FileUploadState.Uploading(percent = null),
                    FileUploadState.Uploading(percent = fixtureMonkey.giveMeOne<Int>()),
                ).forEach { state ->
                    val requestUseCase = mockk<RequestFileUploadUseCase>()
                    val viewModel =
                        viewModel(
                            requestUseCase = requestUseCase,
                            stateFlow = MutableStateFlow(Result.success(state)),
                        )

                    val job = launch { viewModel.uiState.collect {} }
                    advanceUntilIdle()
                    viewModel.uiState.value.isUploading
                        .shouldBeTrue()

                    viewModel.upload(title = "title-${fixtureMonkey.giveMeOne<String>()}", description = "")
                    advanceUntilIdle()

                    coVerify(exactly = 0) { requestUseCase(parameter = any()) }
                    job.cancel()
                }
            }
        }

        test("TC-50400477-91a5-4e1e-ad53-9b488e23801c 이 화면에서 맡기는 동안과 올리는 동안 진행 중이다") {
            runTest(mainDispatcher) {
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
                viewModel.upload(title = "title-${fixtureMonkey.giveMeOne<String>()}", description = "")
                runCurrent()
                viewModel.uiState.value.isUploading
                    .shouldBeTrue()

                viewModel.upload(title = "title-${fixtureMonkey.giveMeOne<String>()}", description = "")
                requested.complete(Unit)
                advanceUntilIdle()
                viewModel.uiState.value.isUploading
                    .shouldBeTrue()
                coVerify(exactly = 1) { requestUseCase(parameter = any()) }

                stateFlow.value = Result.success(FileUploadState.Idle)
                advanceUntilIdle()
                viewModel.uiState.value.isUploading
                    .shouldBeFalse()
            }
        }

        test("TC-2bdb00f3-e9db-459e-a33a-82f72bcf4a92 올리는 중에도 파일을 고를 수 있다") {
            runTest(mainDispatcher) {
                val source = fixtureMonkey.fileUploadSource()
                val readUseCase = mockk<ReadFileUploadSourceUseCase>()
                coEvery { readUseCase(parameter = source.uri) } returns Result.success(source)
                val viewModel =
                    viewModel(
                        readUseCase = readUseCase,
                        stateFlow = MutableStateFlow(Result.success(FileUploadState.Uploading(percent = null))),
                    )

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.select(uri = source.uri)
                advanceUntilIdle()

                viewModel.uiState.value shouldBe FileAddUiState(selectedFile = source, isUploading = true)
            }
        }

        test("TC-f33a3f46-e611-4fd6-b6a4-453ca777497a 화면을 보는 동안 끝난 올리기의 결과를 알리고 고른 파일은 둔다") {
            runTest(mainDispatcher) {
                val source = fixtureMonkey.fileUploadSource()
                val readUseCase = mockk<ReadFileUploadSourceUseCase>()
                coEvery { readUseCase(parameter = source.uri) } returns Result.success(source)
                val eventChannel = Channel<Result<FileUploadEvent>>(Channel.UNLIMITED)
                val viewModel = viewModel(readUseCase = readUseCase, eventChannel = eventChannel)

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.select(uri = source.uri)
                advanceUntilIdle()

                viewModel.effect.test {
                    mapOf(
                        FileUploadEvent.Succeeded(fileId = fixtureMonkey.giveMeOne<Uuid>()) to FileAddEffect.UploadSucceeded,
                        FileUploadEvent.TooLarge to FileAddEffect.FileTooLarge,
                        FileUploadEvent.Failed to FileAddEffect.UploadFailed,
                    ).forEach { (event, effect) ->
                        eventChannel.send(Result.success(event))
                        awaitItem() shouldBe effect
                    }
                    expectNoEvents()
                }
                viewModel.uiState.value.selectedFile shouldBe source
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

        test("TC-a1099bb4-240a-42f7-9476-8fc329821611 TC-4828e410-703c-4644-a5aa-6bc44c948b6b 올리기를 맡기지 못하면 올리지 못했다고 알리고 고른 파일을 둔다") {
            runTest(mainDispatcher) {
                val source = fixtureMonkey.fileUploadSource()
                val readUseCase = mockk<ReadFileUploadSourceUseCase>()
                coEvery { readUseCase(parameter = source.uri) } returns Result.success(source)
                val requestUseCase = mockk<RequestFileUploadUseCase>()
                coEvery { requestUseCase(parameter = any()) } returns Result.failure(SecurityException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(readUseCase = readUseCase, requestUseCase = requestUseCase)

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.select(uri = source.uri)
                advanceUntilIdle()

                viewModel.effect.test {
                    viewModel.upload(title = "title-${fixtureMonkey.giveMeOne<String>()}", description = "")
                    advanceUntilIdle()

                    awaitItem() shouldBe FileAddEffect.UploadFailed
                    expectNoEvents()
                }
                viewModel.uiState.value shouldBe FileAddUiState(selectedFile = source, isUploading = false)
            }
        }

        test("같은 파일을 읽는 중에 다시 고르면 한 번만 읽는다") {
            runTest(mainDispatcher) {
                val source = fixtureMonkey.fileUploadSource()
                val readUseCase = mockk<ReadFileUploadSourceUseCase>()
                coEvery { readUseCase(parameter = source.uri) } returns Result.success(source)
                val viewModel = viewModel(readUseCase = readUseCase)

                viewModel.select(uri = source.uri)
                viewModel.select(uri = source.uri)
                advanceUntilIdle()

                coVerify(exactly = 1) { readUseCase(parameter = source.uri) }
            }
        }
    }

    private fun viewModel(
        readUseCase: ReadFileUploadSourceUseCase = mockk(),
        requestUseCase: RequestFileUploadUseCase = mockk<RequestFileUploadUseCase>().also { useCase -> coEvery { useCase(parameter = any()) } returns Result.success(Unit) },
        stateFlow: MutableStateFlow<Result<FileUploadState>> = MutableStateFlow(Result.success(FileUploadState.Idle)),
        eventChannel: Channel<Result<FileUploadEvent>> = Channel(Channel.UNLIMITED),
    ): FileAddViewModel {
        val getStateUseCase = mockk<GetFileUploadStateUseCase>()
        every { getStateUseCase(parameter = Unit) } returns stateFlow
        val getEventUseCase = mockk<GetFileUploadEventUseCase>()
        every { getEventUseCase(parameter = Unit) } returns eventChannel.receiveAsFlow()

        return FileAddViewModel(
            readFileUploadSourceUseCase = readUseCase,
            requestFileUploadUseCase = requestUseCase,
            getFileUploadStateUseCase = getStateUseCase,
            getFileUploadEventUseCase = getEventUseCase,
        )
    }
}
