@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.home

import app.cash.turbine.test
import io.github.taetae98coding.diary.domain.browser.usecase.FindChromeSessionImportSupportUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicDownloadSupportUseCase
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
        SettingHomeItem.DOWNLOAD,
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

        test("TC-SETTING-HOME-FEATURE-005 TC-SETTING-HOME-DOMAIN-001 브라우저와 다운로드 항목을 제공하는 환경에서는 다섯 항목을 선언된 순서로 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(chromeResult = Result.success(true), downloadResult = Result.success(true))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe
                        SettingHomeUiState.Loaded(
                            itemList =
                                listOf(
                                    SettingHomeItem.HOLIDAY,
                                    SettingHomeItem.MAP,
                                    SettingHomeItem.GEMINI,
                                    SettingHomeItem.BROWSER,
                                    SettingHomeItem.DOWNLOAD,
                                ),
                        )
                }
            }
        }

        test("TC-SETTING-HOME-FEATURE-011 브라우저 항목을 제공하지 않는 환경에서는 그 항목을 빼고 같은 순서로 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(chromeResult = Result.success(false), downloadResult = Result.success(true))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe
                        SettingHomeUiState.Loaded(
                            itemList = listOf(SettingHomeItem.HOLIDAY, SettingHomeItem.MAP, SettingHomeItem.GEMINI, SettingHomeItem.DOWNLOAD),
                        )
                }
            }
        }

        test("TC-SETTING-HOME-FEATURE-014 다운로드 항목을 제공하지 않는 환경에서는 그 항목을 빼고 같은 순서로 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(chromeResult = Result.success(false), downloadResult = Result.success(false))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe
                        SettingHomeUiState.Loaded(
                            itemList = listOf(SettingHomeItem.HOLIDAY, SettingHomeItem.MAP, SettingHomeItem.GEMINI),
                        )
                }
            }
        }

        test("TC-SETTING-HOME-FEATURE-012 제공하는 항목을 확인하기 전에는 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val pending = CompletableDeferred<Result<Boolean>>()
                val chromeUseCase = mockk<FindChromeSessionImportSupportUseCase>()
                coEvery { chromeUseCase(Unit) } coAnswers { pending.await() }
                val downloadUseCase = mockk<FindMusicDownloadSupportUseCase>()
                coEvery { downloadUseCase(Unit) } returns Result.success(true)
                val viewModel =
                    SettingHomeViewModel(
                        findChromeSessionImportSupportUseCase = chromeUseCase,
                        findMusicDownloadSupportUseCase = downloadUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-SETTING-HOME-FEATURE-015 제공 여부를 읽지 못하면 확인 중 상태를 유지한다") {
            val caseList =
                listOf(
                    Result.failure<Boolean>(IllegalStateException("read error")) to Result.success(true),
                    Result.success(true) to Result.failure(IllegalStateException("read error")),
                )

            caseList.forEach { (chromeResult, downloadResult) ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(chromeResult = chromeResult, downloadResult = downloadResult)

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingHomeUiState.Loading
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-SETTING-HOME-FEATURE-016 화면을 보는 동안에는 제공 여부가 바뀌어도 항목 목록이 바뀌지 않는다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))
                val viewModel = viewModel(downloadUseCase = downloadUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList)
                    advanceTimeBy(UI_STOP_TIMEOUT_MILLIS * 2)
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-SETTING-HOME-FEATURE-017 화면을 떠났다가 다시 들어오면 제공하는 항목을 다시 확인한다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))

                viewModel(downloadUseCase = downloadUseCase).uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList)
                }

                viewModel(downloadUseCase = downloadUseCase).uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList - SettingHomeItem.DOWNLOAD)
                }
                coVerify(exactly = 2) { downloadUseCase(Unit) }
            }
        }

        test("TC-SETTING-HOME-FEATURE-018 다른 앱에 다녀온 지 5초가 지나 돌아오면 제공하는 항목을 다시 확인한다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))
                val viewModel = viewModel(downloadUseCase = downloadUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList)
                }
                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS + 1)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList)
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList - SettingHomeItem.DOWNLOAD)
                }
            }
        }

        test("TC-SETTING-HOME-FEATURE-019 다른 앱에 다녀온 지 5초 안에 돌아오면 확인해 둔 항목을 그대로 둔다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))
                val viewModel = viewModel(downloadUseCase = downloadUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList)
                }
                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS - 1)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList)
                    advanceUntilIdle()
                    expectNoEvents()
                }
                coVerify(exactly = 1) { downloadUseCase(Unit) }
            }
        }
        test("TC-SETTING-HOME-FEATURE-020 단독으로 표시된 세부 설정에 다녀온 지 5초 안에 돌아오면 확인해 둔 항목을 그대로 둔다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))
                val viewModel = viewModel(downloadUseCase = downloadUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList)
                }
                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS - 1)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList)
                    advanceUntilIdle()
                    expectNoEvents()
                }
                coVerify(exactly = 1) { downloadUseCase(Unit) }
            }
        }

        test("TC-SETTING-HOME-FEATURE-021 단독으로 표시된 세부 설정에 다녀온 지 5초가 지나 돌아오면 제공하는 항목을 다시 확인한다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))
                val viewModel = viewModel(downloadUseCase = downloadUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList)
                }
                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS + 1)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList)
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList - SettingHomeItem.DOWNLOAD)
                }
            }
        }

        test("TC-SETTING-HOME-FEATURE-022 설정 목록과 세부 설정이 함께 표시되는 동안에는 세부 설정을 골라도 항목을 다시 확인하지 않는다") {
            runTest(mainDispatcher) {
                val downloadUseCase = downloadUseCaseReturning(Result.success(true), Result.success(false))
                val viewModel = viewModel(downloadUseCase = downloadUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe SettingHomeUiState.Loaded(itemList = fullItemList)
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
    ): FindMusicDownloadSupportUseCase {
        val downloadUseCase = mockk<FindMusicDownloadSupportUseCase>()
        coEvery { downloadUseCase(Unit) } returnsMany listOf(first, next)
        return downloadUseCase
    }

    private fun viewModel(downloadUseCase: FindMusicDownloadSupportUseCase): SettingHomeViewModel {
        val chromeUseCase = mockk<FindChromeSessionImportSupportUseCase>()
        coEvery { chromeUseCase(Unit) } returns Result.success(true)

        return SettingHomeViewModel(
            findChromeSessionImportSupportUseCase = chromeUseCase,
            findMusicDownloadSupportUseCase = downloadUseCase,
        )
    }

    private fun viewModel(
        chromeResult: Result<Boolean>,
        downloadResult: Result<Boolean>,
    ): SettingHomeViewModel {
        val chromeUseCase = mockk<FindChromeSessionImportSupportUseCase>()
        coEvery { chromeUseCase(Unit) } returns chromeResult
        val downloadUseCase = mockk<FindMusicDownloadSupportUseCase>()
        coEvery { downloadUseCase(Unit) } returns downloadResult

        return SettingHomeViewModel(
            findChromeSessionImportSupportUseCase = chromeUseCase,
            findMusicDownloadSupportUseCase = downloadUseCase,
        )
    }
}
