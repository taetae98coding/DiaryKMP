package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagDetailFormState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailScaffoldEmojiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-001 이모지가 있으면 상단 바에 이모지와 제목을 함께 표시한다`() {
        setTagDetailScaffold(detail = tagDetail(title = TAG_TITLE, emoji = TAG_EMOJI))

        composeRule.onNodeWithText("$TAG_EMOJI $TAG_TITLE").assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-029 이모지가 비어 있으면 상단 바에 제목만 표시한다`() {
        setTagDetailScaffold(detail = tagDetail(title = TAG_TITLE))

        composeRule.onNode(hasText(TAG_TITLE) and !hasSetTextAction()).assertExists()
        composeRule.onNodeWithText(" $TAG_TITLE").assertDoesNotExist()
    }

    private fun setTagDetailScaffold(detail: TagDetail) {
        composeRule.setContent {
            val state = rememberTagDetailFormState(initialDetail = detail)

            DiaryTheme {
                TagDetailScaffold(
                    onEvent = {},
                    uiStateProvider = { tagDetailUiState(detail = detail) },
                    state = state,
                    tabFloatingActionButton = {},
                ) { tab ->
                    TagDetailTestTabContent(
                        tab = tab,
                        uiStateProvider = { tagDetailUiState(detail = detail) },
                        state = state,
                    )
                }
            }
        }
    }

    public companion object {
        private const val TAG_EMOJI = "🏃"
    }
}
