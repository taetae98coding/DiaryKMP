@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.map

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.SetDefaultMapProviderUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class SettingMapViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-SETTING-MAP-FEATURE-011 저장된 기본 지도를 확인하기 전에는 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { getDefaultMapProviderUseCase(Unit) } returns emptyFlow()
                val viewModel =
                    SettingMapViewModel(
                        getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
                        setDefaultMapProviderUseCase = mockk(),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMapUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-SETTING-MAP-FEATURE-006 저장된 기본 지도를 선택 상태로 제공한다") {
            MapProvider.entries.forEach { provider ->
                runTest(mainDispatcher) {
                    val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                    every { getDefaultMapProviderUseCase(Unit) } returns flowOf(Result.success(provider))
                    val viewModel =
                        SettingMapViewModel(
                            getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
                            setDefaultMapProviderUseCase = mockk(),
                        )

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingMapUiState.Loading
                        awaitItem() shouldBe SettingMapUiState.Loaded(defaultProvider = provider)
                    }
                }
            }
        }

        test("TC-SETTING-MAP-FEATURE-008 선택되어 있지 않은 지도를 선택하면 저장되고 선택 상태가 옮겨진다") {
            runTest(mainDispatcher) {
                val storedProvider = MutableStateFlow(Result.success(MapProvider.NAVER))
                val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { getDefaultMapProviderUseCase(Unit) } returns storedProvider
                val setDefaultMapProviderUseCase = mockk<SetDefaultMapProviderUseCase>()
                coEvery { setDefaultMapProviderUseCase(MapProvider.GOOGLE) } coAnswers {
                    storedProvider.value = Result.success(MapProvider.GOOGLE)
                    Result.success(Unit)
                }
                val viewModel =
                    SettingMapViewModel(
                        getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
                        setDefaultMapProviderUseCase = setDefaultMapProviderUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMapUiState.Loading
                    awaitItem() shouldBe SettingMapUiState.Loaded(defaultProvider = MapProvider.NAVER)

                    viewModel.selectDefaultProvider(provider = MapProvider.GOOGLE)
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingMapUiState.Loaded(defaultProvider = MapProvider.GOOGLE)
                }

                coVerify(exactly = 1) { setDefaultMapProviderUseCase(MapProvider.GOOGLE) }
            }
        }

        test("TC-SETTING-MAP-FEATURE-009 이미 선택된 지도를 다시 선택하면 아무것도 바뀌지 않는다") {
            runTest(mainDispatcher) {
                val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { getDefaultMapProviderUseCase(Unit) } returns MutableStateFlow(Result.success(MapProvider.NAVER))
                val setDefaultMapProviderUseCase = mockk<SetDefaultMapProviderUseCase>()
                val viewModel =
                    SettingMapViewModel(
                        getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
                        setDefaultMapProviderUseCase = setDefaultMapProviderUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMapUiState.Loading
                    awaitItem() shouldBe SettingMapUiState.Loaded(defaultProvider = MapProvider.NAVER)

                    viewModel.selectDefaultProvider(provider = MapProvider.NAVER)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 0) { setDefaultMapProviderUseCase(any()) }
            }
        }

        test("TC-SETTING-MAP-FEATURE-010 저장되지 않으면 선택 상태가 옮겨지지 않는다") {
            runTest(mainDispatcher) {
                val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { getDefaultMapProviderUseCase(Unit) } returns MutableStateFlow(Result.success(MapProvider.NAVER))
                val setDefaultMapProviderUseCase = mockk<SetDefaultMapProviderUseCase>()
                coEvery { setDefaultMapProviderUseCase(MapProvider.GOOGLE) } returns Result.failure(IllegalStateException("save error"))
                val viewModel =
                    SettingMapViewModel(
                        getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
                        setDefaultMapProviderUseCase = setDefaultMapProviderUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMapUiState.Loading
                    awaitItem() shouldBe SettingMapUiState.Loaded(defaultProvider = MapProvider.NAVER)

                    viewModel.selectDefaultProvider(provider = MapProvider.GOOGLE)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-SETTING-MAP-FEATURE-011 기본 지도를 읽지 못하면 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { getDefaultMapProviderUseCase(Unit) } returns flowOf(Result.failure(IllegalStateException("read error")))
                val viewModel =
                    SettingMapViewModel(
                        getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
                        setDefaultMapProviderUseCase = mockk(),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMapUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }
    }
}
