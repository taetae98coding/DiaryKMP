package io.github.taetae98coding.diary.feature.routine.ui.home.record

import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.routineAmountText
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.home.press.longPress
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_quick_record_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_quick_record_custom_action
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_quick_record_label
import org.jetbrains.compose.resources.stringResource

internal const val ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG: String = "RoutineQuickRecordButton"

@Composable
internal fun RoutineQuickRecordButton(
    onClick: () -> Unit,
    onAmountInputRequest: () -> Unit,
    title: String,
    amount: Int,
    unit: String,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val latestOnAmountInputRequest by rememberUpdatedState(onAmountInputRequest)
    val amountText = routineAmountText(amount = amount.toLong(), unit = unit)
    val name = stringResource(Res.string.routine_home_quick_record_content_description, title, amountText)
    val customActionLabel = stringResource(Res.string.routine_home_quick_record_custom_action)

    FilledTonalButton(
        onClick = onClick,
        modifier =
            modifier
                .testTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG)
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.isAmountInputShortcut()) {
                        if (keyEvent.type == KeyEventType.KeyUp) latestOnAmountInputRequest()
                        true
                    } else {
                        false
                    }
                }.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)

                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                event.changes.forEach { change -> change.consume() }
                                latestOnAmountInputRequest()
                            }
                        }
                    }
                }.longPress {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAmountInputRequest()
                }.semantics {
                    contentDescription = name
                    customActions =
                        listOf(
                            CustomAccessibilityAction(label = customActionLabel) {
                                latestOnAmountInputRequest()
                                true
                            },
                        )
                },
    ) {
        Text(
            text = stringResource(Res.string.routine_home_quick_record_label, amountText),
            modifier = Modifier.clearAndSetSemantics { },
            maxLines = 1,
            softWrap = false,
        )
    }
}

private fun KeyEvent.isAmountInputShortcut(): Boolean = isShiftPressed && (key == Key.Enter || key == Key.NumPadEnter || key == Key.Spacebar)

@ComponentPreview
@Composable
private fun RoutineQuickRecordButtonPreview() {
    DiaryTheme {
        Surface {
            RoutineQuickRecordButton(
                onClick = {},
                onAmountInputRequest = {},
                title = "물 마시기",
                amount = 250,
                unit = "ml",
            )
        }
    }
}
