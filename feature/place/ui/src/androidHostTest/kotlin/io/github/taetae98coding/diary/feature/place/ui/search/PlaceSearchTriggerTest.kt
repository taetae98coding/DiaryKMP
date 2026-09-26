package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.map.MapProvider
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
    fun `TC-PLACE-SEARCH-DIALOG-DOMAIN-001 검색어를 입력하지 않으면 검색하지 않는다`() {
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        composeRule.setPlaceSearchEffect(onSearch = { request -> requestList += request })

        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList shouldBe emptyList()
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-DOMAIN-001 검색어가 공백뿐이면 검색하지 않는다`() {
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        composeRule.setPlaceSearchEffect(onSearch = { request -> requestList += request })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(BLANK_QUERY)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList shouldBe emptyList()
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-DOMAIN-002 입력을 멈추고 0_2초가 지나면 한 번 검색한다`() {
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
    fun `TC-PLACE-SEARCH-DIALOG-DOMAIN-003 기다리는 동안 다시 입력하면 마지막 검색어로만 검색한다`() {
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
    fun `TC-PLACE-SEARCH-DIALOG-DOMAIN-004 제공자를 바꾸면 곧바로 다시 검색한다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        val state = composeRule.setPlaceSearchEffect(onSearch = { request -> requestList += request })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList.single().provider shouldBe MapProvider.NAVER

        composeRule.runOnIdle { state().mapState.select(DiaryMapProvider.GOOGLE) }
        composeRule.mainClock.advanceTimeBy(IMMEDIATE_MILLIS)

        requestList.size shouldBe 2
        requestList.last().provider shouldBe MapProvider.GOOGLE
        requestList.last().query shouldBe query
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-DOMAIN-007 검색어가 비어 있으면 제공자를 바꿔도 검색하지 않는다`() {
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
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-008 검색어를 지우면 기다림 없이 결과를 비운다`() {
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
    fun `TC-PLACE-SEARCH-DIALOG-DOMAIN-009 네이버 검색에는 기준 위치를 넘기지 않는다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val requestList = mutableListOf<PlaceSearchRequest>()

        composeRule.mainClock.autoAdvance = false
        composeRule.setPlaceSearchEffect(onSearch = { request -> requestList += request })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)

        requestList.single().provider shouldBe MapProvider.NAVER
        requestList.single().bounds shouldBe null
    }

    private companion object {
        private const val QUERY_PREFIX = "query"
        private const val CHANGED_QUERY_PREFIX = "changed-query"
        private const val BLANK_QUERY = "   "

        // 기다림 없이 실행되는 것을 확인하려고 debounce 시간보다 짧게 둔다.
        private const val IMMEDIATE_MILLIS = SEARCH_DELAY_MILLIS - 1
    }
}
