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

        test("TC-311eb985-5d6a-4316-84a7-42877c30c595 불러오는 동안에는 불러오는 중 상태다") {
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

        test("TC-e4524154-e04b-42b6-9605-e64a8873f7a5 불러오기에 실패하면 실패 상태다") {
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

        test("TC-fe3f9a4f-fa53-4bb3-9856-89b2ee6d2f1c 실패한 뒤 다시 시도하면 다시 불러오고 그동안 불러오는 중 상태다") {
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

        test("TC-1f753104-5be1-47c2-a16c-5ae4605d9545 불러오는 중이거나 불러온 뒤에는 다시 시도를 처리하지 않는다") {
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

        test("TC-9f38cac4-1cbb-49b5-851f-b225f44255eb TC-a3f4dfdb-e1bd-45c2-9cfc-36710034083e 불러오기에 성공하면 이 파일의 정보를 한 번 불러와 내용 상태가 된다") {
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

        test("TC-3c33f9db-bb3b-447e-8109-25ac4c34aad4 화면에 다시 들어와도 이미 불러온 정보를 다시 불러오지 않는다") {
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

        test("TC-f4446f67-dd3d-4b7f-a77f-d426fff2763d TC-fc9976b9-ffb7-40c5-b037-cbf6a3b979f4 TC-049583bf-ac42-46b6-b79f-1c00b4f53ba3 TC-b4b5c536-1561-4a32-a836-c3a4358e907b 수정 성립 조건을 어기면 이유별로 알리고 내용은 바뀌지 않는다") {
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

        test("TC-d7989670-bd8b-4ee2-b182-7ccea3e78db5 TC-cea02561-804f-4c46-99c6-404560236918 수정에 성공하면 알리고 서버가 돌려준 값이 저장된 값이 된다") {
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

        test("TC-e003678c-6093-4a0b-a547-529034563a5b 수정하는 동안 진행 중이고 다시 실행해도 한 번만 보낸다") {
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

        test("TC-960b0057-5c59-4e39-a64b-9505dce274f6 수정에 실패하면 실패를 알리고 저장된 값은 그대로다") {
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

        test("TC-8a9c6995-d9cd-4082-ba1f-e2019e03b8fe 다운로드는 마지막으로 저장된 파일로 내려받는다") {
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

        test("TC-3db2ad43-e11a-4aeb-ba5d-8782fc472d69 내려받는 동안 진행 중이고 다시 실행해도 한 번만 내려받는다") {
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

        test("TC-b7301a82-d6ad-41f0-b36b-51306b263a7f 다운로드 결과를 알리고 진행 중이 끝난다") {
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

        test("TC-21540fda-5da1-47f7-bd56-38d1e054ba04 브라우저에 파일을 넘겨 끝나면 따로 알리지 않고 진행 중이 끝난다") {
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

        test("TC-24a020d7-5144-4e80-b8ec-4bdee3b8f315 수정과 다운로드는 서로 막지 않는다") {
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

        test("TC-dd6a353f-2bc5-4709-a362-218ee5cdfd32 계정을 확정하지 않아 수정과 다운로드가 실패하면 실패로 알린다") {
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

        test("TC-7e52f974-6964-49bc-9a54-ca74f4b27076 화면을 떠나면 진행 중이던 다운로드를 멈추고 결과를 알리지 않는다") {
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

        test("TC-8b296906-1657-407d-8fd8-1e47037b9e79 화면을 떠난 뒤 서버가 수정에 성공해도 되돌리는 요청을 보내지 않고 결과를 알리지 않는다") {
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

        test("TC-1d35a91d-33e0-444d-a845-c66a87674bd6 진행 중인 수정과 다운로드는 끝난 뒤 결과를 차례로 알린다") {
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
