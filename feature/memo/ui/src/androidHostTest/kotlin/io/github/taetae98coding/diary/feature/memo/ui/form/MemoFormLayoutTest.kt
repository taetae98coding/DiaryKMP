package io.github.taetae98coding.diary.feature.memo.ui.form

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.place.DEFAULT_MAP_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_TAG_SELECT_LABEL
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = WIDE_WINDOW_QUALIFIERS)
class MemoFormWideLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `단독으로 표시하면 장소 카드를 폼 끝 쪽에 나란히 둔다`() {
        composeRule.setMemoForm()

        val tagSelectBounds = composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).getUnclippedBoundsInRoot()
        val mapBounds = composeRule.onNodeWithContentDescription(DEFAULT_MAP_DESCRIPTION).getUnclippedBoundsInRoot()

        mapBounds.left shouldBeGreaterThanOrEqualTo tagSelectBounds.right
    }

    @Test
    fun `단독으로 표시하면 지도가 고정 높이보다 커진다`() {
        composeRule.setMemoForm()

        val mapBounds = composeRule.onNodeWithContentDescription(DEFAULT_MAP_DESCRIPTION).getUnclippedBoundsInRoot()

        mapBounds.height shouldBeGreaterThan COLUMN_MAP_HEIGHT
    }

    @Test
    fun `목록과 함께 표시하면 장소 카드를 폼 아래에 둔다`() {
        composeRule.setMemoForm(isStandalone = false)

        val tagSelectBounds = composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).getUnclippedBoundsInRoot()
        val mapBounds = composeRule.onNodeWithContentDescription(DEFAULT_MAP_DESCRIPTION).getUnclippedBoundsInRoot()

        mapBounds.top shouldBeGreaterThanOrEqualTo tagSelectBounds.bottom
        mapBounds.height shouldBe COLUMN_MAP_HEIGHT
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = COMPACT_WINDOW_QUALIFIERS)
class MemoFormCompactLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `좁은 창에서는 단독으로 표시해도 장소 카드를 폼 아래에 둔다`() {
        composeRule.setMemoForm()

        val tagSelectBounds = composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).getUnclippedBoundsInRoot()
        val mapBounds = composeRule.onNodeWithContentDescription(DEFAULT_MAP_DESCRIPTION).getUnclippedBoundsInRoot()

        mapBounds.top shouldBeGreaterThanOrEqualTo tagSelectBounds.bottom
        mapBounds.height shouldBe COLUMN_MAP_HEIGHT
    }
}

internal const val WIDE_WINDOW_QUALIFIERS: String = "w840dp-h1200dp"
private const val COMPACT_WINDOW_QUALIFIERS = "w411dp-h891dp"
private val COLUMN_MAP_HEIGHT = 240.dp

private fun ComposeContentTestRule.setMemoForm(isStandalone: Boolean = true) {
    setContent {
        DiaryTheme {
            Surface {
                MemoForm(
                    onEvent = {},
                    isStandalone = isStandalone,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
