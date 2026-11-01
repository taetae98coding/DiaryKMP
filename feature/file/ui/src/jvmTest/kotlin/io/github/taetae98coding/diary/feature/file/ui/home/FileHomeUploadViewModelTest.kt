@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.file.ui.home

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadEventUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadStateUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadSucceededEventUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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

        test("TC-36571a73-b36c-45e1-bde6-3929ed3f2b6e 화면에 들어오기 전에 시작한 올리기가 끝나지 않았으면 진행 중 상태다") {
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

        test("TC-ccb81390-a936-4a22-8ce9-4fe3de62a730 올리기에 성공하면 성공을 알린다") {
            runTest(mainDispatcher) {
                val succeededChannel = Channel<Result<FileUploadEvent.Succeeded>>(Channel.UNLIMITED)
                val viewModel = viewModel(succeededChannel = succeededChannel)

                viewModel.effect.test {
                    succeededChannel.send(Result.success(FileUploadEvent.Succeeded(fileId = fixtureMonkey.giveMeOne<Uuid>())))
                    awaitItem() shouldBe FileHomeUploadEffect.UploadSucceeded
                    expectNoEvents()
                }
            }
        }

        test("TC-7ef172cd-3ed6-454e-8f51-2c50f9ee0589 화면이 결과를 받지 않는 동안 쌓인 성공은 다시 받기 시작할 때 알린다") {
            runTest(mainDispatcher) {
                val succeededChannel = Channel<Result<FileUploadEvent.Succeeded>>(Channel.UNLIMITED)
                val viewModel = viewModel(succeededChannel = succeededChannel)

                succeededChannel.send(Result.success(FileUploadEvent.Succeeded(fileId = fixtureMonkey.giveMeOne<Uuid>())))

                viewModel.effect.test {
                    awaitItem() shouldBe FileHomeUploadEffect.UploadSucceeded
                    expectNoEvents()
                }
            }
        }

        test("보이는 동안 받는 결과의 성공은 쌓아 둔 성공과 겹치지 않도록 따로 알리지 않는다") {
            runTest(mainDispatcher) {
                val eventChannel = Channel<Result<FileUploadEvent>>(Channel.UNLIMITED)
                val viewModel = viewModel(eventChannel = eventChannel)

                viewModel.effect.test {
                    eventChannel.send(Result.success(FileUploadEvent.Succeeded(fileId = fixtureMonkey.giveMeOne<Uuid>())))
                    expectNoEvents()
                }
            }
        }

        test("TC-f0d423c5-b463-4cef-9f6c-b17e5e1946d9 올리기에 실패하면 실패 이유를 구분해 알린다") {
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

        test("올리는 중이 아니면 파일 추가 요청이 FileAdd 이동으로 이어진다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel()

                viewModel.uiState.test {
                    awaitItem().isUploading.shouldBeFalse()
                    cancelAndIgnoreRemainingEvents()
                }
                viewModel.effect.test {
                    viewModel.requestAdd()

                    awaitItem() shouldBe FileHomeUploadEffect.NavigateToAdd
                    expectNoEvents()
                }
            }
        }

        test("TC-9707e7f6-f68f-4a28-8701-f2759b92ad40 올리는 중에는 파일 추가를 요청해도 FileAdd로 이동하지 않는다") {
            runTest(mainDispatcher) {
                val stateFlow = MutableStateFlow<Result<FileUploadState>>(Result.success(FileUploadState.Uploading(percent = fixtureMonkey.giveMeOne<Int>())))
                val viewModel = viewModel(stateFlow = stateFlow)

                viewModel.uiState.test {
                    awaitItem()
                    awaitItem().isUploading.shouldBeTrue()

                    viewModel.effect.test {
                        viewModel.requestAdd()

                        expectNoEvents()
                    }
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private fun viewModel(
        stateFlow: MutableStateFlow<Result<FileUploadState>> = MutableStateFlow(Result.success(FileUploadState.Idle)),
        eventChannel: Channel<Result<FileUploadEvent>> = Channel(Channel.UNLIMITED),
        succeededChannel: Channel<Result<FileUploadEvent.Succeeded>> = Channel(Channel.UNLIMITED),
    ): FileHomeUploadViewModel {
        val getStateUseCase = mockk<GetFileUploadStateUseCase>()
        every { getStateUseCase(parameter = Unit) } returns stateFlow
        val getEventUseCase = mockk<GetFileUploadEventUseCase>()
        every { getEventUseCase(parameter = Unit) } returns eventChannel.receiveAsFlow()
        val getSucceededEventUseCase = mockk<GetFileUploadSucceededEventUseCase>()
        every { getSucceededEventUseCase(parameter = Unit) } returns succeededChannel.receiveAsFlow()

        return FileHomeUploadViewModel(
            getFileUploadStateUseCase = getStateUseCase,
            getFileUploadEventUseCase = getEventUseCase,
            getFileUploadSucceededEventUseCase = getSucceededEventUseCase,
        )
    }
}
