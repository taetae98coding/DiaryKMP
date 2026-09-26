@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoPlaceViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MEMO-PLACE-CARD-FEATURE-042 선택 목록을 열지 않아도 선택할 수 있는 장소 전체를 빈 검색어로 조회한다") {
            runTest(mainDispatcher) {
                val item = place()
                val pagePlaceUseCase = mockk<PagePlaceUseCase>()
                every { pagePlaceUseCase(parameter = "") } returns flowOf(Result.success(PagingData.from(listOf(item))))
                val viewModel = viewModel(pagePlaceUseCase = pagePlaceUseCase, isListOpened = false)

                val itemList = flowOf(viewModel.selectablePlacePagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList shouldBe listOf(item)
                verify(exactly = 1) { pagePlaceUseCase(parameter = "") }
            }
        }

        test("저장된 장소 연결이 조회되지 않으면 로딩 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(memoPlaceFlow = emptyFlow())

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceInputUiState()
                    advanceUntilIdle()
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("장소 목록 페이지 조회에 실패하면 없는 것으로 확정할 목록을 전달하지 않고 선택 상태는 그대로 표시한다") {
            runTest(mainDispatcher) {
                val connectedPlace = place()
                val viewModel =
                    viewModel(
                        placePagingFlow = flowOf(Result.failure(IllegalStateException("place error"))),
                        memoPlaceFlow = flowOf(Result.success(listOf(connectedPlace))),
                    )

                viewModel.placePagingData.test {
                    advanceUntilIdle()
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(connectedPlace))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("장소 목록 페이지 조회 결과를 그대로 전달한다") {
            runTest(mainDispatcher) {
                val placeList = List(2) { place() }
                val viewModel = viewModel(placePagingFlow = flowOf(Result.success(PagingData.from(placeList))))

                val itemList = flowOf(viewModel.placePagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList shouldBe placeList
            }
        }

        test("저장된 장소 연결 조회에 실패하면 로딩 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(memoPlaceFlow = flowOf(Result.failure(IllegalStateException("memo place error"))))

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceInputUiState()
                    advanceUntilIdle()
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-DOMAIN-015 장소 목록이 준비되지 않아도 선택한 장소를 표시한다") {
            runTest(mainDispatcher) {
                val connectedPlace = place()
                val viewModel =
                    viewModel(
                        placePagingFlow = emptyFlow(),
                        memoPlaceFlow = flowOf(Result.success(listOf(connectedPlace))),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(connectedPlace))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-055 장소 카드에 저장된 장소 연결이 표시된다") {
            runTest(mainDispatcher) {
                val connectedPlace = place()
                val otherPlace = place()
                val viewModel =
                    viewModel(
                        placePagingFlow = flowOf(Result.success(PagingData.from(listOf(connectedPlace, otherPlace)))),
                        memoPlaceFlow = flowOf(Result.success(listOf(connectedPlace))),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(connectedPlace))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-056 다른 경로로 저장된 장소 연결이 바뀌면 장소 카드에 반영된다") {
            runTest(mainDispatcher) {
                val place = place()
                val memoPlaceFlow = MutableStateFlow(Result.success(emptyList<Place>()))
                val viewModel =
                    viewModel(
                        placePagingFlow = flowOf(Result.success(PagingData.from(listOf(place)))),
                        memoPlaceFlow = memoPlaceFlow,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = emptyList())

                    memoPlaceFlow.value = Result.success(listOf(place))
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(place))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-057 장소 연결 변경에 실패하면 별도 안내 없이 저장된 연결을 그대로 표시한다") {
            runTest(mainDispatcher) {
                val connectedPlace = place()
                val otherPlace = place()
                val addMemoPlaceUseCase = mockk<AddMemoPlaceUseCase>()
                coEvery { addMemoPlaceUseCase(any<AddMemoPlaceUseCase.Parameter>()) } returns Result.failure(IllegalStateException("save error"))
                val removeMemoPlaceUseCase = mockk<RemoveMemoPlaceUseCase>()
                coEvery { removeMemoPlaceUseCase(any<RemoveMemoPlaceUseCase.Parameter>()) } returns Result.failure(IllegalStateException("save error"))
                val viewModel =
                    viewModel(
                        placePagingFlow = flowOf(Result.success(PagingData.from(listOf(connectedPlace, otherPlace)))),
                        memoPlaceFlow = flowOf(Result.success(listOf(connectedPlace))),
                        addMemoPlaceUseCase = addMemoPlaceUseCase,
                        removeMemoPlaceUseCase = removeMemoPlaceUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceInputUiState()
                    advanceUntilIdle()

                    val savedUiState = expectMostRecentItem()
                    savedUiState shouldBe MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(connectedPlace))

                    viewModel.selectPlace(placeId = otherPlace.id)
                    viewModel.unselectPlace(placeId = connectedPlace.id)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value shouldBe savedUiState
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-PLACE-CARD-DOMAIN-011 삭제된 장소는 선택한 것으로 표시하지 않는다") {
            runTest(mainDispatcher) {
                val remainingPlace = place()
                val deletedPlace = place()
                val memoPlaceFlow = MutableStateFlow(Result.success(listOf(deletedPlace)))
                val viewModel =
                    viewModel(
                        placePagingFlow = flowOf(Result.success(PagingData.from(listOf(remainingPlace, deletedPlace)))),
                        memoPlaceFlow = memoPlaceFlow,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(deletedPlace))

                    memoPlaceFlow.value = Result.success(emptyList())
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = emptyList())
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("장소를 선택하면 그 메모와 장소의 연결 추가를 요청한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val placeId = fixtureMonkey.giveMeOne<Uuid>()
                val addMemoPlaceUseCase = mockk<AddMemoPlaceUseCase>(relaxed = true)
                val viewModel = viewModel(id = id, addMemoPlaceUseCase = addMemoPlaceUseCase)

                viewModel.selectPlace(placeId = placeId)
                advanceUntilIdle()

                coVerify(exactly = 1) { addMemoPlaceUseCase(AddMemoPlaceUseCase.Parameter(memoId = id, placeId = placeId)) }
            }
        }

        test("장소 선택을 해제하면 그 메모와 장소의 연결 제거를 요청한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val placeId = fixtureMonkey.giveMeOne<Uuid>()
                val removeMemoPlaceUseCase = mockk<RemoveMemoPlaceUseCase>(relaxed = true)
                val viewModel = viewModel(id = id, removeMemoPlaceUseCase = removeMemoPlaceUseCase)

                viewModel.unselectPlace(placeId = placeId)
                advanceUntilIdle()

                coVerify(exactly = 1) { removeMemoPlaceUseCase(RemoveMemoPlaceUseCase.Parameter(memoId = id, placeId = placeId)) }
            }
        }

        test("TC-MEMO-DETAIL-DOMAIN-008 앞선 장소 연결 변경이 처리 중이어도 뒤이은 장소 연결 변경을 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val firstPlaceId = fixtureMonkey.giveMeOne<Uuid>()
                val secondPlaceId = fixtureMonkey.giveMeOne<Uuid>()
                val completion = CompletableDeferred<Result<Unit>>()
                val addMemoPlaceUseCase = mockk<AddMemoPlaceUseCase>()
                coEvery { addMemoPlaceUseCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(id = id, addMemoPlaceUseCase = addMemoPlaceUseCase)

                viewModel.selectPlace(placeId = firstPlaceId)
                runCurrent()
                viewModel.selectPlace(placeId = secondPlaceId)
                runCurrent()
                completion.complete(Result.success(Unit))
                advanceUntilIdle()

                coVerify(exactly = 1) { addMemoPlaceUseCase(AddMemoPlaceUseCase.Parameter(memoId = id, placeId = firstPlaceId)) }
                coVerify(exactly = 1) { addMemoPlaceUseCase(AddMemoPlaceUseCase.Parameter(memoId = id, placeId = secondPlaceId)) }
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
                val connectedPlaceList = List(2) { place() }
                val viewModel = viewModel(memoPlaceFlow = flowOf(Result.success(connectedPlaceList)))

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoPlaceInputUiState()
                    advanceUntilIdle()
                    expectMostRecentItem() shouldBe
                        MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = connectedPlaceList)

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    public companion object {
        private const val SEARCH_QUERY = "Riverside"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun place(): Place =
            fixtureMonkey
                .giveMeKotlinBuilder<Place>()
                .setExp(Place::isDeleted, false)
                .setExp(Place::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Place::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()

        private fun viewModel(
            id: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            placePagingFlow: Flow<Result<PagingData<Place>>> = emptyFlow(),
            memoPlaceFlow: Flow<Result<List<Place>>> = emptyFlow(),
            addMemoPlaceUseCase: AddMemoPlaceUseCase = mockk(relaxed = true),
            removeMemoPlaceUseCase: RemoveMemoPlaceUseCase = mockk(relaxed = true),
            pagePlaceUseCase: PagePlaceUseCase =
                mockk<PagePlaceUseCase>().apply {
                    every { this@apply(parameter = any()) } returns placePagingFlow
                },
            isListOpened: Boolean = true,
        ): MemoPlaceViewModel {
            val getMemoPlaceUseCase = mockk<GetMemoPlaceUseCase>()
            every { getMemoPlaceUseCase(any()) } returns memoPlaceFlow

            return MemoPlaceViewModel(
                id = id,
                pagePlaceUseCase = pagePlaceUseCase,
                getMemoPlaceUseCase = getMemoPlaceUseCase,
                addMemoPlaceUseCase = addMemoPlaceUseCase,
                removeMemoPlaceUseCase = removeMemoPlaceUseCase,
            ).apply {
                // 화면은 선택 목록을 열 때 검색어를 알려 주므로, 목록이 열린 상태를 만든다.
                if (isListOpened) updateQuery(query = "")
            }
        }
    }
}
