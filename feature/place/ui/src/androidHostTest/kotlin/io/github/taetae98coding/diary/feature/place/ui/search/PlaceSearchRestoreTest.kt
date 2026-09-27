package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceSearchRestoreTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val restorationTester = StateRestorationTester(composeRule)

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-017 화면이 재생성되면 유지한 검색어로 다시 검색한다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val searchList = setRestorablePlaceSearchEffect()

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)
        composeRule.mainClock.autoAdvance = true
        searchList.size shouldBe 1

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(query).assertExists()
        searchList.size shouldBe 2
        searchList.last().request.query shouldBe query
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-DOMAIN-023 복원한 검색어로는 기다리지 않고 곧바로 검색한다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val searchList = setRestorablePlaceSearchEffect()

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.mainClock.advanceTimeBy(SEARCH_DELAY_MILLIS * 2)
        composeRule.mainClock.autoAdvance = true
        searchList.size shouldBe 1

        val restoredAt = composeRule.mainClock.currentTime
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        searchList.size shouldBe 2
        searchList.last().timeMillis - restoredAt shouldBeLessThan SEARCH_DELAY_MILLIS
    }

    private fun setRestorablePlaceSearchEffect(): List<Search> {
        val searchList = mutableListOf<Search>()

        composeRule.mainClock.autoAdvance = false
        restorationTester.setContent {
            DiaryTheme {
                val state = rememberPlaceSearchDialogState(initialProvider = DiaryMapProvider.NAVER)

                PlaceSearchEffect(
                    state = state,
                    onSearch = { request -> searchList += Search(request = request, timeMillis = composeRule.mainClock.currentTime) },
                    onClear = {},
                )
                PlaceSearchQueryInput(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        return searchList
    }

    private data class Search(
        val request: PlaceSearchRequest,
        val timeMillis: Long,
    )

    private companion object {
        private const val QUERY_PREFIX = "query"
    }
}
