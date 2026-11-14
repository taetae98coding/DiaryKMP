package io.github.taetae98coding.diary.feature.routine.ui.detail.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.format.toDisplayText
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineAmountTextField
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineDateText
import io.github.taetae98coding.diary.feature.routine.ui.routine_detail_record_add_button_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_detail_record_amount_label
import io.github.taetae98coding.diary.feature.routine.ui.routine_detail_record_date_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineRecordInputRow(
    onAdd: () -> Unit,
    unit: String,
    modifier: Modifier = Modifier,
    state: RoutineRecordInputState = rememberRoutineRecordInputState(),
    isInProgressProvider: () -> Boolean = { false },
) {
    val date = state.date
    val dateContentDescription = stringResource(Res.string.routine_detail_record_date_content_description, date.toDisplayText())

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = state.datePickerDialogState::show,
            modifier = Modifier.semantics { contentDescription = dateContentDescription },
        ) {
            RoutineDateText(
                date = date,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
        RoutineAmountTextField(
            label = stringResource(Res.string.routine_detail_record_amount_label),
            modifier = Modifier.weight(1F),
            state = state.amountState,
            unitProvider = { unit },
            isClearButtonEnabled = false,
            onKeyboardAction = {
                if (state.isAddable) {
                    onAdd()
                }
            },
        )
        RoutineRecordAddButton(
            onClick = onAdd,
            contentDescription = stringResource(Res.string.routine_detail_record_add_button_content_description),
            isEnabledProvider = { state.isAddable },
            isInProgressProvider = isInProgressProvider,
        )
    }
}

@ComponentPreview
@Composable
private fun RoutineRecordInputRowPreview() {
    DiaryTheme {
        Surface {
            RoutineRecordInputRow(
                onAdd = {},
                unit = "ml",
                state = rememberRoutineRecordInputState(initialAmount = 250),
            )
        }
    }
}
