package io.github.taetae98coding.diary.feature.routine.ui.detail.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.format.toDisplayText
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.routineAmountText
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.previewRoutineRecord
import io.github.taetae98coding.diary.feature.routine.ui.routine_detail_record_remove_button_content_description
import org.jetbrains.compose.resources.stringResource

internal const val ROUTINE_RECORD_ROW_TEST_TAG: String = "RoutineRecordRow"

@Composable
internal fun RoutineRecordRow(
    onRemove: () -> Unit,
    unit: String,
    modifier: Modifier = Modifier,
    record: RoutineRecord? = null,
) {
    val dateText = record?.date?.toDisplayText().orEmpty()
    val amountText = record?.let { value -> routineAmountText(amount = value.amount.toLong(), unit = unit) }.orEmpty()

    Row(
        modifier =
            modifier
                .testTag(ROUTINE_RECORD_ROW_TEST_TAG)
                .heightIn(min = RoutineRecordRowDefaults.MinHeight)
                .padding(horizontal = DiaryTheme.dimens.screenHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier
                    .weight(1F)
                    .semantics(mergeDescendants = true) { },
            horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = dateText,
                modifier = Modifier.weight(1F),
                maxLines = 1,
                style = DiaryTheme.typography.bodyLarge,
            )
            Text(
                text = amountText,
                maxLines = 1,
                style = DiaryTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        if (record != null) {
            RoutineRecordRemoveButton(
                onClick = onRemove,
                contentDescription = stringResource(Res.string.routine_detail_record_remove_button_content_description, dateText, amountText),
            )
        }
    }
}

private class RoutineRecordPreviewParameter : PreviewParameterProvider<RoutineRecord?> {
    override val values: Sequence<RoutineRecord?> = sequenceOf(previewRoutineRecord(), null)
}

@ComponentPreview
@Composable
private fun RoutineRecordRowPreview(
    @PreviewParameter(RoutineRecordPreviewParameter::class) record: RoutineRecord?,
) {
    DiaryTheme {
        Surface {
            RoutineRecordRow(
                onRemove = {},
                unit = "ml",
                record = record,
            )
        }
    }
}
