package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

internal const val RUNNER = "🏃"
internal const val SWIMMER = "🏊"
internal const val DEFAULT_LABEL = "Emoji"
internal const val KOREAN_LABEL = "이모지"
internal const val DEFAULT_CANCEL = "Cancel"
internal const val DEFAULT_CONFIRM = "Confirm"
internal const val KOREAN_CANCEL = "취소"
internal const val KOREAN_CONFIRM = "확인"

@Composable
internal fun DiaryEmojiInputTestContent(content: @Composable () -> Unit) {
    DiaryTheme {
        Surface(content = content)
    }
}

/** 이모지 칸의 크기는 호출부가 정하므로, 실제 배치와 같은 행에 담아 표시한다. */
@Composable
internal fun DiaryEmojiInputTestRow(state: DiaryEmojiInputState) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        DiaryEmojiInput(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .aspectRatio(ratio = 1F, matchHeightConstraintsFirst = true),
            state = state,
        )
        DiaryTitleInput()
    }
}

internal fun ComposeContentTestRule.emojiInput(): SemanticsNodeInteraction = onNodeWithTag(DIARY_EMOJI_INPUT_TEST_TAG)

internal fun ComposeContentTestRule.dialogInput(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasAnyAncestor(isDialog()))

internal fun ComposeContentTestRule.titleInput(): SemanticsNodeInteraction = onNode(hasSetTextAction() and !hasAnyAncestor(isDialog()))
