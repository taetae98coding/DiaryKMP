@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.routine

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleFadeVisibility
import io.github.taetae98coding.diary.compose.core.color.DiaryColorIndicator
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import kotlinx.datetime.LocalDate

public const val ROUTINE_CARD_TEST_TAG: String = "RoutineCard"
public const val ROUTINE_COLOR_INDICATOR_TEST_TAG: String = "RoutineColorIndicator"
public const val ROUTINE_PROGRESS_BAR_TEST_TAG: String = "RoutineProgressBar"

private const val PERCENT: Float = 100F

@Composable
public fun RoutineCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: RoutineListState = rememberRoutineListState(),
    summary: RoutineSummary? = null,
    quickRecord: (@Composable (RoutineSummary) -> Unit)? = null,
) {
    val cardText = summary?.toCardText(today = state.today)

    Card(
        onClick = onClick,
        modifier = modifier.testTag(ROUTINE_CARD_TEST_TAG),
        enabled = summary != null,
    ) {
        Row(
            modifier = Modifier.styleable(style = DiaryTheme.styles.cardContent),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoutineCardTextGroup(
                modifier = Modifier.weight(1F),
                summary = summary,
                cardText = cardText,
            )
            DiaryScaleFadeVisibility(visible = summary != null && quickRecord != null && cardText?.isInProgress == true) {
                Box(modifier = Modifier.padding(start = DiaryTheme.dimens.colorIndicatorSpacing)) {
                    if (summary != null && quickRecord != null) {
                        quickRecord(summary)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineCardTextGroup(
    modifier: Modifier = Modifier,
    summary: RoutineSummary? = null,
    cardText: RoutineCardText? = null,
) {
    val achievementRate = cardText?.achievementRate

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.cardLineSpacing),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.colorIndicatorSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            summary?.let {
                DiaryColorIndicator(
                    color =
                        it.routine.detail.color
                            .toColor(),
                    modifier = Modifier.testTag(ROUTINE_COLOR_INDICATOR_TEST_TAG),
                )
            }
            Text(
                text =
                    summary
                        ?.routine
                        ?.detail
                        ?.title
                        .orEmpty(),
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
            )
        }
        RoutineCardTextRow(
            modifier = Modifier.fillMaxWidth(),
            cardText = cardText,
        )
        if (summary != null && achievementRate != null) {
            RoutineProgressBar(
                achievementRate = achievementRate,
                color =
                    summary.routine.detail.color
                        .toColor(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RoutineCardTextRow(
    modifier: Modifier = Modifier,
    cardText: RoutineCardText? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.colorIndicatorSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = cardText?.text.orEmpty(),
            modifier =
                Modifier
                    .weight(1F)
                    .basicMarquee(iterations = Int.MAX_VALUE),
            color = if (cardText == null || cardText.isInProgress) Color.Unspecified else DiaryTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            style = DiaryTheme.typography.bodyMedium,
        )
        cardText?.achievementRateText?.let { text ->
            Text(
                text = text,
                color = DiaryTheme.colorScheme.primary,
                maxLines = 1,
                style = DiaryTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun RoutineProgressBar(
    achievementRate: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = achievementRate / PERCENT,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )

    LinearProgressIndicator(
        progress = { progress },
        modifier =
            modifier
                .testTag(ROUTINE_PROGRESS_BAR_TEST_TAG)
                .clearAndSetSemantics { },
        color = color,
    )
}

private class RoutineCardPreviewParameter : PreviewParameterProvider<RoutineSummary?> {
    override val values: Sequence<RoutineSummary?> =
        sequenceOf(
            previewRoutineSummary(title = "물 마시기"),
            previewRoutineSummary(title = "달리기", cycleAmount = 12_000, detail = previewRoutineDetail(title = "달리기", goalAmount = null)),
            previewRoutineSummary(title = "시작 전", detail = previewRoutineDetail(title = "시작 전", startDate = LocalDate(year = 2027, month = 1, day = 1))),
            previewRoutineSummary(title = "완료", isFinished = true),
            null,
        )
}

@ComponentPreview
@Composable
private fun RoutineCardPreview(
    @PreviewParameter(RoutineCardPreviewParameter::class) summary: RoutineSummary?,
) {
    DiaryTheme {
        RoutineCard(
            onClick = {},
            state = rememberRoutineListState(initialToday = previewRoutineToday()),
            summary = summary,
        )
    }
}
