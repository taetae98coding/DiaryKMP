package io.github.taetae98coding.diary.compose.list.sort

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.height
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryListSortBarHostTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val isSortVisible = mutableStateOf(true)

    @Test
    fun `정렬 컨트롤을 감추면 끝 쪽 컨트롤은 항목이 있을 때와 같은 자리와 높이를 유지한다`() {
        setContent(hasTrailing = true)
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertExists()
        val visibleTrailingBounds = composeRule.onNodeWithText(TRAILING_LABEL).getBoundsInRoot()
        val visibleBarHeight = composeRule.onNodeWithTag(SORT_BAR_TEST_TAG).getBoundsInRoot().height

        isSortVisible.value = false
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(TRAILING_LABEL).getBoundsInRoot() shouldBe visibleTrailingBounds
        composeRule.onNodeWithTag(SORT_BAR_TEST_TAG).getBoundsInRoot().height shouldBe visibleBarHeight
    }

    @Test
    fun `끝 쪽 컨트롤이 없는 줄은 정렬 컨트롤을 감추면 자리를 차지하지 않는다`() {
        isSortVisible.value = false
        setContent(hasTrailing = false)

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithTag(SORT_BAR_TEST_TAG).assertDoesNotExist()
    }

    private fun setContent(hasTrailing: Boolean) {
        composeRule.setContent {
            DiaryTheme {
                DiaryListSortBarHost(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().testTag(SORT_BAR_TEST_TAG),
                    isSortVisibleProvider = { isSortVisible.value },
                    trailing =
                        if (hasTrailing) {
                            { TextButton(onClick = {}) { Text(text = TRAILING_LABEL) } }
                        } else {
                            null
                        },
                )
            }
        }
    }

    private companion object {
        private const val SORT_BAR_TEST_TAG = "DiaryListSortBar"
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val TRAILING_LABEL = "Trailing"
    }
}
