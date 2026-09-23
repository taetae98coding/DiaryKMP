package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.form.WIDE_WINDOW_QUALIFIERS
import io.github.taetae98coding.diary.feature.memo.ui.place.DEFAULT_MAP_DESCRIPTION
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = WIDE_WINDOW_QUALIFIERS)
class MemoAddScaffoldLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `본문을 좌우로 나눠도 추가 버튼은 화면 끝 쪽 아래에 두고 지도를 가리지 않는다`() {
        composeRule.setContent {
            DiaryTheme {
                MemoAddScaffold(
                    onEvent = {},
                    onFormEvent = {},
                    onTagPickerEvent = {},
                    onWebPickerEvent = {},
                    onContactPickerEvent = {},
                    onPlacePickerEvent = {},
                    onGeminiEvent = {},
                    onGeminiDismissRequest = {},
                )
            }
        }

        val mapBounds = composeRule.onNodeWithContentDescription(DEFAULT_MAP_DESCRIPTION).getUnclippedBoundsInRoot()
        val buttonBounds = composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).getUnclippedBoundsInRoot()

        buttonBounds.left shouldBeGreaterThan mapBounds.left
        buttonBounds.top shouldBeGreaterThanOrEqualTo mapBounds.bottom
    }
}

private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add memo"
