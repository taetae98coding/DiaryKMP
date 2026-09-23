@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.place.usecase.GetTagPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PageTagPlaceUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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

        test("TC-TAG-DETAIL-PLACE-DATA-001 상세 대상 태그로 조회한 목록을 그대로 전달한다") {
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
                    )

                viewModel.placePagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
            }
        }

        test("TC-TAG-DETAIL-PLACE-DOMAIN-002 상세 대상 태그로만 조회한다") {
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
                    )

                viewModel.placePagingData.test {
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
                verify(exactly = 0) { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = otherTagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
            }
        }

        test("TC-TAG-DETAIL-PLACE-DOMAIN-009 지도 모드는 대상 태그와 보이는 영역을 기준으로 조회한 장소를 노출한다") {
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

        test("TC-TAG-DETAIL-PLACE-FEATURE-034 표시 범위를 바꿔도 보고 있던 지도 영역을 그대로 쓴다") {
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

        test("TC-TAG-DETAIL-DATA-005 표시 범위를 바꾸면 장소 목록을 새 기준으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                every { pageTagPlaceUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(emptyList<Place>())))
                val viewModel = viewModel(tagId = tagId, pageTagPlaceUseCase = pageTagPlaceUseCase)

                viewModel.placePagingData.test {
                    awaitItem()
                    viewModel.select(scope = TagScope.DESCENDANT)
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.scope.value shouldBe TagScope.DESCENDANT
                verify(exactly = 1) { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
                verify(exactly = 1) {
                    pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.DESCENDANT, sort = ListSort.TITLE))
                }
            }
        }

        test("TC-TAG-DETAIL-PLACE-DOMAIN-010 TC-TAG-DETAIL-PLACE-DATA-008 목록 모드는 보이는 영역 밖의 장소도 노출한다") {
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

        test("TC-TAG-DETAIL-PLACE-FEATURE-024 보이는 영역을 확인하기 전에는 지도 모드의 목록을 조회하지 않는다") {
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

        test("TC-TAG-DETAIL-PLACE-FEATURE-025 보이는 영역이 바뀌면 바뀐 영역으로 다시 조회한다") {
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

        test("TC-TAG-DETAIL-PLACE-FEATURE-033 정렬을 바꾸면 두 보기 모드의 조회에 함께 적용된다") {
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

        test("TC-TAG-DETAIL-PLACE-FEATURE-004 지도 모드의 조회가 실패하면 빈 목록을 노출한다") {
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

        test("TC-TAG-DETAIL-PLACE-FEATURE-004 조회가 실패하면 목록을 전달하지 않는다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                every { pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns flowOf(Result.failure(throwable))
                val viewModel =
                    TagDetailPlaceViewModel(
                        tagId = tagId,
                        getTagPlaceListUseCase = mockk(),
                        pageTagPlaceUseCase = pageTagPlaceUseCase,
                    )

                viewModel.placePagingData.test {
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            tagId: Uuid,
            getTagPlaceListUseCase: GetTagPlaceListUseCase = mockk(),
            pageTagPlaceUseCase: PageTagPlaceUseCase = mockk(relaxed = true),
        ): TagDetailPlaceViewModel =
            TagDetailPlaceViewModel(
                tagId = tagId,
                getTagPlaceListUseCase = getTagPlaceListUseCase,
                pageTagPlaceUseCase = pageTagPlaceUseCase,
            )

        private fun bounds(): CoordinateBounds =
            fixtureMonkey
                .giveMeKotlinBuilder<CoordinateBounds>()
                .sample()

        private fun item(): Place =
            fixtureMonkey
                .giveMeKotlinBuilder<Place>()
                .setExp(Place::isDeleted, false)
                .setExp(Place::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Place::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
