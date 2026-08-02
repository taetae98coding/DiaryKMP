package io.github.taetae98coding.diary.feature.tag.ui.add

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagAddFormState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagAddScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 제목은 태그 추가이다`() {
        setTagAddScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    fun `기본 환경에서 제목은 Add Tag이다`() {
        setTagAddScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 뒤로가기 버튼 이름은 뒤로가기이다`() {
        setTagAddScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `기본 뒤로가기 버튼 이름은 Navigate up이다`() {
        setTagAddScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 추가 버튼 이름은 태그 추가이다`() {
        setTagAddScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `기본 추가 버튼 이름은 Add tag이다`() {
        setTagAddScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-009 추가 처리 중 추가 버튼이 진행 표시로 바뀐다`() {
        setTagAddScaffold(uiStateProvider = { TagAddUiState(isInProgress = true) })

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    fun `목록과 함께 표시되면 뒤로가기 버튼이 표시되지 않는다`() {
        setTagAddScaffold(componentVisibleProvider = { TagAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    private fun setTagAddScaffold(
        uiStateProvider: () -> TagAddUiState = { TagAddUiState() },
        componentVisibleProvider: () -> TagAddScaffoldComponentVisible = { TagAddScaffoldComponentVisible() },
    ) {
        composeRule.setContent {
            DiaryTheme {
                TagAddScaffold(
                    state = rememberTagAddFormState(initialColor = Color.Red),
                    uiStateProvider = uiStateProvider,
                    onEvent = {},
                    onLinkPickerEvent = {},
                    componentVisibleProvider = componentVisibleProvider,
                )
            }
        }
    }

    public companion object {
        private const val KOREAN_TITLE = "태그 추가"
        private const val DEFAULT_TITLE = "Add Tag"
        private const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val KOREAN_ADD_BUTTON_DESCRIPTION = "태그 추가"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add tag"
    }
}
