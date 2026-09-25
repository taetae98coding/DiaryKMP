@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.search

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.domain.place.usecase.FetchSearchedPlaceUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class PlaceSearchViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("검색에 성공하면 받은 장소 목록을 결과로 둔다") {
            runTest(mainDispatcher) {
                val placeList = List(SEARCHED_PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
                val request = searchRequest()
                val fetchSearchedPlaceUseCase = mockk<FetchSearchedPlaceUseCase>()
                coEvery { fetchSearchedPlaceUseCase(request.toParameter()) } returns Result.success(placeList)
                val viewModel = viewModel(fetchSearchedPlaceUseCase = fetchSearchedPlaceUseCase)
                collectUiState(viewModel)

                viewModel.search(request = request)
                advanceUntilIdle()

                viewModel.uiState.value shouldBe PlaceSearchUiState.Loaded(placeList = placeList)
            }
        }

        test("검색에 실패하면 실패 상태로 둔다") {
            runTest(mainDispatcher) {
                val request = searchRequest()
                val fetchSearchedPlaceUseCase = mockk<FetchSearchedPlaceUseCase>()
                coEvery { fetchSearchedPlaceUseCase(request.toParameter()) } returns
                    Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(fetchSearchedPlaceUseCase = fetchSearchedPlaceUseCase)
                collectUiState(viewModel)

                viewModel.search(request = request)
                advanceUntilIdle()

                viewModel.uiState.value shouldBe PlaceSearchUiState.Failed
            }
        }

        test("검색 결과를 비우면 결과 없음 상태로 되돌린다") {
            runTest(mainDispatcher) {
                val placeList = List(SEARCHED_PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
                val request = searchRequest()
                val fetchSearchedPlaceUseCase = mockk<FetchSearchedPlaceUseCase>()
                coEvery { fetchSearchedPlaceUseCase(request.toParameter()) } returns Result.success(placeList)
                val viewModel = viewModel(fetchSearchedPlaceUseCase = fetchSearchedPlaceUseCase)
                collectUiState(viewModel)

                viewModel.search(request = request)
                advanceUntilIdle()

                viewModel.uiState.value shouldBe PlaceSearchUiState.Loaded(placeList = placeList)

                viewModel.clear()
                advanceUntilIdle()

                viewModel.uiState.value shouldBe PlaceSearchUiState.Idle
            }
        }

        test("TC-PLACE-SEARCH-DIALOG-FEATURE-009 다시 검색하는 동안 직전 결과를 그대로 둔다") {
            runTest(mainDispatcher) {
                val firstList = List(SEARCHED_PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
                val secondList = List(SEARCHED_PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
                val completion = CompletableDeferred<Result<List<SearchedPlace>>>()
                val firstRequest = searchRequest()
                val secondRequest = searchRequest()
                val fetchSearchedPlaceUseCase = mockk<FetchSearchedPlaceUseCase>()
                coEvery { fetchSearchedPlaceUseCase(firstRequest.toParameter()) } returns Result.success(firstList)
                coEvery { fetchSearchedPlaceUseCase(secondRequest.toParameter()) } coAnswers { completion.await() }
                val viewModel = viewModel(fetchSearchedPlaceUseCase = fetchSearchedPlaceUseCase)
                collectUiState(viewModel)

                viewModel.search(request = firstRequest)
                advanceUntilIdle()
                viewModel.search(request = secondRequest)
                runCurrent()

                viewModel.uiState.value shouldBe PlaceSearchUiState.Loaded(placeList = firstList)

                completion.complete(Result.success(secondList))
                advanceUntilIdle()

                viewModel.uiState.value shouldBe PlaceSearchUiState.Loaded(placeList = secondList)
            }
        }

        test("TC-PLACE-SEARCH-DIALOG-DOMAIN-008 앞선 검색의 늦은 결과는 쓰지 않는다") {
            runTest(mainDispatcher) {
                val lateList = List(SEARCHED_PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
                val latestList = List(SEARCHED_PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
                val lateCompletion = CompletableDeferred<Result<List<SearchedPlace>>>()
                val firstRequest = searchRequest()
                val secondRequest = searchRequest()
                val fetchSearchedPlaceUseCase = mockk<FetchSearchedPlaceUseCase>()
                coEvery { fetchSearchedPlaceUseCase(firstRequest.toParameter()) } coAnswers { lateCompletion.await() }
                coEvery { fetchSearchedPlaceUseCase(secondRequest.toParameter()) } returns Result.success(latestList)
                val viewModel = viewModel(fetchSearchedPlaceUseCase = fetchSearchedPlaceUseCase)
                collectUiState(viewModel)

                viewModel.search(request = firstRequest)
                runCurrent()
                viewModel.search(request = secondRequest)
                advanceUntilIdle()

                lateCompletion.complete(Result.success(lateList))
                advanceUntilIdle()

                viewModel.uiState.value shouldBe PlaceSearchUiState.Loaded(placeList = latestList)
            }
        }

        test("TC-PLACE-SEARCH-DIALOG-DOMAIN-017 제공자를 바꾸면 직전 결과를 곧바로 비운다") {
            runTest(mainDispatcher) {
                val naverList = List(SEARCHED_PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
                val googleList = List(SEARCHED_PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
                val completion = CompletableDeferred<Result<List<SearchedPlace>>>()
                val naverRequest = searchRequest(provider = MapProvider.NAVER)
                val googleRequest = naverRequest.copy(provider = MapProvider.GOOGLE)
                val fetchSearchedPlaceUseCase = mockk<FetchSearchedPlaceUseCase>()
                coEvery { fetchSearchedPlaceUseCase(naverRequest.toParameter()) } returns Result.success(naverList)
                coEvery { fetchSearchedPlaceUseCase(googleRequest.toParameter()) } coAnswers { completion.await() }
                val viewModel = viewModel(fetchSearchedPlaceUseCase = fetchSearchedPlaceUseCase)
                collectUiState(viewModel)

                viewModel.search(request = naverRequest)
                advanceUntilIdle()
                viewModel.uiState.value shouldBe PlaceSearchUiState.Loaded(placeList = naverList)

                viewModel.search(request = googleRequest)
                runCurrent()

                viewModel.uiState.value shouldBe PlaceSearchUiState.Idle

                completion.complete(Result.success(googleList))
                advanceUntilIdle()

                viewModel.uiState.value shouldBe PlaceSearchUiState.Loaded(placeList = googleList)
            }
        }

        test("TC-PLACE-SEARCH-DIALOG-DOMAIN-018 한 제공자의 검색 실패는 다른 제공자로 바꾼 검색에 영향을 주지 않는다") {
            runTest(mainDispatcher) {
                val googleList = List(SEARCHED_PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
                val naverRequest = searchRequest(provider = MapProvider.NAVER)
                val googleRequest = naverRequest.copy(provider = MapProvider.GOOGLE)
                val fetchSearchedPlaceUseCase = mockk<FetchSearchedPlaceUseCase>()
                coEvery { fetchSearchedPlaceUseCase(naverRequest.toParameter()) } returns
                    Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                coEvery { fetchSearchedPlaceUseCase(googleRequest.toParameter()) } returns Result.success(googleList)
                val viewModel = viewModel(fetchSearchedPlaceUseCase = fetchSearchedPlaceUseCase)
                collectUiState(viewModel)

                viewModel.search(request = naverRequest)
                advanceUntilIdle()
                viewModel.uiState.value shouldBe PlaceSearchUiState.Failed

                viewModel.search(request = googleRequest)
                advanceUntilIdle()

                viewModel.uiState.value shouldBe PlaceSearchUiState.Loaded(placeList = googleList)
            }
        }

        test("같은 제공자로 다시 검색하면 제공자 전환으로 다루지 않는다") {
            runTest(mainDispatcher) {
                val firstList = List(SEARCHED_PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
                val completion = CompletableDeferred<Result<List<SearchedPlace>>>()
                val firstRequest = searchRequest(provider = MapProvider.GOOGLE)
                val secondRequest = searchRequest(provider = MapProvider.GOOGLE, bounds = fixtureMonkey.giveMeOne<CoordinateBounds>())
                val fetchSearchedPlaceUseCase = mockk<FetchSearchedPlaceUseCase>()
                coEvery { fetchSearchedPlaceUseCase(firstRequest.toParameter()) } returns Result.success(firstList)
                coEvery { fetchSearchedPlaceUseCase(secondRequest.toParameter()) } coAnswers { completion.await() }
                val viewModel = viewModel(fetchSearchedPlaceUseCase = fetchSearchedPlaceUseCase)
                collectUiState(viewModel)

                viewModel.search(request = firstRequest)
                advanceUntilIdle()
                viewModel.search(request = secondRequest)
                runCurrent()

                viewModel.uiState.value shouldBe PlaceSearchUiState.Loaded(placeList = firstList)
            }
        }

        test("TC-PLACE-SEARCH-DIALOG-DOMAIN-020 검색 중에 검색어를 비우면 늦게 도착한 결과를 쓰지 않는다") {
            runTest(mainDispatcher) {
                val lateList = List(SEARCHED_PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
                val lateCompletion = CompletableDeferred<Result<List<SearchedPlace>>>()
                val request = searchRequest()
                val fetchSearchedPlaceUseCase = mockk<FetchSearchedPlaceUseCase>()
                coEvery { fetchSearchedPlaceUseCase(request.toParameter()) } coAnswers { lateCompletion.await() }
                val viewModel = viewModel(fetchSearchedPlaceUseCase = fetchSearchedPlaceUseCase)
                collectUiState(viewModel)

                viewModel.search(request = request)
                runCurrent()
                viewModel.clear()
                advanceUntilIdle()

                lateCompletion.complete(Result.success(lateList))
                advanceUntilIdle()

                viewModel.uiState.value shouldBe PlaceSearchUiState.Idle
            }
        }

        test("검색은 입력한 검색어와 제공자와 보이는 영역을 그대로 요청한다") {
            runTest(mainDispatcher) {
                val request = searchRequest(provider = MapProvider.GOOGLE, bounds = fixtureMonkey.giveMeOne<CoordinateBounds>())
                val fetchSearchedPlaceUseCase = mockk<FetchSearchedPlaceUseCase>()
                coEvery { fetchSearchedPlaceUseCase(any()) } returns Result.success(emptyList())
                val viewModel = viewModel(fetchSearchedPlaceUseCase = fetchSearchedPlaceUseCase)
                collectUiState(viewModel)

                viewModel.search(request = request)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    fetchSearchedPlaceUseCase(
                        FetchSearchedPlaceUseCase.Parameter(
                            query = request.query,
                            provider = request.provider,
                            bounds = request.bounds,
                        ),
                    )
                }
            }
        }
    }

    private companion object {
        private const val SEARCHED_PLACE_COUNT = 3

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        // uiState는 구독자가 있을 때 시작되므로 검증 동안 수집을 유지한다.
        private fun TestScope.collectUiState(viewModel: PlaceSearchViewModel) {
            backgroundScope.launch { viewModel.uiState.collect() }
        }

        private fun viewModel(fetchSearchedPlaceUseCase: FetchSearchedPlaceUseCase = mockk()): PlaceSearchViewModel = PlaceSearchViewModel(fetchSearchedPlaceUseCase = fetchSearchedPlaceUseCase)

        private fun searchRequest(
            query: String = "검색어-${fixtureMonkey.giveMeOne<String>()}",
            provider: MapProvider = MapProvider.NAVER,
            bounds: CoordinateBounds? = null,
        ): PlaceSearchRequest =
            PlaceSearchRequest(
                query = query,
                provider = provider,
                bounds = bounds,
            )
    }
}
