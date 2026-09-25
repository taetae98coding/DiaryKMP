@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.place.usecase.GetSelectedPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceUseCase
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceInputUiState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

class MemoAddPlaceViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("선택한 장소가 조회되지 않으면 장소 카드는 로딩 상태를 유지한다") {
            runTest(mainDispatcher) {
                val place = place()
                val viewModel = viewModel(savedPlaceListFlow = emptyFlow())

                viewModel.selectPlace(id = place.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoPlaceInputUiState()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("선택한 장소 조회에 실패하면 장소 카드는 로딩 상태를 유지한다") {
            runTest(mainDispatcher) {
                val place = place()
                val viewModel = viewModel(savedPlaceListFlow = flowOf(Result.failure(IllegalStateException("place error"))))

                viewModel.selectPlace(id = place.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoPlaceInputUiState()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("장소 선택 목록은 페이지 조회 결과를 그대로 전달한다") {
            runTest(mainDispatcher) {
                val placeList = listOf(place(), place())
                val viewModel = viewModel(placePagingFlow = flowOf(Result.success(PagingData.from(placeList))))

                val itemList = flowOf(viewModel.placePagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList shouldBe placeList
            }
        }

        test("장소 선택 목록 페이지 조회에 실패하면 없는 것으로 확정할 목록을 전달하지 않는다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(placePagingFlow = flowOf(Result.failure(IllegalStateException("place error"))))

                viewModel.placePagingData.test {
                    advanceUntilIdle()
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-MEMO-ADD-FEATURE-063 PlaceDetail 메모 탭에서 진입하면 그 장소만 선택된 상태로 시작한다") {
            runTest(mainDispatcher) {
                val target = place()
                val other = place()
                val viewModel =
                    viewModel(
                        initialPlaceId = target.id,
                        placePagingFlow = flowOf(Result.success(PagingData.from(listOf(target, other)))),
                        savedPlaceListFlow = flowOf(Result.success(listOf(target, other))),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedPlaceList shouldBe listOf(target)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-FEATURE-064 PlaceDetail 메모 탭에서 진입해도 초기 선택을 해제하거나 더 선택할 수 있다") {
            runTest(mainDispatcher) {
                val target = place()
                val other = place()
                val viewModel =
                    viewModel(
                        initialPlaceId = target.id,
                        savedPlaceListFlow = flowOf(Result.success(listOf(target, other))),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().selectedPlaceList shouldBe listOf(target)

                    viewModel.unselectPlace(id = target.id)
                    viewModel.selectPlace(id = other.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedPlaceList shouldBe listOf(other)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-DOMAIN-015 삭제된 대상 장소는 PlaceDetail 메모 탭에서 진입해도 선택되지 않은 상태로 시작한다") {
            runTest(mainDispatcher) {
                val deletedTarget = place()
                val viewModel =
                    viewModel(
                        initialPlaceId = deletedTarget.id,
                        savedPlaceListFlow = flowOf(Result.success(emptyList())),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedPlaceList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-FEATURE-048 진입하면 선택한 장소가 없는 상태로 시작한다") {
            runTest(mainDispatcher) {
                val placeList = listOf(place(), place())
                val viewModel =
                    viewModel(
                        placePagingFlow = flowOf(Result.success(PagingData.from(placeList))),
                        savedPlaceListFlow = flowOf(Result.success(placeList)),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedPlaceList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("선택한 장소를 즉시 선택 장소 목록으로 노출하고 해제하면 즉시 제외한다") {
            runTest(mainDispatcher) {
                val place = place()
                val viewModel = viewModel(savedPlaceListFlow = flowOf(Result.success(listOf(place))))

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedPlaceList.shouldBeEmpty()

                    viewModel.selectPlace(id = place.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedPlaceList shouldBe listOf(place)

                    viewModel.unselectPlace(id = place.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedPlaceList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-FEATURE-050 화면 구성이 변경되어도 선택한 장소를 유지한다") {
            runTest(mainDispatcher) {
                val place = place()
                val viewModel = viewModel(savedPlaceListFlow = flowOf(Result.success(listOf(place))))

                viewModel.uiState.test {
                    advanceUntilIdle()
                    viewModel.selectPlace(id = place.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedPlaceList shouldBe listOf(place)
                    cancelAndIgnoreRemainingEvents()
                }

                // 화면 구성 변경으로 UI가 재생성되어 같은 ViewModel을 새로 구독해도 선택이 유지된다.
                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedPlaceList shouldBe listOf(place)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-DOMAIN-009 선택한 장소가 삭제되면 선택에서 제외된 것으로 표시된다") {
            runTest(mainDispatcher) {
                val remainingPlace = place()
                val removedPlace = place()
                val savedPlaceListFlow = MutableStateFlow(Result.success(listOf(remainingPlace, removedPlace)))
                val viewModel = viewModel(savedPlaceListFlow = savedPlaceListFlow)

                viewModel.selectPlace(id = removedPlace.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedPlaceList shouldBe listOf(removedPlace)

                    savedPlaceListFlow.value = Result.success(listOf(remainingPlace))
                    advanceUntilIdle()

                    expectMostRecentItem().selectedPlaceList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-DATA-013 선택한 뒤 삭제된 장소도 선택으로 남는다") {
            runTest(mainDispatcher) {
                val remainingPlace = place()
                val deletedPlace = place()
                val savedPlaceListFlow = MutableStateFlow(Result.success(listOf(remainingPlace, deletedPlace)))
                val viewModel = viewModel(savedPlaceListFlow = savedPlaceListFlow)

                viewModel.uiState.test {
                    advanceUntilIdle()
                    viewModel.selectPlace(id = remainingPlace.id)
                    viewModel.selectPlace(id = deletedPlace.id)
                    advanceUntilIdle()

                    savedPlaceListFlow.value = Result.success(listOf(remainingPlace))
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.placeIdSet.value shouldBe setOf(remainingPlace.id, deletedPlace.id)
            }
        }

        test("선택할 수 있는 장소에 없는 장소도 선택으로 남는다") {
            runTest(mainDispatcher) {
                val selectablePlace = place()
                val unselectablePlace = place()
                val viewModel = viewModel(savedPlaceListFlow = flowOf(Result.success(listOf(selectablePlace))))

                viewModel.uiState.test {
                    advanceUntilIdle()
                    viewModel.selectPlace(id = selectablePlace.id)
                    viewModel.selectPlace(id = unselectablePlace.id)
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.placeIdSet.value shouldBe setOf(selectablePlace.id, unselectablePlace.id)
            }
        }
        searchTests()
    }

    private fun searchTests() {
        test("TC-MEMO-PLACE-CARD-DATA-004 검색어가 바뀌면 새 검색어 기준으로 선택 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val allPlaceList = List(2) { place() }
                val matchedPlaceList = listOf(allPlaceList.first())
                val pagePlaceUseCase = mockk<PagePlaceUseCase>()
                every { pagePlaceUseCase(parameter = "") } returns flowOf(Result.success(PagingData.from(allPlaceList)))
                every { pagePlaceUseCase(parameter = SEARCH_QUERY) } returns flowOf(Result.success(PagingData.from(matchedPlaceList)))
                val viewModel = viewModel(pagePlaceUseCase = pagePlaceUseCase)

                viewModel.placePagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe allPlaceList

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot() shouldBe matchedPlaceList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-DOMAIN-022 검색어를 바꿔도 카드에 노출하는 장소는 그대로다") {
            runTest(mainDispatcher) {
                val place = place()
                val viewModel = viewModel(savedPlaceListFlow = flowOf(Result.success(listOf(place))))
                viewModel.selectPlace(id = place.id)

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem() shouldBe MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(place))

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private companion object {
        private const val SEARCH_QUERY = "Riverside"
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun place(): Place =
            fixtureMonkey
                .giveMeKotlinBuilder<Place>()
                .setExp(Place::isDeleted, false)
                .setExp(Place::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Place::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun viewModel(
            initialPlaceId: Uuid? = null,
            placePagingFlow: Flow<Result<PagingData<Place>>> = emptyFlow(),
            savedPlaceListFlow: Flow<Result<List<Place>>> = flowOf(Result.success(emptyList())),
            pagePlaceUseCase: PagePlaceUseCase =
                mockk<PagePlaceUseCase>().apply {
                    every { this@apply(parameter = any()) } returns placePagingFlow
                },
        ): MemoAddPlaceViewModel {
            // 선택한 식별자로 저장소를 조회하는 동작을 저장된 장소 목록에서 골라내는 방식으로 대신한다.
            val getSelectedPlaceUseCase = mockk<GetSelectedPlaceUseCase>()
            every { getSelectedPlaceUseCase(parameter = any()) } answers {
                val placeIdSet = firstArg<Set<Uuid>>()

                if (placeIdSet.isEmpty()) {
                    flowOf(Result.success(emptyList()))
                } else {
                    savedPlaceListFlow.map { result -> result.map { placeList -> placeList.filter { place -> place.id in placeIdSet } } }
                }
            }

            return MemoAddPlaceViewModel(
                initialPlaceId = initialPlaceId,
                pagePlaceUseCase = pagePlaceUseCase,
                getSelectedPlaceUseCase = getSelectedPlaceUseCase,
            )
        }
    }
}
