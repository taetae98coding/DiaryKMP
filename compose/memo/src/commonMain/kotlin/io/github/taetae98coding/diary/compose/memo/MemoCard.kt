@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.memo

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.color.DiaryColorIndicator
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import kotlinx.datetime.LocalDateTime

public const val MEMO_CARD_TEST_TAG: String = "MemoCard"
public const val MEMO_COLOR_INDICATOR_TEST_TAG: String = "MemoColorIndicator"
public const val MEMO_DATE_TIME_TEST_TAG: String = "MemoDateTime"

@Composable
public fun MemoCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    memo: Memo? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag(MEMO_CARD_TEST_TAG),
        enabled = memo != null,
    ) {
        Row(
            modifier = Modifier.styleable(style = DiaryTheme.styles.cardContent),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            memo?.let {
                DiaryColorIndicator(
                    color = it.detail.color.toColor(),
                    modifier = Modifier.testTag(MEMO_COLOR_INDICATOR_TEST_TAG),
                )
            }
            Column {
                Text(
                    text = memo?.detail?.title.orEmpty(),
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                    style = DiaryTheme.typography.titleMediumEmphasized,
                )
                memo?.detail?.dateTime?.let {
                    Text(
                        text = it.toDisplayText(),
                        modifier = Modifier.testTag(MEMO_DATE_TIME_TEST_TAG),
                        color = DiaryTheme.colorScheme.onSurfaceVariant,
                        style = DiaryTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

private class MemoCardPreviewParameter : PreviewParameterProvider<Memo?> {
    override val values: Sequence<Memo?> =
        sequenceOf(
            previewMemo(title = "메모 제목", color = 0xFF3A7BD5),
            previewMemo(title = "메모 제목", color = 0xFF3A7BD5)
                .withDateTime(
                    MemoDateTime.DateTime(
                        start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 13, minute = 30),
                        endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 9, minute = 0),
                    ),
                ),
            null,
        )
}

private fun Memo.withDateTime(dateTime: MemoDateTime): Memo = copy(detail = detail.copy(dateTime = dateTime))

@ComponentPreview
@Composable
private fun MemoCardPreview(
    @PreviewParameter(MemoCardPreviewParameter::class) memo: Memo?,
) {
    DiaryTheme {
        MemoCard(
            memo = memo,
            onClick = {},
        )
    }
}
