package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.format.toDisplayText
import io.github.taetae98coding.diary.compose.core.icon.CheckIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.text.DiaryMarkdown
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_all_day_period_format
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_applied_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_apply_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_apply_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_date_time_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_description_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_empty_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_period_format
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_title_label
import io.github.taetae98coding.diary.feature.memo.ui.previewMemoDraft
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoGeminiResultPage(
    onEvent: (MemoGeminiDialogEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoGeminiUiState = { MemoGeminiUiState() },
) {
    val uiState = uiStateProvider()

    if (!uiState.hasDraft) {
        Text(
            text = stringResource(Res.string.memo_gemini_empty_message),
            modifier = modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        return
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
    ) {
        memoGeminiFieldList
            .filter { field -> field.hasValue(draft = uiState.draft) }
            .forEach { field ->
                MemoGeminiResultField(
                    field = field,
                    onEvent = onEvent,
                    uiStateProvider = uiStateProvider,
                )
            }
    }
}

@Composable
private fun MemoGeminiResultField(
    field: MemoGeminiField,
    onEvent: (MemoGeminiDialogEvent) -> Unit,
    uiStateProvider: () -> MemoGeminiUiState,
    modifier: Modifier = Modifier,
) {
    val label = field.toLabel()
    val applyContentDescription = stringResource(Res.string.memo_gemini_apply_button_content_description, label)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = DiaryTheme.typography.labelLarge,
                )
                if (field in uiStateProvider().appliedFieldSet) {
                    CheckIcon(contentDescription = stringResource(Res.string.memo_gemini_applied_content_description))
                }
            }
            TextButton(
                onClick = { onEvent(MemoGeminiDialogEvent.ClickApply(field = field)) },
                modifier =
                    Modifier.semantics {
                        contentDescription = applyContentDescription
                    },
            ) {
                Text(text = stringResource(Res.string.memo_gemini_apply_action))
            }
        }

        MemoGeminiResultValue(
            field = field,
            draftProvider = { uiStateProvider().draft },
        )
    }
}

@Composable
private fun MemoGeminiResultValue(
    field: MemoGeminiField,
    draftProvider: () -> MemoDraft,
    modifier: Modifier = Modifier,
) {
    val draft = draftProvider()

    when (field) {
        MemoGeminiField.TITLE ->
            Text(
                text = draft.title,
                modifier = modifier.fillMaxWidth(),
                maxLines = MemoGeminiResultPageDefaults.TITLE_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )

        MemoGeminiField.DESCRIPTION ->
            DiaryMarkdown(
                content = draft.description,
                modifier = modifier.fillMaxWidth(),
            )

        MemoGeminiField.DATE_TIME ->
            Text(
                text = draft.dateTime?.toDisplayText().orEmpty(),
                modifier = modifier.fillMaxWidth(),
            )
    }
}

@Composable
private fun MemoGeminiField.toLabel(): String =
    when (this) {
        MemoGeminiField.TITLE -> stringResource(Res.string.memo_gemini_title_label)
        MemoGeminiField.DESCRIPTION -> stringResource(Res.string.memo_gemini_description_label)
        MemoGeminiField.DATE_TIME -> stringResource(Res.string.memo_gemini_date_time_label)
    }

@Composable
private fun MemoDateTime.toDisplayText(): String =
    when (this) {
        is MemoDateTime.AllDay ->
            stringResource(
                Res.string.memo_gemini_all_day_period_format,
                dateRange.start.toDisplayText(),
                dateRange.endInclusive.toDisplayText(),
            )

        is MemoDateTime.DateTime ->
            stringResource(
                Res.string.memo_gemini_period_format,
                start.toDateTimeDisplayText(),
                endInclusive.toDateTimeDisplayText(),
            )
    }

@Composable
private fun LocalDateTime.toDateTimeDisplayText(): String = "${date.toDisplayText()} ${time.toDisplayText()}"

private class MemoDraftPreviewParameter : PreviewParameterProvider<MemoDraft> {
    override val values: Sequence<MemoDraft> =
        sequenceOf(
            previewMemoDraft(),
            MemoDraft.EMPTY,
        )
}

@ComponentPreview
@Composable
private fun MemoGeminiResultPagePreview(
    @PreviewParameter(MemoDraftPreviewParameter::class) draft: MemoDraft,
) {
    DiaryTheme {
        Surface {
            MemoGeminiResultPage(
                onEvent = {},
                uiStateProvider = {
                    MemoGeminiUiState(
                        step = MemoGeminiStep.RESULT,
                        draft = draft,
                        appliedFieldSet = setOf(MemoGeminiField.TITLE),
                    )
                },
            )
        }
    }
}
