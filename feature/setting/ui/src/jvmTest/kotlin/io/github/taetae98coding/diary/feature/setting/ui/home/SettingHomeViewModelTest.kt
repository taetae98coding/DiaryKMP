@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.home

import app.cash.turbine.test
import io.github.taetae98coding.diary.domain.setting.usecase.FindChromeSessionImportSupportUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

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

        test("TC-SETTING-HOME-FEATURE-005 TC-SETTING-HOME-DOMAIN-001 브라우저 항목을 제공하는 환경에서는 네 항목을 선언된 순서로 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(Result.success(true))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    awaitItem() shouldBe
                        SettingHomeUiState.Loaded(
                            itemList = listOf(SettingHomeItem.HOLIDAY, SettingHomeItem.MAP, SettingHomeItem.GEMINI, SettingHomeItem.BROWSER),
                        )
                }
            }
        }

        test("TC-SETTING-HOME-FEATURE-011 브라우저 항목을 제공하지 않는 환경에서는 그 항목을 빼고 같은 순서로 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(Result.success(false))

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
                val useCase = mockk<FindChromeSessionImportSupportUseCase>()
                coEvery { useCase(Unit) } coAnswers { pending.await() }
                val viewModel = SettingHomeViewModel(findChromeSessionImportSupportUseCase = useCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-SETTING-HOME-FEATURE-012 제공 여부를 읽지 못하면 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(Result.failure(IllegalStateException("read error")))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHomeUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }
    }

    private fun viewModel(result: Result<Boolean>): SettingHomeViewModel {
        val useCase = mockk<FindChromeSessionImportSupportUseCase>()
        coEvery { useCase(Unit) } returns result

        return SettingHomeViewModel(findChromeSessionImportSupportUseCase = useCase)
    }
}
