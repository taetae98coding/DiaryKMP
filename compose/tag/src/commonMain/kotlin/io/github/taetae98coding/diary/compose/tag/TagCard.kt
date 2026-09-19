@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.tag

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.color.DIARY_COLOR_TITLE_INDICATOR_TEST_TAG
import io.github.taetae98coding.diary.compose.core.color.DiaryColorTitleRow
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.library.compose.ui.color.toColor

public const val TAG_CARD_TEST_TAG: String = "TagCard"
public const val TAG_COLOR_INDICATOR_TEST_TAG: String = DIARY_COLOR_TITLE_INDICATOR_TEST_TAG

@Composable
public fun TagCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: Tag? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag(TAG_CARD_TEST_TAG),
        enabled = tag != null,
    ) {
        DiaryColorTitleRow(
            title = tag?.detail?.emojiWithTitle.orEmpty(),
            modifier = Modifier.styleable(style = DiaryTheme.styles.cardContent),
            color = tag?.detail?.color?.toColor() ?: Color.Transparent,
        )
    }
}

private class TagCardPreviewParameter : PreviewParameterProvider<Tag?> {
    override val values: Sequence<Tag?> =
        sequenceOf(
            previewTag(emoji = "🏃", title = "태그 제목", color = 0xFF3A7BD5),
            null,
        )
}

@ComponentPreview
@Composable
private fun TagCardPreview(
    @PreviewParameter(TagCardPreviewParameter::class) tag: Tag?,
) {
    DiaryTheme {
        TagCard(
            tag = tag,
            onClick = {},
        )
    }
}
