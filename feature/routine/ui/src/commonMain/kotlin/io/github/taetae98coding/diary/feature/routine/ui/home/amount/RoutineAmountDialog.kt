package io.github.taetae98coding.diary.feature.routine.ui.home.amount

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.previewRoutineSummary
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineAmountInputTransformation
import io.github.taetae98coding.diary.feature.routine.ui.home.record.RoutineHomeRecord
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_amount_dialog_cancel
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_amount_dialog_confirm
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_amount_dialog_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineAmountDialogHost(
    onConfirm: (RoutineHomeRecord) -> Unit,
    state: RoutineAmountDialogState = rememberRoutineAmountDialogState(),
) {
    val target = state.target ?: return

    RoutineAmountDialog(
        onDismissRequest = state::hide,
        onConfirm = {
            state.record?.let { record ->
                state.hide()
                onConfirm(record)
            }
        },
        target = target,
        state = state,
    )
}

@Composable
private fun RoutineAmountDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    target: RoutineAmountDialogTarget,
    modifier: Modifier = Modifier,
    state: RoutineAmountDialogState = rememberRoutineAmountDialogState(),
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = state.record != null,
            ) {
                Text(text = stringResource(Res.string.routine_home_amount_dialog_confirm))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(Res.string.routine_home_amount_dialog_cancel))
            }
        },
        title = {
            Text(
                text = target.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            OutlinedTextField(
                state = state.amountState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                label = { Text(text = stringResource(Res.string.routine_home_amount_dialog_label)) },
                suffix =
                    if (target.unit.isEmpty()) {
                        null
                    } else {
                        {
                            Text(
                                text = target.unit,
                                maxLines = 1,
                            )
                        }
                    },
                inputTransformation = RoutineAmountInputTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                // 확인할 수 없는 동안에는 아무 일도 하지 않아 창과 초점을 그대로 둔다.
                onKeyboardAction = { onConfirm() },
                lineLimits = TextFieldLineLimits.SingleLine,
            )
        },
    )
}

@ComponentPreview
@Composable
private fun RoutineAmountDialogPreview() {
    val state = remember { RoutineAmountDialogState().apply { show(routine = previewRoutineSummary(title = "물 마시기").routine) } }

    DiaryTheme {
        RoutineAmountDialogHost(
            onConfirm = {},
            state = state,
        )
    }
}
