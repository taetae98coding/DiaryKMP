@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.place.PlaceListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetTagPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PageTagPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.RestorePlaceUseCase
import io.github.taetae98coding.diary.feature.tag.ui.appendFailingPagingData
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
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
import kotlinx.coroutines.flow.MutableStateFlow
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

class TagDetailPlaceViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-68f91887-9083-475d-815a-be0bbbbc9fa1 상세 대상 태그로 조회한 목록을 그대로 전달한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val itemList = List(2) { item() }
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                every { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns flowOf(Result.success(PagingData.from(itemList)))
                val viewModel =
                    TagDetailPlaceViewModel(
                        tagId = tagId,
                        getTagPlaceListUseCase = mockk(),
                        pageTagPlaceUseCase = pageTagPlaceUseCase,
                        deletePlaceUseCase = mockk(),
                        restorePlaceUseCase = mockk(),
                    )

                viewModel.placePagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
            }
        }

        test("TC-6d577b03-3e3d-4397-adb0-3cc423bcec12 상세 대상 태그로만 조회한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val otherTagId = fixtureMonkey.giveMeOne<Uuid>()
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                every { pageTagPlaceUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(emptyList<Place>())))
                val viewModel =
                    TagDetailPlaceViewModel(
                        tagId = tagId,
                        getTagPlaceListUseCase = mockk(),
                        pageTagPlaceUseCase = pageTagPlaceUseCase,
                        deletePlaceUseCase = mockk(),
                        restorePlaceUseCase = mockk(),
                    )

                viewModel.placePagingData.test {
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
                verify(exactly = 0) { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = otherTagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
            }
        }

        test("TC-8243e022-52a0-4d7c-b764-a3ee5f9c965e 지도 모드의 목록에는 보이는 영역 안의 장소만 남고 영역 밖의 장소는 나타나지 않는다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val bounds = bounds()
                val insidePlace = item()
                val outsidePlace = item()
                val getTagPlaceListUseCase = mockk<GetTagPlaceListUseCase>()
                every {
                    getTagPlaceListUseCase(
                        parameter = GetTagPlaceListUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, bounds = bounds, sort = ListSort.TITLE),
                    )
                } returns flowOf(Result.success(listOf(insidePlace)))
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                every { pageTagPlaceUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(listOf(insidePlace, outsidePlace))))
                val viewModel = viewModel(tagId = tagId, getTagPlaceListUseCase = getTagPlaceListUseCase, pageTagPlaceUseCase = pageTagPlaceUseCase)

                viewModel.placeListUiState.test {
                    awaitItem() shouldBe TagDetailPlaceListUiState()

                    viewModel.updateVisibleBounds(bounds)

                    awaitItem() shouldBe TagDetailPlaceListUiState(isLoaded = true, placeList = listOf(insidePlace))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-91c58f1b-a800-4028-90b3-e6b6e9fa953c 지도 모드는 대상 태그와 보이는 영역을 기준으로 조회한 장소를 노출한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val bounds = bounds()
                val placeList = List(2) { item() }
                val getTagPlaceListUseCase = mockk<GetTagPlaceListUseCase>()
                val parameter = GetTagPlaceListUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, bounds = bounds, sort = ListSort.TITLE)
                every { getTagPlaceListUseCase(parameter = parameter) } returns flowOf(Result.success(placeList))
                val viewModel = viewModel(tagId = tagId, getTagPlaceListUseCase = getTagPlaceListUseCase)

                viewModel.placeListUiState.test {
                    awaitItem() shouldBe TagDetailPlaceListUiState()

                    viewModel.updateVisibleBounds(bounds)

                    awaitItem() shouldBe TagDetailPlaceListUiState(isLoaded = true, placeList = placeList)
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { getTagPlaceListUseCase(parameter = parameter) }
            }
        }

        test("TC-4c9da472-7698-4c44-9b72-81f2e2887a25 표시 범위를 바꿔도 보고 있던 지도 영역을 그대로 쓴다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val bounds = bounds()
                val widenedPlaceList = List(2) { item() }
                val getTagPlaceListUseCase = mockk<GetTagPlaceListUseCase>()
                val selfParameter = GetTagPlaceListUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, bounds = bounds, sort = ListSort.TITLE)
                val childParameter = GetTagPlaceListUseCase.Parameter(tagId = tagId, scope = TagScope.CHILD, bounds = bounds, sort = ListSort.TITLE)
                every { getTagPlaceListUseCase(parameter = selfParameter) } returns flowOf(Result.success(emptyList()))
                every { getTagPlaceListUseCase(parameter = childParameter) } returns flowOf(Result.success(widenedPlaceList))
                val viewModel = viewModel(tagId = tagId, getTagPlaceListUseCase = getTagPlaceListUseCase)

                viewModel.placeListUiState.test {
                    awaitItem() shouldBe TagDetailPlaceListUiState()

                    viewModel.updateVisibleBounds(bounds)
                    awaitItem() shouldBe TagDetailPlaceListUiState(isLoaded = true, placeList = emptyList())

                    viewModel.select(scope = TagScope.CHILD)
                    awaitItem() shouldBe TagDetailPlaceListUiState(isLoaded = true, placeList = widenedPlaceList)
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { getTagPlaceListUseCase(parameter = childParameter) }
            }
        }

        test("TC-6e914ebb-0357-4604-87f2-6dc4438a9e03 표시 범위를 넓혔다 되돌리면 장소 목록을 각 범위 기준으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val selfPlaceList = List(2) { item() }
                val childPlaceList = selfPlaceList + List(2) { item() }
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                every { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns
                    flowOf(Result.success(PagingData.from(selfPlaceList)))
                every { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.CHILD, sort = ListSort.TITLE)) } returns
                    flowOf(Result.success(PagingData.from(childPlaceList)))
                val viewModel = viewModel(tagId = tagId, pageTagPlaceUseCase = pageTagPlaceUseCase)

                viewModel.placePagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe selfPlaceList

                    viewModel.select(scope = TagScope.CHILD)
                    flowOf(awaitItem()).asSnapshot() shouldBe childPlaceList

                    viewModel.select(scope = TagScope.SELF)
                    flowOf(awaitItem()).asSnapshot() shouldBe selfPlaceList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
        test("TC-4544d96c-500c-4c7b-82bc-83e74cb32cf6 목록 모드는 보이는 영역 밖의 장소도 노출한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val bounds = bounds()
                val outsidePlaceList = List(2) { item() }
                val getTagPlaceListUseCase = mockk<GetTagPlaceListUseCase>()
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                every { getTagPlaceListUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                every { pageTagPlaceUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(outsidePlaceList)))
                val viewModel =
                    viewModel(
                        tagId = tagId,
                        getTagPlaceListUseCase = getTagPlaceListUseCase,
                        pageTagPlaceUseCase = pageTagPlaceUseCase,
                        deletePlaceUseCase = mockk(),
                        restorePlaceUseCase = mockk(),
                    )

                viewModel.placeListUiState.test {
                    awaitItem() shouldBe TagDetailPlaceListUiState()
                    viewModel.updateVisibleBounds(bounds)
                    awaitItem() shouldBe TagDetailPlaceListUiState(isLoaded = true, placeList = emptyList())
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.placePagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe outsidePlaceList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-4a4dd24a-ceb3-4905-b07e-2a55d2749637 지도 모드의 목록은 영역 안 장소가 한 페이지보다 많아도 한 번에 모두 조회한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val bounds = bounds()
                val boundsPlaceList = List(MORE_THAN_ONE_PAGE) { item() }
                val getTagPlaceListUseCase = mockk<GetTagPlaceListUseCase>()
                every {
                    getTagPlaceListUseCase(parameter = GetTagPlaceListUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, bounds = bounds, sort = ListSort.TITLE))
                } returns flowOf(Result.success(boundsPlaceList))
                val viewModel = viewModel(tagId = tagId, getTagPlaceListUseCase = getTagPlaceListUseCase)

                viewModel.placeListUiState.test {
                    awaitItem() shouldBe TagDetailPlaceListUiState()
                    viewModel.updateVisibleBounds(bounds)
                    awaitItem() shouldBe TagDetailPlaceListUiState(isLoaded = true, placeList = boundsPlaceList)
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { getTagPlaceListUseCase(parameter = any()) }
            }
        }

        test("TC-c7314bd3-f4a3-42e1-a386-c51b0b0931d0 보이는 영역을 확인하기 전에는 지도 모드의 목록을 조회하지 않는다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val getTagPlaceListUseCase = mockk<GetTagPlaceListUseCase>()
                val viewModel = viewModel(tagId = tagId, getTagPlaceListUseCase = getTagPlaceListUseCase)

                viewModel.placeListUiState.test {
                    awaitItem() shouldBe TagDetailPlaceListUiState()
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 0) { getTagPlaceListUseCase(parameter = any()) }
            }
        }

        test("TC-bfb27e9f-3201-4ee7-861f-fd9c26ee3584 보이는 영역이 바뀌면 바뀐 영역으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val firstBounds = bounds()
                val secondBounds = bounds()
                val firstPlaceList = List(1) { item() }
                val secondPlaceList = List(2) { item() }
                val getTagPlaceListUseCase = mockk<GetTagPlaceListUseCase>()
                val firstParameter = GetTagPlaceListUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, bounds = firstBounds, sort = ListSort.TITLE)
                val secondParameter = GetTagPlaceListUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, bounds = secondBounds, sort = ListSort.TITLE)
                every { getTagPlaceListUseCase(parameter = firstParameter) } returns flowOf(Result.success(firstPlaceList))
                every { getTagPlaceListUseCase(parameter = secondParameter) } returns flowOf(Result.success(secondPlaceList))
                val viewModel = viewModel(tagId = tagId, getTagPlaceListUseCase = getTagPlaceListUseCase)

                viewModel.placeListUiState.test {
                    awaitItem() shouldBe TagDetailPlaceListUiState()

                    viewModel.updateVisibleBounds(firstBounds)
                    awaitItem() shouldBe TagDetailPlaceListUiState(isLoaded = true, placeList = firstPlaceList)

                    viewModel.updateVisibleBounds(secondBounds)
                    awaitItem() shouldBe TagDetailPlaceListUiState(isLoaded = true, placeList = secondPlaceList)

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-1335953b-f1a2-4ad9-adf5-f4f1459b190a 정렬을 바꾸면 두 보기 모드의 조회에 함께 적용된다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val bounds = bounds()
                val titlePlaceList = List(1) { item() }
                val recentlyUpdatedPlaceList = List(2) { item() }
                val getTagPlaceListUseCase = mockk<GetTagPlaceListUseCase>()
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                every {
                    getTagPlaceListUseCase(parameter = GetTagPlaceListUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, bounds = bounds, sort = ListSort.TITLE))
                } returns flowOf(Result.success(titlePlaceList))
                every {
                    getTagPlaceListUseCase(parameter = GetTagPlaceListUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, bounds = bounds, sort = ListSort.RECENTLY_UPDATED))
                } returns flowOf(Result.success(recentlyUpdatedPlaceList))
                every { pageTagPlaceUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(emptyList<Place>())))
                val viewModel =
                    viewModel(
                        tagId = tagId,
                        getTagPlaceListUseCase = getTagPlaceListUseCase,
                        pageTagPlaceUseCase = pageTagPlaceUseCase,
                        deletePlaceUseCase = mockk(),
                        restorePlaceUseCase = mockk(),
                    )

                viewModel.placeListUiState.test {
                    awaitItem() shouldBe TagDetailPlaceListUiState()

                    viewModel.updateVisibleBounds(bounds)
                    awaitItem() shouldBe TagDetailPlaceListUiState(isLoaded = true, placeList = titlePlaceList)

                    viewModel.select(sort = ListSort.RECENTLY_UPDATED)
                    awaitItem() shouldBe TagDetailPlaceListUiState(isLoaded = true, placeList = recentlyUpdatedPlaceList)

                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.placePagingData.test {
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) {
                    getTagPlaceListUseCase(
                        parameter = GetTagPlaceListUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, bounds = bounds, sort = ListSort.RECENTLY_UPDATED),
                    )
                }
                verify(exactly = 1) {
                    pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.RECENTLY_UPDATED))
                }
            }
        }

        test("TC-86a673d2-f136-4633-a5b5-a02c9417252f 지도 모드의 조회가 실패하면 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val bounds = bounds()
                val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
                val getTagPlaceListUseCase = mockk<GetTagPlaceListUseCase>()
                every { getTagPlaceListUseCase(parameter = any()) } returns flowOf(Result.failure(throwable))
                val viewModel = viewModel(tagId = tagId, getTagPlaceListUseCase = getTagPlaceListUseCase)

                viewModel.placeListUiState.test {
                    awaitItem() shouldBe TagDetailPlaceListUiState()

                    viewModel.updateVisibleBounds(bounds)

                    awaitItem() shouldBe TagDetailPlaceListUiState(isLoaded = true, placeList = emptyList())
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-86a673d2-f136-4633-a5b5-a02c9417252f 최초 조회가 실패하면 조회가 끝난 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                every { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns flowOf(Result.failure(throwable))
                val viewModel = viewModel(tagId = tagId, pageTagPlaceUseCase = pageTagPlaceUseCase)

                viewModel.placePagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList.shouldBeEmpty()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-86a673d2-f136-4633-a5b5-a02c9417252f 이어서 불러오기에 실패해도 이미 불러온 장소를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val placeList = List(2) { item() }
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                every { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns
                    appendFailingPagingData(firstPage = placeList).map { pagingData -> Result.success(pagingData) }
                val viewModel = viewModel(tagId = tagId, pageTagPlaceUseCase = pageTagPlaceUseCase)

                val itemList =
                    flowOf(viewModel.placePagingData.first()).asSnapshot(onError = { ErrorRecovery.RETURN_CURRENT_SNAPSHOT }) {
                        appendScrollWhile { true }
                    }

                itemList shouldBe placeList
            }
        }

        test("TC-86a673d2-f136-4633-a5b5-a02c9417252f 조회가 성공한 뒤 다시 조회하다 실패하면 이전 장소를 남기지 않고 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val placeList = List(2) { item() }
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                val resultFlow = MutableStateFlow<Result<PagingData<Place>>>(Result.success(PagingData.from(placeList)))
                every { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns resultFlow
                val viewModel = viewModel(tagId = tagId, pageTagPlaceUseCase = pageTagPlaceUseCase)

                viewModel.placePagingData.test {
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe placeList

                    resultFlow.value = Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot() shouldBe emptyList()
                    expectNoEvents()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("삭제를 처리하는 동안 같은 장소의 삭제를 다시 요청해도 삭제 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val gate = CompletableDeferred<Unit>()
                val deletePlaceUseCase = mockk<DeletePlaceUseCase>()
                coEvery { deletePlaceUseCase(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = viewModel(tagId = fixtureMonkey.giveMeOne<Uuid>(), deletePlaceUseCase = deletePlaceUseCase)

                viewModel.delete(id = id)
                viewModel.delete(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { deletePlaceUseCase(parameter = id) }
            }
        }

        test("복구를 처리하는 동안 같은 장소의 복구를 다시 요청해도 복구 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val gate = CompletableDeferred<Unit>()
                val restorePlaceUseCase = mockk<RestorePlaceUseCase>()
                coEvery { restorePlaceUseCase(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = viewModel(tagId = fixtureMonkey.giveMeOne<Uuid>(), restorePlaceUseCase = restorePlaceUseCase)

                viewModel.restore(id = id)
                viewModel.restore(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { restorePlaceUseCase(parameter = id) }
            }
        }

        test("TC-92903379-bc52-4bad-8c9a-fe9055f0e396 삭제에 성공하면 그 장소의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deletePlaceUseCase = mockk<DeletePlaceUseCase>()
                coEvery { deletePlaceUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(tagId = fixtureMonkey.giveMeOne<Uuid>(), deletePlaceUseCase = deletePlaceUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = id)

                    awaitItem() shouldBe PlaceListEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { deletePlaceUseCase(parameter = id) }
            }
        }

        test("TC-5e317aae-d4da-4972-a4ce-53ad464cf901 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deletePlaceUseCase = mockk<DeletePlaceUseCase>()
                coEvery { deletePlaceUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(tagId = fixtureMonkey.giveMeOne<Uuid>(), deletePlaceUseCase = deletePlaceUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-2d118d0a-ac53-41f3-b4ea-8a6a05fbda7a 실행 취소하면 그 장소의 삭제를 되돌리는 요청을 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restorePlaceUseCase = mockk<RestorePlaceUseCase>()
                coEvery { restorePlaceUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(tagId = fixtureMonkey.giveMeOne<Uuid>(), restorePlaceUseCase = restorePlaceUseCase)

                viewModel.effect.test {
                    viewModel.restore(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { restorePlaceUseCase(parameter = id) }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private const val MORE_THAN_ONE_PAGE = 25

        private fun viewModel(
            tagId: Uuid,
            getTagPlaceListUseCase: GetTagPlaceListUseCase = mockk(),
            pageTagPlaceUseCase: PageTagPlaceUseCase = mockk(relaxed = true),
            deletePlaceUseCase: DeletePlaceUseCase = mockk(),
            restorePlaceUseCase: RestorePlaceUseCase = mockk(),
        ): TagDetailPlaceViewModel =
            TagDetailPlaceViewModel(
                tagId = tagId,
                getTagPlaceListUseCase = getTagPlaceListUseCase,
                pageTagPlaceUseCase = pageTagPlaceUseCase,
                deletePlaceUseCase = deletePlaceUseCase,
                restorePlaceUseCase = restorePlaceUseCase,
            )

        private fun bounds(): CoordinateBounds =
            fixtureMonkey
                .giveMeKotlinBuilder<CoordinateBounds>()
                .sample()

        private fun item(): Place =
            fixtureMonkey
                .giveMeKotlinBuilder<Place>()
                .setExp(Place::isDeleted, false)
                .setExp(Place::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Place::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
