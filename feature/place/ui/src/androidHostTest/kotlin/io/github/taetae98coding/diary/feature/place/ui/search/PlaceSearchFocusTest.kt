package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceSearchFocusTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-018 다이얼로그를 열면 검색어 입력에 초점이 맞춰진다`() {
        setPlaceSearchFocusContent()

        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).assertIsFocused()
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-018 다이얼로그를 닫았다 다시 열어도 검색어 입력에 초점이 맞춰진다`() {
        var isVisible by mutableStateOf(true)
        setPlaceSearchFocusContent(isVisibleProvider = { isVisible })
        composeRule.waitForIdle()

        composeRule.runOnIdle { isVisible = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isVisible = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).assertIsFocused()
    }

    // 다이얼로그 전체는 지도를 포함해 호스트 테스트에서 구성할 수 없으므로, 다이얼로그가 부르는 초점 효과와 검색어 입력만 함께 구성한다.
    private fun setPlaceSearchFocusContent(isVisibleProvider: () -> Boolean = { true }) {
        composeRule.setContent {
            DiaryTheme {
                if (isVisibleProvider()) {
                    PlaceSearchFocusContent()
                }
            }
        }
    }

    @Composable
    private fun PlaceSearchFocusContent() {
        val state = rememberPlaceSearchDialogState(initialProvider = DiaryMapProvider.NAVER)

        PlaceSearchFocusEffect(state = state)
        PlaceSearchQueryInput(
            state = state,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
