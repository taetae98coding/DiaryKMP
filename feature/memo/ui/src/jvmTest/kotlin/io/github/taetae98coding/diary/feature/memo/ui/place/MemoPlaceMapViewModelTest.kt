@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.place

import app.cash.turbine.ReceiveTurbine
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
import kotlinx.coroutines.CompletableDeferred
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

        test("TC-a2329351-5d96-4a5d-b6c2-465d26366250 기본 지도를 읽지 못하면 지도 로딩 상태를 유지한다") {
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

        test("TC-a7e0269c-f3f0-49d9-b62d-af38505b8a0c 현재 위치를 확인하기 전에는 확인하지 않은 상태로 지도를 제공하고 확인이 끝나면 확인한 상태로 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)))

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceMapUiState.Loading
                    advanceUntilIdle()
                    expectMostRecentItem() shouldBe MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = false, currentCoordinate = null)

                    viewModel.fetchCurrentLocation()
                    awaitFetchedContent().isCurrentLocationFetched shouldBe true
                }
            }
        }

        test("TC-3f0bd41c-022f-4747-8db8-80ef4f29acf8 저장된 기본 지도를 초기 제공자로 제공한다") {
            MapProvider.entries.forEach { provider ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(defaultMapProvider = flowOf(Result.success(provider)))

                    viewModel.uiState.test {
                        awaitItem() shouldBe MemoPlaceMapUiState.Loading
                        viewModel.fetchCurrentLocation()
                        awaitFetchedContent() shouldBe MemoPlaceMapUiState.Content(provider = provider, isCurrentLocationFetched = true, currentCoordinate = null)
                    }
                }
            }
        }

        test("TC-fa77ea4b-85b7-4eb9-b471-5ff34a1b9c94 확인한 현재 위치를 제공한다") {
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
                        awaitFetchedContent() shouldBe MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = true, currentCoordinate = coordinate)
                    }
                }
            }
        }

        test("TC-f9b48925-b080-439c-8d41-881da67b84af 현재 위치를 확인하지 못하면 좌표 없이 제공한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)),
                        currentLocation = Result.failure(IllegalStateException("location error")),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceMapUiState.Loading
                    viewModel.fetchCurrentLocation()
                    awaitFetchedContent() shouldBe MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = true, currentCoordinate = null)
                }
            }
        }

        test("TC-718d19df-b17b-4756-b3ce-2e652ef8ffb3 현재 위치 확인을 다시 요청해도 한 번만 확인한다") {
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
                    awaitFetchedContent() shouldBe MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = true, currentCoordinate = coordinate)

                    repeat(REPEAT_COUNT) { viewModel.fetchCurrentLocation() }
                    advanceUntilIdle()
                    expectNoEvents()
                }

                coVerify(exactly = 1) { fetchCurrentLocationUseCase(parameter = Unit) }
            }
        }

        test("TC-d69e72f7-f42b-4b45-ba68-b5c7956e161a 새 화면에서는 현재 위치를 새로 확인한다") {
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
                        awaitFetchedContent() shouldBe MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = true, currentCoordinate = beforeCoordinate)
                    }
                }

                viewModel(
                    defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)),
                    fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                ).also { viewModel ->
                    viewModel.uiState.test {
                        awaitItem() shouldBe MemoPlaceMapUiState.Loading
                        viewModel.fetchCurrentLocation()
                        awaitFetchedContent() shouldBe MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = true, currentCoordinate = afterCoordinate)
                    }
                }
            }
        }

        test("TC-ccada8e8-fc89-41f3-afbe-6446af288e35 메모리 정리 뒤 복원한 화면은 현재 위치를 다시 확인하고 확인이 끝날 때까지 확인하지 않은 상태로 제공한다") {
            runTest(mainDispatcher) {
                val restoredCoordinate = fixtureMonkey.giveMeOne<Coordinate>()
                val restoredLocation = CompletableDeferred<Result<Coordinate>>()
                val fetchCurrentLocationUseCase = mockk<FetchCurrentLocationUseCase>()
                coEvery { fetchCurrentLocationUseCase(parameter = Unit) } returns
                    Result.success(fixtureMonkey.giveMeOne<Coordinate>()) coAndThen { restoredLocation.await() }

                viewModel(
                    defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)),
                    fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                ).also { viewModel ->
                    viewModel.uiState.test {
                        awaitItem() shouldBe MemoPlaceMapUiState.Loading
                        viewModel.fetchCurrentLocation()
                        awaitFetchedContent()
                    }
                }

                // 확인 결과는 저장해 두지 않으므로 메모리 정리 뒤 복원된 화면은 새 상태 홀더로 시작한다.
                viewModel(
                    defaultMapProvider = flowOf(Result.success(MapProvider.NAVER)),
                    fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                ).also { viewModel ->
                    viewModel.uiState.test {
                        awaitItem() shouldBe MemoPlaceMapUiState.Loading
                        viewModel.fetchCurrentLocation()
                        advanceUntilIdle()
                        expectMostRecentItem() shouldBe MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = false, currentCoordinate = null)

                        restoredLocation.complete(Result.success(restoredCoordinate))
                        awaitItem() shouldBe MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = true, currentCoordinate = restoredCoordinate)
                    }
                }

                coVerify(exactly = 2) { fetchCurrentLocationUseCase(parameter = Unit) }
            }
        }

        test("TC-5a66c202-a4b1-456e-ab60-ad42422f0d35 기본 지도가 바뀌면 새 초기 제공자를 제공한다") {
            runTest(mainDispatcher) {
                val storedProvider = MutableStateFlow(Result.success(MapProvider.NAVER))
                val viewModel = viewModel(defaultMapProvider = storedProvider)

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceMapUiState.Loading
                    viewModel.fetchCurrentLocation()
                    awaitFetchedContent() shouldBe MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = true, currentCoordinate = null)

                    storedProvider.value = Result.success(MapProvider.GOOGLE)

                    awaitItem() shouldBe MemoPlaceMapUiState.Content(provider = MapProvider.GOOGLE, isCurrentLocationFetched = true, currentCoordinate = null)
                }
            }
        }
    }

    private companion object {
        private const val REPEAT_COUNT = 3

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private suspend fun ReceiveTurbine<MemoPlaceMapUiState>.awaitFetchedContent(): MemoPlaceMapUiState.Content {
            var item = awaitItem()
            while (item !is MemoPlaceMapUiState.Content || !item.isCurrentLocationFetched) {
                item = awaitItem()
            }

            return item
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
