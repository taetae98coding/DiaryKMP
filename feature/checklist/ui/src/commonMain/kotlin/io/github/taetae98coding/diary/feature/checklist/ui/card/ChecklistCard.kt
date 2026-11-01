@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.feature.checklist.ui.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_home_progress_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.previewChecklistSummary
import org.jetbrains.compose.resources.stringResource

internal const val CHECKLIST_CARD_TEST_TAG: String = "ChecklistCard"

@Composable
internal fun ChecklistCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: ChecklistSummary? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag(CHECKLIST_CARD_TEST_TAG),
        enabled = summary != null,
    ) {
        Column(
            modifier = Modifier.styleable(style = DiaryTheme.styles.cardContent),
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.cardLineSpacing),
        ) {
            Text(
                text =
                    summary
                        ?.checklist
                        ?.detail
                        ?.title
                        .orEmpty(),
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
                style = DiaryTheme.typography.titleMediumEmphasized,
            )
            ChecklistProgress(
                modifier = Modifier.fillMaxWidth(),
                summary = summary,
            )
        }
    }
}

@Composable
private fun ChecklistProgress(
    modifier: Modifier = Modifier,
    summary: ChecklistSummary? = null,
) {
    val progressContentDescription =
        summary?.let { value ->
            stringResource(
                Res.string.checklist_home_progress_content_description,
                value.totalCount.toString(),
                value.checkedCount.toString(),
            )
        }
    val progress by animateFloatAsState(
        targetValue = summary?.progress ?: 0F,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.cardLineSpacing),
    ) {
        val progressText = summary?.let { value -> "${value.checkedCount}/${value.totalCount}" }.orEmpty()

        Text(
            text = progressText,
            modifier =
                Modifier
                    .clearAndSetSemantics {
                        text = AnnotatedString(progressText)
                        if (progressContentDescription != null) {
                            contentDescription = progressContentDescription
                        }
                    }.basicMarquee(iterations = Int.MAX_VALUE),
            maxLines = 1,
            color = DiaryTheme.colorScheme.onSurfaceVariant,
            style = DiaryTheme.typography.bodySmall,
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics {},
        )
    }
}

private val ChecklistSummary.progress: Float
    get() = if (totalCount == 0) 0F else checkedCount.toFloat() / totalCount

private class ChecklistCardPreviewParameter : PreviewParameterProvider<ChecklistSummary?> {
    override val values: Sequence<ChecklistSummary?> =
        sequenceOf(
            previewChecklistSummary(title = "여행 준비"),
            previewChecklistSummary(title = "빈 체크리스트", checkedCount = 0, totalCount = 0),
            null,
        )
}

@ComponentPreview
@Composable
private fun ChecklistCardPreview(
    @PreviewParameter(ChecklistCardPreviewParameter::class) summary: ChecklistSummary?,
) {
    DiaryTheme {
        ChecklistCard(
            onClick = {},
            summary = summary,
        )
    }
}
