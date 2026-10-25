@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.home

import app.cash.turbine.test
import io.github.taetae98coding.diary.domain.browser.usecase.ReadChromeSessionImportSupportUseCase
import io.github.taetae98coding.diary.domain.file.usecase.ReadFileDownloadDirectorySupportUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.ReadMusicDownloadSupportUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.UI_STOP_TIMEOUT_MILLIS
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private val fullItemList =
    listOf(
        SettingHomeItem.HOLIDAY,
        SettingHomeItem.MAP,
        SettingHomeItem.GEMINI,
        SettingHomeItem.BROWSER,
        SettingHomeItem.PLAYLIST,
        SettingHomeItem.FILE,
    )

class SettingHomeViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-26463de7-89f3-4930-ba91-c3c650a71ea0 TC-5a53040e-4d56-49b3-93be-c2a4a640f7d7 브라우저, 플레이리스트와 파일 항목을 제공하는 환경에서는 여섯 항목을 선언된 순서로 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(chromeResult = Result.success(true), downloadResult = Result.success(true))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe
                        SettingHomeUiState.Content(
                            itemList =
                                listOf(
                                    SettingHomeItem.HOLIDAY,
                                    SettingHomeItem.MAP,
                                    SettingHomeItem.GEMINI,
                                    SettingHomeItem.BROWSER,
                                    SettingHomeItem.PLAYLIST,
                                    SettingHomeItem.FILE,
                                ),
                        )
                }
            }
        }

        test("TC-2f983b47-41cf-4382-8b81-09b996c3ab20 브라우저 항목을 제공하지 않는 환경에서는 그 항목을 빼고 같은 순서로 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(chromeResult = Result.success(false), downloadResult = Result.success(true))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe
                        SettingHomeUiState.Content(
                            itemList = listOf(SettingHomeItem.HOLIDAY, SettingHomeItem.MAP, SettingHomeItem.GEMINI, SettingHomeItem.PLAYLIST, SettingHomeItem.FILE),
                        )
                }
            }
        }

        test("TC-cfe6e208-1394-4aac-aa77-3ea5630d41c1 브라우저, 플레이리스트와 파일 항목을 제공하지 않는 환경에서는 그 항목을 빼고 같은 순서로 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(chromeResult = Result.success(false), downloadResult = Result.success(false), fileResult = Result.success(false))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe
                        SettingHomeUiState.Content(
                            itemList = listOf(SettingHomeItem.HOLIDAY, SettingHomeItem.MAP, SettingHomeItem.GEMINI),
                        )
                }
            }
        }

        test("TC-b229f1d1-84b1-4b8c-9994-3e063807175a 파일 항목을 제공하지 않는 환경에서는 그 항목을 빼고 같은 순서로 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(chromeResult = Result.success(true), downloadResult = Result.success(true), fileResult = Result.success(false))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList - SettingHomeItem.FILE)
                }
            }
        }

        test("TC-1d05e6d2-8452-43cb-b3a4-2eb757fbb510 제공하는 항목을 확인하기 전에는 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val pending = CompletableDeferred<Result<Boolean>>()
                val chromeUseCase = mockk<ReadChromeSessionImportSupportUseCase>()
                coEvery { chromeUseCase(Unit) } coAnswers { pending.await() }
                val downloadUseCase = mockk<ReadMusicDownloadSupportUseCase>()
                coEvery { downloadUseCase(Unit) } returns Result.success(true)
                val viewModel =
                    SettingHomeViewModel(
                        readChromeSessionImportSupportUseCase = chromeUseCase,
                        readMusicDownloadSupportUseCase = downloadUseCase,
                        readFileDownloadDirectorySupportUseCase = fileUseCase(Result.success(true)),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-65478d8f-82d4-4a82-9f7d-31b0e7885809 제공 여부를 읽지 못하면 확인 중 상태를 유지한다") {
            val failure = Result.failure<Boolean>(IllegalStateException("read error"))
            val caseList =
                listOf(
                    Triple(failure, Result.success(true), Result.success(true)),
                    Triple(Result.success(true), failure, Result.success(true)),
                    Triple(Result.success(true), Result.success(true), failure),
                )

            caseList.forEach { (chromeResult, downloadResult, fileResult) ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(chromeResult = chromeResult, downloadResult = downloadResult, fileResult = fileResult)

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingHomeUiState.Loading
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-1146e230-f3a9-493c-afa2-61762b676e52 화면을 보는 동안에는 제공 여부가 바뀌어도 항목 목록이 바뀌지 않는다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))
                val viewModel = viewModel(downloadUseCase = downloadUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList)
                    advanceTimeBy(UI_STOP_TIMEOUT_MILLIS * 2)
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-7079bfbe-b398-442a-8fc2-ef04b7653600 화면을 떠났다가 다시 들어오면 제공하는 항목을 다시 확인한다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))

                viewModel(downloadUseCase = downloadUseCase).uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList)
                }

                viewModel(downloadUseCase = downloadUseCase).uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList - SettingHomeItem.PLAYLIST)
                }
                coVerify(exactly = 2) { downloadUseCase(Unit) }
            }
        }

        test("TC-1371d92d-5b10-418e-a508-cdf4917b56d8 다른 앱에 다녀온 지 5초가 지나 돌아오면 제공하는 항목을 다시 확인한다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))
                val viewModel = viewModel(downloadUseCase = downloadUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList)
                }
                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS + 1)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList)
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList - SettingHomeItem.PLAYLIST)
                }
            }
        }

        test("TC-ba40a9f9-b4d9-4da9-8b28-c2dadaa0ccdf 다른 앱에 다녀온 지 5초 안에 돌아오면 확인해 둔 항목을 그대로 둔다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))
                val viewModel = viewModel(downloadUseCase = downloadUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList)
                }
                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS - 1)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList)
                    advanceUntilIdle()
                    expectNoEvents()
                }
                coVerify(exactly = 1) { downloadUseCase(Unit) }
            }
        }
        test("TC-c3d4cac7-43f9-42d1-b034-954cd5a77896 단독으로 표시된 세부 설정에 다녀온 지 5초 안에 돌아오면 확인해 둔 항목을 그대로 둔다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))
                val viewModel = viewModel(downloadUseCase = downloadUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList)
                }
                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS - 1)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList)
                    advanceUntilIdle()
                    expectNoEvents()
                }
                coVerify(exactly = 1) { downloadUseCase(Unit) }
            }
        }

        test("TC-c43989de-87c6-4ee6-a669-d08f5dd51c5d 단독으로 표시된 세부 설정에 다녀온 지 5초가 지나 돌아오면 제공하는 항목을 다시 확인한다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))
                val viewModel = viewModel(downloadUseCase = downloadUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList)
                }
                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS + 1)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList)
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList - SettingHomeItem.PLAYLIST)
                }
            }
        }

        test("TC-1ab3d452-c22b-46cf-aee1-0067d6d22abc 설정 목록과 세부 설정이 함께 표시되는 동안에는 세부 설정을 골라도 항목을 다시 확인하지 않는다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))
                val viewModel = viewModel(downloadUseCase = downloadUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Content(itemList = fullItemList)
                    advanceTimeBy(UI_STOP_TIMEOUT_MILLIS + 1)
                    advanceUntilIdle()
                    expectNoEvents()
                }
                coVerify(exactly = 1) { downloadUseCase(Unit) }
            }
        }
    }

    private fun downloadUseCaseReturning(
        first: Result<Boolean>,
        next: Result<Boolean>,
    ): ReadMusicDownloadSupportUseCase {
        val downloadUseCase = mockk<ReadMusicDownloadSupportUseCase>()
        coEvery { downloadUseCase(Unit) } returnsMany listOf(first, next)
        return downloadUseCase
    }

    private fun viewModel(downloadUseCase: ReadMusicDownloadSupportUseCase): SettingHomeViewModel {
        val chromeUseCase = mockk<ReadChromeSessionImportSupportUseCase>()
        coEvery { chromeUseCase(Unit) } returns Result.success(true)

        return SettingHomeViewModel(
            readChromeSessionImportSupportUseCase = chromeUseCase,
            readMusicDownloadSupportUseCase = downloadUseCase,
            readFileDownloadDirectorySupportUseCase = fileUseCase(Result.success(true)),
        )
    }

    private fun fileUseCase(result: Result<Boolean>): ReadFileDownloadDirectorySupportUseCase {
        val fileUseCase = mockk<ReadFileDownloadDirectorySupportUseCase>()
        coEvery { fileUseCase(Unit) } returns result
        return fileUseCase
    }

    private fun viewModel(
        chromeResult: Result<Boolean>,
        downloadResult: Result<Boolean>,
        fileResult: Result<Boolean> = Result.success(true),
    ): SettingHomeViewModel {
        val chromeUseCase = mockk<ReadChromeSessionImportSupportUseCase>()
        coEvery { chromeUseCase(Unit) } returns chromeResult
        val downloadUseCase = mockk<ReadMusicDownloadSupportUseCase>()
        coEvery { downloadUseCase(Unit) } returns downloadResult

        return SettingHomeViewModel(
            readChromeSessionImportSupportUseCase = chromeUseCase,
            readMusicDownloadSupportUseCase = downloadUseCase,
            readFileDownloadDirectorySupportUseCase = fileUseCase(fileResult),
        )
    }
}
