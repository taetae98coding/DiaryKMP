package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.toCoordinateBounds
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.testing.place.coordinateInFormPrecision
import io.github.taetae98coding.diary.feature.place.ui.boundsAround
import io.github.taetae98coding.diary.feature.place.ui.reportCamera
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceSearchTriggerTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-2a4b5f0a-6f18-4f71-8a49-3874203acca9 검색어를 입력하지 않으면 검색하지 않는다`() {
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        composeRule.setPlaceSearchEffect(onSearch = { request -> requestList += request })

        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList shouldBe emptyList()
    }

    @Test
    fun `TC-2a4b5f0a-6f18-4f71-8a49-3874203acca9 검색어가 공백뿐이면 검색하지 않는다`() {
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        composeRule.setPlaceSearchEffect(onSearch = { request -> requestList += request })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(BLANK_QUERY)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList shouldBe emptyList()
    }

    @Test
    fun `TC-e54f08ff-fd57-4216-b806-fe8aa0fa3f9f 입력을 멈추고 0_2초가 지나면 한 번 검색한다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        composeRule.setPlaceSearchEffect(onSearch = { request -> requestList += request })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS - 1)

        requestList.size shouldBe 0

        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS)

        requestList.size shouldBe 1
        requestList.single().query shouldBe query
    }

    @Test
    fun `TC-01b6b7b0-6457-4fe5-a164-a6cd8d1267b0 기다리는 동안 다시 입력하면 마지막 검색어로만 검색한다`() {
        val changedQuery = randomText(prefix = CHANGED_QUERY_PREFIX)
        val query = randomText(prefix = QUERY_PREFIX)
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        composeRule.setPlaceSearchEffect(onSearch = { request -> requestList += request })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS / 2)
        composeRule.onNodeWithText(query).performTextReplacement(changedQuery)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList.size shouldBe 1
        requestList.single().query shouldBe changedQuery
    }

    @Test
    fun `TC-3df85429-996d-43ad-86e1-fd8d32290d4c 제공자를 바꾸면 0_2초가 지난 뒤 다시 검색한다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        val state = composeRule.setPlaceSearchEffect(onSearch = { request -> requestList += request })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList.single().provider shouldBe MapProvider.NAVER

        composeRule.runOnIdle { state().mapState.select(DiaryMapProvider.GOOGLE) }
        composeRule.mainClock.advanceTimeBy(IMMEDIATE_MILLIS)

        requestList.size shouldBe 1

        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS)

        requestList.size shouldBe 2
        requestList.last().provider shouldBe MapProvider.GOOGLE
        requestList.last().query shouldBe query
    }

    @Test
    fun `TC-f0af275c-669c-4614-89d4-fb32041377b3 제공자를 바꾸면 다시 검색을 기다리지 않고 결과를 비운다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        var clearCount = 0

        composeRule.mainClock.autoAdvance = false
        val state = composeRule.setPlaceSearchEffect(onClear = { clearCount++ })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)
        val clearCountAfterSearch = clearCount

        composeRule.runOnIdle { state().mapState.select(DiaryMapProvider.GOOGLE) }
        composeRule.mainClock.advanceTimeBy(IMMEDIATE_MILLIS)

        clearCount shouldBe clearCountAfterSearch + 1
    }

    @Test
    fun `TC-ff385436-b2ef-4ecf-8980-04e5e30cb736 검색어가 비어 있으면 제공자를 바꿔도 검색하지 않는다`() {
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        val state = composeRule.setPlaceSearchEffect(onSearch = { request -> requestList += request })

        composeRule.runOnIdle { state().mapState.select(DiaryMapProvider.GOOGLE) }
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList shouldBe emptyList()
    }

    @Test
    fun `검색어가 비어 있는 상태로 시작하면 기다림 없이 결과를 비운다`() {
        var clearCount = 0

        composeRule.mainClock.autoAdvance = false
        composeRule.setPlaceSearchEffect(onClear = { clearCount++ })

        composeRule.mainClock.advanceTimeBy(IMMEDIATE_MILLIS)

        clearCount shouldBe 1
    }

    @Test
    fun `TC-514c3942-86ec-40d4-a90a-696a6bb33b8b 검색어를 지우면 기다림 없이 결과를 비운다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        var clearCount = 0
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        composeRule.setPlaceSearchEffect(
            onSearch = { request -> requestList += request },
            onClear = { clearCount++ },
        )

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList.size shouldBe 1
        val clearCountAfterSearch = clearCount

        composeRule.onNodeWithText(query).performTextClearance()
        composeRule.mainClock.advanceTimeBy(IMMEDIATE_MILLIS)

        clearCount shouldBe clearCountAfterSearch + 1
        requestList.size shouldBe 1
    }

    @Test
    fun `TC-5c1e6b45-1497-4f93-8892-b4d226032ee7 네이버 검색에는 기준 위치를 넘기지 않는다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        composeRule.setPlaceSearchEffect(onSearch = { request -> requestList += request })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList.single().provider shouldBe MapProvider.NAVER
        requestList.single().bounds shouldBe null
    }

    @Test
    fun `TC-bc47b8df-c5e9-4cc9-a567-d5e882cf9b4e Google로 검색할 때 지도를 옮기면 바뀐 영역을 기준으로 다시 검색한다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val movedBounds = boundsAround(center = randomCoordinate(), span = BOUNDS_SPAN)
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        val state = composeRule.setPlaceSearchEffect(initialProvider = DiaryMapProvider.GOOGLE, onSearch = { request -> requestList += request })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList.size shouldBe 1

        composeRule.runOnIdle { state().mapState.reportCamera(center = randomCoordinate(), bounds = movedBounds) }
        composeRule.mainClock.advanceTimeBy(IMMEDIATE_MILLIS)

        requestList.size shouldBe 1

        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS)

        requestList.size shouldBe 2
        requestList.last().provider shouldBe MapProvider.GOOGLE
        requestList.last().query shouldBe query
        requestList.last().bounds shouldBe movedBounds.toCoordinateBounds()
    }

    @Test
    fun `TC-a5ab0598-65a5-4f72-83cd-568afdf6f714 네이버로 검색할 때는 지도를 옮겨도 다시 검색하지 않고 결과도 비우지 않는다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val requestList = mutableListOf<PlaceSearchRequest>()
        var clearCount = 0

        composeRule.mainClock.autoAdvance = false
        val state =
            composeRule.setPlaceSearchEffect(
                initialProvider = DiaryMapProvider.NAVER,
                onSearch = { request -> requestList += request },
                onClear = { clearCount++ },
            )

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)
        val clearCountAfterSearch = clearCount

        composeRule.runOnIdle {
            state().mapState.reportCamera(center = randomCoordinate(), bounds = boundsAround(center = randomCoordinate(), span = BOUNDS_SPAN))
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList.size shouldBe 1
        clearCount shouldBe clearCountAfterSearch
    }

    @Test
    fun `TC-6bf0069a-8bac-4bc6-8008-cb85fb9e9229 검색어가 비어 있으면 지도를 옮겨도 검색하지 않는다`() {
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        val state = composeRule.setPlaceSearchEffect(initialProvider = DiaryMapProvider.GOOGLE, onSearch = { request -> requestList += request })

        composeRule.runOnIdle {
            state().mapState.reportCamera(center = randomCoordinate(), bounds = boundsAround(center = randomCoordinate(), span = BOUNDS_SPAN))
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList shouldBe emptyList()
    }

    @Test
    fun `TC-2bf9ced9-1602-4210-a48b-58e0942f6a44 목록의 장소를 눌러 지도가 옮겨져도 다시 검색하지 않고 그 뒤 직접 옮기면 다시 검색한다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val shownCoordinate = randomCoordinate()
        val draggedBounds = boundsAround(center = randomCoordinate(), span = BOUNDS_SPAN)
        val requestList = mutableListOf<PlaceSearchRequest>()
        var clearCount = 0

        composeRule.mainClock.autoAdvance = false
        val state =
            composeRule.setPlaceSearchEffect(
                initialProvider = DiaryMapProvider.GOOGLE,
                onSearch = { request -> requestList += request },
                onClear = { clearCount++ },
            )

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)
        val clearCountAfterSearch = clearCount

        composeRule.runOnIdle { state().showOnMapAndReport(coordinate = shownCoordinate) }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList.size shouldBe 1
        clearCount shouldBe clearCountAfterSearch

        composeRule.runOnIdle { state().mapState.reportCamera(center = randomCoordinate(), bounds = draggedBounds) }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList.size shouldBe 2
        requestList.last().bounds shouldBe draggedBounds.toCoordinateBounds()
    }

    @Test
    fun `TC-b6c6e3e0-1fb9-4e3e-b4e3-33615d59275e 목록의 장소로 지도를 옮긴 뒤 검색어를 바꾸면 그때 보고 있는 영역을 기준으로 검색한다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val changedQuery = randomText(prefix = CHANGED_QUERY_PREFIX)
        val shownCoordinate = randomCoordinate()
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        val state = composeRule.setPlaceSearchEffect(initialProvider = DiaryMapProvider.GOOGLE, onSearch = { request -> requestList += request })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)
        composeRule.runOnIdle { state().showOnMapAndReport(coordinate = shownCoordinate) }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        composeRule.onNodeWithText(query).performTextReplacement(changedQuery)
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList.size shouldBe 2
        requestList.last().query shouldBe changedQuery
        requestList.last().bounds shouldBe boundsAround(center = shownCoordinate, span = BOUNDS_SPAN).toCoordinateBounds()
    }

    // 목록의 장소를 누르면 지도가 그 장소로 옮겨지고, 옮겨진 지도는 그 장소를 가운데에 둔 영역을 알려 온다.
    private fun PlaceSearchDialogState.showOnMapAndReport(coordinate: DiaryMapCoordinate) {
        showOnMap(coordinate)
        mapState.reportCamera(center = coordinate, bounds = boundsAround(center = coordinate, span = BOUNDS_SPAN))
    }

    private fun randomCoordinate(): DiaryMapCoordinate = fixtureMonkey.coordinateInFormPrecision().toDiaryMapCoordinate()

    private companion object {
        private const val QUERY_PREFIX = "query"
        private const val CHANGED_QUERY_PREFIX = "changed-query"
        private const val BLANK_QUERY = "   "
        private const val BOUNDS_SPAN = 0.01

        // 기다림 없이 실행되는 것을 확인하려고 debounce 시간보다 짧게 둔다.
        private const val IMMEDIATE_MILLIS = SEARCH_DELAY_MILLIS - 1
    }
}
