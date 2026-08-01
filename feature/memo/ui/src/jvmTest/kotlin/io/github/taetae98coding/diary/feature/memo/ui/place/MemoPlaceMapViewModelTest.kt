@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.place

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.location.usecase.FetchCurrentLocationUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class MemoPlaceMapViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("기본 지도를 확인하기 전에는 지도 로딩 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(defaultMapProvider = emptyFlow())

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceMapUiState.Loading
                    viewModel.fetchCurrentLocation()
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-FEATURE-003 기본 지도를 읽지 못하면 지도 로딩 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(defaultMapProvider = flowOf(Result.failure(IllegalStateException("read error"))))

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceMapUiState.Loading
                    viewModel.fetchCurrentLocation()
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("현재 위치를 확인하기 전에는 지도 로딩 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)))

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceMapUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-DOMAIN-006 저장된 기본 지도를 초기 제공자로 제공한다") {
            MapProvider.entries.forEach { provider ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(defaultMapProvider = flowOf(Result.success(provider)))

                    viewModel.uiState.test {
                        awaitItem() shouldBe MemoPlaceMapUiState.Loading
                        viewModel.fetchCurrentLocation()
                        awaitItem() shouldBe MemoPlaceMapUiState.Loaded(provider = provider, currentCoordinate = null)
                    }
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-DOMAIN-002 확인한 현재 위치를 제공한다") {
            listOf(
                Coordinate(latitude = 37.5666102, longitude = 126.9783881),
                Coordinate(latitude = -33.8688, longitude = -70.6693),
            ).forEach { coordinate ->
                runTest(mainDispatcher) {
                    val viewModel =
                        viewModel(
                            defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)),
                            currentLocation = Result.success(coordinate),
                        )

                    viewModel.uiState.test {
                        awaitItem() shouldBe MemoPlaceMapUiState.Loading
                        viewModel.fetchCurrentLocation()
                        awaitItem() shouldBe MemoPlaceMapUiState.Loaded(provider = MapProvider.NAVER, currentCoordinate = coordinate)
                    }
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-DOMAIN-003 현재 위치를 확인하지 못하면 좌표 없이 제공한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)),
                        currentLocation = Result.failure(IllegalStateException("location error")),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceMapUiState.Loading
                    viewModel.fetchCurrentLocation()
                    awaitItem() shouldBe MemoPlaceMapUiState.Loaded(provider = MapProvider.NAVER, currentCoordinate = null)
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-DOMAIN-004 현재 위치 확인을 다시 요청해도 한 번만 확인한다") {
            runTest(mainDispatcher) {
                val coordinate = fixtureMonkey.giveMeOne<Coordinate>()
                val fetchCurrentLocationUseCase = fetchCurrentLocationUseCase(Result.success(coordinate))
                val viewModel =
                    viewModel(
                        defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)),
                        fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceMapUiState.Loading
                    viewModel.fetchCurrentLocation()
                    awaitItem() shouldBe MemoPlaceMapUiState.Loaded(provider = MapProvider.NAVER, currentCoordinate = coordinate)

                    repeat(REPEAT_COUNT) { viewModel.fetchCurrentLocation() }
                    advanceUntilIdle()
                    expectNoEvents()
                }

                coVerify(exactly = 1) { fetchCurrentLocationUseCase(parameter = Unit) }
            }
        }

        test("TC-MEMO-PLACE-CARD-DOMAIN-005 새 화면에서는 현재 위치를 새로 확인한다") {
            runTest(mainDispatcher) {
                val beforeCoordinate = fixtureMonkey.giveMeOne<Coordinate>()
                val afterCoordinate = fixtureMonkey.giveMeOne<Coordinate>()
                val fetchCurrentLocationUseCase = mockk<FetchCurrentLocationUseCase>()
                coEvery { fetchCurrentLocationUseCase(parameter = Unit) } returnsMany
                    listOf(Result.success(beforeCoordinate), Result.success(afterCoordinate))

                viewModel(
                    defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)),
                    fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                ).also { viewModel ->
                    viewModel.uiState.test {
                        awaitItem() shouldBe MemoPlaceMapUiState.Loading
                        viewModel.fetchCurrentLocation()
                        awaitItem() shouldBe MemoPlaceMapUiState.Loaded(provider = MapProvider.NAVER, currentCoordinate = beforeCoordinate)
                    }
                }

                viewModel(
                    defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)),
                    fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                ).also { viewModel ->
                    viewModel.uiState.test {
                        awaitItem() shouldBe MemoPlaceMapUiState.Loading
                        viewModel.fetchCurrentLocation()
                        awaitItem() shouldBe MemoPlaceMapUiState.Loaded(provider = MapProvider.NAVER, currentCoordinate = afterCoordinate)
                    }
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-DOMAIN-007 기본 지도가 바뀌면 새 초기 제공자를 제공한다") {
            runTest(mainDispatcher) {
                val storedProvider = MutableStateFlow(Result.success(MapProvider.NAVER))
                val viewModel = viewModel(defaultMapProvider = storedProvider)

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceMapUiState.Loading
                    viewModel.fetchCurrentLocation()
                    awaitItem() shouldBe MemoPlaceMapUiState.Loaded(provider = MapProvider.NAVER, currentCoordinate = null)

                    storedProvider.value = Result.success(MapProvider.GOOGLE)

                    awaitItem() shouldBe MemoPlaceMapUiState.Loaded(provider = MapProvider.GOOGLE, currentCoordinate = null)
                }
            }
        }
    }

    private companion object {
        private const val REPEAT_COUNT = 3

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun fetchCurrentLocationUseCase(currentLocation: Result<Coordinate>): FetchCurrentLocationUseCase {
            val useCase = mockk<FetchCurrentLocationUseCase>()
            coEvery { useCase(parameter = Unit) } returns currentLocation

            return useCase
        }

        private fun viewModel(
            defaultMapProvider: Flow<Result<MapProvider>>,
            currentLocation: Result<Coordinate> = Result.failure(IllegalStateException("location unavailable")),
            fetchCurrentLocationUseCase: FetchCurrentLocationUseCase = fetchCurrentLocationUseCase(currentLocation),
        ): MemoPlaceMapViewModel {
            val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
            every { getDefaultMapProviderUseCase(Unit) } returns defaultMapProvider

            return MemoPlaceMapViewModel(
                fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
            )
        }
    }
}
