@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.color_picker_dialog_cancel
import io.github.taetae98coding.diary.compose.core.color_picker_dialog_confirm
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource

@Composable
public fun ColorPickerDialog(
    state: ColorPickerState,
    onDismissRequest: () -> Unit,
    onConfirm: (Color) -> Unit,
    modifier: Modifier = Modifier,
    colors: ColorPickerDialogColors = ColorPickerDialogDefaults.colors(),
) {
    ColorPickerSyncEffect(state = state)

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column {
                ColorPickerPreview(state = state)
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ColorPickerChannelSlider(
                        label = "R",
                        labelColor = colors.redLabelColor,
                        state = state.redSliderState,
                    )
                    ColorPickerChannelSlider(
                        label = "G",
                        labelColor = colors.greenLabelColor,
                        state = state.greenSliderState,
                    )
                    ColorPickerChannelSlider(
                        label = "B",
                        labelColor = colors.blueLabelColor,
                        state = state.blueSliderState,
                    )
                }
                Row(
                    modifier =
                        Modifier
                            .align(Alignment.End)
                            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(text = stringResource(Res.string.color_picker_dialog_cancel))
                    }
                    TextButton(onClick = { onConfirm(state.color) }) {
                        Text(text = stringResource(Res.string.color_picker_dialog_confirm))
                    }
                }
            }
        }
    }
}

@ComponentPreview
@Composable
private fun ColorPickerDialogPreview() {
    DiaryTheme {
        ColorPickerDialog(
            state = rememberColorPickerState(initialColor = Color(color = 0xFF3A7BD5.toInt())),
            onDismissRequest = {},
            onConfirm = {},
        )
    }
}
