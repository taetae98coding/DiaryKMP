@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.file.ui.home

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.exception.FileUploadAccountChangedException
import io.github.taetae98coding.diary.domain.file.usecase.UploadFileUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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

        test("TC-FILE-HOME-FEATURE-017 고른 파일을 곧바로 올리고 올리는 동안 진행 중 상태가 되며 다시 올리지 않는다") {
            runTest(mainDispatcher) {
                val uri = fixtureMonkey.fileUri()
                val completion = CompletableDeferred<DiaryFile>()
                val useCase = mockk<UploadFileUseCase>()
                coEvery { useCase(parameter = any()) } coAnswers { Result.success(completion.await()) }
                val viewModel = FileHomeUploadViewModel(uploadFileUseCase = useCase)

                viewModel.upload(uri = uri)
                runCurrent()
                viewModel.uiState.value.isUploading
                    .shouldBeTrue()
                viewModel.upload(uri = fixtureMonkey.fileUri())
                runCurrent()

                coVerify(exactly = 1) { useCase(parameter = uri) }
                coVerify(exactly = 1) { useCase(parameter = any()) }

                completion.complete(fixtureMonkey.giveMeOne<DiaryFile>())
                advanceUntilIdle()

                viewModel.uiState.value.isUploading
                    .shouldBeFalse()
            }
        }

        test("TC-FILE-HOME-FEATURE-018 올리기에 성공하면 진행 중 상태가 끝나고 올린 파일을 알린다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.giveMeOne<DiaryFile>()
                val useCase = mockk<UploadFileUseCase>()
                coEvery { useCase(parameter = any()) } returns Result.success(file)
                val viewModel = FileHomeUploadViewModel(uploadFileUseCase = useCase)

                viewModel.effect.test {
                    viewModel.upload(uri = fixtureMonkey.fileUri())
                    advanceUntilIdle()

                    awaitItem() shouldBe FileHomeUploadEffect.UploadSucceeded(id = file.id)
                    expectNoEvents()
                }
                viewModel.uiState.value.isUploading
                    .shouldBeFalse()
            }
        }

        test("TC-FILE-HOME-FEATURE-019 올리기에 실패하면 실패 이유를 구분해 알리고 다시 올릴 수 있다") {
            runTest(mainDispatcher) {
                mapOf(
                    FileTooLargeException(message = "too large") to FileHomeUploadEffect.UploadTooLarge,
                    IllegalStateException("failed") to FileHomeUploadEffect.UploadFailed,
                ).forEach { (throwable, effect) ->
                    val useCase = mockk<UploadFileUseCase>()
                    coEvery { useCase(parameter = any()) } returns Result.failure(throwable)
                    val viewModel = FileHomeUploadViewModel(uploadFileUseCase = useCase)

                    viewModel.effect.test {
                        viewModel.upload(uri = fixtureMonkey.fileUri())
                        advanceUntilIdle()
                        awaitItem() shouldBe effect

                        viewModel.upload(uri = fixtureMonkey.fileUri())
                        advanceUntilIdle()
                        awaitItem() shouldBe effect
                    }
                    viewModel.uiState.value.isUploading
                        .shouldBeFalse()
                    coVerify(exactly = 2) { useCase(parameter = any()) }
                }
            }
        }

        test("TC-FILE-HOME-FEATURE-024 계정이 바뀌어 올리기가 중단되면 진행 중 상태만 끝나고 결과를 알리지 않는다") {
            runTest(mainDispatcher) {
                val useCase = mockk<UploadFileUseCase>()
                coEvery { useCase(parameter = any()) } returns Result.failure(FileUploadAccountChangedException(message = "changed"))
                val viewModel = FileHomeUploadViewModel(uploadFileUseCase = useCase)

                viewModel.effect.test {
                    viewModel.upload(uri = fixtureMonkey.fileUri())
                    advanceUntilIdle()

                    expectNoEvents()
                }
                viewModel.uiState.value.isUploading
                    .shouldBeFalse()
            }
        }
    }
}
