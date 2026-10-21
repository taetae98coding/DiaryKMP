@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.place

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class MemoPlacePickerMapViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MEMO-PLACE-CARD-FEATURE-052 보이는 영역을 확인하기 전에는 목록을 준비하는 중으로 둔다") {
            runTest(mainDispatcher) {
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                val viewModel = MemoPlacePickerMapViewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlacePickerMapUiState()

                    viewModel.updateVisibleBounds(null)
                    expectNoEvents()
                }

                verify(exactly = 0) { getPlaceListUseCase(parameter = any()) }
            }
        }

        test("보이는 영역과 제목 오름차순 기준으로 조회한 장소를 그대로 나타낸다") {
            runTest(mainDispatcher) {
                val bounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val placeList = placeList()
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)) } returns flowOf(Result.success(placeList))
                val viewModel = MemoPlacePickerMapViewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlacePickerMapUiState()

                    viewModel.updateVisibleBounds(bounds)

                    awaitItem() shouldBe MemoPlacePickerMapUiState(isLoaded = true, placeList = placeList)
                }
            }
        }

        test("날짜변경선을 걸친 영역을 그대로 조회 기준으로 쓴다") {
            runTest(mainDispatcher) {
                val bounds = CoordinateBounds(south = -10.0, north = 10.0, west = 179.0, east = -179.0)
                val placeList = placeList()
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)) } returns flowOf(Result.success(placeList))
                val viewModel = MemoPlacePickerMapViewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.uiState.test {
                    awaitItem()

                    viewModel.updateVisibleBounds(bounds)

                    awaitItem().placeList shouldBe placeList
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-DOMAIN-038 보이는 영역이 바뀌면 바뀐 영역의 장소로 목록을 바꾼다") {
            runTest(mainDispatcher) {
                val firstBounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val secondBounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val firstPlaceList = placeList()
                val secondPlaceList = placeList()
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = firstBounds, sort = ListSort.TITLE)) } returns flowOf(Result.success(firstPlaceList))
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = secondBounds, sort = ListSort.TITLE)) } returns flowOf(Result.success(secondPlaceList))
                val viewModel = MemoPlacePickerMapViewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.uiState.test {
                    awaitItem()

                    viewModel.updateVisibleBounds(firstBounds)
                    awaitItem().placeList shouldBe firstPlaceList

                    viewModel.updateVisibleBounds(secondBounds)
                    awaitItem().placeList shouldBe secondPlaceList
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-DATA-007 저장된 장소가 바뀌면 지도 보기의 목록에 바로 반영한다") {
            runTest(mainDispatcher) {
                val bounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val firstPlaceList = placeList()
                val changedPlaceList = placeList()
                val placeListFlow = MutableStateFlow(Result.success(firstPlaceList))
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)) } returns placeListFlow
                val viewModel = MemoPlacePickerMapViewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.uiState.test {
                    awaitItem()
                    viewModel.updateVisibleBounds(bounds)
                    awaitItem().placeList shouldBe firstPlaceList

                    placeListFlow.value = Result.success(changedPlaceList)
                    awaitItem().placeList shouldBe changedPlaceList

                    placeListFlow.value = Result.success(emptyList())
                    awaitItem() shouldBe MemoPlacePickerMapUiState(isLoaded = true, placeList = emptyList())
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-DATA-009 보이는 영역 안의 장소가 많아도 한 번에 모두 나타낸다") {
            runTest(mainDispatcher) {
                val bounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val placeList = List(size = MANY_PLACE_COUNT) { fixtureMonkey.place(isDeleted = false) }
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)) } returns flowOf(Result.success(placeList))
                val viewModel = MemoPlacePickerMapViewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.uiState.test {
                    awaitItem()

                    viewModel.updateVisibleBounds(bounds)

                    awaitItem().placeList shouldBe placeList
                    expectNoEvents()
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-DATA-008 지도 보기의 목록을 불러오지 못하면 장소 없음과 같게 다룬다") {
            runTest(mainDispatcher) {
                val bounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)) } returns flowOf(Result.failure(IllegalStateException()))
                val viewModel = MemoPlacePickerMapViewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.uiState.test {
                    awaitItem()

                    viewModel.updateVisibleBounds(bounds)

                    awaitItem() shouldBe MemoPlacePickerMapUiState(isLoaded = true, placeList = emptyList())
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-DATA-008 장소가 나타나 있던 중 다시 불러오지 못하면 보이던 장소 대신 장소 없음과 같게 다룬다") {
            runTest(mainDispatcher) {
                val bounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val placeList = placeList()
                val placeListFlow = MutableStateFlow(Result.success(placeList))
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)) } returns placeListFlow
                val viewModel = MemoPlacePickerMapViewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.uiState.test {
                    awaitItem()
                    viewModel.updateVisibleBounds(bounds)
                    awaitItem().placeList shouldBe placeList

                    placeListFlow.value = Result.failure(IllegalStateException())

                    awaitItem() shouldBe MemoPlacePickerMapUiState(isLoaded = true, placeList = emptyList())
                }
            }
        }
    }

    private fun placeList(): List<Place> = List(size = PLACE_COUNT) { fixtureMonkey.place(isDeleted = false) }

    private companion object {
        const val PLACE_COUNT = 3
        const val MANY_PLACE_COUNT = 200

        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
