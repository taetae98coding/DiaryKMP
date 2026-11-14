@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.map

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.SetDefaultMapProviderUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
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

        test("TC-b3d3d71c-dc8b-4df2-a279-68e891d1d5ac 저장된 기본 지도를 확인하기 전에는 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { getDefaultMapProviderUseCase() } returns emptyFlow()
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

        test("TC-9d9c7d1e-01fb-45cb-b486-4280e3c9d708 저장된 기본 지도를 선택 상태로 제공한다") {
            MapProvider.entries.forEach { provider ->
                runTest(mainDispatcher) {
                    val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                    every { getDefaultMapProviderUseCase() } returns flowOf(Result.success(provider))
                    val viewModel =
                        SettingMapViewModel(
                            getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
                            setDefaultMapProviderUseCase = mockk(),
                        )

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingMapUiState.Loading
                        awaitItem() shouldBe SettingMapUiState.Content(defaultProvider = provider)
                    }
                }
            }
        }

        test("TC-5675f575-9e4c-44ce-9b35-6a1ff32ac7aa 선택되어 있지 않은 지도를 선택하면 저장되고 선택 상태가 옮겨진다") {
            listOf(
                MapProvider.NAVER to MapProvider.GOOGLE,
                MapProvider.GOOGLE to MapProvider.NAVER,
            ).forEach { (stored, selected) ->
                runTest(mainDispatcher) {
                    val storedProvider = MutableStateFlow(Result.success(stored))
                    val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                    every { getDefaultMapProviderUseCase() } returns storedProvider
                    val setDefaultMapProviderUseCase = mockk<SetDefaultMapProviderUseCase>()
                    coEvery { setDefaultMapProviderUseCase(selected) } coAnswers {
                        storedProvider.value = Result.success(selected)
                        Result.success(Unit)
                    }
                    val viewModel =
                        SettingMapViewModel(
                            getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
                            setDefaultMapProviderUseCase = setDefaultMapProviderUseCase,
                        )

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingMapUiState.Loading
                        awaitItem() shouldBe SettingMapUiState.Content(defaultProvider = stored)

                        viewModel.selectDefaultProvider(provider = selected)
                        advanceUntilIdle()

                        awaitItem() shouldBe SettingMapUiState.Content(defaultProvider = selected)
                    }

                    coVerify(exactly = 1) { setDefaultMapProviderUseCase(selected) }
                }
            }
        }

        test("TC-b4ecf394-17c5-4b09-a718-a8d8ede7b3c3 이미 선택된 지도를 다시 선택하면 아무것도 바뀌지 않는다") {
            runTest(mainDispatcher) {
                val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { getDefaultMapProviderUseCase() } returns MutableStateFlow(Result.success(MapProvider.NAVER))
                val setDefaultMapProviderUseCase = mockk<SetDefaultMapProviderUseCase>()
                val viewModel =
                    SettingMapViewModel(
                        getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
                        setDefaultMapProviderUseCase = setDefaultMapProviderUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMapUiState.Loading
                    awaitItem() shouldBe SettingMapUiState.Content(defaultProvider = MapProvider.NAVER)

                    viewModel.selectDefaultProvider(provider = MapProvider.NAVER)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 0) { setDefaultMapProviderUseCase(any()) }
            }
        }

        test("TC-578c5b17-14c5-4ca2-8013-6800ccdfd158 저장하는 동안 다시 선택해도 한 번만 저장한다") {
            runTest(mainDispatcher) {
                val saving = CompletableDeferred<Result<Unit>>()
                val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { getDefaultMapProviderUseCase() } returns MutableStateFlow(Result.success(MapProvider.NAVER))
                val setDefaultMapProviderUseCase = mockk<SetDefaultMapProviderUseCase>()
                coEvery { setDefaultMapProviderUseCase(MapProvider.GOOGLE) } coAnswers { saving.await() }
                val viewModel =
                    SettingMapViewModel(
                        getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
                        setDefaultMapProviderUseCase = setDefaultMapProviderUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMapUiState.Loading
                    awaitItem() shouldBe SettingMapUiState.Content(defaultProvider = MapProvider.NAVER)

                    viewModel.selectDefaultProvider(provider = MapProvider.GOOGLE)
                    advanceUntilIdle()
                    viewModel.selectDefaultProvider(provider = MapProvider.GOOGLE)
                    advanceUntilIdle()
                    saving.complete(Result.success(Unit))
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { setDefaultMapProviderUseCase(MapProvider.GOOGLE) }
            }
        }

        test("TC-aa38b8e1-3272-47ba-b270-f8d06880b9c1 저장이 끝난 뒤 다시 선택하면 저장한다") {
            runTest(mainDispatcher) {
                val storedProvider = MutableStateFlow(Result.success(MapProvider.NAVER))
                val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { getDefaultMapProviderUseCase() } returns storedProvider
                val setDefaultMapProviderUseCase = mockk<SetDefaultMapProviderUseCase>()
                coEvery { setDefaultMapProviderUseCase(any()) } coAnswers {
                    storedProvider.value = Result.success(firstArg())
                    Result.success(Unit)
                }
                val viewModel =
                    SettingMapViewModel(
                        getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
                        setDefaultMapProviderUseCase = setDefaultMapProviderUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMapUiState.Loading
                    awaitItem() shouldBe SettingMapUiState.Content(defaultProvider = MapProvider.NAVER)

                    viewModel.selectDefaultProvider(provider = MapProvider.GOOGLE)
                    advanceUntilIdle()
                    awaitItem() shouldBe SettingMapUiState.Content(defaultProvider = MapProvider.GOOGLE)

                    viewModel.selectDefaultProvider(provider = MapProvider.NAVER)
                    advanceUntilIdle()
                    awaitItem() shouldBe SettingMapUiState.Content(defaultProvider = MapProvider.NAVER)
                }

                coVerifyOrder {
                    setDefaultMapProviderUseCase(MapProvider.GOOGLE)
                    setDefaultMapProviderUseCase(MapProvider.NAVER)
                }
            }
        }

        test("TC-4f24cd13-6ed2-423e-a95a-3a83c293b027 저장되지 않으면 선택 상태가 옮겨지지 않는다") {
            runTest(mainDispatcher) {
                val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { getDefaultMapProviderUseCase() } returns MutableStateFlow(Result.success(MapProvider.NAVER))
                val setDefaultMapProviderUseCase = mockk<SetDefaultMapProviderUseCase>()
                coEvery { setDefaultMapProviderUseCase(MapProvider.GOOGLE) } returns Result.failure(IllegalStateException("save error"))
                val viewModel =
                    SettingMapViewModel(
                        getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
                        setDefaultMapProviderUseCase = setDefaultMapProviderUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingMapUiState.Loading
                    awaitItem() shouldBe SettingMapUiState.Content(defaultProvider = MapProvider.NAVER)

                    viewModel.selectDefaultProvider(provider = MapProvider.GOOGLE)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-b3d3d71c-dc8b-4df2-a279-68e891d1d5ac 기본 지도를 읽지 못하면 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { getDefaultMapProviderUseCase() } returns flowOf(Result.failure(IllegalStateException("read error")))
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
