@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.dday

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.color.DiaryColorIndicator
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.domain.dday.count.count
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import kotlinx.datetime.LocalDate

public const val DDAY_CARD_TEST_TAG: String = "DDayCard"
public const val DDAY_COLOR_INDICATOR_TEST_TAG: String = "DDayColorIndicator"

@Composable
public fun DDayCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: DDayListState = rememberDDayListState(),
    dday: DDay? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag(DDAY_CARD_TEST_TAG),
        enabled = dday != null,
    ) {
        Column(
            modifier = Modifier.styleable(style = DiaryTheme.styles.cardContent),
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.cardLineSpacing),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.colorIndicatorSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                dday?.let {
                    DiaryColorIndicator(
                        color = it.detail.color.toColor(),
                        modifier = Modifier.testTag(DDAY_COLOR_INDICATOR_TEST_TAG),
                    )
                }
                Text(
                    text = dday?.detail?.title.orEmpty(),
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                )
            }
            DDayCardCountText(
                state = state,
                dday = dday,
            )
        }
    }
}

@Composable
private fun DDayCardCountText(
    state: DDayListState,
    dday: DDay?,
) {
    Text(
        text = dday?.countText(today = state.today).orEmpty(),
        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
        maxLines = 1,
        style = DiaryTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun DDay.countText(today: LocalDate): String {
    val count = remember(detail, today) { detail.count(today = today) }

    return count.toCardText()
}

private class DDayCardPreviewParameter : PreviewParameterProvider<DDay?> {
    override val values: Sequence<DDay?> =
        sequenceOf(
            previewDDay(title = "만난 날"),
            previewDDay(title = "시험", baseDate = LocalDate(year = 2026, month = 4, day = 29)),
            previewDDay(title = "오늘", baseDate = previewDDayToday()),
            null,
        )
}

@ComponentPreview
@Composable
private fun DDayCardPreview(
    @PreviewParameter(DDayCardPreviewParameter::class) dday: DDay?,
) {
    DiaryTheme {
        DDayCard(
            onClick = {},
            state = rememberDDayListState(initialToday = previewDDayToday()),
            dday = dday,
        )
    }
}
