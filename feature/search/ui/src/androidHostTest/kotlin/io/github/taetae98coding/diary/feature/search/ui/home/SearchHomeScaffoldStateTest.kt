package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SearchHomeScaffoldStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SEARCH-HOME-DOMAIN-010 화면이 재생성되어도 질의와 유형 선택을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: SearchHomeScaffoldState

        restorationTester.setContent {
            DiaryTheme {
                state = rememberSearchHomeScaffoldState()

                SearchHomeScaffold(
                    onEvent = {},
                    state = state,
                ) { type ->
                    Text(text = type.name)
                }
            }
        }

        composeRule.runOnIdle { state.queryState.setTextAndPlaceCursorAtEnd(QUERY) }
        composeRule.onNodeWithText(DEFAULT_PLACE_TAB_LABEL).performClick()
        composeRule.waitUntil { state.type == SearchHomeType.PLACE }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            state.queryState.text.toString() shouldBe QUERY
            state.type shouldBe SearchHomeType.PLACE
        }
    }

    @Test
    fun `처음에는 질의가 비어 있고 메모 유형이 선택된다`() {
        lateinit var state: SearchHomeScaffoldState

        composeRule.setContent {
            DiaryTheme {
                state = rememberSearchHomeScaffoldState()

                SearchHomeScaffold(
                    onEvent = {},
                    state = state,
                ) { type ->
                    Text(text = type.name)
                }
            }
        }

        composeRule.runOnIdle {
            state.queryState.text.toString() shouldBe ""
            state.type shouldBe SearchHomeType.MEMO
        }
    }

    public companion object {
        private const val QUERY = "여행"
        private const val DEFAULT_PLACE_TAB_LABEL = "Place"
    }
}
