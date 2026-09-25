@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.home.list

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceHomeUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class PlaceHomePlaceListViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-PLACE-HOME-FEATURE-018 보이는 영역이 알려지면 그 영역의 장소 목록을 제공한다") {
            runTest(mainDispatcher) {
                val bounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val placeList = placeList(PLACE_COUNT)
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)) } returns flowOf(Result.success(placeList))

                val viewModel = viewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.placeListFlow().test {
                    awaitItem() shouldBe emptyList()

                    viewModel.updateVisibleBounds(bounds)

                    awaitItem() shouldBe placeList
                }

                verify(exactly = 1) { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)) }
            }
        }

        test("TC-PLACE-HOME-FEATURE-019 보이는 영역이 바뀌면 바뀐 영역의 장소 목록으로 갱신한다") {
            runTest(mainDispatcher) {
                val beforeBounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val afterBounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val beforePlaceList = placeList(PLACE_COUNT)
                val afterPlaceList = placeList(PLACE_COUNT)
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = beforeBounds, sort = ListSort.TITLE)) } returns flowOf(Result.success(beforePlaceList))
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = afterBounds, sort = ListSort.TITLE)) } returns flowOf(Result.success(afterPlaceList))

                val viewModel = viewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.placeListFlow().test {
                    awaitItem() shouldBe emptyList()

                    viewModel.updateVisibleBounds(beforeBounds)
                    awaitItem() shouldBe beforePlaceList

                    viewModel.updateVisibleBounds(afterBounds)
                    awaitItem() shouldBe afterPlaceList
                }
            }
        }

        test("TC-PLACE-HOME-FEATURE-020 보이는 영역 안에 장소가 없으면 빈 목록을 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(getPlaceListUseCase = getPlaceListUseCase(Result.success(emptyList())))

                viewModel.placeListFlow().test {
                    awaitItem() shouldBe emptyList()

                    viewModel.updateVisibleBounds(fixtureMonkey.giveMeOne<CoordinateBounds>())

                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-PLACE-HOME-FEATURE-021 보이는 영역을 확인하기 전에는 장소를 조회하지 않고 빈 목록을 제공한다") {
            runTest(mainDispatcher) {
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                val viewModel = viewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.placeListFlow().test {
                    awaitItem() shouldBe emptyList()

                    advanceUntilIdle()
                    expectNoEvents()
                }

                verify(exactly = 0) { getPlaceListUseCase(parameter = any()) }
            }
        }

        test("TC-PLACE-HOME-FEATURE-022 조회 결과가 바뀌면 장소 목록을 갱신한다") {
            runTest(mainDispatcher) {
                val bounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val beforePlaceList = placeList(PLACE_COUNT)
                val addedPlace = place()
                val placeListFlow = MutableStateFlow(Result.success(beforePlaceList))
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)) } returns placeListFlow

                val viewModel = viewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.placeListFlow().test {
                    awaitItem() shouldBe emptyList()

                    viewModel.updateVisibleBounds(bounds)
                    awaitItem() shouldBe beforePlaceList

                    placeListFlow.value = Result.success(beforePlaceList + addedPlace)
                    awaitItem() shouldBe beforePlaceList + addedPlace
                }
            }
        }

        test("TC-PLACE-HOME-FEATURE-023 목록을 조회하지 못하면 빈 상태로 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(getPlaceListUseCase = getPlaceListUseCase(Result.failure(IllegalStateException("query error"))))

                viewModel.placeListUiState.test {
                    awaitItem().isEmpty shouldBe false

                    viewModel.updateVisibleBounds(fixtureMonkey.giveMeOne<CoordinateBounds>())

                    val uiState = awaitItem()
                    uiState.placeList.shouldBeEmpty()
                    uiState.isEmpty shouldBe true
                }
            }
        }

        test("TC-PLACE-HOME-FEATURE-032 새로고침으로 기기에 저장된 장소가 보이는 영역 안에 있으면 목록에 나타난다") {
            runTest(mainDispatcher) {
                val bounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val shownPlaceList = placeList(PLACE_COUNT)
                val receivedPlace = place()
                val placeListFlow = MutableStateFlow(Result.success(shownPlaceList))
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)) } returns placeListFlow

                val viewModel = viewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.placeListFlow().test {
                    awaitItem() shouldBe emptyList()

                    viewModel.updateVisibleBounds(bounds)
                    awaitItem() shouldBe shownPlaceList

                    placeListFlow.value = Result.success(shownPlaceList + receivedPlace)

                    awaitItem() shouldBe shownPlaceList + receivedPlace
                }

                verify(exactly = 1) { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)) }
            }
        }

        test("TC-PLACE-HOME-FEATURE-035 보이는 영역이 바뀌면 앞선 조회를 기다리지 않고 새 영역의 목록으로 갱신한다") {
            runTest(mainDispatcher) {
                val beforeBounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val afterBounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val beforePlaceList = placeList(PLACE_COUNT)
                val afterPlaceList = placeList(PLACE_COUNT)
                val beforePlaceListFlow = MutableStateFlow(Result.success(beforePlaceList))
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = beforeBounds, sort = ListSort.TITLE)) } returns beforePlaceListFlow
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = afterBounds, sort = ListSort.TITLE)) } returns flowOf(Result.success(afterPlaceList))

                val viewModel = viewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.placeListFlow().test {
                    awaitItem() shouldBe emptyList()

                    viewModel.updateVisibleBounds(beforeBounds)
                    awaitItem() shouldBe beforePlaceList

                    viewModel.updateVisibleBounds(afterBounds)
                    awaitItem() shouldBe afterPlaceList

                    beforePlaceListFlow.value = Result.success(beforePlaceList + place())
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("보이는 영역이 없어지면 빈 목록으로 되돌린다") {
            runTest(mainDispatcher) {
                val bounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
                val placeList = placeList(PLACE_COUNT)
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)) } returns flowOf(Result.success(placeList))

                val viewModel = viewModel(getPlaceListUseCase = getPlaceListUseCase)

                viewModel.placeListFlow().test {
                    awaitItem() shouldBe emptyList()

                    viewModel.updateVisibleBounds(bounds)
                    awaitItem() shouldBe placeList

                    viewModel.updateVisibleBounds(null)
                    awaitItem() shouldBe emptyList()
                }
            }
        }

        test("TC-PLACE-HOME-DOMAIN-020 TC-PLACE-HOME-DATA-004 목록 모드는 보이는 영역과 무관하게 계정의 장소를 제공한다") {
            runTest(mainDispatcher) {
                val placeList = placeList(PLACE_COUNT)
                val viewModel =
                    viewModel(
                        getPlaceListUseCase = getPlaceListUseCase(Result.success(emptyList())),
                        placePagingFlow = flowOf(Result.success(PagingData.from(placeList))),
                    )

                viewModel.updateVisibleBounds(fixtureMonkey.giveMeOne<CoordinateBounds>())

                flowOf(viewModel.placePagingData.first()).asSnapshot() shouldBe placeList
            }
        }

        test("TC-PLACE-HOME-DOMAIN-021 계정의 장소를 조회하지 못하면 목록 모드에 노출하는 장소가 없다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(placePagingFlow = flowOf(Result.failure(IllegalStateException("account error"))))

                flowOf(viewModel.placePagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }
    }

    private companion object {
        private const val PLACE_COUNT = 3

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        // FixtureMonkey가 Instant를 생성하지 못하므로 장소는 직접 만든다.
        private fun place(): Place =
            Place(
                id = Uuid.random(),
                detail = fixtureMonkey.giveMeOne<PlaceDetail>(),
                isDeleted = false,
                updatedAt = instant(),
                createdAt = instant(),
            )

        private fun placeList(count: Int): List<Place> = List(count) { place() }

        private fun getPlaceListUseCase(placeList: Result<List<Place>> = Result.success(emptyList())): GetPlaceListUseCase {
            val useCase = mockk<GetPlaceListUseCase>()
            every { useCase(parameter = any()) } returns flowOf(placeList)

            return useCase
        }

        private fun viewModel(
            getPlaceListUseCase: GetPlaceListUseCase = getPlaceListUseCase(),
            placePagingFlow: Flow<Result<PagingData<Place>>> = emptyFlow(),
        ): PlaceHomePlaceListViewModel {
            val pagePlaceHomeUseCase = mockk<PagePlaceHomeUseCase>()
            every { pagePlaceHomeUseCase(parameter = ListSort.TITLE) } returns placePagingFlow

            return PlaceHomePlaceListViewModel(
                getPlaceListUseCase = getPlaceListUseCase,
                pagePlaceHomeUseCase = pagePlaceHomeUseCase,
            )
        }

        private fun PlaceHomePlaceListViewModel.placeListFlow(): Flow<List<Place>> =
            placeListUiState
                .map { uiState -> uiState.placeList }
                .distinctUntilChanged()
    }
}
