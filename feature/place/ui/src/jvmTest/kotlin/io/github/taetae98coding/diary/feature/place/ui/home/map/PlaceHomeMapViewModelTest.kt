@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.home.map

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.location.usecase.FetchCurrentLocationUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeUiState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
import kotlin.uuid.Uuid

class PlaceHomeMapViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("기본 지도를 확인하기 전에는 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(defaultMapProvider = emptyFlow())

                viewModel.uiState.test {
                    awaitItem() shouldBe PlaceHomeUiState.Loading
                    viewModel.fetchCurrentLocation()
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-PLACE-HOME-FEATURE-013 기본 지도가 바뀌면 바뀐 기본 지도를 제공한다") {
            runTest(mainDispatcher) {
                val providerFlow = MutableStateFlow(Result.success(MapProvider.NAVER))
                val viewModel = viewModel(defaultMapProvider = providerFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe PlaceHomeUiState.Loading
                    viewModel.fetchCurrentLocation()
                    awaitItem().shouldBeLoaded(defaultProvider = MapProvider.NAVER, initialCoordinate = null)

                    providerFlow.value = Result.success(MapProvider.GOOGLE)
                    awaitItem().shouldBeLoaded(defaultProvider = MapProvider.GOOGLE, initialCoordinate = null)
                }
            }
        }

        test("TC-PLACE-HOME-FEATURE-012 저장된 기본 지도를 초기 제공자로 제공한다") {
            MapProvider.entries.forEach { provider ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(defaultMapProvider = flowOf(Result.success(provider)))

                    viewModel.uiState.test {
                        awaitItem() shouldBe PlaceHomeUiState.Loading
                        viewModel.fetchCurrentLocation()
                        awaitItem().shouldBeLoaded(defaultProvider = provider, initialCoordinate = null)
                    }
                }
            }
        }

        test("TC-PLACE-HOME-FEATURE-011 기본 지도를 읽지 못하면 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(defaultMapProvider = flowOf(Result.failure(IllegalStateException("read error"))))

                viewModel.uiState.test {
                    awaitItem() shouldBe PlaceHomeUiState.Loading
                    viewModel.fetchCurrentLocation()
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-PLACE-HOME-FEATURE-016 현재 위치를 확인하기 전에는 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)))

                viewModel.uiState.test {
                    awaitItem() shouldBe PlaceHomeUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-PLACE-HOME-DOMAIN-001 확인한 현재 위치를 초기 위치로 제공한다") {
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
                        awaitItem() shouldBe PlaceHomeUiState.Loading
                        viewModel.fetchCurrentLocation()
                        awaitItem().shouldBeLoaded(defaultProvider = MapProvider.NAVER, initialCoordinate = coordinate)
                    }
                }
            }
        }

        test("TC-PLACE-HOME-FEATURE-017 TC-PLACE-HOME-DOMAIN-002 현재 위치를 확인하지 못하면 초기 위치 없이 제공한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)),
                        currentLocation = Result.failure(IllegalStateException("location error")),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe PlaceHomeUiState.Loading
                    viewModel.fetchCurrentLocation()
                    awaitItem().shouldBeLoaded(defaultProvider = MapProvider.NAVER, initialCoordinate = null)
                }
            }
        }

        test("TC-PLACE-HOME-DOMAIN-003 같은 화면에서 현재 위치 확인을 다시 요청해도 한 번만 확인한다") {
            runTest(mainDispatcher) {
                val coordinate = fixtureMonkey.giveMeOne<Coordinate>()
                val fetchCurrentLocationUseCase = fetchCurrentLocationUseCase(Result.success(coordinate))
                val viewModel =
                    viewModel(
                        defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)),
                        fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe PlaceHomeUiState.Loading
                    viewModel.fetchCurrentLocation()
                    awaitItem().shouldBeLoaded(defaultProvider = MapProvider.NAVER, initialCoordinate = coordinate)

                    repeat(REPEAT_COUNT) { viewModel.fetchCurrentLocation() }
                    advanceUntilIdle()
                    expectNoEvents()
                }

                coVerify(exactly = 1) { fetchCurrentLocationUseCase(parameter = Unit) }
            }
        }

        test("TC-PLACE-HOME-DOMAIN-004 새 화면에서는 현재 위치를 새로 확인한다") {
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
                        awaitItem() shouldBe PlaceHomeUiState.Loading
                        viewModel.fetchCurrentLocation()
                        awaitItem().shouldBeLoaded(defaultProvider = MapProvider.NAVER, initialCoordinate = beforeCoordinate)
                    }
                }

                viewModel(
                    defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)),
                    fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                ).also { viewModel ->
                    viewModel.uiState.test {
                        awaitItem() shouldBe PlaceHomeUiState.Loading
                        viewModel.fetchCurrentLocation()
                        awaitItem().shouldBeLoaded(defaultProvider = MapProvider.NAVER, initialCoordinate = afterCoordinate)
                    }
                }
            }
        }

        test("기본 지도가 바뀌면 새 초기 제공자를 제공한다") {
            runTest(mainDispatcher) {
                val storedProvider = MutableStateFlow(Result.success(MapProvider.NAVER))
                val viewModel = viewModel(defaultMapProvider = storedProvider)

                viewModel.uiState.test {
                    awaitItem() shouldBe PlaceHomeUiState.Loading
                    viewModel.fetchCurrentLocation()
                    awaitItem().shouldBeLoaded(defaultProvider = MapProvider.NAVER, initialCoordinate = null)

                    storedProvider.value = Result.success(MapProvider.GOOGLE)

                    awaitItem().shouldBeLoaded(defaultProvider = MapProvider.GOOGLE, initialCoordinate = null)
                }
            }
        }
        test("기본 지도가 바뀌어도 같은 현재 위치 확인 결과로 제공한다") {
            runTest(mainDispatcher) {
                val storedProvider = MutableStateFlow(Result.success(MapProvider.NAVER))
                val viewModel = viewModel(defaultMapProvider = storedProvider)

                viewModel.uiState.test {
                    awaitItem() shouldBe PlaceHomeUiState.Loading
                    viewModel.fetchCurrentLocation()
                    val before = awaitItem().shouldBeLoaded(defaultProvider = MapProvider.NAVER, initialCoordinate = null)

                    storedProvider.value = Result.success(MapProvider.GOOGLE)

                    val after = awaitItem().shouldBeLoaded(defaultProvider = MapProvider.GOOGLE, initialCoordinate = null)
                    after.currentLocationFetchId shouldBe before.currentLocationFetchId
                }
            }
        }

        test("새 화면이 같은 위치를 확인해도 이전 화면과 다른 확인 결과로 제공한다") {
            listOf(
                Result.success(fixtureMonkey.giveMeOne<Coordinate>()),
                Result.failure(IllegalStateException("location error")),
            ).forEach { currentLocation ->
                runTest(mainDispatcher) {
                    val fetchIdList = mutableListOf<Uuid>()
                    repeat(2) {
                        val viewModel =
                            viewModel(
                                defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)),
                                currentLocation = currentLocation,
                            )

                        viewModel.uiState.test {
                            awaitItem() shouldBe PlaceHomeUiState.Loading
                            viewModel.fetchCurrentLocation()
                            fetchIdList +=
                                awaitItem()
                                    .shouldBeLoaded(defaultProvider = MapProvider.NAVER, initialCoordinate = currentLocation.getOrNull())
                                    .currentLocationFetchId
                        }
                    }

                    fetchIdList[0] shouldNotBe fetchIdList[1]
                }
            }
        }
    }

    private companion object {
        private const val REPEAT_COUNT = 3

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun PlaceHomeUiState.shouldBeLoaded(
            defaultProvider: MapProvider,
            initialCoordinate: Coordinate?,
        ): PlaceHomeUiState.Content {
            val loaded = shouldBeInstanceOf<PlaceHomeUiState.Content>()
            loaded.defaultProvider shouldBe defaultProvider
            loaded.initialCoordinate shouldBe initialCoordinate

            return loaded
        }

        private fun fetchCurrentLocationUseCase(currentLocation: Result<Coordinate>): FetchCurrentLocationUseCase {
            val useCase = mockk<FetchCurrentLocationUseCase>()
            coEvery { useCase(parameter = Unit) } returns currentLocation

            return useCase
        }

        private fun viewModel(
            defaultMapProvider: Flow<Result<MapProvider>>,
            currentLocation: Result<Coordinate> = Result.failure(IllegalStateException("location unavailable")),
            fetchCurrentLocationUseCase: FetchCurrentLocationUseCase = fetchCurrentLocationUseCase(currentLocation),
        ): PlaceHomeMapViewModel {
            val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
            every { getDefaultMapProviderUseCase(Unit) } returns defaultMapProvider

            return PlaceHomeMapViewModel(
                fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
            )
        }
    }
}
