@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.file.ui.detail

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.DiaryFileDetail
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import io.github.taetae98coding.diary.core.testing.file.diaryFile
import io.github.taetae98coding.diary.domain.file.exception.FileDownloadDirectoryUnavailableException
import io.github.taetae98coding.diary.domain.file.exception.FileNameBlankException
import io.github.taetae98coding.diary.domain.file.exception.FileNameTooLongException
import io.github.taetae98coding.diary.domain.file.exception.FileTitleBlankException
import io.github.taetae98coding.diary.domain.file.usecase.DownloadFileUseCase
import io.github.taetae98coding.diary.domain.file.usecase.FetchFileUseCase
import io.github.taetae98coding.diary.domain.file.usecase.UpdateFileUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileDetailViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-FILE-DETAIL-FEATURE-001 불러오는 동안에는 불러오는 중 상태다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val fetchUseCase = mockk<FetchFileUseCase>()
                coEvery { fetchUseCase(parameter = file.id) } coAnswers { awaitCancellation() }
                val viewModel = viewModel(id = file.id, fetchUseCase = fetchUseCase)

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.fetch()
                advanceUntilIdle()

                viewModel.uiState.value shouldBe FileDetailUiState.Loading
            }
        }

        test("TC-FILE-DETAIL-FEATURE-002 불러오기에 실패하면 실패 상태다") {
            runTest(mainDispatcher) {
                listOf<Throwable>(NoSuchElementException(fixtureMonkey.giveMeOne<String>()), IllegalStateException(fixtureMonkey.giveMeOne<String>())).forEach { throwable ->
                    val id = fixtureMonkey.giveMeOne<Uuid>()
                    val fetchUseCase = mockk<FetchFileUseCase>()
                    coEvery { fetchUseCase(parameter = id) } returns Result.failure(throwable)
                    val viewModel = viewModel(id = id, fetchUseCase = fetchUseCase)

                    backgroundScope.launch { viewModel.uiState.collect {} }
                    viewModel.fetch()
                    advanceUntilIdle()

                    viewModel.uiState.value shouldBe FileDetailUiState.Failed
                }
            }
        }

        test("TC-FILE-DETAIL-FEATURE-003 실패한 뒤 다시 시도하면 다시 불러오고 그동안 불러오는 중 상태다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val response = CompletableDeferred<Result<DiaryFile>>()
                val fetchUseCase = mockk<FetchFileUseCase>()
                coEvery { fetchUseCase(parameter = file.id) } returns Result.failure(IllegalStateException()) coAndThen { response.await() }
                val viewModel = viewModel(id = file.id, fetchUseCase = fetchUseCase)

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.fetch()
                advanceUntilIdle()
                viewModel.retry()
                advanceUntilIdle()

                viewModel.uiState.value shouldBe FileDetailUiState.Loading
                coVerify(exactly = 2) { fetchUseCase(parameter = file.id) }

                response.complete(Result.success(file))
                advanceUntilIdle()

                viewModel.uiState.value shouldBe FileDetailUiState.Content(file = file)
            }
        }

        test("TC-FILE-DETAIL-FEATURE-020 불러오는 중이거나 불러온 뒤에는 다시 시도를 처리하지 않는다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val fetchUseCase = mockk<FetchFileUseCase>()
                coEvery { fetchUseCase(parameter = file.id) } returns Result.success(file)
                val viewModel = viewModel(id = file.id, fetchUseCase = fetchUseCase)

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.fetch()
                viewModel.retry()
                advanceUntilIdle()
                viewModel.retry()
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchUseCase(parameter = file.id) }
            }
        }

        test("TC-FILE-DETAIL-FEATURE-004 TC-FILE-DETAIL-DATA-001 불러오기에 성공하면 이 파일의 정보를 한 번 불러와 내용 상태가 된다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val fetchUseCase = mockk<FetchFileUseCase>()
                coEvery { fetchUseCase(parameter = file.id) } returns Result.success(file)
                val viewModel = viewModel(id = file.id, fetchUseCase = fetchUseCase)

                backgroundScope.launch { viewModel.uiState.collect {} }
                viewModel.fetch()
                advanceUntilIdle()

                viewModel.uiState.value shouldBe FileDetailUiState.Content(file = file)
                coVerify(exactly = 1) { fetchUseCase(parameter = file.id) }
            }
        }

        test("TC-FILE-DETAIL-DOMAIN-006 화면에 다시 들어와도 이미 불러온 정보를 다시 불러오지 않는다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val fetchUseCase = mockk<FetchFileUseCase>()
                coEvery { fetchUseCase(parameter = file.id) } returns Result.success(file)
                val viewModel = viewModel(id = file.id, fetchUseCase = fetchUseCase)

                viewModel.fetch()
                advanceUntilIdle()
                viewModel.fetch()
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchUseCase(parameter = file.id) }
            }
        }

        test("TC-FILE-DETAIL-FEATURE-007 TC-FILE-DETAIL-DOMAIN-001 TC-FILE-DETAIL-DOMAIN-002 TC-FILE-DETAIL-DOMAIN-003 수정 성립 조건을 어기면 이유별로 알리고 내용은 바뀌지 않는다") {
            runTest(mainDispatcher) {
                listOf(
                    FileTitleBlankException() to FileDetailEffect.TitleBlank,
                    FileNameBlankException() to FileDetailEffect.NameBlank,
                    FileNameTooLongException() to FileDetailEffect.NameTooLong,
                ).forEach { (throwable, expected) ->
                    val file = fixtureMonkey.diaryFile()
                    val updateUseCase = mockk<UpdateFileUseCase>()
                    coEvery { updateUseCase(parameter = any()) } returns Result.failure(throwable)
                    val viewModel = loadedViewModel(file = file, updateUseCase = updateUseCase)

                    viewModel.effect.test {
                        viewModel.update(detail = fixtureMonkey.giveMeOne<DiaryFileDetail>())
                        awaitItem() shouldBe expected
                    }
                    viewModel.uiState.value shouldBe FileDetailUiState.Content(file = file)
                }
            }
        }

        test("TC-FILE-DETAIL-FEATURE-008 TC-FILE-DETAIL-DATA-002 수정에 성공하면 알리고 서버가 돌려준 값이 저장된 값이 된다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val detail = fixtureMonkey.giveMeOne<DiaryFileDetail>()
                val updated = file.copy(name = detail.name, title = fixtureMonkey.giveMeOne<String>(), description = detail.description)
                val updateUseCase = mockk<UpdateFileUseCase>()
                coEvery { updateUseCase(parameter = UpdateFileUseCase.Parameter(id = file.id, detail = detail)) } returns Result.success(updated)
                val viewModel = loadedViewModel(file = file, updateUseCase = updateUseCase)

                viewModel.effect.test {
                    viewModel.update(detail = detail)
                    awaitItem() shouldBe FileDetailEffect.UpdateSucceeded
                }

                viewModel.uiState.value shouldBe FileDetailUiState.Content(file = updated)
                coVerify(exactly = 1) { updateUseCase(parameter = UpdateFileUseCase.Parameter(id = file.id, detail = detail)) }
            }
        }

        test("TC-FILE-DETAIL-FEATURE-009 수정하는 동안 진행 중이고 다시 실행해도 한 번만 보낸다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val updateUseCase = mockk<UpdateFileUseCase>()
                coEvery { updateUseCase(parameter = any()) } coAnswers { awaitCancellation() }
                val viewModel = loadedViewModel(file = file, updateUseCase = updateUseCase)

                viewModel.update(detail = fixtureMonkey.giveMeOne<DiaryFileDetail>())
                advanceUntilIdle()
                viewModel.update(detail = fixtureMonkey.giveMeOne<DiaryFileDetail>())
                advanceUntilIdle()

                viewModel.uiState.value shouldBe FileDetailUiState.Content(file = file, isUpdateInProgress = true)
                coVerify(exactly = 1) { updateUseCase(parameter = any()) }
            }
        }

        test("TC-FILE-DETAIL-FEATURE-011 수정에 실패하면 실패를 알리고 저장된 값은 그대로다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val updateUseCase = mockk<UpdateFileUseCase>()
                coEvery { updateUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel = loadedViewModel(file = file, updateUseCase = updateUseCase)

                viewModel.effect.test {
                    viewModel.update(detail = fixtureMonkey.giveMeOne<DiaryFileDetail>())
                    awaitItem() shouldBe FileDetailEffect.UpdateFailed
                }

                viewModel.uiState.value shouldBe FileDetailUiState.Content(file = file)
            }
        }

        test("TC-FILE-DETAIL-FEATURE-012 다운로드는 마지막으로 저장된 파일로 내려받는다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val updated = file.copy(name = "saved-${fixtureMonkey.giveMeOne<Int>()}.txt")
                val updateUseCase = mockk<UpdateFileUseCase>()
                coEvery { updateUseCase(parameter = any()) } returns Result.success(updated)
                val downloadUseCase = mockk<DownloadFileUseCase>()
                coEvery { downloadUseCase(parameter = any()) } returns Result.success(FileDownloadResult.Saved(name = updated.name))
                val viewModel = loadedViewModel(file = file, updateUseCase = updateUseCase, downloadUseCase = downloadUseCase)

                viewModel.download()
                advanceUntilIdle()
                viewModel.update(detail = fixtureMonkey.giveMeOne<DiaryFileDetail>())
                advanceUntilIdle()
                viewModel.download()
                advanceUntilIdle()

                coVerify(exactly = 1) { downloadUseCase(parameter = file) }
                coVerify(exactly = 1) { downloadUseCase(parameter = updated) }
            }
        }

        test("TC-FILE-DETAIL-FEATURE-013 내려받는 동안 진행 중이고 다시 실행해도 한 번만 내려받는다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val downloadUseCase = mockk<DownloadFileUseCase>()
                coEvery { downloadUseCase(parameter = file) } coAnswers { awaitCancellation() }
                val viewModel = loadedViewModel(file = file, downloadUseCase = downloadUseCase)

                viewModel.download()
                advanceUntilIdle()
                viewModel.download()
                advanceUntilIdle()

                viewModel.uiState.value shouldBe FileDetailUiState.Content(file = file, isDownloadInProgress = true)
                coVerify(exactly = 1) { downloadUseCase(parameter = file) }
            }
        }

        test("TC-FILE-DETAIL-FEATURE-014 다운로드 결과를 알리고 진행 중이 끝난다") {
            runTest(mainDispatcher) {
                val savedName = fixtureMonkey.giveMeOne<String>()

                listOf(
                    Result.success<FileDownloadResult>(FileDownloadResult.Saved(name = savedName)) to FileDetailEffect.DownloadSucceeded(name = savedName),
                    Result.failure<FileDownloadResult>(FileDownloadDirectoryUnavailableException()) to FileDetailEffect.DownloadDirectoryUnavailable,
                    Result.failure<FileDownloadResult>(IllegalStateException()) to FileDetailEffect.DownloadFailed,
                ).forEach { (result, expected) ->
                    val file = fixtureMonkey.diaryFile()
                    val downloadUseCase = mockk<DownloadFileUseCase>()
                    coEvery { downloadUseCase(parameter = file) } returns result
                    val viewModel = loadedViewModel(file = file, downloadUseCase = downloadUseCase)

                    viewModel.effect.test {
                        viewModel.download()
                        awaitItem() shouldBe expected
                    }

                    viewModel.uiState.value shouldBe FileDetailUiState.Content(file = file)
                }
            }
        }

        test("TC-FILE-DETAIL-FEATURE-015 브라우저에 파일을 넘겨 끝나면 따로 알리지 않고 진행 중이 끝난다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val downloadUseCase = mockk<DownloadFileUseCase>()
                coEvery { downloadUseCase(parameter = file) } returns Result.success(FileDownloadResult.HandedToBrowser)
                val viewModel = loadedViewModel(file = file, downloadUseCase = downloadUseCase)

                viewModel.effect.test {
                    viewModel.download()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                viewModel.uiState.value shouldBe FileDetailUiState.Content(file = file)
            }
        }

        test("TC-FILE-DETAIL-FEATURE-016 수정과 다운로드는 서로 막지 않는다") {
            runTest(mainDispatcher) {
                listOf(true, false).forEach { isUpdateFirst ->
                    val file = fixtureMonkey.diaryFile()
                    val updateUseCase = mockk<UpdateFileUseCase>()
                    coEvery { updateUseCase(parameter = any()) } coAnswers { awaitCancellation() }
                    val downloadUseCase = mockk<DownloadFileUseCase>()
                    coEvery { downloadUseCase(parameter = file) } coAnswers { awaitCancellation() }
                    val viewModel = loadedViewModel(file = file, updateUseCase = updateUseCase, downloadUseCase = downloadUseCase)

                    if (isUpdateFirst) {
                        viewModel.update(detail = fixtureMonkey.giveMeOne<DiaryFileDetail>())
                        viewModel.download()
                    } else {
                        viewModel.download()
                        viewModel.update(detail = fixtureMonkey.giveMeOne<DiaryFileDetail>())
                    }
                    advanceUntilIdle()

                    viewModel.uiState.value shouldBe FileDetailUiState.Content(file = file, isUpdateInProgress = true, isDownloadInProgress = true)
                    coVerify(exactly = 1) { updateUseCase(parameter = any()) }
                    coVerify(exactly = 1) { downloadUseCase(parameter = file) }
                }
            }
        }

        test("TC-FILE-DETAIL-FEATURE-018 계정을 확정하지 않아 수정과 다운로드가 실패하면 실패로 알린다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val updateUseCase = mockk<UpdateFileUseCase>()
                coEvery { updateUseCase(parameter = any()) } returns Result.failure(IllegalStateException("account"))
                val downloadUseCase = mockk<DownloadFileUseCase>()
                coEvery { downloadUseCase(parameter = file) } returns Result.failure(IllegalStateException("account"))
                val viewModel = loadedViewModel(file = file, updateUseCase = updateUseCase, downloadUseCase = downloadUseCase)

                viewModel.effect.test {
                    viewModel.update(detail = fixtureMonkey.giveMeOne<DiaryFileDetail>())
                    awaitItem() shouldBe FileDetailEffect.UpdateFailed
                    viewModel.download()
                    awaitItem() shouldBe FileDetailEffect.DownloadFailed
                }
            }
        }

        test("TC-FILE-DETAIL-DOMAIN-005 화면을 떠나면 진행 중이던 다운로드를 멈추고 결과를 알리지 않는다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                var isCancelled = false
                val downloadUseCase = mockk<DownloadFileUseCase>()
                coEvery { downloadUseCase(parameter = file) } coAnswers {
                    try {
                        awaitCancellation()
                    } catch (exception: CancellationException) {
                        isCancelled = true
                        throw exception
                    }
                }
                val viewModel = loadedViewModel(file = file, downloadUseCase = downloadUseCase)
                val store = ViewModelStore().apply { put(key = "FileDetail", viewModel = viewModel) }

                viewModel.effect.test {
                    viewModel.download()
                    advanceUntilIdle()
                    store.clear()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                isCancelled.shouldBeTrue()
            }
        }

        test("TC-FILE-DETAIL-DOMAIN-010 화면을 떠난 뒤 서버가 수정에 성공해도 되돌리는 요청을 보내지 않고 결과를 알리지 않는다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val detail = fixtureMonkey.giveMeOne<DiaryFileDetail>()
                val response = CompletableDeferred<Result<DiaryFile>>()
                val updateUseCase = mockk<UpdateFileUseCase>()
                coEvery { updateUseCase(parameter = any()) } coAnswers { response.await() }
                val viewModel = loadedViewModel(file = file, updateUseCase = updateUseCase)
                val store = ViewModelStore().apply { put(key = "FileDetail", viewModel = viewModel) }

                viewModel.effect.test {
                    viewModel.update(detail = detail)
                    advanceUntilIdle()
                    store.clear()
                    response.complete(Result.success(file.copy(title = detail.title, name = detail.name, description = detail.description)))
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { updateUseCase(parameter = any()) }
                coVerify(exactly = 1) { updateUseCase(parameter = UpdateFileUseCase.Parameter(id = file.id, detail = detail)) }
            }
        }

        test("TC-FILE-DETAIL-DOMAIN-007 진행 중인 수정과 다운로드는 끝난 뒤 결과를 차례로 알린다") {
            runTest(mainDispatcher) {
                val file = fixtureMonkey.diaryFile()
                val updateResponse = CompletableDeferred<Result<DiaryFile>>()
                val downloadResponse = CompletableDeferred<Result<FileDownloadResult>>()
                val updateUseCase = mockk<UpdateFileUseCase>()
                coEvery { updateUseCase(parameter = any()) } coAnswers { updateResponse.await() }
                val downloadUseCase = mockk<DownloadFileUseCase>()
                coEvery { downloadUseCase(parameter = file) } coAnswers { downloadResponse.await() }
                val viewModel = loadedViewModel(file = file, updateUseCase = updateUseCase, downloadUseCase = downloadUseCase)

                viewModel.update(detail = fixtureMonkey.giveMeOne<DiaryFileDetail>())
                viewModel.download()
                advanceUntilIdle()

                viewModel.effect.test {
                    updateResponse.complete(Result.success(file))
                    awaitItem() shouldBe FileDetailEffect.UpdateSucceeded
                    downloadResponse.complete(Result.failure(IllegalStateException()))
                    awaitItem() shouldBe FileDetailEffect.DownloadFailed
                }

                viewModel.uiState.value
                    .shouldBeInstanceOf<FileDetailUiState.Content>()
                    .isUpdateInProgress shouldBe false
            }
        }
    }

    private suspend fun kotlinx.coroutines.test.TestScope.loadedViewModel(
        file: DiaryFile,
        updateUseCase: UpdateFileUseCase = mockk(),
        downloadUseCase: DownloadFileUseCase = mockk(),
    ): FileDetailViewModel {
        val fetchUseCase = mockk<FetchFileUseCase>()
        coEvery { fetchUseCase(parameter = file.id) } returns Result.success(file)
        val viewModel = viewModel(id = file.id, fetchUseCase = fetchUseCase, updateUseCase = updateUseCase, downloadUseCase = downloadUseCase)

        backgroundScope.launch { viewModel.uiState.collect {} }
        viewModel.fetch()
        advanceUntilIdle()

        return viewModel
    }

    private fun viewModel(
        id: Uuid,
        fetchUseCase: FetchFileUseCase = mockk(),
        updateUseCase: UpdateFileUseCase = mockk(),
        downloadUseCase: DownloadFileUseCase = mockk(),
    ): FileDetailViewModel =
        FileDetailViewModel(
            id = id,
            fetchFileUseCase = fetchUseCase,
            updateFileUseCase = updateUseCase,
            downloadFileUseCase = downloadUseCase,
        )
}
